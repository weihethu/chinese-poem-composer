package composer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import managers.PoemManager;
import managers.TokenCollocationManager;
import managers.TonalManager;
import managers.TopicModelManager;

import utils.ChineseUtil;

import entities.Collocation;
import entities.PatternInformation;
import entities.Poem;

class GradedCandidate implements Comparable<GradedCandidate> {
    LineInPoem candidate;
    Collocation collo;
    double grade;

    GradedCandidate(LineInPoem lp, double g) {
	candidate = lp;
	grade = g;
	collo = null;
    }

    GradedCandidate(LineInPoem lp, double g, Collocation col) {
	candidate = lp;
	grade = g;
	collo = col;
    }

    @Override
    public int compareTo(GradedCandidate other) {
	return Double.compare(other.grade, this.grade);
    }
}

class LineInPoem {
    String line;
    Poem sourcePoem;

    LineInPoem(String l, Poem p) {
	line = l;
	sourcePoem = p;
    }
}

public class PoemComposer {

    private static int CENTER_TOPIC_CNT = 2;
    private static int MAX_ITERATION_CNT = 10;
    private static double lamda = 15;

    public PoemComposer() {

    }

    private static boolean checkConstraints(List<String> composed, String line) {
	if (composed.contains(line))
	    return false;

	for (String existing : composed) {
	    if (line.charAt(0) == existing.charAt(0))
		return false;
	}
	return true;
    }

    private static boolean checkConstraints(Poem oldPoem, String line,
	    int rowIndex) {
	for (String oldLine : oldPoem.content)
	    if (oldLine.equals(line))
		return false;
	for (int i = 0; i < oldPoem.row; i++) {
	    if (i != rowIndex && oldPoem.content[i].charAt(0) == line.charAt(0))
		return false;
	}
	return true;
    }

    private static boolean conformPingze(int pingzeRequired[],
	    int pingzeAcutal[]) {
	for (int i = 0; i < pingzeAcutal.length; i++) {
	    if (pingzeRequired[i] + pingzeAcutal[i] == 3) {
		return false;
	    }
	}
	return true;
    }

