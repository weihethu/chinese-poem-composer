package entities;

/**
 * Collocation
 * @author wei.he
 *
 */
public class Collocation implements Comparable<Collocation> {
    /**
     * first token in pair
     */
    public String token;
    /**
     * second token in pair
     */
    public String pairToken;
    /**
     * t value
     */
    public double t_value;
    
    /**
     * constructor
     * @param tok first token
     * @param pair second token
     * @param t t value
     */
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