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

/**
 * the topic model manager
 * 
 * @author wei.he
 * 
 */
public class TopicModelManager {
    /**
     * manager instance
     */
    private static TopicModelManager instance = null;
    /**
     * topic model solver, can be plsi or lda
     */
    private solverInterface topicModelSolver = null;
    /**
     * the term-topic probability
     */
    private Map<String, double[]> termTopicProbsMap = null;
    /**
     * the topic number
     */
    private int nTopic = -1;

    /**
     * get manager instance
     * 
     * @return instance
     */
    public static TopicModelManager getInstance() {
	if (instance == null)
	    instance = new TopicModelManager();

	return instance;
    }

    /**
     * private constructor
     */
    private TopicModelManager() {
    }

    /**
     * train topic models
     * 
     * @param nTopic
     *            topic number
     * @param trainResultFile
     *            train result file
     */
    public void train(int nTopic, String trainResultFile) {
	topicModelSolver = new plsiSolver(nTopic, true);
	topicModelSolver.readInput();
	topicModelSolver.solve();
	topicModelSolver.printResults(trainResultFile);
    }

    /**
     * read train result file and construct term-topic probability matrix
     * 
     * @param trainResultFile
     */
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
	    e.printStackTrace();
	}
    }

    /**
     * get topic distributions of a given line by calculating the sum of each
     * tokens
     * 
     * @param line
     *            line
     * @return an array indication topic distributions
     */
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

    /**
     * get topic distributions of multiple lines
     * 
     * @param lines
     *            lines in array
     * @return topic distributions
     */
    public double[] getTopicProbVector(String[] lines) {
	List<String> linesList = new ArrayList<String>();
	for (String line : lines)
	    linesList.add(line);
	return getTopicProbVector(linesList);
    }

    /**
     * get topic distributions of multiple lines
     * 
     * @param lines
     *            lines in list
     * @return topic distributions
     */
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

    /**
     * get similarity of two distributions
     * 
     * @param v1
     *            first distribution
     * @param v2
     *            second distribution
     * @return similarity
     */
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

    /**
     * calculate the concentration level of topic distributions by measuring the
     * proportion of the probability masses in its top K peak topics
     * 
     * @param distributions
     *            topic distributions
     * @param K
     *            the number of peak topics measured
     * @return a score which indicates the concentration level of top K peak
     *         topics, 1/nTopic is the minimum in the case of even distributions
     *         among topics, meaning no concentration at all, 1/K is the maximum
     *         in the case the top K peak topics occupy all the probability
     *         masses
     */
    public double calculateTopicConcentrationScore(double[] distributions, int K) {
	assert (distributions.length == nTopic);
	List<Double> tmpList = new ArrayList<Double>();
	double sum = 0;
	for (double val : distributions) {
	    sum += val;
	    tmpList.add(val);
	}
	Collections.sort(tmpList);

	double sum1 = 0;
	for (int i = tmpList.size() - 1; i >= tmpList.size() - K; i--) {
	    sum1 += tmpList.get(i);
	}
	return sum1 / (sum * K);
    }
}
