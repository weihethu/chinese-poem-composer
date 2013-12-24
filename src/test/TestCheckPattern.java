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
		lines.add("���ڸ�����");
		lines.add("���������");
		lines.add("��������");
		lines.add("ͬ�ǻ�����");
		lines.add("���ڴ�֪��");
		lines.add("����������");
		lines.add("��Ϊ����·");
		lines.add("��Ů��մ��");
		Poem poem = new Poem("�Ͷ��ٸ�֮������", "����", lines);
		PatternCheckResult res = PatternManager.getInstance().findOptimalPattern(poem);
		System.out.println(res.patternName + " " + res.yunbu + " " + res.pingzeErrCnt + " " + res.yunjiaoErrCnt);
	}

}
