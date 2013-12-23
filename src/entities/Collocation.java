package entities;

public class Collocation implements Comparable<Collocation> {
    public String token;
    public String pairToken;
    public double t_value;

    public Collocation(String tok, String pair, double t) {
	token = tok;
	pairToken = pair;
	t_value = t;
    }

    @Override
    public int compareTo(Collocation other) {
	// TODO Auto-generated method stub
	return (token + ":" + pairToken).compareTo(other.token + ":"
		+ pairToken);
    }

    public int hashCode() {
	return (token + ":" + pairToken).hashCode();
    }
}