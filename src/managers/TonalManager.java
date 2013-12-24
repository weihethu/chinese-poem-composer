package managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.ChineseUtil;

import entities.TonalInformation;

/**
 * tonal manager for characters
 * 
 * @author wei.he
 * 
 */
public class TonalManager {

    /**
     * the map which maps a character to its associated tonal informations
     */
    private Map<Character, List<TonalInformation>> charToTonalInfoMap = null;
    /**
     * manager instance
     */
    private static TonalManager instance = null;

    /**
     * get manager instance
     * 
     * @return instance
     */
    public static TonalManager getInstance() {
	if (instance == null)
	    instance = new TonalManager();
	return instance;
    }

    /**
     * private constructor
     */
    private TonalManager() {

    }

    /**
     * read pingshuiyun(an ancient tonal table), load into memory
     * 
     * @param dictFile
     *            pingshuiyun file
     */
    public void readPingshuiyun(String dictFile) {
	try {
	    charToTonalInfoMap = new HashMap<Character, List<TonalInformation>>();
	    BufferedReader br = new BufferedReader(new FileReader(new File(
		    dictFile)));
	    String line, lastLine = "";
	    // the section index, the first 2 sections of pingshuiyun are
	    // characters of ping
	    // the remaining sections are characters of ze
	    int sectionIndex = 0;
	    while ((line = br.readLine()) != null) {
		line = ChineseUtil.trim(line);
		if (line.isEmpty()) {
		    lastLine = line;
		    continue;
		}
		if (lastLine.isEmpty()) {
		    sectionIndex++;
		}

		int commaIndex = line.indexOf(ChineseUtil.COMMA_CHN);
		if (commaIndex > 0) {
		    String rhythm = line.substring(0, commaIndex);
		    String characters = line.substring(commaIndex + 1);
		    for (char c : characters.toCharArray()) {
			if (c == ChineseUtil.BLANK_CHN)
			    continue;
			TonalInformation info = new TonalInformation(rhythm,
				sectionIndex <= 2);
			if (!charToTonalInfoMap.containsKey(c))
			    charToTonalInfoMap.put(c,
				    new ArrayList<TonalInformation>());
			charToTonalInfoMap.get(c).add(info);
		    }
		}
		lastLine = line;
	    }
	    br.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * get pingze info for a given character
     * 
     * @param c
     *            character
     * @return -1 means unknown, 0 means both, 1 means ze, 2 means ping
     */
    public int getPingzeInfo(char c) {
	List<TonalInformation> infos = charToTonalInfoMap.get(c);
	if (infos != null) {
	    int x = 0;
	    for (TonalInformation info : infos) {
		x |= (info.isPing ? 2 : 1);
	    }
	    if (x == 3)
		return 0;
	    else
		return (x == 2) ? 2 : 1;
	} else
	    return -1;
    }
    
    /**
     * get yunbus for a character
     * @param c character
     * @return yunbus
     */
    public Set<String> getYunbuInfo(char c) {
	Set<String> yunbus = new HashSet<String>();
	List<TonalInformation> infos = charToTonalInfoMap.get(c);
	if (infos != null) {
	    for (TonalInformation info : infos)
		yunbus.add(info.yunbu);
	}
	return yunbus;
    }
}
