package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * term and its associated probability in topic
 * @author wei.he
 *
 */
class TermWithProb {
	String term;
	double prob;

	TermWithProb(String t, double p) {
		term = t;
		prob = p;
	}
}

/**
 * extract the high probability tokens in topics
 * @author wei.he
 *
 */
public class TestTopicModel {

	private static Map<String, double[]> termTopicProbsMap = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			termTopicProbsMap = new HashMap<String, double[]>();
			BufferedReader br = new BufferedReader(new FileReader(new File("topic1.txt")));
			String line;
			int nTopic = -1;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				String parts[] = line.split("\\s+");
				double probs[] = new double[parts.length - 1];
				if (nTopic < 0)
					nTopic = probs.length;
				else
					assert (nTopic == probs.length);
				for (int i = 0; i < probs.length; i++) {
					probs[i] = Double.parseDouble(parts[i + 1]);
				}
				assert (!termTopicProbsMap.containsKey(parts[0]));
				termTopicProbsMap.put(parts[0], probs);
			}
			br.close();

			List<TermWithProb> terms = new ArrayList<TermWithProb>();
			for (int i = 0; i < nTopic; i++) {
				terms.clear();

				for (String term : termTopicProbsMap.keySet()) {
					terms.add(new TermWithProb(term, termTopicProbsMap.get(term)[i]));
				}

				Collections.sort(terms, new Comparator<TermWithProb>() {

					@Override
					public int compare(TermWithProb t1, TermWithProb t2) {
						return Double.compare(t2.prob, t1.prob);
					}

				});
				System.out.print("Topic #" + (i + 1) + ":");
				for (int j = 0; j < 20; j++) {
					System.out.print(" " + terms.get(j).term);
				}
				System.out.println();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
