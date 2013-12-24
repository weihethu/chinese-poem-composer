package topicModelSolvers;

import java.util.Arrays;

/**
 * lda implementation with gibs sampling
 * 
 * @author wei.he
 * 
 */
public class ldaGibbsSamplingSolver extends solverInterface {
    /**
     * beta
     */
    private double beta = 2;
    /**
     * alpha
     */
    private double alpha = 5;
    /**
     * maximum iteration
     */
    private int MAX_ITERATION = 500;

    private int document_topic_cnt[][];
    private int topic_term_cnt[][];
    private int document_topic_sum[];
    private int topic_term_sum[];
    private int topic_assigned[][];
    private double topic_term_phi[][];

    /**
     * constructor
     * 
     * @param topicNum
     *            number of topics
     * @param detailed
     *            whether output detailed information
     */
    public ldaGibbsSamplingSolver(int topicNum, boolean detailed) {
	super(topicNum, detailed);
    }

    /**
     * init parameters
     */
    private void init() {
	document_topic_cnt = new int[nDocument][nTopic];// topic distribution
							// given document
	topic_term_cnt = new int[nTopic][nTerm];// term distribution given topic
	document_topic_sum = new int[nDocument];// terms count in each document
	topic_term_sum = new int[nTopic];// terms count in each topic
	topic_term_phi = new double[nTopic][nTerm];
	topic_assigned = new int[nDocument][];// which topic each term is
					      // assigned to

	for (int i = 0; i < nDocument; i++) {
	    topic_assigned[i] = new int[termsInDocumentList.get(i).size()];
	    Arrays.fill(topic_assigned[i], -1);
	}
	for (int i = 0; i < nDocument; i++)
	    Arrays.fill(document_topic_cnt[i], 0);
	for (int i = 0; i < nTopic; i++)
	    Arrays.fill(topic_term_cnt[i], 0);
	Arrays.fill(topic_term_sum, 0);
	Arrays.fill(document_topic_sum, 0);

	for (int documentIndex = 0; documentIndex < nDocument; documentIndex++) {
	    for (int pos = 0; pos < termsInDocumentList.get(documentIndex)
		    .size(); pos++) {
		String term = termsInDocumentList.get(documentIndex).get(pos);
		if (termToIndexMap.containsKey(term)) {
		    int termIndex = termToIndexMap.get(term);
		    int rndTopic = ((int) (Math.random() * 1000)) % nTopic;//
		    // randomly assign a word to a given topic
		    topic_assigned[documentIndex][pos] = rndTopic;
		    document_topic_cnt[documentIndex][rndTopic]++;
		    document_topic_sum[documentIndex]++;
		    topic_term_cnt[rndTopic][termIndex]++;
		    topic_term_sum[rndTopic]++;
		}
	    }// for each word
	}// for each document
    }

    /**
     * gibs sampling
     */
    private void gibs() {
	// use fixed iteration count instead of convergent conditions
	for (int iteration_index = 0; iteration_index < MAX_ITERATION; iteration_index++) {
	    System.out.println("Iteration: " + (iteration_index + 1));
	    for (int documentIndex = 0; documentIndex < nDocument; documentIndex++) {
		for (int pos = 0; pos < termsInDocumentList.get(documentIndex)
			.size(); pos++) {
		    String term = termsInDocumentList.get(documentIndex).get(
			    pos);
		    if (termToIndexMap.containsKey(term)) {
			int termIndex = termToIndexMap.get(term);
			// remove current topicAssignment
			int currentTopic = topic_assigned[documentIndex][pos];
			document_topic_cnt[documentIndex][currentTopic]--;
			document_topic_sum[documentIndex]--;
			topic_term_cnt[currentTopic][termIndex]--;
			topic_term_sum[currentTopic]--;

			// calculate new estimates of every topic for term
			double probs[] = new double[nTopic];
			for (int topicIndex = 0; topicIndex < nTopic; topicIndex++) {
			    // predictive distributions of sampling a new token
			    // of term from topic topicIndex
			    double phi = (topic_term_cnt[topicIndex][termIndex] + beta)
				    / (topic_term_sum[topicIndex] + beta
					    * nTerm);
			    // predictive distributions of sampling a new token
			    // in document documentIndex from topic topicIndex
			    double theta = (document_topic_cnt[documentIndex][topicIndex] + alpha)
				    / (document_topic_sum[documentIndex] + alpha
					    * nTopic);
			    probs[topicIndex] = phi * theta;
			}

			// calculate accumulative probability
			for (int topicIndex = 1; topicIndex < nTopic; topicIndex++) {
			    probs[topicIndex] += probs[topicIndex - 1];
			}

			// sampling
			double rndProb = Math.random() * probs[nTopic - 1];
			int newTopic = 0;
			for (; newTopic < nTopic; newTopic++) {
			    if (rndProb <= probs[newTopic])
				break;
			}
			// assign new topic
			topic_assigned[documentIndex][pos] = newTopic;
			document_topic_cnt[documentIndex][newTopic]++;
			document_topic_sum[documentIndex]++;
			topic_term_cnt[newTopic][termIndex]++;
			topic_term_sum[newTopic]++;
		    }
		}// for each word
	    }// for each document
	}
    }

    /**
     * compute phi matrix, which represents distributions of term in topics
     */
    private void computePhi() {
	for (int topicIndex = 0; topicIndex < nTopic; topicIndex++) {
	    double sum = 0;
	    for (int termIndex = 0; termIndex < nTerm; termIndex++) {
		topic_term_phi[topicIndex][termIndex] = (topic_term_cnt[topicIndex][termIndex] + beta);
		sum += topic_term_phi[topicIndex][termIndex];
	    }
	    for (int termIndex = 0; termIndex < nTerm; termIndex++) {
		topic_term_phi[topicIndex][termIndex] /= sum;
	    }
	}
    }

    @Override
    public void solve() {
	init();
	gibs();
	computePhi();
    }

    @Override
    public double getTermProbInTopic(int topicIndex, int termIndex) {
	return topic_term_phi[topicIndex][termIndex];
    }
}
