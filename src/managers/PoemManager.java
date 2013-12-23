package managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import entities.PatternCheckResult;
import entities.Poem;

import utils.ChineseUtil;

public class PoemManager {
	private static PoemManager instance = null;
	public List<Poem> poems;

	public static PoemManager getInstance() {
		if (instance == null) {
			instance = new PoemManager();
		}
		return instance;
	}

	private PoemManager() {

	}

	public void preProcessRawdata(String shiFile, String preProcessedFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(shiFile)));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(preProcessedFile)));

			String line, title = "", author = "";
			List<String> contentsInPoem = new ArrayList<String>();
			boolean expectAuthorOnNextLine = false;
			while ((line = br.readLine()) != null) {
				line = ChineseUtil.trim(line);
				if (line.isEmpty())
					continue;
				if (line.startsWith("̫�ڻʵۡ��������ϣ�������"))
					break;
				int pos1 = line.indexOf('��'), pos2 = line.indexOf('��');
				if (pos1 != -1 && pos2 != -1) {
					if (!contentsInPoem.isEmpty() && checkValidPoem(contentsInPoem)) {
						writePoemWithPatternInfo(new Poem(title, author, contentsInPoem), bw);
					}
					title = line.substring(pos1 + 1, pos2);
					contentsInPoem = new ArrayList<String>();
					author = "";
					if (pos2 != line.length() - 1) {
						author = line.substring(pos2 + 1);
						author = ChineseUtil.trim(author);
						expectAuthorOnNextLine = false;
					} else
						expectAuthorOnNextLine = true;
				} else if (!title.isEmpty()
						&& (line.indexOf(ChineseUtil.COMMA_CHN) != -1 || line.indexOf(ChineseUtil.DOT_CHN) != -1)) {
					expectAuthorOnNextLine = false;
					int lastCommaDotIndex = Math.max(line.lastIndexOf(ChineseUtil.COMMA_CHN),
							line.lastIndexOf(ChineseUtil.DOT_CHN));
					line = line.substring(0, lastCommaDotIndex + 1);
					String regex = "[\\u" + Integer.toHexString(ChineseUtil.COMMA_CHN) + "\\u"
							+ Integer.toHexString(ChineseUtil.DOT_CHN) + "]";
					String[] parts = line.split(regex);
					for (String part : parts)
						contentsInPoem.add(part);
				} else if (!title.isEmpty() && expectAuthorOnNextLine) {
					author = line;
					expectAuthorOnNextLine = false;
				}
			}
			if (!contentsInPoem.isEmpty() && checkValidPoem(contentsInPoem)) {
				writePoemWithPatternInfo(new Poem(title, author, contentsInPoem), bw);
			}
			bw.close();
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readPreprocessed(String preprocessedFile) {
		try {
			poems = new ArrayList<Poem>();
			BufferedReader br = new BufferedReader(new FileReader(new File(preprocessedFile)));
			String line, title = "", author = "";
			List<String> contentsInPoem = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				else if (line.startsWith("#")) {
					if (!contentsInPoem.isEmpty())
						poems.add(new Poem(title, author, contentsInPoem));
					line = line.substring(1);
					String parts[] = line.split("\\s+");
					title = parts[0];
					author = (parts.length < 2) ? "" : parts[1];
					contentsInPoem = new ArrayList<String>();
				} else if (line.startsWith("%")) {

				} else {
					contentsInPoem.add(line);
				}
			}
			if (!contentsInPoem.isEmpty())
				poems.add(new Poem(title, author, contentsInPoem));
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean checkValidPoem(List<String> contents) {
		if (contents.size() != 4 && contents.size() != 8)
			return false;
		int len1stLine = contents.get(0).length();
		if (len1stLine != 5 && len1stLine != 7)
			return false;
		for (String line : contents) {
			if (line.length() != len1stLine)
				return false;
			if (line.contains("��"))
				return false;
		}
		return true;
	}

	private void writePoemWithPatternInfo(Poem poem, Writer writer) throws IOException {
		writer.write("#" + poem.title + " " + poem.author + "\n");
		for (int i = 0; i < poem.row; i++) {
			writer.write(poem.content[i] + "\n");
		}
		PatternCheckResult res = PatternManager.getInstance().findOptimalPattern(poem);
		writer.write("%" + res.patternName + " " + res.yunbu + " " + res.pingzeErrCnt + " " + res.yunjiaoErrCnt
				+ "\n\n");
	}
}
