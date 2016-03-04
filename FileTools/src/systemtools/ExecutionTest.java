package systemtools;

import java.io.InputStream;

public class ExecutionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		String command = "ls /Users/mjl3p";
		String output = null;
		
		String[] commands = new String[3];
		
		commands[0] = "/bin/sh";
		commands[1] = "-c";
		commands[2] = "/Users/mjl3p/samtools-0.1.7_i386-darwin/samtools view -H ~/exome_project/NA12878.bam > ~/test/bamheader.txt";
		
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
		int exitVal = 0;
		
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
			
//			exitVal = proc.waitFor();
			
			output = sb.toString();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		System.out.println(output.isEmpty());
		

	}

}
