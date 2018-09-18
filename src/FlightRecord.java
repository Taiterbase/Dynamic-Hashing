import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;

/**
 * -----Class Record-----
 * Incoming mound of poo.
 * This class is a helper object for Prog1A. This class's purpose is meant to provide
 * a few easy ways of writing and reading in an object from a csv or binary file.
 *
 * A Record is a data object that represents each record inside of the csv file
 * that we will be reading from. This makes it easier for us to manage the massive
 * amount of data that needs to be read and written to the file.
 * This object has many, many getters and setters due to it needing to store
 * 19 different fields of data. This object is huge and disgusting,
 * reminiscent of someone I used to date.
 *
 * @since 1.0.3
 *
 */
public class FlightRecord {
//    "FL_DATE","UNIQUE_CARRIER","AIRLINE_ID","TAIL_NUM","FL_NUM","ORIGIN","DEST","DEP_TIME",
// "DEP_DELAY","TAXI_OUT","WHEELS_OFF","WHEELS_ON","TAXI_IN","ARR_TIME","ARR_DELAY",
// "CANCELLED","CANCELLATION_CODE","AIR_TIME","DISTANCE"

    private String date;
    private String uniqueCharacter;
    private int airlineID;
    private String tailNum;
    private String flNum;
    private String origin;
    private String dest;
    private String depTime;
    private double depDelay;
    private double taxiOut;
    private String wheelsOff;
    private String wheelsOn;
    private double taxiIn;
    private String arrTime;
    private double arrDelay;
    private double cancelled;
    private String cancellationCode;
    private double airTime;
    private double distance;

    //The only public static int thingie that gives us the record length.
    //This never changes and is only initialized once, so I am making an
    //executive decision and leaving it public. Call my parents if you want.
    public static int RECORD_LEN;


    /*
        Incoming swath of mother-loving getters and setters that I will
        package up all cute-and-nicely for my adorable ex and mail
        it to her in a lovely packaged box. Hopefully she will enjoy how
        ugly it is since it fully represents her personality.
     */

    public String getDate() {return date;}
    public void setDate(String date) {this.date = date;}

    public String getUniqueCharacter() {return uniqueCharacter;}
    public void setUniqueCharacter(String uniqueCharacter) {this.uniqueCharacter = uniqueCharacter;}

    public int getAirlineID() {return airlineID;}
    public void setAirlineID(int airlineID) {this.airlineID = airlineID;}

    public String getTailNum() {return tailNum;}
    public void setTailNum(String tailNum) {this.tailNum = tailNum;}

    public String getFlNum() {return flNum;}
    public void setFlNum(String flNum) {this.flNum = flNum;}

    public String getOrigin() {return origin;}
    public void setOrigin(String origin) {this.origin = origin;}

    public String getDest() {return dest;}
    public void setDest(String dest) {this.dest = dest;}

    public String getDepTime() {return depTime;}
    public void setDepTime(String depTime) {this.depTime = depTime;}

    public double getDepDelay() {return depDelay;}
    public void setDepDelay(double depDelay) {this.depDelay = depDelay;}

    public double getTaxiOut() {return taxiOut;}
    public void setTaxiOut(double taxiOut) {this.taxiOut = taxiOut;}

    public String getWheelsOff() {return wheelsOff;}
    public void setWheelsOff(String wheelsOff) {this.wheelsOff = wheelsOff;}

    public String getWheelsOn() {return wheelsOn;}
    public void setWheelsOn(String wheelsOn) {this.wheelsOn = wheelsOn;}

    public double getTaxiIn() {return taxiIn;}
    public void setTaxiIn(double taxiIn) {this.taxiIn = taxiIn;}

    public String getArrTime() {return arrTime;}
    public void setArrTime(String arrTime) {this.arrTime = arrTime;}

    public double getArrDelay() {return arrDelay;}
    public void setArrDelay(double arrDelay) {this.arrDelay = arrDelay;}

    public double getCancelled() {return cancelled;}
    public void setCancelled(double cancelled) {this.cancelled = cancelled;}

    public String getCancellationCode() {return cancellationCode;}
    public void setCancellationCode(String cancellationCode) {this.cancellationCode = cancellationCode;}

    public double getAirTime() {return airTime;}
    public void setAirTime(double airTime) {this.airTime = airTime;}

    public double getDistance() {return distance;}
    public void setDistance(double distance) {this.distance = distance;}

