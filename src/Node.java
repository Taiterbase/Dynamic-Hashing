import java.util.HashMap;

/**
 * Class node is the data structure spine for
 * our bucket structure inside of our hash bucket file.
 *
 * This class should contain 10 pointers, each pointer is a
 * reference to a bucket inside of the binary file, so probably
 * an offset of some sort that we can seek to in order to find
 * the proper bucket that we are referencing.
 * This class has methods for getting its multiple pointers...
 */
public class Node {
    private HashMap<Integer, Long> pointers; //Pointers are offsets to the hash bucket binary file... nuts.
    private Node child;
    /**
     * This constructor will initialize the hashmap, along with
     * initializing the child of this node to null until we are
     * obligated to split our buckets up, creating a deeper branch.
     */
    public Node(){
        pointers = new HashMap<>();
        child = null;
    }

    public void createChild(){

    }



}
