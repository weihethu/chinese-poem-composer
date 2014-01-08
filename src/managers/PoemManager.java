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
import java.util.Collections;
import java.util.Comparator;
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
     * the ranking thresholds for evaluating a poem's popularity
     */
    private static int[] popularity_rankthresholds = new int[] { 20, 50, 200,
	    500, 2000, 5000, Integer.MAX_VALUE };
    
    /**
     * the corresponding scores of each ranking category
     */
    private static int[] popularity_scores = new int[] { 10, 8, 6, 4, 3, 2, 1 };

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
			writePoem(new Poem(title, author, -1, contentsInPoem),
				bw);
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
		writePoem(new Poem(title, author, -1, contentsInPoem), bw);
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
	    long popularity = -1;
	    List<String> contentsInPoem = new ArrayList<String>();
	    while ((line = br.readLine()) != null) {
		line = line.trim();
		if (line.isEmpty())
		    continue;
		else if (line.startsWith("#")) {
		    // this line contains the title and author
		    if (!contentsInPoem.isEmpty())
			poems.add(new Poem(title, author, popularity,
				contentsInPoem));
		    line = line.substring(1);
		    String parts[] = line.split("\\s+");
		    title = parts[0];
		    try {
			// new format
			popularity = Long.parseLong(parts[parts.length - 1]);
			author = (parts.length < 3) ? "" : parts[1];
		    } catch (NumberFormatException ex) {
			// old format
			popularity = -1;
			author = (parts.length < 2) ? "" : parts[1];
		    }
		    contentsInPoem = new ArrayList<String>();
		} else if (line.startsWith("%")) {
		    // this line contains the pattern check result, don't need
		    // to load this into memory
		} else {
		    contentsInPoem.add(line);
		}
	    }
	    if (!contentsInPoem.isEmpty())
		poems.add(new Poem(title, author, popularity, contentsInPoem));
	    br.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	Collections.sort(poems, new Comparator<Poem>() {

	    @Override
	    public int compare(Poem poem1, Poem poem2) {
		return Long.valueOf(poem2.popularity).compareTo(
			Long.valueOf(poem1.popularity));
	    }

	});

	int current_index = 0;
	for (int i = 0; i < poems.size(); i++) {
	    Poem poem = poems.get(i);
	    if (i < popularity_rankthresholds[current_index])
		poem.popularityScore = popularity_scores[current_index];
	    else
		current_index++;
	}
    }

    /**
     * check if a poem is valid
     * 
     * @param contents
     *            contents
     * @return valid or not
     */
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
	    int cnt = 0;
	    for (Poem poem : this.poems) {
		cnt++;
		if (poem.popularity >= 0)
		    continue;
		System.out.println(cnt + "/" + this.poems.size());
		String firstLine = poem.content[0];
		int waitCnt = 1;
		while (true) {
		    poem.popularity = getBaiduSearchResultCnt(firstLine);
		    if (poem.popularity >= 0) {
			waitCnt = 1;
			break;
		    } else {
			if (waitCnt <= 16) {
			    waitCnt *= 2;
			    System.out.println("waiting " + waitCnt + " secs");
			} else {
			    System.out.println("stop waiting..");
			    break;
			}
		    }
		    try {
			Thread.sleep(1000 * waitCnt);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}
	    }
	    bw = new BufferedWriter(new OutputStreamWriter(
		    new FileOutputStream(new File(outputPath)), "GBK"));
	    for (Poem poem : this.poems)
		writePoem(poem, bw);
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
	    URL url = new URL("http://www.baidu.com/s?wd=\"" + str + "\"");
	    inputStream = url.openStream();
	    inputStreamReader = new InputStreamReader(inputStream, "utf-8");

	    StringBuffer sb = new StringBuffer();
	    int ch;
	    while ((ch = inputStreamReader.read()) != -1) {
		sb.append((char) ch);
	    }
	    String content = sb.toString();
	    // System.out.println(content);
	    String key = "百度为您找到相关结果";
	    int startIndex = content.indexOf(key);
	    if (startIndex >= 0) {
		startIndex += key.length();
		if (!Character.isDigit(content.charAt(startIndex)))//some times there's an additional '約'
		    startIndex++;
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
