package composer;

import entities.Poem;

public class LineInPoem {
    public String line;
    public Poem sourcePoem;

    public LineInPoem(String l, Poem p) {
	line = l;
	sourcePoem = p;
    }
}