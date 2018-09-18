/*
    Name: Taite Nazifi
    Course: CSc460 Database Design
    Assignment: Project 1 Part B Binary IO.
    Instructor: Dr. McCann
    Due: September 6th, 2018

    Required operations: Java 1.8, binary input file, input file
    is passed in as a command line argument and sorted on fl_num,
    also has required 19 fields each with the same length
    Max field lengths written to binary file at end or beginning
    of file.

    Program specifications:
    For this program, we have lines of data items that we need to read,
    and each lineâ€™s items need to be sorted as a group (in a database,
    such a group of fields is called a record). Making this happen in Java requires a bit
    of effort; Java wasnâ€™t originally designed for this sort of task. By comparison,
    "systems" languages like C can interact more directly with the operating system to
    provide more convenient file I/O. On the class web page youâ€™ll find a sample Java binary
    file I/O program, which should help get you started. Yes, I just copy-pasta'd that.
    Sue me.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

/**
 * -----Prog1B-----
 * This class is designed for reading and outputting to the console the
 * first five, middle 3 or 4, and last five records in the binary file that
 * was created by part A of this assignment. The program will read in from the
 * command line a file path that points to a binary file created by part A.
 * After this file has been read, the program determines how many records
 * were written to the file, and then seek to the end of the file to
 * read the maximum field sizes from the binary file. We use these lengths
 * to normalize our data so we can read in a massive amount of records really
 * easily.
 *
 * This program comes with a few givens: we know which field comes before
 * another, and how many fields there are in a record.
 *
 * @author Taite Nazifi
 * @version 1.1.7
 * @since 1.1.0
 */
public class Prog1B {

    /*
        Declaring private instance variables...
     */
    private static int[] MAX_LEN; //int array for maximum field lengths
    private static File file; //file pointer
    private static RandomAccessFile dataStream; //data stream for reading from the file.
    private static long filelen, numRecs; //length of file, and number of records.


    /**
     * -----Program Flow; main()-----
     * 1. Initialize instance variables to avoid null ptr exceptions
     * 2. Create a data stream to the given file argument
     * 3. Get the length of the file so we can determine how many bytes the file is.
     * 4. Calculate num of records based off of file length and rec length
     * 5. Read max_field values from file
     * 6. Read first 5, middle 3 or 4, last 5 records from the binary file.
     * 7. Start Interpolation Search queries.
     *      a. Read all target flnums from the file with the same target.
     *
     * @param args
     *
     * @author Taite Nazifi
     * @version 1.1.7
     * @since 1.1.0
     */
    public static void main(String[] args){
        if(args.length < 1) {
            System.out.println("Program requires filepath argument.");
            System.exit(-1);
        }

        //Initializing instance variables with values.
	try {
            file = new File(args[0].substring(args[0].lastIndexOf('/'), args[0].length() - 3) + "bin");
        } catch (StringIndexOutOfBoundsException sioobe){
            file = new File(args[0].substring(0, args[0].length() - 3) + "bin");
        }
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
        FlightRecord.RECORD_LEN =  MAX_LEN[0] + MAX_LEN[1] + 4 + MAX_LEN[3] +
                                    MAX_LEN[4] + MAX_LEN[5] + MAX_LEN[6] +
                                    MAX_LEN[7] + 8 + 8 + MAX_LEN[10] +
                                    MAX_LEN[11] + 8 + MAX_LEN[13] + 8 +
                                    8 + MAX_LEN[16] + 8 + 8;
        numRecs = getNumOfRecords(); //calculating number of records

        /*
            Begin reading in from the binary file.
            Say your prayers, children. Daddy's coming
            home.
         */
        readFirstFive();
        readMiddleRecords();
        readLastFive();

        System.out.printf("There are %s record(s) in the file.\n", numRecs);

        /*
            Begin interpolation search on the binary file.
            Prepare yourselves for the end
         */

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter a flight number (FL_NUM) that you would like to search i.e. 2817. Enter zero(0) to end your search.");
        long flight = sc.nextInt();
        while(flight != 0) {
            if(search(flight) == 0){
                System.out.printf("No records found matching flight %s.\n", flight);
            }
            System.out.println("Enter a flight number (FL_NUM) that you would like to search i.e. 2817. Enter zero(0) to end your search.");
            flight = sc.nextInt();
        }
        System.out.println("End of search.");
        try {
            dataStream.close();
        } catch (IOException e) {
            System.out.println("Odd error- ask yo mama son.");
            System.exit(-1);
        }
        System.out.println("End of program.");
    }

