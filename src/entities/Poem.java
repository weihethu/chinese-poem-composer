package entities;

import java.util.List;
import managers.TonalManager;

public class Poem {
    public String author, title;
    public int row, col;
    public String content[];
    public int pingzeTable[][];

    public Poem(String ti, String au, List<String> lines) {
	title = ti;
	author = au;
	row = lines.size();
	col = lines.get(0).length();
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