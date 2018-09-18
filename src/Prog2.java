/*
    Name: Taite Nazifi
    Course: CSc460 Database Design
    Assignment: Project 1 Part A Binary IO.
    Instructor: Dr. McCann
    Due: September 20th, 2018

    Required operations: Java 1.8, csv input file, input file
    is passed in as a command line argument.

    Program Specifications:
    The purpose of this program is to perform a file indexer using
    multiple buckets and a max bucket size. In particualr we are to
    index the ARR_TIME field using the digits in reversed order,
    assuming the implied zeroes to create a four-digit value. Since
    this is a time, we have to create a special kind of indexer along
    with the required logic for ordering and sorting if that's a thing.

*/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * -----Prog2-----
 *
 * This class is designed for reading in a CSV file, processing the input in such a way
 * that we can build a file indexer using the binary file that our Program1A spit out.
 * The binary file that we will be reading hsa 19 fields, but we'll only be paying special
 * attention to one of them, as the program will be using the ARR_TIME field for indexing
 * our database file (the binary file that prog1a created).
 * This program creates a sort of query system that can handle multiple requests to the
 * database file, providing information on the record at the specific ARR_TIME.
 * This is not a simple task, as we have to work with time, and time never ever cooperates
 * well with computers. If this is the rest of my life, I'd rather just end it here.
 *
 * @author Taite Nazifi
 * @version 1.0
 * @since 1.0
 *
 */
public class Prog2 {
    /*
            Declaring instance variables that will be used throughout the program.
         */
    private static int[] MAX_LEN; //For the max lengths of each field in the file.
    private static File file; //File to be reading from and writing to
    private static RandomAccessFile readStream, writeStream; //data stream for reading and writing.
    private static long filelen, numRecs; //length of the file, number of records
    private static Node root;
    private static int recLen = 105;
    private static int acceptedRecs, rejectedRecs;

    /*
        Constants that don't change...
     */
    protected static final int indexLen = 12; //4 bytes for time, 8 bytes for offset pointer.

    /**
     * -----Program Flow------
     *
     * 1. Initialize instance variables
     * 2. Get max field lengths, number of records in the file.
     * 3. Read the binary file, creating the index binary file.
     * 4.
     * 5.
     * 6.
     *
     * @author Taite Nazifi
     * @since 1.0
     * @param args command-line arguments, like a file-path to a bin db.
     */
    public static void main(String[] args){
        if(args.length < 1){
            System.out.println("Program requires filepath argument.");
            System.exit(-1);
        }

        file = new File("./index.bin");

        //initializing the randomaccessfile
        try {
            readStream = new RandomAccessFile("./" + args[0], "rw");
            writeStream = new RandomAccessFile(file, "rw");
            //Get the length of the file in bytes.
            filelen = readStream.length();
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find file to open. Are you typing it correctly?");
            System.exit(-1);
        } catch (IOException e) {
            System.out.println("Couldn't read from file...");
            System.exit(-1);
        }
        if(filelen < 105){
            System.out.println("No records in file to read.");
            System.exit(-1);
        }
        try {Files.deleteIfExists(Paths.get("./" + file)); }
        catch (IOException ioe) {
            System.out.println("Creating index file.");
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("File already exists somehow...");
        }

        //Initialize int arr with 0s
        MAX_LEN = new int[19];
        // Gets the max field lengths in bytes.
        getMaxFieldLen();
        //Assigning record length in bytes.
        recLen =  MAX_LEN[0] + MAX_LEN[1] + 4 + MAX_LEN[3] +
                MAX_LEN[4] + MAX_LEN[5] + MAX_LEN[6] +
                MAX_LEN[7] + 8 + 8 + MAX_LEN[10] +
                MAX_LEN[11] + 8 + MAX_LEN[13] + 8 +
                8 + MAX_LEN[16] + 8 + 8;
        numRecs = getNumOfRecords(); //calculating number of records

        //Creating the index file
        createIndex();
        //Populating it and creating the directory tree.
        populateIndex();

    }