    /**
     * Get's the number of records.
     *
     * @return long value of the number of records in the file. file length / record length.
     * @version 1.0
     * @since 1.1.0
     */
    private static long getNumOfRecords(){
        return filelen / FlightRecord.RECORD_LEN;
    }

    /**
     * This method is important-- it reads the binary file and
     * outputs to the console the first five records; this method
     * will only print out the four fields: the unique carrier, flight number,
     * origin and destination. Only prints first five lines of the file
     *
     * Dynamically moves the file pointers to the first spot of the first
     * record. Then calculates the distance from the current file pointer
     * location to grab the rest of the field values within each record.
     * Pretty neat.
     *
     * @version 1.3
     * @since 1.1.1
     */
    private static void readFirstFive(){
        try {
            System.out.println("FIRST FIVE RECORDS:");
            //Place file pointer in correct location...
            byte[] uniqueCarrier = new byte[MAX_LEN[1]];
            byte[] flnum = new byte[MAX_LEN[4]];
            byte[] origin = new byte[MAX_LEN[5]];
            byte[] dest = new byte[MAX_LEN[6]];
	    byte[] arrtime = new byte[MAX_LEN[13]];
            for (int i = 0; i < numRecs; i++) {
                if(i == 5)
                    break;
                dataStream.seek((i*FlightRecord.RECORD_LEN) + MAX_LEN[0]); //
                dataStream.readFully(uniqueCarrier);
                dataStream.seek(dataStream.getFilePointer() + 4 + MAX_LEN[3]);
                dataStream.readFully(flnum);
                dataStream.readFully(origin);
                dataStream.readFully(dest);
		dataStream.seek((i*FlightRecord.RECORD_LEN) + MAX_LEN[0] + MAX_LEN[1] + MAX_LEN[2] +
                        MAX_LEN[3] + MAX_LEN[4] + MAX_LEN[5] + MAX_LEN[5] + MAX_LEN[6] +
                        MAX_LEN[7] + MAX_LEN[8] + MAX_LEN[9] + MAX_LEN[10] +  MAX_LEN[11] +
                        MAX_LEN[12]);
		dataStream.readFully(arrtime);

                System.out.printf("[%s]: %s, %s, %s, %s, %s.\n", i, new String(uniqueCarrier), new String(flnum), new String(origin), new String(dest), new String(arrtime));

            }
            System.out.println("-----------------------------------");
        } catch (IOException ioe){
            System.out.println("Some io error occurred, couldn't read or place file pointer correctly.");
            System.exit(-1);
        }
    }

    /**
     * This method is important-- it reads the binary file and
     * outputs to the console the first five records; this method
     * will only print out the four fields: the unique carrier, flight number,
     * origin and destination. Only prints first five lines of the file
     *
     * Dynamically moves the file pointers to the first spot of the first
     * record. Then calculates the distance from the current file pointer
     * location to grab the rest of the field values within each record.
     * only kind of neat. Just barely.
     *
     * @version 1.3
     * @since 1.1.2
     */
    private static void readMiddleRecords(){
        System.out.println("MIDDLE " + (numRecs%2 == 0 ? "FOUR" : "THREE") + " RECORDS:");
        try {
            byte[] uniqueCarrier = new byte[MAX_LEN[1]];
            byte[] flnum = new byte[MAX_LEN[4]];
            byte[] origin = new byte[MAX_LEN[5]];
            byte[] dest = new byte[MAX_LEN[6]];
	    byte[] arrtime = new byte[MAX_LEN[13]];

            for (long i = (numRecs%2 == 0 ? numRecs/2-2 : numRecs/2 - 1); i < numRecs; i++) {
                if(i == (numRecs%2 == 0 ? numRecs/2+2 :
                        numRecs/2+2))
                    break;
                dataStream.seek((i*FlightRecord.RECORD_LEN) + MAX_LEN[0]); //
                dataStream.readFully(uniqueCarrier);
                dataStream.seek(dataStream.getFilePointer() + 4 +  MAX_LEN[3]);
                dataStream.readFully(flnum);
                dataStream.readFully(origin);
                dataStream.readFully(dest);
		dataStream.seek((i*FlightRecord.RECORD_LEN) + MAX_LEN[0] + MAX_LEN[1] + MAX_LEN[2] +
                        MAX_LEN[3] + MAX_LEN[4] + MAX_LEN[5] + MAX_LEN[5] + MAX_LEN[6] +
                        MAX_LEN[7] + MAX_LEN[8] + MAX_LEN[9] + MAX_LEN[10] +  MAX_LEN[11] +
                        MAX_LEN[12]);
		dataStream.readFully(arrtime);

                System.out.printf("[%s]: %s, %s, %s, %s, %s.\n", i, new String(uniqueCarrier), new String(flnum), new String(origin), new String(dest), new String(arrtime));


            }
        } catch (IOException e) {
            System.out.println("Problem reading file");
            System.exit(-1);
        }
        System.out.println("-----------------------------------");
    }