    private static void tunePoem(PatternInformation patternInfo, Poem oldPoem,
	    ComposeInformation info) {
	// calculate old topic distributions
	double[] topicDistributions = TopicModelManager.getInstance()
		.getTopicProbVector(oldPoem.content);
	// calculate old score
	double oldTopicConcentrationScore = TopicModelManager.getInstance()
		.calculateTopicConcentrationScore(topicDistributions,
			CENTER_TOPIC_CNT);
	double collocationTSum = 0;
	for (int j = 0; j < oldPoem.row / 2; j++)
	    collocationTSum += info.collos.get(j).t_value;
	double oldCollocationScore = collocationTSum / (oldPoem.row / 2);
	double oldScore = lamda * oldTopicConcentrationScore
		+ oldCollocationScore;
	System.out.println("Old:" + oldScore + "(" + oldTopicConcentrationScore
		+ "," + oldCollocationScore + ")");
	// iterate at most fixed times
	boolean changeInLastIter = true;
	for (int iteration_cnt = 0; iteration_cnt < MAX_ITERATION_CNT
		&& changeInLastIter; iteration_cnt++) {
	    System.out.println(iteration_cnt);
	    // try to replace each row with a better one
	    changeInLastIter = false;
	    for (int i = 0; i < oldPoem.row; i++) {
		// calculate the topic distributions of the other rows
		double[] lineTopicDistributions = TopicModelManager
			.getInstance().getTopicProbVector(oldPoem.content[i]);
		double[] remainTopicDistributions = new double[topicDistributions.length];
		for (int j = 0; j < topicDistributions.length; j++)
		    remainTopicDistributions[j] = topicDistributions[j]
			    - lineTopicDistributions[j];

		double bestScoreSoFar = -1;
		double bestTopicScoreSoFar = -1, bestColloScoreSoFar = -1;
		Collocation bestCollocation = null;
		LineInPoem bestReplace = null;
		for (Poem poem : PoemManager.getInstance().poems) {
		    if (poem.col != patternInfo.col)
			continue;
		    for (int j = 0; j < poem.row; j++) {
			// potential new line
			String newLine = poem.content[j];
			if (!checkConstraints(oldPoem, newLine, i)
				|| !conformPingze(patternInfo.tonals[i],
					poem.pingzeTable[j]))
			    continue;
			// check conform yunjiao
			if (patternInfo.ruyun[i]
				&& !TonalManager
					.getInstance()
					.getYunbuInfo(
						newLine.charAt(poem.col - 1))
					.contains(info.yunbu))
			    continue;
			// check no duplicate yunjiaos
			boolean duplicateYunjiao = false;
			if (patternInfo.ruyun[i]) {
			    for (int k = 0; k < oldPoem.row; k++) {
				if (k == i)
				    continue;
				if (patternInfo.ruyun[k]
					&& (oldPoem.content[k]
						.charAt(oldPoem.col - 1) == newLine
						.charAt(oldPoem.col - 1))) {
				    duplicateYunjiao = true;
				    break;
				}
			    }
			}
			if (duplicateYunjiao)
			    continue;
			Set<Collocation> existingCollos = new HashSet<Collocation>();
			for (int k = 0; k < oldPoem.row / 2; k++) {
			    if (k != i % 2)
				existingCollos.add(info.collos.get(k));
			}
			Collocation maxCollo;
			if (i % 2 == 0) {
			    maxCollo = findMaxCollocationPair(newLine,
				    oldPoem.content[i + 1], existingCollos);
			} else {
			    maxCollo = findMaxCollocationPair(
				    oldPoem.content[i - 1], newLine,
				    existingCollos);
			}
			// calculate new score
			if (maxCollo.t_value < 0)
			    continue;
			double[] newLineTopicDistributions = TopicModelManager
				.getInstance().getTopicProbVector(newLine);
			double[] newDistributions = new double[topicDistributions.length];
			for (int k = 0; k < newDistributions.length; k++)
			    newDistributions[k] = remainTopicDistributions[k]
				    + newLineTopicDistributions[k];
			double newTopicConcentrationScore = TopicModelManager
				.getInstance()
				.calculateTopicConcentrationScore(
					newDistributions, CENTER_TOPIC_CNT);
			double newCollocationTSum = collocationTSum;
			newCollocationTSum -= info.collos.get(i / 2).t_value;
			newCollocationTSum += maxCollo.t_value;
			double newCollocationScore = newCollocationTSum
				/ (oldPoem.row / 2);
			double newScore = lamda * newTopicConcentrationScore
				+ newCollocationScore;
			if (newScore > oldScore && newScore > bestScoreSoFar) {
			    bestScoreSoFar = newScore;
			    bestTopicScoreSoFar = newTopicConcentrationScore;
			    bestColloScoreSoFar = newCollocationScore;
			    bestReplace = new LineInPoem(newLine, poem);
			    bestCollocation = maxCollo;
			}
		    }
		}
		// replace the old line with the new line
		if (bestScoreSoFar > oldScore) {
		    System.out.println(bestScoreSoFar + "("
			    + bestTopicScoreSoFar + "," + bestColloScoreSoFar
			    + ")->" + oldScore);
		    changeInLastIter = true;
		    oldScore = bestScoreSoFar;
		    oldPoem.content[i] = bestReplace.line;
		    info.srcs.put(i, bestReplace.sourcePoem);
		    collocationTSum += (bestCollocation.t_value - info.collos
			    .get(i / 2).t_value);
		    info.collos.put(i / 2, bestCollocation);

		    double[] newLineTopicDistributions = TopicModelManager
			    .getInstance().getTopicProbVector(bestReplace.line);
		    for (int j = 0; j < topicDistributions.length; j++) {
			topicDistributions[j] = remainTopicDistributions[j]
				+ newLineTopicDistributions[j];
		    }
		}
	    }
	}
    }

