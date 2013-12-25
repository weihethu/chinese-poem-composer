package topicModelSolvers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import utils.ChineseUtil;

import entities.Poem;

import managers.PoemManager;

/**
 * class that represents a term and probability associated with it
 * 
 * @author wei.he
 * 
 */
class WeightedWord {
    /**
     * term
     */
    String word;
    /**
     * probability
     */
    double w;

    /**
     * constructor
     * 
     * @param _word
     *            term
     * @param _weight
     *            probability
     */
    WeightedWord(String _word, double _weight) {
	word = _word;
	w = _weight;
    }
}

/**
 * solver interface, extended by plsiSolver and ldaGibsSolver
 * 
 * @author wei.he
 * 
 */
public abstract class solverInterface {
    /**
	 * 
	 */
    protected int nDocument;
    protected int nTerm;
    protected int nTopic;

    /**
     * a list that saves term-frequencies in each line
     */
    protected List<Map<String, Integer>> tfsList;
    /**
     * a list that saves terms in each line
     */
    protected List<List<String>> termsInDocumentList;
    /**
     * a map that maps a term to its unique id
     */
    protected Map<String, Integer> termToIndexMap;
    /**
     * whether output detailed information
     */
    protected boolean detailedOutput = false;

    /**
     * constructor
     * 
     * @param topicNum
     *            number of topics
     * @param detailed
     *            whether output detailed information
     */
    public solverInterface(int topicNum, boolean detailed) {
	nTopic = topicNum;
	detailedOutput = detailed;
    }

    /**
     * process input
     */
    public void readInput() {
	assert (PoemManager.getInstance().poems != null && PoemManager
		.getInstance().poems.size() > 0);
	try {
	    Set<String> dict = new HashSet<String>();
	    termsInDocumentList = new ArrayList<List<String>>();
	    termToIndexMap = new HashMap<String, Integer>();
	    tfsList = new ArrayList<Map<String, Integer>>();

	    for (Poem poem : PoemManager.getInstance().poems) {
		List<String> termsInDoc = new ArrayList<String>();
		Map<String, Integer> tf = new HashMap<String, Integer>();
		for (String line : poem.content) {
		    Set<String> terms = ChineseUtil.Tokenize(line);

		    for (String term : terms) {
			termsInDoc.add(term);
			if (tf.containsKey(term))
			    tf.put(term, tf.get(term) + 1);
			else
			    tf.put(term, 1);
		    }
		}

		tfsList.add(tf);
		termsInDocumentList.add(termsInDoc);
		dict.addAll(tf.keySet());
	    }

	    Iterator<String> dict_iter = dict.iterator();
	    int index = 0;
	    while (dict_iter.hasNext()) {
		termToIndexMap.put(dict_iter.next(), index++);
	    }

	    nDocument = termsInDocumentList.size();
	    nTerm = dict.size();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * print results: each topic and top 10 words relative to it
     */
    public void printResults(String resultFile) {

	for (int i = 0; i < nTopic; i++) {
	    System.out.println("Topic #" + (i + 1));
	    List<WeightedWord> wordsList = new ArrayList<WeightedWord>();
	    Iterator<String> iter = termToIndexMap.keySet().iterator();
	    while (iter.hasNext()) {
		String term = iter.next();
		int termIndex = termToIndexMap.get(term);
		wordsList.add(new WeightedWord(term, getTermProbInTopic(i,
			termIndex)));
	    }
	    // sort words by their relevance to a topic
	    Collections.sort(wordsList, new Comparator<WeightedWord>() {

		@Override
		public int compare(WeightedWord arg0, WeightedWord arg1) {
		    return Double.compare(arg1.w, arg0.w);
		}

	    });
	    for (int j = 0; j <= 10; j++)
		System.out.println(wordsList.get(j).word + ":"
			+ wordsList.get(j).w);
	}

	try {
	    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
		    new FileOutputStream(new File(resultFile)), "GBK"));
	    for (String term : termToIndexMap.keySet()) {
		String line = term;
		int termIndex = termToIndexMap.get(term);
		for (int i = 0; i < nTopic; i++) {
		    line += " " + getTermProbInTopic(i, termIndex);
		}
		bw.write(line + "\n");
	    }
	    bw.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * solve, where actual work is done
     * 
     * @return run result status
     */
    public abstract void solve();

    /**
     * get a term's probability within a given topic
     * 
     * @param topicIndex
     *            topic index
     * @param termIndex
     *            term index
     * @return probability
     */
    protected abstract double getTermProbInTopic(int topicIndex, int termIndex);
}