    /**
     * This method is like the other ones, it reads the binary file and
     * outputs to the console the last five records; this method
     * will only print out the four fields: unique carrier, flight number,
     * origin, and destination. Only prints last five lines of the file
     *
     * Dynamically moves the file pointers to the first spot of the last
     * five records. Then calculates the distance from the current file pointer
     * location to grab the rest of the field values within each record.
     * Super awesome. One of the best methods I've ever licked with my toes.
     *
     * @version 1.3
     * @since 1.1.2
     */
    private static void readLastFive(){
        System.out.println("LAST FIVE RECORDS:");

        try {
            byte[] uniqueCarrier = new byte[MAX_LEN[1]];
            byte[] flnum = new byte[MAX_LEN[4]];
            byte[] origin = new byte[MAX_LEN[5]];
            byte[] dest = new byte[MAX_LEN[6]];
	    byte[] arrtime = new byte[MAX_LEN[13]];

            for (long i = numRecs-5; i < numRecs; i++) {
                if(i < 0)
                    i = 0;
                if(i == numRecs)
                    break;
                dataStream.seek((i*FlightRecord.RECORD_LEN) + MAX_LEN[0]);
                dataStream.readFully(uniqueCarrier);
                dataStream.seek(dataStream.getFilePointer() + 4 + MAX_LEN[3]);
                dataStream.readFully(flnum);
                dataStream.readFully(origin);
                dataStream.readFully(dest);
		dataStream.seek((i*FlightRecord.RECORD_LEN) + MAX_LEN[0] + MAX_LEN[1] + MAX_LEN[2] +
                        MAX_LEN[3] + MAX_LEN[4] + MAX_LEN[5] + MAX_LEN[5] + MAX_LEN[6] +
                        MAX_LEN[7] + MAX_LEN[8] + MAX_LEN[9] + MAX_LEN[10] +  MAX_LEN[11] +
                        MAX_LEN[12]);
		dataStream.readFully(arrtime);

                System.out.printf("[%s]: %s, %s, %s, %s, %s.\n", i, new String(uniqueCarrier), new String(flnum), new String(origin), new String(dest), new String(arrtime));


            }
        } catch (IOException e) {
            System.out.println("Problem reading file");
            System.exit(-1);
        }

        System.out.println("-----------------------------------");
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
     * This is my interpretation of interpolation search. Interpolation search
     * is a take on binary search with a twist; instead of trimming the data
     * set by 2, we interpolate and make an educated guess- which is something
     * that binary search isn't capable of.
     * Interpolation search makes an educated guess such that it infers where
     * the key is located in the file. IS requires a uniformly distritbuted data
     * set for it to work efficiently. Since our data is stored in a direct-access
     * data structure (a database, in a higher-level granularity as a bin file), the
     * data is sorted, and there's a lot of data, IS will work pretty well in this
     * scenario. Probably not that well if we are testing 10-100000 records...
     *
     * @version 1.3
     * @since 1.1.5
     */
    private static int search(long flight){
        try {
        /*
            Initializing field variables for reading and string casting.
         */
            byte[] flnum = new byte[MAX_LEN[4]];
            int flnumlen = MAX_LEN[0] + MAX_LEN[1] + 4 + MAX_LEN[3];
            long lowkey = 0, highkey = numRecs-1;

            /*
                Initializing interpol values for search before the
                while loop, so we can check their conditions.
                This is kind of a failsafe to make sure that we
                sorted the file correctly.
             */
            dataStream.seek(lowkey*FlightRecord.RECORD_LEN + flnumlen);
            dataStream.readFully(flnum);
            long lowval = Integer.parseInt(new String(flnum).trim());
            dataStream.seek(highkey*FlightRecord.RECORD_LEN + flnumlen);
            dataStream.readFully(flnum);
            long highval = Integer.parseInt(new String(flnum).trim());

            /*
                Now, we begin the start of the while loop. This while loop iterates through
                the range of indices and performs the interpolation search until the flight
                given is found or not found.
             */
            while (lowkey  <= highkey && flight >= lowval && flight <= highval) {
                //Get new probe key. we must be casting to a double since the result
                //of this division would be 0, and 0*anything is 0... sadlife.
                long pos = (int)(lowkey + (((double)(flight - lowval)/(highval-lowval))*(highkey-lowkey)));
                //System.out.printf("%s + (((%s - %s) / (%s - %s)) * (%s - %s));\n", lowkey, flight, lowval, highval, lowval, highkey, lowkey);
                dataStream.seek(pos * FlightRecord.RECORD_LEN + flnumlen);
                dataStream.readFully(flnum);
                //probe value
                long posval = Integer.parseInt(new String(flnum).trim());
                if (posval == flight) {
                    //Find all of the other fields with the same flight value.
                    printRecords(flight, pos);
                    return 1;
                } else if (posval < flight) {
                    lowkey = pos + 1; //boop
                } else {
                    highkey = pos - 1; //bop
                }

                 //readjusting low and high vals for next iteration condition check.
                dataStream.seek(lowkey*FlightRecord.RECORD_LEN + flnumlen);
                dataStream.readFully(flnum);
                lowval = Integer.parseInt(new String(flnum).trim());
                dataStream.seek(highkey*FlightRecord.RECORD_LEN + flnumlen);
                dataStream.readFully(flnum);
                highval = Integer.parseInt(new String(flnum).trim());
               // System.out.printf("Lowkey: %s, highkey: %s, pos: %s\nLowval: %s, highval: %S, posval: %s\n\n", lowkey, highkey, pos, lowval, highval, posval);
                //return 0;
            }
        } catch (IOException io){
            System.out.println("Seeking and reading file error in search.");
            System.exit(-1);
        }
        return 0;
    }

    /**
     * printRecords will print out every record in the binary file that
     * matches the flight long. It does this by backtracking through the file
     * until it find the first occurrence of the flight number target, then
     * sets our probing index to this value. Once we have this value, we can
     * then iterate and walk through the file until there are no more records
     * to read, or we've read the last record with the target flight number.
     *
     * @since 1.1.6
     * @version 1.2
     * @param flight target flight number that we are trying to read from the file.
     * @param pos index position in the file
     */
    public static void printRecords(long flight, long pos){
        byte[] uniqueCarrier = new byte[MAX_LEN[1]];
        byte[] flnum = new byte[MAX_LEN[4]];
        byte[] origin = new byte[MAX_LEN[5]];
        byte[] dest = new byte[MAX_LEN[6]];

        /*
            Find the starting position; the first place in the file where
            we see this flight value. We will backtrack until we hit the
            last occurrence of it.
         */
        int flnumlen = MAX_LEN[0] + MAX_LEN[1] + 4 + MAX_LEN[3];
        long lastOcc = pos-1;
        while(true){
            try {
                //If we've gone all the way to the start of the file,
                //just back out and set lastOcc to the first record.
                if(lastOcc < 0)
                {
                    lastOcc = 0;
                    break;
                }
                //Find the current position and check its flight number.
                dataStream.seek(lastOcc * FlightRecord.RECORD_LEN + flnumlen);
                dataStream.readFully(flnum);
                if(Integer.parseInt(new String(flnum).trim()) == flight){
                    lastOcc--; // keep looking for first occurrence.
                } else {
                    lastOcc++; //we found it.
                    break;
                }
            }catch(IOException ioe){
                //Something wrong happened... Probably hit a negative or went too far?
                System.out.println(lastOcc + " " + pos);
                System.exit(-1);
            }
        }
        //Set last position to lastOcc.
        pos = lastOcc;
        while(true) {
            try {
                dataStream.seek((pos * FlightRecord.RECORD_LEN) + MAX_LEN[0]);
                dataStream.readFully(uniqueCarrier);
                dataStream.seek(dataStream.getFilePointer() + 4 + MAX_LEN[3]);
                dataStream.readFully(flnum);
                dataStream.readFully(origin);
                dataStream.readFully(dest);
            } catch (IOException ioe) {
                System.out.println("Can't seek or read while printing result...");
                System.exit(-1);
            }
            try {
                //If this condition is met, we've probably hit the end of the road,
                //buddy-ol-pal. Return back to caller.
                if (Integer.parseInt(new String(flnum).trim()) != flight)
                    return;
            } catch (Exception e) {
                return;
            }
            //Prints out the thing in the format.
            System.out.printf("[%s]: %s, %s, %s, %s\n", pos, new String(uniqueCarrier), new String(flnum), new String(origin), new String(dest));
            pos++;
        }
    }
}
