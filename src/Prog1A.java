/*
    Name: Taite Nazifi
    Course: CSc460 Database Design
    Assignment: Project 1 Part A Binary IO.
    Instructor: Dr. McCann
    Due: August 30th, 2018

    Required operations: Java 1.8, csv input file, input file
    is passed in as a command line argument.

    Program specifications:
    For this program, we have lines of data items that we need to store,
    and each lineâ€™s items need to be stored as a group (in a database,
    such a group of fields is called a record). Making this happen in Java requires a bit
    of effort; Java wasnâ€™t originally designed for this sort of task. By comparison,
    "systems" languages like C can interact more directly with the operating system to
    provide more convenient file I/O. On the class web page youâ€™ll find a sample Java binary
    file I/O program, which should help get you started. Yes, I just copy-pasta'd that.
    Sue me.
 */
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

/**
 * -----Prog1A-----
 * This class is designed for consuming a csv file, from a command-line-argument
 * specified path, and outputting its binary representation in the form of
 * ./path/filename.csv -> ./filename.bin. These .csv files are limited to the
 * data-types INT, DOUBLE, and STRING. Each field must maintain a homogeneous number
 * of bytes for every record. To do this, we must find the maximum number of bytes a
 * field may have.
 * This program finds the maximum number of bytes for each field upon each execution
 * as we can do this in O(1) time while reading in the file line-by-line.
 *
 * @author Taite Nazifi
 * @version 1.0.6
 * @since 1.0.0
 */
public class Prog1A {

    /*
        Variables concerning the data related to the file that
        the program is reading from.
     */
    private static File file; //The file in which we are writing to.
    public static int[] MAX_STRING_FIELD_LEN = null; // An array of the maximum string lengths of each string var.
    private static ArrayList<FlightRecord> records; //List of records that we read from the file.


    /**
     * -----Program Flow; main()-----
     * 1. Read file in, line-by-line
     *  a. Find number of fields in first record with string type (int numStrings)
     *  b. Create collection of size numStrings
     *  c. maintain proper bookkeeping of largest string value for each field in collection
     * 2. Once we have a collection of all records, use Collections.sort() to sort on 5th field.
     * 3. Begin writing to binary file.
     * @since 1.0.0
     * @param args the command-line argument, typically a file path.
     */
    public static void main(String [] args) {
        //Creates a File object with filename 'name.bin'. This is
        //the file in which we will be writing to.

        if(args.length < 1) {
            System.out.println("Program requires filepath argument.");
            System.exit(-1);
        }
	try {
            file = new File(args[0].substring(args[0].lastIndexOf('/'), args[0].length() - 3) + "bin");
        } catch (StringIndexOutOfBoundsException sioobe){
            file = new File(args[0].substring(0, args[0].length() - 3) + "bin");
        }
        System.out.println(String.format("Creating file with obj name: %s", file.getName()));
        //If the file exists, then we're deleting it. no ifs, ands or butts.
        try {Files.deleteIfExists(Paths.get("./" + file)); }
        catch (IOException ioe) {
            System.out.println("Couldn't find or access file to delete...");
            System.exit(-1);
        }

        MAX_STRING_FIELD_LEN = new int[19];
        try {
            Files.newBufferedReader(Paths.get(args[0])).readLine().split(",");
        } catch (IOException e) {
            System.out.println("Can't read from file");
            System.exit(-1);
        } catch (NullPointerException np){
            System.out.println("File is empty, or not csv formatted");
            System.exit(-1);
        }

        records = getRecordsFromFile(Paths.get(args[0])); //Paths.get(args[0]) is the file to read from.
        if(records.isEmpty()){
            System.out.println("Empty records list.");
            System.exit(-1); //IT's the 27th DAWG.
        }
        //Print out what the collection looks like before sorting...
        System.out.println("Before sorting");

        //Sort the list of records on its fifth field FL_NUM.
        Collections.sort(records, (e1, e2) -> Integer.parseInt(e1.getFlNum()) - Integer.parseInt(e2.getFlNum()));

        System.out.println("Done sorting");
        /*
            Now that the collection has been sorted we can begin
            writing out the contents to a binary file.
         */
        RandomAccessFile dataStream = null;
        try {
            dataStream = new RandomAccessFile("./" + file, "rw");
        } catch (FileNotFoundException e) {
            System.out.println("Could not create random access file.");
            System.exit(-1);
        }
        writeRecordsToFile(dataStream);
        System.out.println("Done writing records to file in binary.");
    }

