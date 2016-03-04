package tavernaTests;

import tavernaPBS.*;

public class BasicFileTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		/*
		 * Initialization
		 */
		
		PBS head = new PBS();
		
		head.setUser("mjl3p");
		
		String logName = "basicFileTest.log";
		
		// start the log
		try {
			head.startLog(logName);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		head.blockOff();
		head.cphgDefault();
		
		String outPBS = PBSConvert.fromPBS(head);
		
		/*
		 * WorkNode1
		 */
		
		String inputFile = "/home/mjl3p/tavernaTest.txt";
		
		PBS in1 = PBSConvert.toPBS(outPBS); // recreate PBS object
		 
		// the name of the output file
		String output = inputFile.replace(".txt", "_out.txt");
		 
		// the command to be executed
		String command = "cp" + " " + inputFile + " " + output;
		 
		// create the job
		Job job1 = new Job(command);
		job1.addInput(inputFile);
		job1.addOutput(output);
		 
		// do the job in PBS
		try {
			in1.doJob(job1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		 
		String outPBS1 = PBSConvert.fromPBS(in1);
		
		/*
		 * WorkNode2
		 */
		
		PBS in2 = PBSConvert.toPBS(outPBS1); // recreate PBS object
		 
		// extract the file name and the directory
		String fileName = Extract.getFile(output);
		String directory = Extract.getDirectory(output);
		 
		// create a new subdirectory
		String subDirectory = directory + "tavernaTest/";
		 
		// create output
		String outputCopy = subDirectory + fileName;
		 
		String command1 = "mkdir -p " + subDirectory;
		String command2 = "cp " + output + " " + outputCopy;
		 
		// create the job
		Job job = new Job(command1);
		job.addCommand(command2);
		job.addInput(output);
		job.addOutput(outputCopy);
		 
		// do the job
		try {
			in2.doJob(job);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		 
		String outPBS2 = PBSConvert.fromPBS(in2);
		
		/*
		 * Wait Node
		 */
		
		PBS wait = PBSConvert.toPBS(outPBS2);
		
		try {
			wait.jobWait();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Done with Execution!");

	}

}
