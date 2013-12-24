package composer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import managers.PoemManager;
import managers.TokenCollocationManager;
import managers.TonalManager;
import managers.TopicModelManager;

import utils.ChineseUtil;

import entities.Collocation;
import entities.PatternInformation;
import entities.Poem;

/**
 * Poem composer
 * 
 * @author wei.he
 * 
 */
public class PoemComposer {

    /**
     * the central topics number
     */
    private static int CENTER_TOPIC_CNT = 2;
    /**
     * the maximum iteration number when performing incremental update
     */
    private static int MAX_ITERATION_CNT = 10;
    /**
     * lamda, used to balance the topic concentration score and collocation
     * score
     */
    private static double lamda = 15;

    /**
     * constructor
     */
    public PoemComposer() {

    }

    /**
     * check whether the new line meet certain constraints given existing lines
     * 
     * @param composed
     *            existing lines
     * @param line
     *            new line
     * @return whether the constraint is meet
     */
    private static boolean checkConstraints(List<String> composed, String line) {
	// no two lines should be the same
	if (composed.contains(line))
	    return false;

	// no two lines should begin with the same character
	for (String existing : composed) {
	    if (line.charAt(0) == existing.charAt(0))
		return false;
	}
	return true;
    }

    /**
     * check whether the new line meet certain constraints when replacing a
     * given row of an existing poem
     * 
     * @param oldPoem
     *            poem
     * @param line
     *            new line
     * @param rowIndex
     *            the row position to be replaced
     * @return whether the constraint is meet
     */
    private static boolean checkConstraints(Poem oldPoem, String line,
	    int rowIndex) {
	// no two lines should be the same
	for (String oldLine : oldPoem.content)
	    if (oldLine.equals(line))
		return false;
	// no two lines should begin with the same character
	for (int i = 0; i < oldPoem.row; i++) {
	    if (i != rowIndex && oldPoem.content[i].charAt(0) == line.charAt(0))
		return false;
	}
	return true;
    }

    /**
     * get the maximum occurrence count of a token allowed in a poem
     * 
     * @param poemRowCnt
     *            the row count of a poem
     * @return maximum occurrence count
     */
    private static int getDominantTokenThreshold(int poemRowCnt) {
	return (poemRowCnt == 8) ? 3 : 2;
    }

    /**
     * check whether the pingze given conforms with the pinze required
     * 
     * @param pingzeRequired
     *            pingze required
     * @param pingzeAcutal
     *            pingze given
     * @return
     */
    private static boolean conformPingze(int pingzeRequired[],
	    int pingzeAcutal[]) {
	for (int i = 0; i < pingzeAcutal.length; i++) {
	    if (pingzeRequired[i] + pingzeAcutal[i] == 3) {
		return false;
	    }
	}
	return true;
    }

    /**
     * find the best replacements to a given position of a poem
     * 
     * @param patternInfo
     *            the pattern requirement associated with the poem
     * @param poem
     *            existing poem
     * @param lineIndex
     *            the row position to replace
     * @param composeInfo
     *            compose information
     * @return a sorted list of candidates
     */
    public static List<GradedCandidate> findBestReplacements(
	    PatternInformation patternInfo, Poem poem, int lineIndex,
	    ComposeInformation composeInfo) {
	if (lineIndex < 0 || lineIndex >= poem.row)
	    return null;
	double[] topicDistributions = TopicModelManager.getInstance()
		.getTopicProbVector(poem.content);
	double collocationTSum = calculateColloTSum(poem.row, composeInfo);
	Map<String, Integer> tokensCntMap = calculateTokenCnts(poem.content);

	List<GradedCandidate> candidates = findReplacements(patternInfo, poem,
		lineIndex, composeInfo, topicDistributions, tokensCntMap,
		collocationTSum);

	Collections.sort(candidates);
	// the dataset contains duplicate poems, so there maybe duplicate
	// candidates, we will remove them
	String last = "";
	for (int i = 0; i < candidates.size(); i++) {
	    if (!candidates.get(i).candidate.line.equals(last))
		last = candidates.get(i).candidate.line;
	    else
		candidates.remove(i--);
	}
	return candidates;
    }

