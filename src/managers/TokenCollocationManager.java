package managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.ChineseUtil;

import entities.Collocation;
import entities.Poem;

class TokensInPairLines {
    List<Set<String>> tokens = null;

    TokensInPairLines() {
	tokens = new ArrayList<Set<String>>();
    }
}

public class TokenCollocationManager {
    private Map<String, List<Collocation>> tokenToCollocationsMap = null;
    private static TokenCollocationManager instance = null;

    public static TokenCollocationManager getInstance() {
	if (instance == null)
	    instance = new TokenCollocationManager();
	return instance;
    }

    private TokenCollocationManager() {

    }

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

    public void readCollocations(String collocationFile) {
	try {
	    tokenToCollocationsMap = new HashMap<String, List<Collocation>>();
	    BufferedReader br = new BufferedReader(new FileReader(new File(
		    collocationFile)));
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
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void extractTokenCollocations(String outputFile, double t_threshold) {
	List<TokensInPairLines> pairTokensList = new ArrayList<TokensInPairLines>();
	List<Map<String, Integer>> tokensCntMap = new ArrayList<Map<String, Integer>>();
	tokensCntMap.add(new HashMap<String, Integer>());
	tokensCntMap.add(new HashMap<String, Integer>());

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
		    for (String token : pairTokens.tokens.get(index)) {
			if (tokensCntMap.get(index).containsKey(token))
			    tokensCntMap.get(index).put(token,
				    tokensCntMap.get(index).get(token) + 1);
			else
			    tokensCntMap.get(index).put(token, 1);
		    }
		    if (index % 2 == 1) {
			pairTokensList.add(pairTokens);
			pairTokens = new TokensInPairLines();
		    }
		    cnt++;
		}
	    }
	    // find collocations of two tokens
	    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
		    outputFile)));
	    int N = pairTokensList.size();
	    int cnt = 0;
	    for (String token1 : tokensCntMap.get(0).keySet()) {
		System.out.println(++cnt);
		List<Set<String>> tokensIn2ndPair = new ArrayList<Set<String>>();
		for (TokensInPairLines tokens : pairTokensList) {
		    if (tokens.tokens.get(0).contains(token1))
			tokensIn2ndPair.add(tokens.tokens.get(1));
		}
		for (String token2 : tokensCntMap.get(1).keySet()) {
		    double miu = (double) tokensCntMap.get(0).get(token1)
			    * (double) tokensCntMap.get(1).get(token2)
			    / (double) (N * N);
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
