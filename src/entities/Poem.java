package entities;

import java.util.List;
import managers.TonalManager;

/**
 * Poem
 * @author wei.he
 *
 */
public class Poem {
    /**
     * author
     */
    public String author;
    /**
     * title
     */
    public String title;
    /**
     * row count
     */
    public int row;
    /**
     * column count
     */
    public int col;
    /**
     * content
     */
    public String content[];
    /**
     * the table of pingze for each character
     */
    public int pingzeTable[][];
    /**
     * size of result sets when searching in baidu
     */
    public long popularity;
    
    /**
     * a score given the poem's popularity
     */
    public int popularityScore;
    
    /**
     * constructor
     * @param ti title
     * @param au author
     * @param po popularity
     * @param lines lines
     */
    public Poem(String ti, String au, long po, List<String> lines) {
	title = ti;
	author = au;
	row = lines.size();
	col = lines.get(0).length();
	popularity = po;
	assert (row == 4 || row == 8);
	assert (col == 5 || col == 7);
	content = new String[row];
	for (int i = 0; i < lines.size(); i++) {
	    assert (lines.get(i).length() == col);
	    content[i] = lines.get(i);
	}

	pingzeTable = new int[row][col];
	for (int i = 0; i < row; i++) {
	    for (int j = 0; j < col; j++) {
		pingzeTable[i][j] = TonalManager.getInstance().getPingzeInfo(
			lines.get(i).charAt(j));
	    }
	}
    }
}