package managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import entities.PatternCheckResult;
import entities.Poem;
import utils.ChineseUtil;

/**
 * the poem manager
 * 
 * @author wei.he
 * 
 */
public class PoemManager {
    /**
     * manager instance
     */
    private static PoemManager instance = null;
    /**
     * poems
     */
    public List<Poem> poems;

    /**
     * get manager instance
     * 
     * @return instance
     */
    public static PoemManager getInstance() {
	if (instance == null) {
	    instance = new PoemManager();
	}
	return instance;
    }

    /**
     * private constructor
     */
    private PoemManager() {

    }

    /**
     * preprocess raw data which I download from internet the following code
     * only applies for quantangshi of a specific format since I already provide
     * a preprocessed file, the method should not be called I add no comments
     * because you don't need to read the code
     * 
     * @param shiFile
     *            the raw data file
     * @param preProcessedFile
     *            the preprocess file
     */
    public void preProcessRawdata(String shiFile, String preProcessedFile) {
	try {
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    new FileInputStream(new File(shiFile)), "GBK"));
	    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
		    new FileOutputStream(new File(preProcessedFile)), "GBK"));

	    String line, title = "", author = "";
	    List<String> contentsInPoem = new ArrayList<String>();
	    boolean expectAuthorOnNextLine = false;
	    while ((line = br.readLine()) != null) {
		line = ChineseUtil.trim(line);
		if (line.isEmpty())
		    continue;
		if (line.startsWith("太宗皇帝。帝姓李氏，讳世民。"))
		    break;
		int pos1 = line.indexOf('【'), pos2 = line.indexOf('】');
		if (pos1 != -1 && pos2 != -1) {
		    if (!contentsInPoem.isEmpty()
			    && checkValidPoem(contentsInPoem)) {
			writePoem(new Poem(title, author, contentsInPoem), bw);
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
			&& (line.indexOf(ChineseUtil.COMMA_CHN) != -1 || line
				.indexOf(ChineseUtil.DOT_CHN) != -1)) {
		    expectAuthorOnNextLine = false;
		    int lastCommaDotIndex = Math.max(
			    line.lastIndexOf(ChineseUtil.COMMA_CHN),
			    line.lastIndexOf(ChineseUtil.DOT_CHN));
		    line = line.substring(0, lastCommaDotIndex + 1);
		    String regex = "[\\u"
			    + Integer.toHexString(ChineseUtil.COMMA_CHN)
			    + "\\u" + Integer.toHexString(ChineseUtil.DOT_CHN)
			    + "]";
		    String[] parts = line.split(regex);
		    for (String part : parts)
			contentsInPoem.add(part);
		} else if (!title.isEmpty() && expectAuthorOnNextLine) {
		    author = line;
		    expectAuthorOnNextLine = false;
		}
	    }
	    if (!contentsInPoem.isEmpty() && checkValidPoem(contentsInPoem)) {
		writePoem(new Poem(title, author, contentsInPoem), bw);
	    }
	    bw.close();
	    br.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * read the preprocessed file and load poems into memory
     * 
     * @param preprocessedFile
     *            preprocessed file
     */
    public void readPreprocessed(String preprocessedFile) {
	try {
	    poems = new ArrayList<Poem>();
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    new FileInputStream(new File(preprocessedFile)), "GBK"));
	    String line, title = "", author = "";
	    List<String> contentsInPoem = new ArrayList<String>();
	    while ((line = br.readLine()) != null) {
		line = line.trim();
		if (line.isEmpty())
		    continue;
		else if (line.startsWith("#")) {
		    // this line contains the title and author
		    if (!contentsInPoem.isEmpty())
			poems.add(new Poem(title, author, contentsInPoem));
		    line = line.substring(1);
		    String parts[] = line.split("\\s+");
		    title = parts[0];
		    author = (parts.length < 2) ? "" : parts[1];
		    contentsInPoem = new ArrayList<String>();
		} else if (line.startsWith("%")) {
		    // this line contains the pattern check result, don't need
		    // to load this into memory
		} else {
		    contentsInPoem.add(line);
		}
	    }
	    if (!contentsInPoem.isEmpty())
		poems.add(new Poem(title, author, contentsInPoem));
	    br.close();
	} catch (IOException e) {
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
	    if (line.contains("□"))
		return false;
	}
	return true;
    }

    /**
     * get poem popularity information, and write to file
     * 
     * @param outputPath
     *            output path
     */
    public void getPoemPopularities(String outputPath) {
	BufferedWriter bw;
	try {
	    bw = new BufferedWriter(new OutputStreamWriter(
		    new FileOutputStream(new File(outputPath)), "GBK"));
	    int cnt = 0;
	    for (Poem poem : this.poems) {
		System.out.println(++cnt + "/" + this.poems.size());
		String firstLine = poem.content[0];
		poem.popularity = getBaiduSearchResultCnt(firstLine);
		writePoem(poem, bw);
	    }
	    bw.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * get result set size when searching in baidu
     * 
     * @param str
     *            search key
     * @return result-set size
     */
    private long getBaiduSearchResultCnt(String str) {
	long resultCnt = -1;
	InputStream inputStream = null;
	InputStreamReader inputStreamReader = null;
	try {
	    URL url = new URL("http://www.baidu.com/s?wd=" + str);
	    inputStream = url.openStream();
	    inputStreamReader = new InputStreamReader(inputStream, "utf-8");

	    StringBuffer sb = new StringBuffer();
	    int ch;
	    while ((ch = inputStreamReader.read()) != -1) {
		sb.append((char) ch);
	    }
	    String content = sb.toString();
	    String key = "百度为您找到相关结果约";
	    int startIndex = content.indexOf(key);
	    if (startIndex >= 0) {
		startIndex += key.length();
		int endIndex = startIndex + 1;
		while (content.charAt(endIndex) != '个')
		    endIndex++;
		String resultSetSizeStr = content.substring(startIndex,
			endIndex);
		resultSetSizeStr = resultSetSizeStr.replaceAll("\\D", "");
		resultCnt = Long.parseLong(resultSetSizeStr);
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	} finally {
	    try {
		if (inputStreamReader != null)
		    inputStreamReader.close();
		if (inputStream != null)
		    inputStream.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	return resultCnt;
    }

    /**
     * write the information of a poem to file
     * 
     * @param poem
     *            poem
     * @param writer
     *            writer
     * @throws IOException
     */
    private void writePoem(Poem poem, Writer writer) throws IOException {
	writer.write("#" + poem.title + " " + poem.author + " "
		+ poem.popularity + "\n");
	for (int i = 0; i < poem.row; i++) {
	    writer.write(poem.content[i] + "\n");
	}
	PatternCheckResult res = PatternManager.getInstance()
		.findOptimalPattern(poem);
	writer.write("%" + res.patternName + " " + res.yunbu + " "
		+ res.pingzeErrCnt + " " + res.yunjiaoErrCnt + "\n\n");
    }
}
