package composer;

import java.util.HashMap;
import java.util.Map;

import entities.Collocation;
import entities.Poem;

public class ComposeInformation {
    public String pattern;
    public String yunbu;
    public Map<Integer, Poem> srcs;
    public Map<Integer, Collocation> collos;

    public ComposeInformation(String p) {
	pattern = p;
	reset();
    }

    public void reset() {
	yunbu = "";
	collos = new HashMap<Integer, Collocation>();
	srcs = new HashMap<Integer, Poem>();
    }
}
