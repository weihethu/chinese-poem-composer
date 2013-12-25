package topicModelSolvers;

import java.util.Arrays;
import java.util.Date;

/**
 * plsi implementation
 * 
 * @author wei.he
 * 
 */
public class plsiSolver extends solverInterface {
	/**
	 * document-topic probability matrix
	 */
	private double p_dz_n[][];
	/**
	 * term-topic probability matrix
	 */
	private double p_wz_n[][];
	/**
	 * topic distribution
	 */
	private double p_z_n[];
	/**
	 * maximum iterations
	 */
	private static int MAX_ITERATION = 2500;
	/**
	 * threshold for convergent condition
	 */
	private static double THRESHOLD = 1;

	/**
	 * constructor
	 * 
	 * @param topicNum
	 *            topic number
	 * @param detailed
	 *            whether output detailed information
	 */
	public plsiSolver(int topicNum, boolean detailed) {
		super(topicNum, detailed);
	}

	/**
	 * random assign parameters, meeting sum(p_dz_n) = 1, sum(p_wz_n) = 1,
	 * sum(p_z_n) = 1
	 */
	private void randomInitParameters() {
		p_dz_n = new double[nTopic][nDocument];
		p_wz_n = new double[nTopic][nTerm];
		p_z_n = new double[nTopic];

		double sum = 0;
		for (int i = 0; i < nTopic; i++) {
			p_z_n[i] = Math.random();
			sum += p_z_n[i];
		}
		for (int i = 0; i < nTopic; i++) {
			p_z_n[i] /= sum;
		}
		for (int i = 0; i < nTopic; i++) {
			sum = 0;
			for (int j = 0; j < nDocument; j++) {
				p_dz_n[i][j] = Math.random();
				sum += p_dz_n[i][j];
			}
			for (int j = 0; j < nDocument; j++) {
				p_dz_n[i][j] /= sum;
			}
			sum = 0;
			for (int j = 0; j < nTerm; j++) {
				p_wz_n[i][j] = Math.random();
				sum += p_wz_n[i][j];
			}
			for (int j = 0; j < nTerm; j++) {
				p_wz_n[i][j] /= sum;
			}
		}
	}

	/**
	 * update
	 */
	private void update() {
		double nominator_p_dz_n[][] = new double[nTopic][nDocument];
		double nominator_p_wz_n[][] = new double[nTopic][nTerm];
		double denominator_p_dz_n[] = new double[nTopic];
		double denominator_p_wz_n[] = new double[nTopic];
		double nominator_p_z_n[] = new double[nTopic];
		double denominator_p_z_n = 0;

		double lastLogLikelihood = 0;
		int iteration_index = 0;
		for (; iteration_index < MAX_ITERATION; iteration_index++) {
			for (int i = 0; i < nTopic; i++) {
				Arrays.fill(nominator_p_dz_n[i], 0);
				Arrays.fill(nominator_p_wz_n[i], 0);
			}
			Arrays.fill(denominator_p_dz_n, 0);
			Arrays.fill(denominator_p_wz_n, 0);
			Arrays.fill(nominator_p_z_n, 0);
			denominator_p_z_n = 0;

			for (int documentIndex = 0; documentIndex < nDocument; documentIndex++) {
				for (String term : tfsList.get(documentIndex).keySet()) {
					if (!termToIndexMap.containsKey(term))
						continue;
					int termIndex = termToIndexMap.get(term);
					int tf = tfsList.get(documentIndex).get(term);
					double denominator = 0;
					double nominator[] = new double[nTopic];

					for (int k = 0; k < nTopic; k++) {
						nominator[k] = p_dz_n[k][documentIndex]
								* p_wz_n[k][termIndex] * p_z_n[k];
						denominator += nominator[k];
					}

					for (int k = 0; k < nTopic; k++) {
						double P_z_condition_d_w = nominator[k] / denominator;
						nominator_p_dz_n[k][documentIndex] += tf
								* P_z_condition_d_w;
						nominator_p_wz_n[k][termIndex] += tf
								* P_z_condition_d_w;
						denominator_p_dz_n[k] += tf * P_z_condition_d_w;
						denominator_p_wz_n[k] += tf * P_z_condition_d_w;
						nominator_p_z_n[k] += tf * P_z_condition_d_w;
					}
					denominator_p_z_n += tf;
				}// end for each word j included in document i
			}// end for each document i

			for (int i = 0; i < nDocument; i++) {
				for (int j = 0; j < nTopic; j++) {
					p_dz_n[j][i] = nominator_p_dz_n[j][i]
							/ denominator_p_dz_n[j];
				}
			}

			for (int i = 0; i < nTerm; i++) {
				for (int j = 0; j < nTopic; j++) {
					p_wz_n[j][i] = nominator_p_wz_n[j][i]
							/ denominator_p_wz_n[j];
				}
			}

			for (int i = 0; i < nTopic; i++) {
				p_z_n[i] = nominator_p_z_n[i] / denominator_p_z_n;
			}
			double logLikelihood = calculateLogLikelihood();
			if (detailedOutput)
				System.out.println("Iteration: " + (iteration_index + 1)
						+ " Log Likelihood: " + logLikelihood);
			// break when convergent condition is met
			if (iteration_index > 0
					&& logLikelihood - lastLogLikelihood < THRESHOLD)
				break;
			lastLogLikelihood = logLikelihood;
		}
		if (iteration_index == MAX_ITERATION) {
			System.out
					.println("warning: iteration cnt exceeds upper limit while running with nTopic = "
							+ nTopic);
		}
	}

	/**
	 * calculate log likelihood
	 * 
	 * @return likelihood
	 */
	private double calculateLogLikelihood() {
		double result = 0;
		for (int documentIndex = 0; documentIndex < nDocument; documentIndex++) {
			for (String term : tfsList.get(documentIndex).keySet()) {
				if (!termToIndexMap.containsKey(term))
					continue;

				int termIndex = termToIndexMap.get(term);
				int tf = tfsList.get(documentIndex).get(term);
				double tmp = 0;
				for (int topicIndex = 0; topicIndex < nTopic; topicIndex++) {
					tmp += p_wz_n[topicIndex][termIndex]
							* p_dz_n[topicIndex][documentIndex]
							* p_z_n[topicIndex];
				}
				result += tf * Math.log(tmp);
			}
		}
		return result;
	}

	@Override
	public void solve() {
		randomInitParameters();
		update();
	}

	@Override
	public double getTermProbInTopic(int topicIndex, int termIndex) {
		return p_wz_n[topicIndex][termIndex];
	}

}