    /**
     * Initializes each field in a record with the string array parameter.
     * This will also take into account the padding that needs to occur
     * for each field in each record that is a string. Since we have an
     * array that maintains the maximum string length of each field at our
     * disposal, we can take the difference of the max string length and the
     * given string length for this record and pad that many spaces to it.
     * @since 1.0.3
     * @param v String array representation of a record.
     */
    public FlightRecord(String[] v){
        setDate(v[0]);
        setUniqueCharacter(v[1]);
        //Try catch block that will massage the data to be what we want it to be
        //in the event that it is not formatted correctly.
        try {setAirlineID(Integer.parseInt(v[2]));} catch (NumberFormatException nfe)
        {setAirlineID(-1); }
        setTailNum(v[3]);
        setFlNum(v[4]);
        setOrigin(v[5]);
        setDest(v[6]);
        setDepTime(v[7]);
        //Does the same as above, except for doubles.
        try {setDepDelay(Double.parseDouble(v[8]));} catch (NumberFormatException nfe)
        {setDepDelay(-1.0);}
        try {setTaxiOut(Double.parseDouble(v[9]));} catch (NumberFormatException nfe)
        {setTaxiOut(-1.0);}
        setWheelsOff(v[10]);
        setWheelsOn(v[11]);
        try {setTaxiIn(Double.parseDouble(v[12]));} catch (NumberFormatException nfe)
        {setTaxiIn(-1.0);}
        setArrTime(v[13]);
        try {setArrDelay(Double.parseDouble(v[14]));} catch (NumberFormatException nfe)
        {setArrDelay(-1.0);}
        try {setCancelled(Double.parseDouble(v[15]));} catch (NumberFormatException nfe)
        {setCancelled(-1.0);}
        setCancellationCode(v[16]);
        try {setAirTime(Double.parseDouble(v[17]));} catch (NumberFormatException nfe)
        {setAirTime(-1.0);}
        try {setDistance(Double.parseDouble(v[18]));} catch (NumberFormatException nfe)
        {setDistance(-1.0);}
        //disgusting.
    }

    /**
     * writeRecord does what the name implies: this method will write a record
     * to the RandomAccessFile data stream in record-breaking time! Buy now for
     * $19.99 before it's too late.
     * This simply writes each field to the file in the order in which the fields
     * were read in. I do not write strings or ints or doubles first; it is very
     * simply just the order that the fields were read in from the file.
     * @since 1.0.4
     * @param dataStream RandomAccessFile object that we will be using to write ints, doubles, and bytes to the
     *                   file stream.
     */
    public void writeRecord(RandomAccessFile dataStream){
        try {
            /*
                Initializes a new String FROM a new character array. The character array is initialized to the
                difference between the maximum string length and its actual string length. Since automatic char
                inits initialize the char array with null-terminators, I simply replace how many null terminators
                there are with spaces. Simple 1-liner swag. call my mom tell her im on fiyaaaaaaa cus deez comments
                so LIT
            */
            dataStream.writeBytes(getDate() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[0] - getDate().length()]).replace("\0", " "));
            dataStream.writeBytes(getUniqueCharacter() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[1] -
                    getUniqueCharacter().length()]).replace("\0", " ")
            );
            /*
                If it's an int, that's easy and we can just write it. it doesn't need any special
                massaging or therapy from mommy.
             */
            dataStream.writeInt(getAirlineID());
            dataStream.writeBytes(getTailNum() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[3] -
                    getTailNum().length()]).replace("\0", " ")
            );
            dataStream.writeBytes(getFlNum() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[4] -
                    getFlNum().length()]).replace("\0", " ")
            );
            dataStream.writeBytes(getOrigin() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[5] -
                    getOrigin().length()]).replace("\0", " ")
            );
            dataStream.writeBytes(getDest() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[6] -
                    getDest().length()]).replace("\0", " ")
            );
            dataStream.writeBytes(getDepTime() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[7] -
                    getDepTime().length()]).replace("\0", " ")
            );
            /*
                Same with doubles. ez.
             */
            dataStream.writeDouble(getDepDelay());
            dataStream.writeDouble(getTaxiOut());
            dataStream.writeBytes(getWheelsOff() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[10] -
                    getWheelsOff().length()]).replace("\0", " ")
            );
            dataStream.writeBytes(getWheelsOn() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[11] -
                    getWheelsOn().length()]).replace("\0", " ")
            );
            dataStream.writeDouble(getTaxiIn());
            dataStream.writeBytes(getArrTime() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[13] -
                    getArrTime().length()]).replace("\0", " ")
            );
            dataStream.writeDouble(getArrDelay());
            dataStream.writeDouble(getCancelled());
            dataStream.writeBytes(getCancellationCode() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[16] -
                    getCancellationCode().length()]).replace("\0", " ")
            );
            dataStream.writeDouble(getAirTime());
            dataStream.writeDouble(getDistance());

           /* System.out.printf(" %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s.\n", getDate() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[0] - getDate().length()]).replace("\0", " "), getUniqueCharacter() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[1] -
                    getUniqueCharacter().length()]).replace("\0", " "), getAirlineID(), getTailNum() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[3] -
                    getTailNum().length()]).replace("\0", " "), getFlNum() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[4] -
                    getFlNum().length()]).replace("\0", " "), getOrigin() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[5] -
                    getOrigin().length()]).replace("\0", " "), getDest() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[6] -
                    getDest().length()]).replace("\0", " "), getDepTime() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[7] -
                    getDepTime().length()]).replace("\0", " "), getDepDelay(), getTaxiOut(), getWheelsOff() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[10] -
                    getWheelsOff().length()]).replace("\0", " "), getWheelsOn() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[11] -
                    getWheelsOn().length()]).replace("\0", " "), getTaxiIn(), getArrTime() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[13] -
                    getArrTime().length()]).replace("\0", " "), getArrDelay(), getCancelled(), getCancellationCode() + new String(new char[Prog1A.MAX_STRING_FIELD_LEN[16] -
                    getCancellationCode().length()]).replace("\0", " "), getAirTime(), getDistance());
*/
        } catch (IOException e) {
            System.out.println("IO Error; couldn't write to the file.");
            e.printStackTrace();
            System.exit(27); //27 is my native TONGUE.
        }
    }
}
