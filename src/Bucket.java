import java.util.ArrayList;
import java.util.HashMap;

/**
 * -----Bucket-----
 * Class Bucket is the data structure that will contain
 * 100 index records each for every bucket that gets filled
 * in the hash bucket file.
 */
public class Bucket {
    private static HashMap<String, Long> slots;

    public Bucket()
    {
        slots = new HashMap<>();
    }

    public boolean addSlot(String arrTime, long offset){

        if(slots.size() <= 99)
        {
            slots.put(arrTime, offset);
            return true;
        }
        return false;
    }


}
