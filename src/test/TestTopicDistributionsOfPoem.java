package test;

import managers.TopicModelManager;

public class TestTopicDistributionsOfPoem {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	// String lines[] = new String[] { "燕台一望客心惊", "笳鼓喧喧汉将营", "万里寒光生积雪",
	// "三边曙色动危旌", "沙场烽火连胡月", "海畔云山拥蓟城", "少小虽非投笔吏", "论功还欲请长缨" };
	String lines[] = new String[] { "千里嘉陵江水色", "华清宫里打撩声", "故乡今夜思千里", "一夜号猿吊旅情" };
	TopicModelManager.getInstance().readTermTopicProbs("topic.txt");
	double[] distributions = TopicModelManager.getInstance()
		.getTopicProbVector(lines);
	for (int i = 0; i < distributions.length; i++)
	    System.out.println("Topic#" + (i + 1) + "\t" + distributions[i]);
    }
}
