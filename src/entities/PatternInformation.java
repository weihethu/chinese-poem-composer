package entities;

import java.util.List;

public class PatternInformation {
	public int row, col;
	/**
	 * 0, don't care, 1, ze, 2, ping
	 */
	public int tonals[][] = null;
	public boolean ruyun[] = null;

	public PatternInformation(List<String> tonalStrs) {
		row = tonalStrs.size();
		col = tonalStrs.get(0).endsWith("x") ? tonalStrs.get(0).length() - 1 : tonalStrs.get(0).length();
		assert (row == 4 || row == 8);
		assert (col == 5 || col == 7);
		tonals = new int[row][col];
		ruyun = new boolean[row];
		for (int i = 0; i < row; i++) {
			String str = tonalStrs.get(i);
			assert ((str.length() == col) || (str.length() == col + 1 && str.endsWith("x")));
			ruyun[i] = str.endsWith("x");
			for (int j = 0; j < col; j++) {
				char c = tonalStrs.get(i).charAt(j);
				switch (c) {
				case 'Æ½':
					tonals[i][j] = 2;
					break;
				case 'ØÆ':
					tonals[i][j] = 1;
					break;
				case '¡Ñ':
					tonals[i][j] = 0;
					break;
				default:
					assert (false);
				}
			}
		}
	}
}