package entities;

/**
 * The result when checking a pattern of a poem
 * 
 * @author wei.he
 * 
 */
public class PatternCheckResult {
    /**
     * the patter name
     */
    public String patternName;
    /**
     * yunbu
     */
    public String yunbu;
    /**
     * whether yayun
     */
    public boolean yayun;
    /**
     * how many characters are of wrong pingze
     */
    public int pingzeErrCnt;
    /**
     * how wrong yunjiaos
     */
    public int yunjiaoErrCnt;

    /**
     * constructor
     * 
     * @param pattern
     *            pattern name
     * @param yun
     *            yunbu
     * @param pzErrCnt
     *            number of characters of wrong pingze
     * @param yjErrCnt
     *            number of wrong yunjiaos
     */
    public PatternCheckResult(String pattern, String yun, int pzErrCnt,
	    int yjErrCnt) {
	patternName = pattern;
	yunbu = yun;
	yayun = (yjErrCnt == 0);
	pingzeErrCnt = pzErrCnt;
	yunjiaoErrCnt = yjErrCnt;
    }
}