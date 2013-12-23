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

public class ChineseUtil {

    public static int BLANK_CHN = 12288;
    public static int COMMA_CHN = 65292;
    public static int DOT_CHN = 12290;

    private static Map<String, Set<String>> cache = new HashMap<String, Set<String>>();

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