    /**
     * find the replacements to a given position of a poem
     * 
     * @param patternInfo
     *            the pattern requirement associated with the poem
     * @param oldPoem
     *            existing poem
     * @param lineIndex
     *            the row position to replace
     * @param composeInfo
     *            compose information
     * @param topicDistributions
     *            topicDistributions of the existing poem
     * @param tokensCntMap
     *            token-count map of the existing poem
     * @param collocationTSum
     *            the sum of t-values of collocation pairs in existing poem
     * @return an unsorted list of candidates
     */
    private static List<GradedCandidate> findReplacements(
	    PatternInformation patternInfo, Poem oldPoem, int lineIndex,
	    ComposeInformation composeInfo, double[] topicDistributions,
	    Map<String, Integer> tokensCntMap, double collocationTSum) {
	List<GradedCandidate> candidates = new ArrayList<GradedCandidate>();
	int dominantTokenThreshold = getDominantTokenThreshold(patternInfo.row);
	// calculate the topic distributions of the other rows
	double[] lineTopicDistributions = TopicModelManager.getInstance()
		.getTopicProbVector(oldPoem.content[lineIndex]);
	double[] remainTopicDistributions = new double[topicDistributions.length];
	for (int j = 0; j < topicDistributions.length; j++)
	    remainTopicDistributions[j] = topicDistributions[j]
		    - lineTopicDistributions[j];

	// calculate the token-count map of the other rows
	Set<String> oldTokens = ChineseUtil
		.Tokenize(oldPoem.content[lineIndex]);
	for (String token : oldTokens)
	    tokensCntMap.put(token, tokensCntMap.get(token) - 1);

	// iterate all poems to find candidates
	for (Poem poem : PoemManager.getInstance().poems) {
	    if (poem.col != patternInfo.col)
		continue;
	    // iterate each row
	    for (int i = 0; i < poem.row; i++) {
		// potential new line
		String newLine = poem.content[i];
		if (!checkConstraints(oldPoem, newLine, lineIndex)
			|| !conformPingze(patternInfo.tonals[lineIndex],
				poem.pingzeTable[i]))
		    continue;
		// check conform yunjiao
		if (patternInfo.ruyun[lineIndex]
			&& !TonalManager.getInstance()
				.getYunbuInfo(newLine.charAt(poem.col - 1))
				.contains(composeInfo.yunbu))
		    continue;
		// check no duplicate yunjiaos
		boolean duplicateYunjiao = false;
		if (patternInfo.ruyun[lineIndex]) {
		    for (int k = 0; k < oldPoem.row; k++) {
			if (k == lineIndex)
			    continue;
			if (patternInfo.ruyun[k]
				&& (oldPoem.content[k].charAt(oldPoem.col - 1) == newLine
					.charAt(oldPoem.col - 1))) {
			    duplicateYunjiao = true;
			    break;
			}
		    }
		}
		if (duplicateYunjiao)
		    continue;
		// check no dominant tokens
		Set<String> newTokens = ChineseUtil.Tokenize(newLine);
		boolean dominantToken = false;
		for (String token : tokensCntMap.keySet()) {
		    if (tokensCntMap.get(token) >= dominantTokenThreshold - 1
			    && newTokens.contains(token)) {
			dominantToken = true;
			break;
		    }
		}
		if (dominantToken)
		    continue;
		// check there exist obvious collocation pairs
		Set<Collocation> existingCollos = new HashSet<Collocation>();
		for (int k = 0; k < oldPoem.row / 2; k++) {
		    if (k != lineIndex % 2)
			existingCollos.add(composeInfo.collos.get(k));
		}
		Collocation maxCollo;
		if (lineIndex % 2 == 0) {
		    maxCollo = findMaxCollocationPair(newLine,
			    oldPoem.content[lineIndex + 1], existingCollos);
		} else {
		    maxCollo = findMaxCollocationPair(
			    oldPoem.content[lineIndex - 1], newLine,
			    existingCollos);
		}
		if (maxCollo.t_value < 0)
		    continue;
		// calculate new topic distributions
		double[] newLineTopicDistributions = TopicModelManager
			.getInstance().getTopicProbVector(newLine);
		double[] newDistributions = new double[topicDistributions.length];
		for (int k = 0; k < newDistributions.length; k++)
		    newDistributions[k] = remainTopicDistributions[k]
			    + newLineTopicDistributions[k];
		double newTopicConcentrationScore = TopicModelManager
			.getInstance().calculateTopicConcentrationScore(
				newDistributions, CENTER_TOPIC_CNT);
		// calculate sum of t-values of new collocation pairs
		double newCollocationTSum = collocationTSum;
		newCollocationTSum -= composeInfo.collos.get(lineIndex / 2).t_value;
		newCollocationTSum += maxCollo.t_value;
		double newCollocationScore = newCollocationTSum
			/ (oldPoem.row / 2);
		// calculate new score
		double newScore = lamda * newTopicConcentrationScore
			+ newCollocationScore;
		candidates.add(new GradedCandidate(
			new LineInPoem(newLine, poem), newScore, maxCollo));
	    }
	}
	return candidates;
    }

