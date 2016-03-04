package cluster;

import java.awt.List;
import java.io.File;
import java.util.ArrayList;

import systemtools.Move;


public class TestPBS {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
//		String submit = "/h3/t1/users/ajm6q/bin/samtools index /net/cphg-nextgen1/cphg-nextgen1/projects/mouse454/markers.bam";
		
//		PBS.setNodes(2);
		
//		int nodes = PBS.getNodes();
		
//		System.out.println(nodes);
		
//		int nodes = 2;
//		int processors = 2;
//		String memory = "8GB";
		
//		PBS.submitScript(submit);
		
//		ArrayList<String> test = new ArrayList<String>();
		
		String file = "/Users/mjl3p/test/dummy.txt";
		String newDir = "/Users/mjl3p/test/dummy";
		
		String newLocation = Move.createDir(file, newDir);
		
		System.out.println(newLocation);
		
	}

}