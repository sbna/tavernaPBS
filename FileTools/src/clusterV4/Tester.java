package clusterV4;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import systemtools.GetFiles;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		
		PBS head = new PBS();
		
//		head.startLog(logName);
		
//		String result = PBS.executeOut("cat /home/mjl3p/.taverna-2.1.2/logs/gatkCreateTest.log");
		
//		String jobID = head.getJobID("big412.sh");
	
//		System.out.println(result);
		
/*		String fileName = "/home/mjl3p/.taverna-2.1.2/logs/gatkCreateTest.log";
		BufferedReader br = null;
		
		try {
			String fileContents = PBS.executeOut("cat " + fileName);
		
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileContents.getBytes())));
			
			while (br.ready()) {
				String line = br.readLine();
				
				String find = "PBS ID: ";
				
				if (line.contains(find)) {
					System.out.println(line.substring(8));
				}
			}
			
			br.close();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		} */
		
//		String echo = "echo -e  \"\\$PBS_O_WORKDIR\" >> ";
		
//		System.out.println(echo);
		
		//List files = GetFiles.list("/net/cphg-nextgen1/cphg-nextgen1/projects/SWISS/reads/", "_align.bam");
		
		//for (int i = 0; i < files.size(); i++) {
		//	System.out.println(files.get(i));
		//}
		
		
		
		

	}


}
