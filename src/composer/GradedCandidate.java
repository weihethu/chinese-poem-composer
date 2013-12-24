package composer;

import entities.Collocation;

/**
 * a candidate and a grade to represent its fitness
 * 
 * @author wei.he
 * 
 */
public class GradedCandidate implements Comparable<GradedCandidate> {
    /**
     * candidate
     */
    public LineInPoem candidate;
    /**
     * the collocation pair associated, not always used
     */
    public Collocation collo;
    /**
     * a grade to represent its fitness
     */
    public double grade;

    /**
     * constructor
     * 
     * @param lp
     *            candidate
     * @param g
     *            grade
     */
    public GradedCandidate(LineInPoem lp, double g) {
	candidate = lp;
	grade = g;
	collo = null;
    }

    /**
     * constructor
     * 
     * @param lp
     *            candidate
     * @param g
     *            grade
     * @param col
     *            collocation
     */
    public GradedCandidate(LineInPoem lp, double g, Collocation col) {
	candidate = lp;
	grade = g;
	collo = col;
    }

    /**
     * we compare two candidates by their grades
     */
    @Override
    public int compareTo(GradedCandidate other) {
	return Double.compare(other.grade, this.grade);
    }
}