    /**
     * This method is a little tricky. First, we have to read from whereever in the file we
     * saved the maximum field lengths, determine exactly how to read that, then use this information
     * to determine the byte-length of each line so we can iterate through the file. This isn't actually
     * a method necessary for completing the tasks of the assignment, but it is helpful for reading the
     * binary file and seeing if the program is writing correctly.
     * @since 1.1.1
     */
    private static void getMaxFieldLen(){
        try {
            int linelen = 4*19; //int byte size * fields
            long i = filelen - linelen; //points to the very last line of the file where we saved

            readStream.seek(i);
            for(int j = 0 ; j < 19; j++){
                MAX_LEN[j] = readStream.readInt();
            }
            System.out.println();

        } catch (IOException e) {
            System.out.println("Can't read from file.. Is it accessible?");
            System.exit(-1);
        }
    }

    /**
     * Get's the number of records.
     *
     * @return long value of the number of records in the file. file length / record length.
     * @since 1.0
     */
    private static long getNumOfRecords(){
        return filelen / recLen;
    }

    /**
     * Creates the hash bucket binary file used for our indexing into the
     * binary file of flight records. This implies that we are reading
     * all of the arr_time's that we can fit into each bucket of the index
     * file. This is the master function that includes splitting and initializing
     * of the directory tree.
     *
     * To begin with, the index file only has ten buckets, 0-9. So we can
     * leverage this and use an array from 0-9 to set offsets with some easy
     * maths.
     */
    private static void createIndex(){
        /*
            initializing root node of the directory tree.
         */
        root = new Node();
        //Insert blank data to fill the file up.
        for(int i = 0; i < 10; i++){
            //Fill up the bucket at i.
            fillBucket(root.getOffset(i));
        }
    }

