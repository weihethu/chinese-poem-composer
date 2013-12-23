package managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import topicModelSolvers.plsiSolver;
import topicModelSolvers.solverInterface;
import utils.ChineseUtil;

public class TopicModelManager {
    private static TopicModelManager instance = null;
    private solverInterface topicModelSolver = null;
    private Map<String, double[]> termTopicProbsMap = null;
    private int nTopic = -1;

    public static TopicModelManager getInstance() {
	if (instance == null)
	    instance = new TopicModelManager();

	return instance;
    }

    private TopicModelManager() {
    }

    public void train(int nTopic, String trainResultFile) {
	topicModelSolver = new plsiSolver(nTopic, true);
	topicModelSolver.readInput();
	topicModelSolver.solve();
	topicModelSolver.printResults(trainResultFile);
    }

    public void readTermTopicProbs(String trainResultFile) {
	nTopic = -1;
	termTopicProbsMap = new HashMap<String, double[]>();
	try {
	    BufferedReader br = new BufferedReader(new FileReader(new File(
		    trainResultFile)));
	    String line;
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
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public double[] getTopicProbVector(String line) {
	assert (termTopicProbsMap != null && !termTopicProbsMap.isEmpty());
	Set<String> terms = ChineseUtil.Tokenize(line);

	double probsSum[] = new double[nTopic];
	Arrays.fill(probsSum, 0);
	for (String term : terms) {
	    assert (termTopicProbsMap.containsKey(term));
	    double probs[] = termTopicProbsMap.get(term);
	    for (int i = 0; i < nTopic; i++) {
		probsSum[i] += probs[i];
	    }
	}
	for (int i = 0; i < nTopic; i++)
	    probsSum[i] /= terms.size();
	return probsSum;
    }

    public double[] getTopicProbVector(String[] lines) {
	List<String> linesList = new ArrayList<String>();
	for (String line : lines)
	    linesList.add(line);
	return getTopicProbVector(linesList);
    }

    public double[] getTopicProbVector(List<String> lines) {
	double probsSum[] = new double[nTopic];
	Arrays.fill(probsSum, 0);
	for (String line : lines) {
	    double probs[] = getTopicProbVector(line);
	    for (int i = 0; i < nTopic; i++) {
		probsSum[i] += probs[i];
	    }
	}
	return probsSum;
    }

    public static double calculateSimilarity(double[] v1, double[] v2) {
	double tmp = 0, tmp1 = 0, tmp2 = 0;
	for (int i = 0; i < v1.length; i++) {
	    tmp += v1[i] * v2[i];
	    tmp1 += v1[i] * v1[i];
	    tmp2 += v2[i] * v2[i];
	}

	assert (tmp1 * tmp2 != 0);
	return tmp / Math.sqrt(tmp1 * tmp2);
    }

    public double calculateTopicConcentrationScore(double[] distributions,
	    int concentration_topic_num) {
	assert (distributions.length == nTopic);
	List<Double> tmpList = new ArrayList<Double>();
	double sum = 0;
	for (double val : distributions) {
	    sum += val;
	    tmpList.add(val);
	}
	Collections.sort(tmpList);

	double sum1 = 0;
	for (int i = tmpList.size() - 1; i >= tmpList.size()
		- concentration_topic_num; i--) {
	    sum1 += tmpList.get(i);
	}
	return sum1 / (sum * concentration_topic_num);
    }
}
