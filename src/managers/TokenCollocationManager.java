package managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.ChineseUtil;

import entities.Collocation;
import entities.Poem;

/**
 * sets of tokens in two adjacent lines in poems(e.g. the 1st and 2nd line, the
 * 3rd and 4th line)
 * 
 * @author wei.he
 * 
 */
class TokensInPairLines {
    /**
     * sets of tokens, the list size should be 2
     */
    List<Set<String>> tokens = null;

    /**
     * constructor
     */
    TokensInPairLines() {
	tokens = new ArrayList<Set<String>>();
    }
}

/**
 * the token collocation manager
 * 
 * @author wei.he
 * 
 */
public class TokenCollocationManager {
    /**
     * a map which maps a token and associated collocation pairs
     */
    private Map<String, List<Collocation>> tokenToCollocationsMap = null;
    /**
     * manager instance
     */
    private static TokenCollocationManager instance = null;

    /**
     * get manager instance
     * 
     * @return instance
     */
    public static TokenCollocationManager getInstance() {
	if (instance == null)
	    instance = new TokenCollocationManager();
	return instance;
    }

    /**
     * private constructor
     */
    private TokenCollocationManager() {

    }

    /**
     * get the t-value for a collocation pair
     * 
     * @param token1
     *            the first token in pair
     * @param token2
     *            the second token in pair
     * @return t-value, -1 if no such collocation pair found
     */
    public double getTokenCollocation(String token1, String token2) {
	if (tokenToCollocationsMap.containsKey(token1)) {
	    for (Collocation collo : tokenToCollocationsMap.get(token1)) {
		if (collo.pairToken.equals(token2))
		    return collo.t_value;
	    }
	    return -1;
	} else
	    return -1;
    }

    /**
     * read the collocation file, and load into memory
     * 
     * @param collocationFile
     *            collocation file
     */
    public void readCollocations(String collocationFile) {
	try {
	    tokenToCollocationsMap = new HashMap<String, List<Collocation>>();
	    BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(collocationFile)), "GBK"));
	    String line;
	    while ((line = br.readLine()) != null) {
		line = line.trim();
		if (line.isEmpty())
		    continue;

		String[] parts = line.split("\\s+");
		assert (parts.length == 3);
		String tok1 = parts[0], tok2 = parts[1];
		double t = Double.parseDouble(parts[2]);
		if (!tokenToCollocationsMap.containsKey(tok1))
		    tokenToCollocationsMap.put(tok1,
			    new ArrayList<Collocation>());
		tokenToCollocationsMap.get(tok1).add(
			new Collocation(tok1, tok2, t));
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * extract token collocations from poems by t-tests NOTE: this method should
     * be called after poem manger load all poems into memory!
     * 
     * @param outputFile
     *            the collocation result
     * @param t_threshold
     *            a threshold, above which results will be recorded
     */
    public void extractTokenCollocations(String outputFile, double t_threshold) {
	List<TokensInPairLines> pairTokensList = new ArrayList<TokensInPairLines>();
	// the first in array stores the token-count map for the 1st, 3rd, (5th,
	// 7th) line in poem
	// the second in array stores the token-count map for the 2nd, 4th,
	// (6th, 8th) line in poem
	List<Map<String, Integer>> tokensCntMapList = new ArrayList<Map<String, Integer>>();
	tokensCntMapList.add(new HashMap<String, Integer>());
	tokensCntMapList.add(new HashMap<String, Integer>());

	try {
	    assert (PoemManager.getInstance().poems != null && PoemManager
		    .getInstance().poems.size() > 0);
	    for (Poem poem : PoemManager.getInstance().poems) {
		int cnt = 0;
		TokensInPairLines pairTokens = new TokensInPairLines();
		for (String line : poem.content) {
		    int index = cnt % 2;
		    assert (pairTokens.tokens.size() == index);
		    pairTokens.tokens.add(new HashSet<String>());
		    pairTokens.tokens.get(index).addAll(
			    ChineseUtil.Tokenize(line));
		    // update the token-cnt map
		    for (String token : pairTokens.tokens.get(index)) {
			if (tokensCntMapList.get(index).containsKey(token))
			    tokensCntMapList.get(index).put(token,
				    tokensCntMapList.get(index).get(token) + 1);
			else
			    tokensCntMapList.get(index).put(token, 1);
		    }
		    if (index % 2 == 1) {
			pairTokensList.add(pairTokens);
			pairTokens = new TokensInPairLines();
		    }
		    cnt++;
		}
	    }
	    // find collocations of two tokens by t-test
	    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(outputFile)), "GBK"));
	    // the sample number
	    int N = pairTokensList.size();
	    for (String token1 : tokensCntMapList.get(0).keySet()) {
		List<Set<String>> tokensIn2ndPair = new ArrayList<Set<String>>();
		for (TokensInPairLines tokens : pairTokensList) {
		    if (tokens.tokens.get(0).contains(token1))
			tokensIn2ndPair.add(tokens.tokens.get(1));
		}
		for (String token2 : tokensCntMapList.get(1).keySet()) {
		    // null hypothesis is that token1 & token2 are independent
		    // if null hypothesis is true,
		    // p<token1,token2>=p(token1)*p(token2)
		    double miu = (double) tokensCntMapList.get(0).get(token1)
			    * (double) tokensCntMapList.get(1).get(token2)
			    / ((double) N * (double) N);
		    // calculate the actual occurrence count of pair<token1,
		    // token2>
		    int coOccurenceCnt = 0;
		    for (Set<String> tokens : tokensIn2ndPair) {
			if (tokens.contains(token2)) {
			    coOccurenceCnt++;
			}
		    }
		    double sampleMean = (double) coOccurenceCnt / (double) N;
		    if (sampleMean > 0) {
			double t = (sampleMean - miu)
				/ Math.sqrt(sampleMean / N);
			if (t > t_threshold) {
			    System.out.println(token1 + " " + token2 + " " + t
				    + "\n");
			    bw.write(token1 + " " + token2 + " " + t + "\n");
			}
		    }
		}
	    }
	    bw.close();
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }
}