    /**
     * calculate the sum of t-values of collocation pairs
     * 
     * @param rowCnt
     *            the row number
     * @param composeInfo
     *            the compose information
     * @return sum
     */
    private static double calculateColloTSum(int rowCnt,
	    ComposeInformation composeInfo) {
	double collocationTSum = 0;
	for (int i = 0; i < rowCnt / 2; i++)
	    collocationTSum += composeInfo.collos.get(i).t_value;
	return collocationTSum;
    }

    /**
     * calculate the token-count map of given lines
     * 
     * @param lines
     *            lines
     * @return token-count map
     */
    private static Map<String, Integer> calculateTokenCnts(String[] lines) {
	Map<String, Integer> tokensCntMap = new HashMap<String, Integer>();
	for (int i = 0; i < lines.length; i++) {
	    Set<String> tokens = ChineseUtil.Tokenize(lines[i]);
	    for (String token : tokens) {
		if (tokensCntMap.containsKey(token))
		    tokensCntMap.put(token, tokensCntMap.get(token) + 1);
		else
		    tokensCntMap.put(token, 1);
	    }
	}
	return tokensCntMap;
    }

    /**
     * tune a poem in an incremental-update manner
     * 
     * @param patternInfo
     *            the pattern required associated with the poem
     * @param initialPoem
     *            initial poem
     * @param composeInfo
     *            compose information
     */
    private static void tunePoem(PatternInformation patternInfo,
	    Poem initialPoem, ComposeInformation composeInfo) {
	// calculate old topic distributions
	double[] topicDistributions = TopicModelManager.getInstance()
		.getTopicProbVector(initialPoem.content);
	double oldTopicConcentrationScore = TopicModelManager.getInstance()
		.calculateTopicConcentrationScore(topicDistributions,
			CENTER_TOPIC_CNT);
	// calculate old t-values sum of collocation pairs
	double collocationTSum = calculateColloTSum(initialPoem.row,
		composeInfo);
	double oldCollocationScore = collocationTSum / (initialPoem.row / 2);
	// calculate old score
	double oldScore = lamda * oldTopicConcentrationScore
		+ oldCollocationScore;
	// System.out.println("Old:" + oldScore + "(" +
	// oldTopicConcentrationScore
	// + "," + oldCollocationScore + ")");
	Map<String, Integer> tokensCntMap = calculateTokenCnts(initialPoem.content);
	// whether there's update in the last iteration
	boolean changeInLastIter = true;
	// iterate at most fixed times
	for (int iteration_cnt = 0; iteration_cnt < MAX_ITERATION_CNT
		&& changeInLastIter; iteration_cnt++) {
	    System.out.println(iteration_cnt);
	    // in each iteration, try to replace each row with a better one
	    changeInLastIter = false;
	    for (int i = 0; i < initialPoem.row; i++) {
		GradedCandidate bestGradedCandidate = null;
		List<GradedCandidate> candidates = findReplacements(
			patternInfo, initialPoem, i, composeInfo,
			topicDistributions, tokensCntMap, collocationTSum);
		// find the best candidate, there's no need to sort, we just
		// need to iterate each element
		for (GradedCandidate candidate : candidates) {
		    if (bestGradedCandidate == null
			    || candidate.grade > bestGradedCandidate.grade) {
			bestGradedCandidate = candidate;
		    }
		}
		// replace the old line with the new line
		if (bestGradedCandidate != null
			&& bestGradedCandidate.grade > oldScore) {
		    // System.out.println(bestGradedCandidate.grade + "->"
		    // + oldScore);
		    changeInLastIter = true;
		    oldScore = bestGradedCandidate.grade;
		    // update the poem and compose information
		    initialPoem.content[i] = bestGradedCandidate.candidate.line;
		    composeInfo.srcs.put(i,
			    bestGradedCandidate.candidate.sourcePoem);
		    // update the t-values sum of collocation pairs
		    collocationTSum += (bestGradedCandidate.collo.t_value - composeInfo.collos
			    .get(i / 2).t_value);
		    composeInfo.collos.put(i / 2, bestGradedCandidate.collo);

		    // update the topic distributions
		    double[] oldLineTopicDistributions = TopicModelManager
			    .getInstance().getTopicProbVector(
				    initialPoem.content[i]);
		    double[] newLineTopicDistributions = TopicModelManager
			    .getInstance().getTopicProbVector(
				    bestGradedCandidate.candidate.line);
		    for (int j = 0; j < topicDistributions.length; j++) {
			topicDistributions[j] += (newLineTopicDistributions[j] - oldLineTopicDistributions[j]);
		    }

		    // update the token-count map
		    Set<String> replacedLineTokens = ChineseUtil
			    .Tokenize(bestGradedCandidate.candidate.line);
		    for (String token : replacedLineTokens) {
			if (tokensCntMap.containsKey(token))
			    tokensCntMap
				    .put(token, tokensCntMap.get(token) + 1);
			else
			    tokensCntMap.put(token, 1);
		    }
		}
	    }
	}
    }