    /**
     * Populate index is the meat of this program. To populate our index file,
     * we must assign each node an offset to point to where we are in our index file.
     * while doing this, we'll also be storing the offset in bytes of where this arr_time
     * field is located in the main bin data file.
     */
    public static void populateIndex() {
        //byebyte array to tell our readStream how many bytes we need to read for this string.
        byte[] arrTime = new byte[MAX_LEN[13]];
        int i = 0;
        int first, second, third, fourth; //this is going from right to left...first is last char
        try {
            /*
             * Loop through the entire csv->binary file that the user
             * has provided through a command line. We will iterate through each
             * record in the bin file and create a directroy tree and index file
             * out of it.
             */
             while (i < numRecs) {
                 readStream.seek((i * recLen) + MAX_LEN[0] + MAX_LEN[1] + MAX_LEN[2] +
                        MAX_LEN[3] + MAX_LEN[4] + MAX_LEN[5] + MAX_LEN[5] + MAX_LEN[6] +
                        MAX_LEN[7] + MAX_LEN[8] + MAX_LEN[9] + MAX_LEN[10] +  MAX_LEN[11] +
                        MAX_LEN[12]+1);
                 readStream.readFully(arrTime);

                 String arrTimeStr = new String(arrTime).replaceAll("\0", "-");
                 arrTimeStr = arrTimeStr.replaceAll(" ", "-");
                 if(arrTimeStr.equals("----") || arrTimeStr.length() < 4){
                     System.out.printf("Malformed arr_time. %s\n", new String(arrTime));
                     i++;
                     continue;
                 }
                 arrTimeStr = arrTimeStr.replaceAll("-", "0");
                 //System.out.println(arrTimeStr);
                 char[] chars =  arrTimeStr.toCharArray();
                 first = Integer.parseInt(chars[MAX_LEN[13]-1]+"");
                 second = Integer.parseInt(chars[MAX_LEN[13]-2]+"");
                 third = Integer.parseInt(chars[MAX_LEN[13]-3]+"");
                 fourth = Integer.parseInt(chars[MAX_LEN[13]-4]+"");

                 //If this is true, that means we haven't split any buckets yet...
                 if(root.isLeaf(first)){
                    writeStream.seek(root.getOffset(first));
                    int size = writeStream.readInt();
                    if(size < 100) {
                        //Seek to the place in front of where the last record was inserted in the index file.
                        writeStream.seek(root.getOffset(first) + 4 +  indexLen*size);
                        //write the arrival time to this index for us to find it later
                        String arrivalTime = new String(arrTime);
                        writeStream.writeBytes(arrivalTime);
                        //write the offset in which this line is located in the bin file.
                        writeStream.writeLong(i*recLen); //i*recLen

                        //Updating the size variable at the head of the bucket.
                        writeStream.seek(root.getOffset(first));
                        size += 1;
                        writeStream.writeInt(size);
                    }
                    else {
                        System.out.printf("Bucket %s is full. Size=%s, %s\n", first, size, arrTimeStr);
                        Node next = new Node();
                        root.setNode(first, next);
                        next = root.getNode(first);
                        long filelen = writeStream.length();
                        System.out.printf("Creating 10 new buckets at file pointer: %s\n", filelen);
                        for(int j = 0; j < 10; j++) {
                            next.setOffset(j, filelen + (j*indexLen*100));
                        }
                        //Insert blank data to fill the file up.
                        //Seek to the beginning of the file.
                        for(int j = 0; j < 10; j++){
                            fillBucket(next.getOffset(j));
                        }
                        /*
                            Now that we've initialized the new buckets and
                            created a new node for this path, we can start
                            redistributing the data that was in the initial
                            bucket.
                         */
                        byte[] rtime = new byte[MAX_LEN[13]];
                        String str = ""; int ind;
                        long roffset;
                        int rsize;
                        writeStream.seek(root.getOffset(first));
                        rsize = writeStream.readInt();
                        for(int j = 0; j < 100; j++){
                            writeStream.readFully(rtime);
                            roffset = writeStream.readLong();
                            str = new String(rtime);
                            ind = Integer.parseInt("" + str.charAt(MAX_LEN[13]-2));

                            /*
                                Okay, now that we have the bucket index for this slot, and the
                                offset and data that we need to relocate it to its new home, we
                                can go ahead and do that now.
                             */
                            

                            System.out.printf("Redistributing: %s %s %s %s \n", rsize, str, ind, roffset);
                        }
                        /*
                            Now that we have redistributed the contents of the
                            full bucket into new buckets, we can insert the record
                            that would've caused the initial bucket to overflow.
                        */

                    }
                 }
                //Otherwise, that means we have previously split our tree and we
                //need to follow the path to where it is we need to insert in our directory.
                 else if(root.getNode(first).isLeaf(second)) {
                     writeStream.seek(root.getOffset(second));
                     int size = writeStream.readInt();
                     if (size < 100) {

                     } else {
                         System.out.printf("Bucket %s%s is full\n", first, second);
                     }
                 } else if(root.getNode(first).getNode(second).isLeaf(third)) {

                 } else if(root.getNode(first).getNode(second).getNode(third).isLeaf(fourth)){

                 } else {
                     System.out.println("Dunno what's up");
                 }

                 i++;
            }
        } catch (IOException ioe){
            System.out.println("Couldn't seek there");
            System.exit(-1);
        }
    }

    private static void fillBucket(long offset){
        try{
            //Place the file pointer so we are writing to the correct location
            writeStream.seek(offset);
            //First, the program will write the count of how many slots are in this bucket. Right
            //now, that is 0 since we are just creating and initializing it.
            writeStream.writeInt(0);
            int k = 0;
            while(k < 100) {
                writeStream.writeBytes(new String(new char[MAX_LEN[13]]).replace("\0", "0"));
                writeStream.writeLong(0); //offset for main bin file location
                k++;
            }
        } catch (IOException e) {
            System.out.println("Something went wrong when constructing the index file.");
        }
/*
        try {
            writeStream.seek(0);
            byte[] byt = new byte[MAX_LEN[13]];
            long lon = -1;
            writeStream.seek(offset);
            int size = writeStream.readInt();
            for(int i = 0; i < 100; i++){
                writeStream.readFully(byt);
                lon = writeStream.readLong();
                System.out.printf("%s %s %s\n", size, lon, new String(byt));
            }
        } catch(IOException ioe){

        }
//        System.exit(-1);*/
    }
}
