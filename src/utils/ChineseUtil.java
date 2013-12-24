package utils;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 * a utility class for dealing with Chinese characters
 * 
 * @author wei.he
 * 
 */
public class ChineseUtil {

    /**
     * the UNICODE for Chinese blank character
     */
    public static int BLANK_CHN = 12288;
    /**
     * the UNICODE for Chinese comma character
     */
    public static int COMMA_CHN = 65292;
    /**
     * the UNICODE for Chinese dot character
     */
    public static int DOT_CHN = 12290;

    /**
     * a cache that stores the previously computed tokenize results
     */
    private static Map<String, Set<String>> cache = new HashMap<String, Set<String>>();

    /**
     * tokenize a line using SmartChineseAnalyzer in lucene
     * 
     * @param line
     *            line
     * @return tokenized results
     */
    public static Set<String> Tokenize(String line) {
	if (cache.containsKey(line))
	    return cache.get(line);
	Set<String> tokens = new HashSet<String>();
	try {
	    StringReader reader = new StringReader(line);
	    Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_45,
		    true);
	    TokenStream ts = analyzer.tokenStream("content", reader);
	    CharTermAttribute charTermAttribute = ts
		    .addAttribute(CharTermAttribute.class);

	    ts.reset();
	    while (ts.incrementToken()) {
		String token = charTermAttribute.toString();
		tokens.add(token);
	    }
	    ts.end();
	    ts.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	cache.put(line, tokens);
	return tokens;
    }

    /**
     * trim a Chinese line
     * 
     * @param s
     *            line
     * @return trimmed result
     */
    public static String trim(String s) {
	s = s.trim();
	int start = 0, end = s.length() - 1;
	while (start < s.length() && (s.charAt(start) == BLANK_CHN))
	    start++;
	while (end >= 0 && (s.charAt(end) == BLANK_CHN))
	    end--;
	if (start > end)
	    return "";
	else
	    return s.substring(start, end + 1);
    }
}
