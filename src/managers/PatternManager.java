package managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.ChineseUtil;

import entities.PatternCheckResult;
import entities.PatternInformation;
import entities.Poem;

/**
 * the pattern information manager
 * 
 * @author wei.he
 * 
 */
public class PatternManager {
    /**
     * the map which maps a pattern name to its information
     */
    public Map<String, PatternInformation> nameToPatternInfoMap = null;
    /**
     * the manager instance
     */
    private static PatternManager instance = null;

    /**
     * get the manager instance
     * 
     * @return instance
     */
    public static PatternManager getInstance() {
	if (instance == null)
	    instance = new PatternManager();
	return instance;
    }

    /**
     * private constructor
     */
    private PatternManager() {

    }

    /**
     * read from a pattern file
     * 
     * @param patternFile
     */
    public void readPattern(String patternFile) {
	try {
	    nameToPatternInfoMap = new HashMap<String, PatternInformation>();
	    BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(patternFile)), "GBK"));
	    String line, patternName = "";
	    List<String> patternTonalStrs = null;
	    while ((line = br.readLine()) != null) {
		line = ChineseUtil.trim(line);
		if (line.isEmpty())
		    continue;
		if (line.startsWith("#")) {
		    if (patternTonalStrs != null && !patternTonalStrs.isEmpty()) {
			nameToPatternInfoMap.put(patternName,
				new PatternInformation(patternTonalStrs));
		    } else
			assert (patternName.isEmpty());
		    patternName = line.substring(1);
		    patternTonalStrs = new ArrayList<String>();
		} else {
		    patternTonalStrs.add(line);
		}
	    }
	    if (!patternName.isEmpty() && patternTonalStrs != null
		    && !patternTonalStrs.isEmpty()) {
		nameToPatternInfoMap.put(patternName, new PatternInformation(
			patternTonalStrs));
	    }
	    br.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * check the pattern for a given poem
     * 
     * @param poem
     *            poem
     * @return pattern check result
     */
    public PatternCheckResult findOptimalPattern(Poem poem) {
	List<PatternCheckResult> patternCheckResults = new ArrayList<PatternCheckResult>();
	// iterate all patterns to find the most suitable one
	for (String patternName : nameToPatternInfoMap.keySet()) {
	    PatternInformation pattern = nameToPatternInfoMap.get(patternName);
	    if (poem.row == pattern.row && poem.col == pattern.col) {
		// check pingze
		int pingzeErrCnt = 0;
		for (int i = 0; i < poem.row; i++) {
		    for (int j = 0; j < poem.col; j++) {
			if (poem.pingzeTable[i][j] + pattern.tonals[i][j] == 3)
			    pingzeErrCnt++;
		    }
		}
		// check yayun
		Map<String, Integer> yunbuCnts = new HashMap<String, Integer>();
		int yunjiaoExpectedCnt = 0;
		for (int i = 0; i < poem.row; i++) {
		    if (!pattern.ruyun[i])
			continue;
		    char c = poem.content[i].charAt(poem.col - 1);
		    Set<String> yunbus = TonalManager.getInstance()
			    .getYunbuInfo(c);
		    if (yunbus.size() > 0) {
			yunjiaoExpectedCnt++;
			for (String yunbu : yunbus) {
			    if (!yunbuCnts.containsKey(yunbu)) {
				yunbuCnts.put(yunbu, 1);
			    } else
				yunbuCnts.put(yunbu, yunbuCnts.get(yunbu) + 1);
			}
		    }
		}
		String optimalYunbu = "";
		int optimalYunjiaoConformedCnt = -1;
		for (String yunbu : yunbuCnts.keySet()) {
		    if (yunbuCnts.get(yunbu) > optimalYunjiaoConformedCnt) {
			optimalYunjiaoConformedCnt = yunbuCnts.get(yunbu);
			optimalYunbu = yunbu;
		    }
		}
		patternCheckResults.add(new PatternCheckResult(patternName,
			optimalYunbu, pingzeErrCnt, yunjiaoExpectedCnt
				- optimalYunjiaoConformedCnt));
	    }
	}
	// find optimal pattern
	Collections.sort(patternCheckResults,
		new Comparator<PatternCheckResult>() {

		    @Override
		    public int compare(PatternCheckResult res1,
			    PatternCheckResult res2) {
			// if one pattern yayun but the other not, the yayun one
			// should prioritize
			if (res1.yayun && !res2.yayun)
			    return -1;
			else if (!res1.yayun && res2.yayun)
			    return 1;
			else {
			    // if both yayun/not yayun, we make comparisons
			    // first by the pingze error count, if tied, then by
			    // yunjiao error count
			    if (res1.pingzeErrCnt != res2.pingzeErrCnt)
				return new Integer(res1.pingzeErrCnt)
					.compareTo(new Integer(
						res2.pingzeErrCnt));
			    else
				return new Integer(res1.yunjiaoErrCnt)
					.compareTo(new Integer(
						res2.yunjiaoErrCnt));
			}
		    }

		});

	return patternCheckResults.get(0);
    }
}
