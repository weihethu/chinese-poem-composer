package composer;

import entities.Poem;

/**
 * a line and the poem which it belongs to
 * @author wei.he
 *
 */
public class LineInPoem {
    /**
     * the line
     */
    public String line;
    /**
     * the source poem
     */
    public Poem sourcePoem;
    
    /**
     * constructor
     * @param l line
     * @param p poem
     */
    public LineInPoem(String l, Poem p) {
	line = l;
	sourcePoem = p;
    }
}