    /**
     * getRecordsFromFile will create a BufferedReader object that will read from
     * the given parameter path. This method reads line-by-line, looking at each
     * string field and finding the largest string out of the bunch. Eventually,
     * when we have read in every record, we would have found the largest string
     * value out of all the records.
     * @since 1.0.2
     * @param path the path to a csv file.
     * @return a complete collection of each record within the given path to a csv file.
     */
    public static ArrayList<FlightRecord> getRecordsFromFile(Path path){
        ArrayList<FlightRecord> recs = new ArrayList<>();
        int i = 0;
        try (BufferedReader br = Files.newBufferedReader(path)) {

            System.out.println("Reading from " + path);
            // read the first line from the text file
            br.readLine();//Removing the first line of the csv, as it's not data.
            String line = br.readLine(); //actually reading in the first line of data.
            //Loop until we've read every line in the csv file.
            while (line != null) {
                //Split the line using a comma as the delimiter.
                String[] fields = line.split(",");
                /*
                    This loop will compare each string element within the record and find
                    the string that has the largest length, and ultimately the longest string
                    within the entire file.
                 */
                for(int j = 0; j < fields.length; j++){
                    fields[j] = fields[j].replace("\"", "");
                    if(MAX_STRING_FIELD_LEN[j] < fields[j].length()){
                        MAX_STRING_FIELD_LEN[j] = fields[j].length();
                    }
                    if(j == 4) // added s'curritay for FL_NUM being non-numeric.
                    {
                        /*
                            If the fl_num field isn't alphanumeric, then we throw an error.
                            when we catch the error we set fields == null, break from the loop
                            and then check to see if fields is null.
                            if it's null, we just continue and don't add
                            a record.
                         */
                        try{
                            Integer.parseInt(fields[j]);
                        } catch (NumberFormatException nfe){
                            System.out.println(fields[j]);
                            fields = null;
                            break;
                        }
                    }
                }
                //if null, dont add the record cus the data is malformed
                if(fields != null)
                {
                    recs.add(new FlightRecord(fields));
                }
                line = br.readLine();
                i++;
            }
        }
         catch(IOException ioe) {
            System.out.println("Couldn't read from file.");
            System.exit(-1);
         }
        return recs;
    }

    /**
     * writeRecordsToFile will iterate over each record in the file, and then call
     * each record's writeRecord method.
     * After writing each records data, write each integer value for the maximum
     * length of each string field.
     * @since 1.0.5
     * @param dataStream the RAF stream for writing records to the file.
     */
    public static void writeRecordsToFile(RandomAccessFile dataStream){
        for(FlightRecord record : records){
            record.writeRecord(dataStream);
        }
        /*
            Since we're done writing each record to the file, now we must move on
            to writing out each 'max' string length variable. This will actually
            also write out the string lengths of each integer variable as well,
            just because I'm a lazy programmer and it might become useful
            later or something, who knows.
         */
        for(int i : MAX_STRING_FIELD_LEN) {
            try {
                dataStream.writeInt(i);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("IOException occurred while writing out the contents of the max\n" +
                        "string record array. Not really sure about this one, pal.");
                System.exit(-1);
            }
        }
        /*
            For some reason, this throws an error that I gotta catch and do something
            with. What a garbage mechanism.
         */
        try {
            dataStream.close();
        } catch (IOException e) {
            System.out.println("Couldn't close data stream (RandomAccessFile).\nCall mommy, son. Daddy doesn't know.");
            System.exit(-1);
        }
    }


}


