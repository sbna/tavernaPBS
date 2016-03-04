package systemtools;

import clusterV5.PBS;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GetFiles {
	
	
	public static ArrayList<String> list(String directory, String extension) throws IOException{
		
		ArrayList<String> files = new ArrayList<String>();
		
		String command = "ls " + directory;
		
		PBS temp = new PBS();
		
		String result = temp.executeOut(command);
		
		try {
		
			BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.getBytes())));
		
			while (br.ready()) {
				String line = br.readLine();
			
				int extLen = extension.length();
				
				String currExtension = line.substring(line.length()-extLen);
				
				if (currExtension.equals(extension)) {
					files.add(directory + line);
				}
			
			
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return files;
		
	}
	
	public static ArrayList<String> list(String directory, String extension, String userName) throws IOException{
		
		ArrayList<String> files = new ArrayList<String>();
		
		String command = "ls " + directory;
		
		PBS temp = new PBS();
		temp.setUserName(userName);
		
		String result = temp.executeOut(command);
		
		try {
		
			BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.getBytes())));
		
			while (br.ready()) {
				String line = br.readLine();
			
				int extLen = extension.length();
				
				String currExtension = line.substring(line.length()-extLen);
				
				if (currExtension.equals(extension)) {
					files.add(directory + line);
				}
			
			
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return files;
		
		
	}

}
