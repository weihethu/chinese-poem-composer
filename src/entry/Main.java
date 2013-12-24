package entry;

import javax.swing.JFrame;
import gui.MainFrame;
import managers.PatternManager;
import managers.PoemManager;
import managers.TokenCollocationManager;
import managers.TonalManager;
import managers.TopicModelManager;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TonalManager.getInstance().readPingshuiyun("pingshuiyun.txt");
		PatternManager.getInstance().readPattern("pattern.txt");
		// PoemManager.getInstance().preProcessRawdata("quantangshi.txt",
		// "input.txt");
		PoemManager.getInstance().readPreprocessed("input.txt");
		// TokenCollocationManager.getInstance().extractTokenCollocations("collocation.txt",
		// 0.97);
		TokenCollocationManager.getInstance().readCollocations("collocation3.txt");
		// TopicModelManager.getInstance().train(10, "topic.txt");
		TopicModelManager.getInstance().readTermTopicProbs("topic1.txt");

		MainFrame frame = new MainFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}
}