    public static Poem composePoem(PatternInformation patternInfo,
	    ComposeInformation info) {
	while (true) {
	    info.reset();
	    List<String> composedContent = new ArrayList<String>();
	    String yunbu = "";
	    Set<Character> yunjiaos = new HashSet<Character>();
	    boolean failed = false;
	    for (int i = 0; i < patternInfo.row; i++) {
		List<LineInPoem> candidates = new ArrayList<LineInPoem>();
		for (Poem poem : PoemManager.getInstance().poems) {
		    if (poem.col != patternInfo.col)
			continue;
		    for (int j = 0; j < poem.row; j++) {
			if (!conformPingze(patternInfo.tonals[i],
				poem.pingzeTable[j])
				|| !checkConstraints(composedContent,
					poem.content[j]))
			    continue;
			if (!patternInfo.ruyun[i]) {
			    candidates
				    .add(new LineInPoem(poem.content[j], poem));
			} else {
			    if (yunbu.isEmpty()) {
				Set<String> yunbus = TonalManager.getInstance()
					.getYunbuInfo(
						poem.content[j]
							.charAt(poem.col - 1));
				if (yunbus.size() > 0) {
				    candidates.add(new LineInPoem(
					    poem.content[j], poem));
				}
			    } else {
				Set<String> yunbus = TonalManager.getInstance()
					.getYunbuInfo(
						poem.content[j]
							.charAt(poem.col - 1));
				if (yunbus.contains(yunbu)
					&& !yunjiaos.contains(poem.content[j]
						.charAt(poem.col - 1))) {
				    // make sure there are no duplicate
				    // yunjiaos
				    candidates.add(new LineInPoem(
					    poem.content[j], poem));
				}
			    }
			}
		    }
		}
		if (candidates.isEmpty()) {
		    failed = true;
		    break;
		}
		LineInPoem chosen = chooseNextLine(composedContent, candidates,
			info);
		if (chosen == null) {
		    failed = true;
		    break;
		}
		if (patternInfo.ruyun[i] && yunbu.isEmpty()) {
		    Set<String> yunbus = TonalManager.getInstance()
			    .getYunbuInfo(
				    chosen.line.charAt(patternInfo.col - 1));
		    yunbu = (String) yunbus.toArray()[(int) (Math.random() * yunbus
			    .size())];
		    info.yunbu = yunbu;
		}
		if (patternInfo.ruyun[i]) {
		    yunjiaos.add(chosen.line.charAt(patternInfo.col - 1));
		}
		info.srcs.put(composedContent.size(), chosen.sourcePoem);
		composedContent.add(chosen.line);
	    }
	    if (!failed) {
		Poem composedPoem = new Poem("¼¯¾ä", "¼ÆËã»ú", composedContent);
		tunePoem(patternInfo, composedPoem, info);
		return composedPoem;
	    } else {
		continue;
	    }
	}
    }

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

    private static LineInPoem chooseNextLine(List<String> composed,
	    List<LineInPoem> candidates, ComposeInformation why) {
	assert (!candidates.isEmpty());
	if (composed.isEmpty())// the first line
	    return candidates.get((int) (Math.random() * candidates.size()));
	else {
	    List<GradedCandidate> gradedCandidates = new ArrayList<GradedCandidate>();
	    if (composed.size() % 2 == 1) {// the second half of a sentence
		// choose based on token collocations
		for (LineInPoem candidate : candidates) {
		    Collocation maxCollo = findMaxCollocationPair(
			    composed.get(composed.size() - 1), candidate.line,
			    why.collos.values());
		    if (maxCollo.t_value < 0)
			continue;
		    gradedCandidates.add(new GradedCandidate(candidate,
			    maxCollo.t_value, maxCollo));
		}
		if (gradedCandidates.isEmpty())
		    return null;
		Collections.sort(gradedCandidates);
		GradedCandidate bestCandidate = gradedCandidates.get(0);
		why.collos.put((composed.size() - 1) / 2, bestCandidate.collo);
		return bestCandidate.candidate;
	    } else {// the first half of a new sentence
		    // choose based on consistency of topic model
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
