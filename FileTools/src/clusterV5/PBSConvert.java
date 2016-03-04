package clusterV5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

public class PBSConvert {
	
/*	public static String fromPBS(PBS thing) {
		
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
	} */
	
	public static String fromPBS(PBS thing) {
		
		// save any updated config parameters
		if (thing.updatedConfig()) {
			try {
				thing.saveConfig();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		ByteArrayOutputStream byter = null;
		ObjectOutputStream out = null;
		
		try {
			byter = new ByteArrayOutputStream();
			out = new ObjectOutputStream(byter);
			out.writeObject(thing);
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return byter.toString();
	}
	
/*	public static PBS toPBS(String pbsFile) {
		
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
	} */
	
	public static PBS toPBS(String pbsString) {
		
		PBS thing = null;
		ByteArrayInputStream byter = null;
		ObjectInputStream in = null;
		
		try {
			byter = new ByteArrayInputStream(pbsString.getBytes());
			in = new ObjectInputStream(byter);
			thing = (PBS)in.readObject();
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (!Thread.currentThread().getName().equals("main")) {
		
			thing.setNodeName(nodeNameParser());
			thing.setNestedNode(nestNodeNameParser());
			
		}
		
//		thing.setNodeName(Thread.currentThread().getName());
		
		return thing;
	}
	
	private static String nodeNameParser() {
		
		String reverse = new StringBuffer(Thread.currentThread().getName()).reverse().toString();
		
		String nodeName = new StringBuffer(reverse.substring(reverse.indexOf("[") + 1, reverse.indexOf(":"))).reverse().toString();
		
		return nodeName;
		
	}
	
	
	// this function will extract the name of the nest node
	// only detects one level of nesting
	
	private static String nestNodeNameParser() {
		
		String[] threadFields = Thread.currentThread().getName().split(":");
		
		if (threadFields.length > 3) {
			return threadFields[2];
		}
		else {
			return null;
		}
		
	}

}