    /**
     * get an initial poem in a greedy manner
     * 
     * @param patternInfo
     *            the pattern requirement associated with the poem
     * @param composeInfo
     *            the compose information
     * @return a poem, if failed to generate, return null
     */
    private static Poem getInitialPoem(PatternInformation patternInfo,
	    ComposeInformation composeInfo) {

	int dominantTokenThreshold = getDominantTokenThreshold(patternInfo.row);
	Map<String, Integer> tokensCntMap = new HashMap<String, Integer>();
	composeInfo.reset();
	// the lines added
	List<String> composedContent = new ArrayList<String>();
	// the yunbu chosen
	String yunbu = "";
	// the yunjiaos selected
	Set<Character> yunjiaos = new HashSet<Character>();
	// try to generate each line
	for (int i = 0; i < patternInfo.row; i++) {
	    // candidates
	    List<LineInPoem> candidates = new ArrayList<LineInPoem>();
	    // iterate all poems to find candidates
	    for (Poem poem : PoemManager.getInstance().poems) {
		if (poem.col != patternInfo.col)
		    continue;
		// iterate each line
		for (int j = 0; j < poem.row; j++) {
		    // check conform pingze and other constraints
		    if (!conformPingze(patternInfo.tonals[i],
			    poem.pingzeTable[j])
			    || !checkConstraints(composedContent,
				    poem.content[j]))
			continue;
		    // check conform yunjiao
		    if (patternInfo.ruyun[i]) {
			// if yunbu is not determined, we must make sure that
			// last character have non-empty yunbus
			if (yunbu.isEmpty()) {
			    if (TonalManager
				    .getInstance()
				    .getYunbuInfo(
					    poem.content[j]
						    .charAt(poem.col - 1))
				    .isEmpty())
				continue;
			} else {
			    // if yunbu is determined, we must make sure that
			    // last character conform yunbu
			    Set<String> yunbus = TonalManager.getInstance()
				    .getYunbuInfo(
					    poem.content[j]
						    .charAt(poem.col - 1));
			    if (!yunbus.contains(yunbu)
				    || yunjiaos.contains(poem.content[j]
					    .charAt(poem.col - 1)))
				continue;
			}
		    }
		    // check no dominant tokens
		    boolean dominantToken = false;
		    Set<String> tokens = ChineseUtil.Tokenize(poem.content[j]);
		    for (String token : tokensCntMap.keySet()) {
			if (tokensCntMap.get(token) >= dominantTokenThreshold - 1
				&& tokens.contains(token)) {
			    dominantToken = true;
			    break;
			}
		    }
		    if (dominantToken)
			continue;
		    candidates.add(new LineInPoem(poem.content[j], poem));
		}
	    }
	    if (candidates.isEmpty())
		return null;
	    // choose a best candidate
	    LineInPoem chosen = chooseNextLine(composedContent, candidates,
		    composeInfo);
	    if (chosen == null)
		return null;
	    // add line, select yunbu and add yujiaos if necessary
	    if (patternInfo.ruyun[i] && yunbu.isEmpty()) {
		Set<String> yunbus = TonalManager.getInstance().getYunbuInfo(
			chosen.line.charAt(patternInfo.col - 1));
		yunbu = (String) yunbus.toArray()[(int) (Math.random() * yunbus
			.size())];
		composeInfo.yunbu = yunbu;
	    }
	    if (patternInfo.ruyun[i]) {
		yunjiaos.add(chosen.line.charAt(patternInfo.col - 1));
	    }
	    composeInfo.srcs.put(composedContent.size(), chosen.sourcePoem);
	    composedContent.add(chosen.line);
	}
	return new Poem("¼¯¾ä", "¼ÆËã»ú", composedContent);
    }

