package viewerV1;

import java.io.File;
import java.io.InputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;


public class TestSSH {

	static String user = "pds3k";
	static String clusterHost = "lc4.itc.virginia.edu";
	
	// adapted for ssh
	public static int execute(String command) {
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		int exitVal = 0;
		
		String[] commands = null;
			
		
			commands = new String[3];
			
			commands[0] = "ssh";
			commands[1] = user + '@' + clusterHost;
			commands[2] = command;
	
		try {
			proc = rt.exec(commands);
			
			exitVal = proc.waitFor();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		

		return exitVal;
	}
	
	//adapted for ssh
	public static String executeOut(String command) {
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
		
		String[] commands = null;
		
		String result = null;
			
			commands = new String[3];
			
			commands[0] = "ssh";
			commands[1] = user + '@' + clusterHost;
			commands[2] = command;
		
		
		try {
			proc = rt.exec(commands);
			
			// Get the input stream and read from it
			InputStream in = proc.getInputStream();

			int c;
			StringBuffer sb = new StringBuffer();
			while ((c = in.read()) != -1) {
				sb.append((char) c);
			}
			in.close();
			result = sb.toString();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	public static void main(String args[]){
		//Main method for testing
	String location = null;
	if(location == null){
		location = "empty";
	}
		while(!location.endsWith("Monitor.jar")){
			System.out.println("Not the correct file");
			//Bring up a file browser to select the monitor location
			JFileChooser fc = new JFileChooser();
	    	int returnVal = fc.showOpenDialog(new JFrame());
	    	if(returnVal == JFileChooser.CANCEL_OPTION){
	    		break;
	    	}
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	    	    location = file.getAbsolutePath();
	        }
		}
	}
}
