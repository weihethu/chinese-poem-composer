package composer;

import java.util.HashMap;
import java.util.Map;

import entities.Collocation;
import entities.Poem;

/**
 * the information related a poem composition
 * 
 * @author wei.he
 * 
 */
public class ComposeInformation {
    /**
     * the yunbu selected
     */
    public String yunbu;
    /**
     * the sources of each line
     */
    public Map<Integer, Poem> srcs;
    /**
     * the collocation pair of each two lines
     */
    public Map<Integer, Collocation> collos;

    /**
     * constructor
     */
    public ComposeInformation() {
	reset();
    }

    /**
     * reset the compose information
     */
    public void reset() {
	yunbu = "";
	collos = new HashMap<Integer, Collocation>();
	srcs = new HashMap<Integer, Poem>();
    }
}
