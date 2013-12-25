package test;

import managers.TopicModelManager;

public class TestTopicDistributionsOfPoem {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	// String lines[] = new String[] { "��̨һ�����ľ�", "�չ���������Ӫ", "���ﺮ������ѩ",
	// "������ɫ��Σ�", "ɳ�����������", "������ɽӵ����", "��С���Ͷ����", "�۹������볤ӧ" };
	String lines[] = new String[] { "ǧ����꽭ˮɫ", "���幬�������", "�����ҹ˼ǧ��", "һҹ��Գ������" };
	TopicModelManager.getInstance().readTermTopicProbs("topic.txt");
	double[] distributions = TopicModelManager.getInstance()
		.getTopicProbVector(lines);
	for (int i = 0; i < distributions.length; i++)
	    System.out.println("Topic#" + (i + 1) + "\t" + distributions[i]);
    }
}
