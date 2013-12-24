package test;

import java.util.ArrayList;
import java.util.List;

import managers.PatternManager;
import managers.TonalManager;

import entities.PatternCheckResult;
import entities.Poem;

/**
 * Test whether pattern checking works correct
 * @author wei.he
 *
 */
public class TestCheckPattern {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TonalManager.getInstance().readPingshuiyun("pingshuiyun.txt");
		PatternManager.getInstance().readPattern("pattern.txt");
		List<String> lines = new ArrayList<String>();
		lines.add("城阙辅三秦");
		lines.add("风烟望五津");
		lines.add("与君离别意");
		lines.add("同是宦游人");
		lines.add("海内存知己");
		lines.add("天涯若比邻");
		lines.add("无为在歧路");
		lines.add("儿女共沾巾");
		Poem poem = new Poem("送杜少府之任蜀州", "王勃", lines);
		PatternCheckResult res = PatternManager.getInstance().findOptimalPattern(poem);
		System.out.println(res.patternName + " " + res.yunbu + " " + res.pingzeErrCnt + " " + res.yunjiaoErrCnt);
	}

}
