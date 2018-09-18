import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private List<Node> slots;
    private List<Long> offsets;

    /**
     * This constructor will initialize the hashmap, along with
     * initializing the child of this node to null until we are
     * obligated to split our buckets up, creating a deeper branch.
     */
    public Node(){
        slots = new ArrayList<>();
        offsets = new ArrayList<>();
        /*
            Initializes the offsets list and slots list, allowing the program
            to set them later with ease. These are default values that must be changed after
            the first split.
         */
        for(int i = 0; i < 10; i++){
            addOffset(0);
            addNode(null); //initialize list to be null.
            setOffset(i, (i* Prog2.indexLen*100));
            /*
                Now we should have a root node that has 10 children with offsets
                initialized to where they ought to point to in the hash bucket index file.
                We add 4 to the end of each offset to accommodate for the integer count of
                how many slots are filled so far...
            */
        }
    }
    /**
     * Determines if a node is a leaf node by checking whether
     * or not the list is empty.
     * @return
     */
    boolean isLeaf(int i){
        return slots.get(i) == null;
    }
    Node getNode(int n){
        return slots.get(n);
    }
    private void addNode(Node n){
        slots.add(n);
    }
    void setNode(int i, Node n){
        slots.set(i, n);
    }

    void setOffset(int i, long offset){
        offsets.set(i, offset);
    }
    long getOffset(int i){
        return offsets.get(i);
    }
    void addOffset(long n){
        offsets.add(n);
    }

}
