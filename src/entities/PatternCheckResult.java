package entities;

public class PatternCheckResult {
	public String patternName;
	public String yunbu;
	public boolean yayun;
	public int pingzeErrCnt;
	public int yunjiaoErrCnt;

	public PatternCheckResult(String pattern, String yun, int pzErrCnt, int yjErrCnt) {
		patternName = pattern;
		yunbu = yun;
		yayun = (yjErrCnt == 0);
		pingzeErrCnt = pzErrCnt;
		yunjiaoErrCnt = yjErrCnt;
	}
}