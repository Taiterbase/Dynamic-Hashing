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
    private static RandomAccessFile dataStream; //data stream for reading and writing.
    private static long filelen, numRecs; //length of the file, number of records
    private static int reclen;


    /**
     * -----Program Flow------
     *
     * 1.
     * 2.
     * 3.
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

        file = new File(args[0].substring(args[0].lastIndexOf('/'), args[0].length() - 3) + "bin");

        //initializing the randomaccessfile
        try {
            dataStream = new RandomAccessFile(args[0], "rw");
            //Get the lenght of the file in bytes.
            filelen = dataStream.length();
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

        //Initialize int arr with 0s
        MAX_LEN = new int[19];
        // Gets the max field lengths in bytes.
        getMaxFieldLen();
        //Assigning record length in bytes.
        reclen =  MAX_LEN[0] + MAX_LEN[1] + 4 + MAX_LEN[3] +
                MAX_LEN[4] + MAX_LEN[5] + MAX_LEN[6] +
                MAX_LEN[7] + 8 + 8 + MAX_LEN[10] +
                MAX_LEN[11] + 8 + MAX_LEN[13] + 8 +
                8 + MAX_LEN[16] + 8 + 8;
        numRecs = getNumOfRecords(); //calculating number of records

        createIndex();

    }


    /**
     * This method is a little tricky. First, we have to read from whereever in the file we
     * saved the maximum field lengths, determine exactly how to read that, then use this information
     * to determine the byte-length of each line so we can iterate through the file. This isn't actually
     * a method necessary for completing the tasks of the assignment, but it is helpful for reading the
     * binary file and seeing if the program is writing correctly.
     * @version 1.2
     * @since 1.1.1
     */
    private static void getMaxFieldLen(){
        try {
            int linelen = 4*19; //int byte size * fields
            long i = filelen - linelen; //points to the very last line of the file where we saved

            dataStream.seek(i);
            for(int j = 0 ; j < 19; j++){
                MAX_LEN[j] = dataStream.readInt();
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
        return filelen / reclen;
    }

    /**
     * Creates the hash bucket binary file used for our indexing into the
     * binary file of flight records.
     */
    private static void createIndex(){

    }
}
