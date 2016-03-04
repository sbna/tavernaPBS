package clusterV3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LogReadTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String fileName = "/Users/mjl3p/.taverna-2.1.2/logs/gatkTestnoBlock.log";
		
		ArrayList<String> jobIDs = new ArrayList<String>();
		
		try {
		
			File file = new File(fileName);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			
			while (br.ready()) {
				String line = br.readLine();
				
				String find = "PBS ID: ";
				
				if (line.contains(find)) {
					jobIDs.add(line.substring(8));
				}
				
			}
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < jobIDs.size(); i++) {
			System.out.println(jobIDs.get(i));
		}

	}

}
