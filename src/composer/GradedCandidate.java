package composer;

import entities.Collocation;

public class GradedCandidate implements Comparable<GradedCandidate> {
    public LineInPoem candidate;
    public Collocation collo;
    public double grade;

    public GradedCandidate(LineInPoem lp, double g) {
	candidate = lp;
	grade = g;
	collo = null;
    }

    public GradedCandidate(LineInPoem lp, double g, Collocation col) {
	candidate = lp;
	grade = g;
	collo = col;
    }

    @Override
    public int compareTo(GradedCandidate other) {
	return Double.compare(other.grade, this.grade);
    }
}