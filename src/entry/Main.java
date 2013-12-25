package entry;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import gui.MainFrame;
import managers.PatternManager;
import managers.PoemManager;
import managers.TokenCollocationManager;
import managers.TonalManager;
import managers.TopicModelManager;

/**
 * the program parameter
 * 
 * @author wei.he
 * 
 */
class Parameter {
    /**
     * the file path to pingshuiyun
     */
    String pingshuiyunPath;
    /**
     * the file path to pattern
     */
    String patternPath;
    /**
     * the file path to poem
     */
    String poemPath;
    /**
     * the file path to collocation
     */
    String colloPath;
    /**
     * the file path to topic model
     */
    String topicPath;
    /**
     * topic number
     */
    int topicNum;
    /**
     * the parameter check success message
     */
    public static String SUCCESS = "success";

    /**
     * constructor
     * 
     * @param pathYun
     *            the file path to pingshuiyun
     * @param pathPattern
     *            the file path to pattern
     * @param pathPoem
     *            the file path to poem
     * @param pathCollo
     *            the file path to collocation
     * @param pathTopic
     *            the file path to topic model
     * @param nTopic
     *            topic number
     */
    Parameter(String pathYun, String pathPattern, String pathPoem,
	    String pathCollo, String pathTopic, int nTopic) {
	pingshuiyunPath = pathYun;
	patternPath = pathPattern;
	poemPath = pathPoem;
	colloPath = pathCollo;
	topicPath = pathTopic;
	topicNum = nTopic;
    }

    /**
     * check whether all files exist
     * 
     * @param yunRequired
     *            pingshuiyun required
     * @param patternRequired
     *            pattern file required
     * @param poemRequired
     *            poem file required
     * @param colloRequired
     *            collocation file required
     * @param topicRequired
     *            topic model file required
     * @return check result message
     */
    String checkFileExist(boolean yunRequired, boolean patternRequired,
	    boolean poemRequired, boolean colloRequired, boolean topicRequired) {
	String[] paths = new String[] { pingshuiyunPath, patternPath, poemPath,
		colloPath, topicPath };
	boolean[] required = new boolean[] { yunRequired, patternRequired,
		poemRequired, colloRequired, topicRequired };
	for (int i = 0; i < paths.length; i++) {
	    if (required[i]) {
		File f = new File(paths[i]);
		if (!f.exists() || !f.isFile())
		    return paths[i] + " doesn't exist or is not a file";
	    }
	}
	return SUCCESS;
    }
}

/**
 * the entry class
 * 
 * @author wei.he
 * 
 */
public class Main {

    /**
     * the default path to pingshuiyun
     */
    private static String DEFAULT_PINGSHUIYUN_PATH = "pingshuiyun.txt";
    /**
     * the default path to pattern file
     */
    private static String DEFAULT_PATTERN_PATH = "pattern.txt";
    /**
     * the default path to poem
     */
    private static String DEFAULT_POEM_PATH = "poem.txt";
    /**
     * the default path to collocation file
     */
    private static String DEFAULT_COLLO_PATH = "collocation.txt";
    /**
     * the default path to topic model file
     */
    private static String DEFAULT_TOPIC_PATH = "topic.txt";
    /**
     * the default topic numbers
     */
    private static int DEFAULT_TOPIC_NUM = 10;

    /**
     * parse arguments
     * 
     * @param args
     *            arguments
     * @return parameter
     */
    private static Parameter parseArguments(String[] args) {
	// initiate program parameter with default settings
	Parameter para = new Parameter(DEFAULT_PINGSHUIYUN_PATH,
		DEFAULT_PATTERN_PATH, DEFAULT_POEM_PATH, DEFAULT_COLLO_PATH,
		DEFAULT_TOPIC_PATH, DEFAULT_TOPIC_NUM);
	for (int i = 0; i < args.length; i++) {
	    if (args[i].equalsIgnoreCase("-pingshuiyunpath")
		    && i < args.length - 1) {
		para.pingshuiyunPath = args[++i];
	    } else if (args[i].equalsIgnoreCase("-patternpath")
		    && i < args.length - 1) {
		para.patternPath = args[++i];
	    } else if (args[i].equalsIgnoreCase("-poempath")
		    && i < args.length - 1) {
		para.poemPath = args[++i];
	    } else if (args[i].equalsIgnoreCase("-collopath")
		    && i < args.length - 1) {
		para.colloPath = args[++i];
	    } else if (args[i].equalsIgnoreCase("-topicpath")
		    && i < args.length - 1) {
		para.topicPath = args[++i];
	    } else if (args[i].equalsIgnoreCase("-topicnum")
		    && i < args.length - 1) {
		para.topicNum = Integer.parseInt(args[++i]);
	    }
	}
	return para;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	Parameter para = parseArguments(args);
	if (args.length > 0 && args[0].equalsIgnoreCase("-traincollo")) {
	    String msg = para.checkFileExist(true, true, true, false, false);
	    if (msg.equals(Parameter.SUCCESS)) {
		TonalManager.getInstance()
			.readPingshuiyun(para.pingshuiyunPath);
		PatternManager.getInstance().readPattern(para.patternPath);
		PoemManager.getInstance().readPreprocessed(para.poemPath);
		TokenCollocationManager.getInstance().extractTokenCollocations(
			para.colloPath, 1.645);
	    } else
		System.out.println("ERROR:" + msg);
	} else if (args.length > 0 && args[0].equalsIgnoreCase("-traintopic")) {
	    String msg = para.checkFileExist(true, true, true, false, false);
	    if (msg.equals(Parameter.SUCCESS)) {
		TonalManager.getInstance()
			.readPingshuiyun(para.pingshuiyunPath);
		PatternManager.getInstance().readPattern(para.patternPath);
		PoemManager.getInstance().readPreprocessed(para.poemPath);
		TopicModelManager.getInstance().train(10, para.topicPath);
	    } else
		System.out.println("ERROR:" + msg);
	} else {
	    String msg = para.checkFileExist(true, true, true, true, true);
	    if (msg.equalsIgnoreCase(Parameter.SUCCESS)) {
		TonalManager.getInstance()
			.readPingshuiyun(para.pingshuiyunPath);
		PatternManager.getInstance().readPattern(para.patternPath);
		PoemManager.getInstance().readPreprocessed(para.poemPath);
		TokenCollocationManager.getInstance().readCollocations(
			para.colloPath);
		TopicModelManager.getInstance().readTermTopicProbs(
			para.topicPath);

		MainFrame frame = new MainFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	    } else
		JOptionPane.showMessageDialog(null, msg, "Æô¶¯´íÎó",
			JOptionPane.ERROR_MESSAGE);
	}
    }
}
