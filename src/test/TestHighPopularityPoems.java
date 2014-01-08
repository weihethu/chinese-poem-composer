package test;

import java.util.Collections;
import java.util.Comparator;

import entities.Poem;

import managers.PatternManager;
import managers.PoemManager;
import managers.TonalManager;

public class TestHighPopularityPoems {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// getPoemPopularity();
	extractPoemsWithHighPopularity();
    }

    private static void extractPoemsWithHighPopularity() {
	TonalManager.getInstance().readPingshuiyun("pingshuiyun.txt");
	PatternManager.getInstance().readPattern("pattern.txt");
	PoemManager.getInstance().readPreprocessed("poems.txt");
	Collections.sort(PoemManager.getInstance().poems,
		new Comparator<Poem>() {

		    @Override
		    public int compare(Poem poem1, Poem poem2) {
			return Long.valueOf(poem2.popularity).compareTo(
				Long.valueOf(poem1.popularity));
		    }

		});
	for (int rank = 0; rank < 50; rank++) {
	    Poem poem = PoemManager.getInstance().poems.get(rank);
	    System.out.println("Rank #" + (rank + 1) + " Popularity:"
		    + poem.popularity);
	    System.out.println(poem.title + " " + poem.author);
	    for (String line : poem.content) {
		System.out.println(line);
	    }
	}
    }

    /**
     * this function tries to convert the old format(with no poem popularity) to
     * new format(with poem popularity)
     */
    private static void getPoemPopularity() {
	TonalManager.getInstance().readPingshuiyun("pingshuiyun.txt");
	PatternManager.getInstance().readPattern("pattern.txt");
	PoemManager.getInstance().readPreprocessed("poems.txt");
	PoemManager.getInstance().getPoemPopularities(
		"poems.txt");
    }
}
