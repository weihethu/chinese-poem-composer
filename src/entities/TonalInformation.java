package entities;

/**
 * the tonal information for a character, a character may have multiple tonal
 * informations associated
 * 
 * @author wei.he
 * 
 */
public class TonalInformation {
    /**
     * yunbu
     */
    public String yunbu;
    /**
     * whether is ping
     */
    public boolean isPing;

    /**
     * constructor
     * 
     * @param yun
     *            yunbu
     * @param ping
     *            is ping
     */
    public TonalInformation(String yun, boolean ping) {
	yunbu = yun;
	isPing = ping;
    }
}