    /**
     * compose a poem
     * 
     * @param patternInfo
     *            the pattern requirement associated with the poem
     * @param composeInfo
     * @return generated poem
     */
    public static Poem composePoem(PatternInformation patternInfo,
	    ComposeInformation composeInfo) {
	while (true) {
	    // first get an initial poem in greedy manner, and then
	    // incremental-update it to get a locally-optimized solution
	    Poem poem = getInitialPoem(patternInfo, composeInfo);
	    if (poem != null) {
		tunePoem(patternInfo, poem, composeInfo);
		return poem;
	    } else {
		continue;
	    }
	}
    }

    /**
     * find the collocation pair of two lines with highest t-value
     * 
     * @param line1
     *            the first line
     * @param line2
     *            the second line
     * @param forbidden
     *            the collocation pair set which the result should be excluded
     *            from
     * @return the collocation
     */
    private static Collocation findMaxCollocationPair(String line1,
	    String line2, Collection<Collocation> forbidden) {
	Set<String> tokens1 = ChineseUtil.Tokenize(line1);
	Set<String> tokens2 = ChineseUtil.Tokenize(line2);

	double maxCollocationTValue = -2;
	String maxCollocationToken1 = "", maxCollocationToken2 = "";
	for (String token1 : tokens1) {
	    for (String token2 : tokens2) {
		if (forbidden.contains(new Collocation(token1, token2, -1)))
		    continue;
		double t = TokenCollocationManager.getInstance()
			.getTokenCollocation(token1, token2);
		if (t > maxCollocationTValue) {
		    maxCollocationTValue = t;
		    maxCollocationToken1 = token1;
		    maxCollocationToken2 = token2;
		}
	    }
	}
	return new Collocation(maxCollocationToken1, maxCollocationToken2,
		maxCollocationTValue);
    }

    /**
     * choose the next line
     * 
     * @param composed
     *            the composed lines
     * @param candidates
     *            the candidates
     * @param composeInfo
     *            the compose information
     * @return the next line
     */
    private static LineInPoem chooseNextLine(List<String> composed,
	    List<LineInPoem> candidates, ComposeInformation composeInfo) {
	if (candidates.isEmpty())
	    return null;
	// if try to generate the first line, just random select
	if (composed.isEmpty())
	    return candidates.get((int) (Math.random() * candidates.size()));
	else {
	    List<GradedCandidate> gradedCandidates = new ArrayList<GradedCandidate>();
	    // if try to generate the second half of a sentence(line with odd
	    // sequence number), we
	    // choose based on maximizing collocation t-values
	    if (composed.size() % 2 == 1) {
		for (LineInPoem candidate : candidates) {
		    Collocation maxCollo = findMaxCollocationPair(
			    composed.get(composed.size() - 1), candidate.line,
			    composeInfo.collos.values());
		    if (maxCollo.t_value < 0)
			continue;
		    gradedCandidates.add(new GradedCandidate(candidate,
			    maxCollo.t_value, maxCollo));
		}
		if (gradedCandidates.isEmpty())
		    return null;
		Collections.sort(gradedCandidates);
		GradedCandidate bestCandidate = gradedCandidates.get(0);
		composeInfo.collos.put((composed.size() - 1) / 2,
			bestCandidate.collo);
		return bestCandidate.candidate;
	    } else {
		// if try to generate the first half of a new sentence(line with
		// even sequence number), we choose based on consistency of
		// topic model
		double[] existingTopicDistributions = TopicModelManager
			.getInstance().getTopicProbVector(composed);

		for (LineInPoem candidate : candidates) {
		    double[] lineTopicDistributions = TopicModelManager
			    .getInstance().getTopicProbVector(candidate.line);
		    for (int i = 0; i < lineTopicDistributions.length; i++)
			lineTopicDistributions[i] += existingTopicDistributions[i];
		    gradedCandidates.add(new GradedCandidate(candidate,
			    TopicModelManager.getInstance()
				    .calculateTopicConcentrationScore(
					    lineTopicDistributions,
					    CENTER_TOPIC_CNT)));
		}
		Collections.sort(gradedCandidates);
		return gradedCandidates.get(0).candidate;
	    }
	}
    }
}
