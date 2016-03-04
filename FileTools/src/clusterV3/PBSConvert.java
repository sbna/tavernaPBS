package clusterV3;

import java.io.File;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class PBSConvert {
	
	public static String toSerString(PBS thing) {
		
		String location = System.getProperty("user.home") + "/.taverna-2.1.2/";
		
		String filename = location + "pbs" + thing.getNumPBS() + ".ser";
		
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(thing);
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return filename;
	}
	
	public static PBS toPBS(String pbsFile) {
		
		PBS thing = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		
		try {
			fis = new FileInputStream(pbsFile);
			in = new ObjectInputStream(fis);
			thing = (PBS)in.readObject();
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// get rid of the file
//		File temp = new File(pbsFile);
//		temp.delete();
		
		return thing;
	}

}
