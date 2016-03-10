package tavernaPBS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import biz.source_code.base64Coder.Base64Coder;

public class PBSConvert {
	
	// convert a PBS object into a String
	public static String fromPBS(PBS thing) {
	
		// if no connection has been made, attempt one
		if (thing.getRemoteRootDirectory() == null) {
			try {
				thing.startLog();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
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
		
		// return byter.toString();
		// return Base64.getEncoder().encodeToString(byter.toByteArray()); // java 1.8
		return new String(Base64Coder.encode(byter.toByteArray()));
	}
	
	// convert a String into a PBS object
	public static PBS toPBS(String pbsString) {
		
		PBS thing = null;
		ByteArrayInputStream byter = null;
		ObjectInputStream in = null;
		
		try {
	        byte[] data = Base64Coder.decode(pbsString);
	        byter = new ByteArrayInputStream(data);
	        in = new ObjectInputStream(byter);
	        thing  = (PBS)in.readObject();
	        in.close();
			//byter = new ByteArrayInputStream(pbsString.getBytes());
			//in = new ObjectInputStream(byter);
			//thing = (PBS)in.readObject();
			//in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (!Thread.currentThread().getName().equals("main")) {
		
			thing.setNodeName(nodeNameParser());
			thing.setNestedNode(nestNodeNameParser());
			
		}
		
		return thing;
	}
	
	// parse the threadname to extract the node name
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
