package systemtools;

import java.io.File;

public class Move {
	
	public static String createDir(String file, String newDir) {
		
		String newLocation = null;
		
		File directory = new File(newDir);
		
		directory.mkdir();
		
		File Temp = new File(file);
		
		if (Temp.exists() != true) {
			newLocation = "DNE";
			
			return newLocation;
		}
		
		Temp.renameTo(new File(directory, Temp.getName()));
		
		newLocation = Temp.getAbsolutePath();
		
		return newLocation;
	}

}
