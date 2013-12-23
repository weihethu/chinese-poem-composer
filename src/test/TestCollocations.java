package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import entities.Collocation;

public class TestCollocations {

    /**
     * @param args
     */
    public static void main(String[] args) {
	try {
	    BufferedReader br = new BufferedReader(new FileReader(new File(
		    "collocation3.txt")));
	    String line;
	    List<Collocation> collos = new ArrayList<Collocation>();
	    while ((line = br.readLine()) != null) {
		line = line.trim();
		if (line.isEmpty())
		    continue;
		String[] parts = line.split("\\s+");
		collos.add(new Collocation(parts[0], parts[1], Double
			.parseDouble(parts[2])));
	    }
	    br.close();
	    Collections.sort(collos, new Comparator<Collocation>() {

		@Override
		public int compare(Collocation arg0, Collocation arg1) {
		    return Double.compare(arg1.t_value, arg0.t_value);
		}

	    });
	    for (int i = 0; i < 50; i++) {
		System.out
			.println(collos.get(i).token + "->"
				+ collos.get(i).pairToken + ":"
				+ collos.get(i).t_value);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
