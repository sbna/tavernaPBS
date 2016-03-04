package clusterV3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import existence.CreateFile;

public class PBS implements Serializable{
	
	// default parameters for submitting a job
	private int nodes = 1;
	private int processors = 1;
	private String memory = "4GB";
	private int wallTime = 48;	
	private String destination = "cphg";
	private String group = "CPHG";
	
	private int numPBS;
	private static int totalPBS = 0;
	
	private ArrayList<String> prevJobIDs;
	
	private static ArrayList<String> allJobIDs;
	
	// set of commands that get executed at the end of the workflow (clean up commands and such)
	private ArrayList<String> delayedCommands;
	
	// whether to use blocking or not 
	private boolean block;
	
	private static PrintWriter log = null; // log file for jobs
//	private static File file;
	
	private String fileName;
	private String logName;
	
	private String inputFile;
	private String outputFile;
	
	private int create;
	
	// default constructor
	public PBS() {
		
		prevJobIDs = null;
//		log = null;
		delayedCommands = null;
		
		block = true;
		
		fileName = null;
		logName = null;
		
		inputFile = null;
		outputFile = null;
		
		create = 1;
		
		numPBS = 0;
		
		
	}
	
	public PBS clone() {
		
		PBS clone = new PBS();
		
		clone.nodes = this.nodes;
		clone.processors = this.processors;
		clone.memory = this.memory;
		clone.wallTime = this.wallTime;
		clone.destination = this.destination;
		clone.group = this.group;
		
		totalPBS++;
		
		clone.numPBS = totalPBS;
		
		// copy over job IDs
		if (prevJobIDs == null) {
			clone.prevJobIDs = this.prevJobIDs;
		}
		else {
			clone.prevJobIDs = new ArrayList<String>();
			
			for (int i = 0; i < this.prevJobIDs.size(); i++) {
				clone.prevJobIDs.add(this.prevJobIDs.get(i));
			}
		}
		
		clone.delayedCommands = this.delayedCommands;
		
//		clone.log = this.log;
		clone.block = this.block;
		
		clone.fileName = this.fileName;
		clone.logName = this.logName;
		
		
		return clone;
	}
	
	// create the log file with the given filename (maybe also allow for a default name)
/*	public void startLog(String fileName) {
		
		String location = System.getProperty("user.home") + "/.taverna-2.1.2/logs/";
		
		file = new File(location + fileName);
		
		try {
			log = new PrintWriter(new FileWriter(file));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	*/
	
	public void startLog(String logName) {
		
		String location = System.getProperty("user.home") + "/.taverna-2.1.2/logs/";
		
		File file = new File(location + logName);
		
		this.fileName = file.getAbsolutePath();
		
		FileOutputStream out = null;
		
		try {
			out = new FileOutputStream(file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			out.close();
		}
		catch (IOException e){
			e.printStackTrace();
			
		}
		
		this.logName = logName;
	}
	

	
/*	public void printlnLog(String echo) {
		if (log != null) {
			log.print(echo + "\n");
		}
	} */
	
	public void printlnLog(String echo) {
		
		if (fileName != null) {
			
			File outFile = new File(fileName);
			
			FileOutputStream outStream = null;
			
			try {
				outStream = new FileOutputStream(outFile, true);
				
				outStream.write(echo.getBytes());
				
				outStream.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void blockOn() {
		block = true;
	}
	
	public void blockOff() {
		block = false;
	}
	
	public int createFile(String inputFile, String outputFile) {
		this.create = CreateFile.test(inputFile, outputFile);
		
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		
		return this.create;
		
	}
	
	// submit a command to the cluster using the default parameters
	// checks for stored jobIDs, inserts a new one
	public void submitScript(String command) {
		
		// create a command array
		ArrayList<String> commands = new ArrayList<String>();
		
		commands.add(command);
		
		this.submitScript(commands, this.nodes, this.processors, this.memory);

	}
	// same as before but with parameters
	public void submitScript(String command, int nodes, int processors, String memory) {
		
		// create a command array
		ArrayList<String> commands = new ArrayList<String>();
		
		commands.add(command);
		
		this.submitScript(commands, nodes, processors, memory);
		
	}
	
	// submit multiple commands in the same script
	public void submitScript(ArrayList<String> commands) {
		
		this.submitScript(commands, this.nodes, this.processors, this.memory);
		
	}
	// same as before but with parameters
	public void submitScript(ArrayList<String> commands, int nodes, int processors, String memory) {
		
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
//		Random rand = new Random();		
		
//		String jobName = "job" + rand.nextInt(10000000);
		String jobName = hasher(commands);
		
		// check to see if job is running
		if (jobRunning(jobName)) {
			
			// if that is the case, add to previous jobs list
			if (this.prevJobIDs == null) {
				this.prevJobIDs = new ArrayList<String>();
			}
			
			String prevJobID = getJobID(jobName);
			
			if (prevJobID != "N/A") {
				this.prevJobIDs.add(prevJobID);
			}
			
			return;
		}
		
		// if no "creation" needed, skip job and log it
		
		if (this.create == 0) {
			StringBuilder buff = new StringBuilder();
			
			
			buff.append("Job Name: SKIPPED\n");
			
			for (int c = 0; c < commands.size(); c++) {
				buff.append("Command: " + commands.get(c) + "\n");
			}
			if (this.inputFile != null) {
				buff.append("Input: " + this.inputFile + "\n");
			}
			if (this.outputFile != null) {
				buff.append("Output: " + this.outputFile + "\n");
			}
			buff.append("PBS ID: N/A\n");
			buff.append("@\n");
			
			String logString = buff.toString();
			
			printlnLog(logString);
			
			this.create = 1;
			this.inputFile = null;
			this.outputFile = null;
			
			return;
		}
		
		String location = System.getProperty("user.home") + "/.taverna-2.1.2/logs/";		
		File script = new File(location + jobName + ".sh");
		PrintWriter out = null;
				
		try {
			out = new PrintWriter(new FileWriter(script));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		out.println("#!/bin/sh");
		out.println("#PBS -q " + destination);
		
		out.println("#PBS -W group_list=" + group);
		
		// deal with that whole block situation
		if (block) {
			out.println("#PBS -W block=true");
		}
		// list any job dependencies if they are running
		else {
			if (prevJobIDs != null) {
				for (int i = 0; i < prevJobIDs.size(); i++) {
					if (jobRunning(prevJobIDs.get(i))) {
						out.println("#PBS -W depend=afterok:" + prevJobIDs.get(i));
					}
				}
			}

		}
		out.println("#PBS -V");
		out.println("#PBS -l select=" + nodes + ":mem=" + memory + ":ncpus=" + processors);
		out.println("#PBS -N " + jobName);
		out.println("#PBS -o " + location + jobName + ".out");
		out.println("#PBS -e " + location + jobName + ".err");
		out.println();
		out.println("cd $PBS_O_WORKDIR");
		
		for (int c = 0; c < commands.size(); c++) {
			out.println(commands.get(c));
		}
		
		out.close();
		
		String qsubString = "qsub " + script.getAbsolutePath();
		
		String newJobID = null;
		
		try {
			proc = rt.exec(qsubString);
			
			// Get the input stream and read from it
			InputStream in = proc.getInputStream();

			int c;
			StringBuffer sb = new StringBuffer();
			while ((c = in.read()) != -1) {
				sb.append((char) c);
			}
			in.close();
			newJobID = sb.toString();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		 * 
		 * Need some form of error check here that qsub submitted properly
		 * 
		 */
		
		// chop off the ".lc4" part
		int loc = newJobID.indexOf('.');
		
		String jobID = newJobID.substring(0, loc);
		
		
		// empty out old jobIDs and save the new one
		
		if (!block) {
		
			if (prevJobIDs == null) {
				prevJobIDs = new ArrayList<String>();
			}
			else {
				prevJobIDs.clear();
			}
		
			prevJobIDs.add(jobID);
		}
		
		
		// log what happened
		// WARNING: Might have an issue here with concurrency
//		printlnLog("Job Name: " + jobName);
//		for (int c = 0; c < commands.size(); c++) {
//			printlnLog("Command: " + commands.get(c));
//		}
//		printlnLog("PBS ID: " + newJobID);
//		printlnLog("@"); // delimiter
		
		// lets build a string!
		
		StringBuilder buff = new StringBuilder();
		
		
		buff.append("Job Name: " + jobName + "\n");
		
		for (int c = 0; c < commands.size(); c++) {
			buff.append("Command: " + commands.get(c) + "\n");
		}
		if (this.inputFile != null) {
			buff.append("Input: " + this.inputFile + "\n");
		}
		if (this.outputFile != null) {
			buff.append("Output: " + this.outputFile + "\n");
		}
		buff.append("PBS ID: " + newJobID.trim() + "\n");
		buff.append("@\n");
		
		String logString = buff.toString();
		
		printlnLog(logString);
		
		this.inputFile = null;
		this.outputFile = null;
		
		
	}

	
	// returns a hash string for qsub use
	public String hasher(ArrayList<String> commands) {
		
		StringBuilder buff = new StringBuilder();
		
		for (int i = 0; i < commands.size();i++) {
			buff.append(commands.get(i));
		}
		
		String total = buff.toString();
		
		String hash = "J" + total.hashCode();
		
		if (hash.length() > 10) {
			return hash.substring(0, 10);
		}
		else {
			return hash;
		}
		
		
		
	}
	
	// checks if a job is currently on the queue
	// NOTE: this can either be a job ID or a job name
	public boolean jobRunning(String jobName) {
		
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
		
		// lets try this with a list
		String[] commands = new String[3];
		
		commands[0] = "/bin/sh";
		commands[1] = "-c";
		commands[2] = "qstat | grep " + jobName;
		
		String result = null;
		
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
		catch (Exception e){
			e.printStackTrace();
		}
		
		if (result.isEmpty()) {
			return false;
		}
		else {
			return true;
		}
		
	}
	
	// given the job name, extract the jobID from qstat (without the .lc4)
	
	public String getJobID(String jobName) {
		
		String jobID = null;
		
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
		
		// lets try this with a list
		String[] commands = new String[3];
		
		commands[0] = "/bin/sh";
		commands[1] = "-c";
		commands[2] = "qstat | grep " + jobName;
		
		String result = null;
		
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
		catch (Exception e){
			e.printStackTrace();
		}
		
		if (result.isEmpty()) {
			jobID = "N/A";
		}
		else {
			int index = result.indexOf('.');
			
			jobID = result.substring(0, index);
		}
		
		
		return jobID;
	}
	
	
	// wait for all jobs to finish execution
	// creates a job ID that waits on all running jobs (previous jobs)
	// should be used in a "merge node"
	public int jobWait() {
		
		if (prevJobIDs == null) {
			return 0;
		}
		
		if (prevJobIDs.isEmpty()) {
			return 0;
		}
		
		
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
		
				
		String jobName;
		
		if (logName == null) {
			jobName = "WAIT";
		}
		else if (logName.length() > 6) {
			jobName = "WAIT" + logName.substring(0, 6);
		}
		else {
			jobName = "WAIT" + logName;
		}
		
		String location = System.getProperty("user.home") + "/.taverna-2.1.2/logs/";		
		File script = new File(location + jobName + ".sh");
		PrintWriter out = null;
				
		try {
			out = new PrintWriter(new FileWriter(script));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		out.println("#!/bin/sh");
		out.println("#PBS -q " + destination);		
		out.println("#PBS -W group_list=" + group);
		out.println("#PBS -W block=true");
		// print out all dependencies
		if (prevJobIDs != null) {
			for (int i = 0; i < prevJobIDs.size(); i++) {
				if (jobRunning(prevJobIDs.get(i))) {
					out.println("#PBS -W depend=afterok:" + prevJobIDs.get(i));
				}
			}
		}
		out.println("#PBS -V");
		out.println("#PBS -l select=1:mem=1GB:ncpus=1");
		out.println("#PBS -N " + jobName);
		out.println("#PBS -o " + location + jobName + ".out");
		out.println("#PBS -e " + location + jobName + ".err");
		out.println();
		out.println("cd $PBS_O_WORKDIR");
		// essentially a dummy command
		out.println("echo \"Done!\"");
		
		out.close();
		
		String qsubString = "qsub " + script.getAbsolutePath();
		
		int exitVal = 0;
		
		try {
			proc = rt.exec(qsubString);
			
			exitVal = proc.waitFor();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (prevJobIDs != null) {
			prevJobIDs.clear();
		}
		
		// do i clear the total job IDs?
		// add this one?
		
		prevJobIDs = null;
		
		
		
		return exitVal;
		
	}
	
	
	
	public static int execute(String command) {
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		int exitVal = 0;
		
		String[] commands = new String[3];
		
		commands[0] = "/bin/sh";
		commands[1] = "-c";
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
	
	public void justLogIt(String command) {
		
		String jobName = "jobName.sh";
		String newJobID = "jobid.lc4";
		
		StringBuilder buff = new StringBuilder();
		
		buff.append("Job Name: " + jobName + "\n");
		
		buff.append("Command: " + command + "\n");
		
		buff.append("PBS ID: " + newJobID + "\n");
		buff.append("@\n");
		
		String logString = buff.toString();
		
		printlnLog(logString);
	}

	// merge a different PBS object with the current one
	// important for having more than one job ID
	public void merge(PBS other) {
		
		/*
		 * Other information to transfer over?
		 */
		
		// transfer jobs over
		if (this.prevJobIDs == null) {
			this.prevJobIDs = new ArrayList<String>();
		}
		
		if (other.prevJobIDs != null) {
			for (int i = 0; i < other.prevJobIDs.size(); i++) {
				this.prevJobIDs.add(other.prevJobIDs.get(i));
			}
		}
		
		// transfer delayed jobs over
		if (this.delayedCommands == null) {
			this.delayedCommands = new ArrayList<String>();
		}
		
		if (other.delayedCommands != null) {
			for (int i = 0; i < other.delayedCommands.size(); i++) {
				this.delayedCommands.add(other.delayedCommands.get(i));
			}
		}
		
	}
	
	public void delayJob(String command) {
		
		if (this.delayedCommands == null) {
			this.delayedCommands = new ArrayList<String>();
		}
		
		this.delayedCommands.add(command);
	}
	
	public void skipJob(String command) {
		
		printlnLog("Skipped Job: " + command + "\n");
		
	}
	
	// WARNING: This should only be called AFTER all cluster jobs have run
	
	public boolean cleanUpJobs() {
		
		if (this.prevJobIDs != null) {
			return false;
		}
		
		printlnLog("Delayed Jobs:\n");
		
		for (int i = 0; i < this.delayedCommands.size(); i++) {
			
			
			int exitVal = 0;
			
			exitVal = execute(delayedCommands.get(i));
			
			if (exitVal == 0) {
				printlnLog("Executed Successfully\n"); 
			}
			else {
				printlnLog("Error in execution. Code: " + exitVal + "\n");
			}
			
		}
			
		return true;
	}
	

	// delete all serializations (necessary? safe? good idea?)
	public void cleanUpSer() {
		
	}
	
	public ArrayList<String> getAllJobIDs() {
		
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
		
		return jobIDs;
	}
	
	// if something goes wrong, kill all the jobs
	
	public void killAllJobs() {
		
		ArrayList<String> jobIDs = getAllJobIDs();
		
		if (jobIDs.size() == 0) {
			return;
		}
		
		for (int i = 0; i < jobIDs.size(); i++) {
			execute("qdel " + jobIDs.get(i));
		}
		
	}
	
	/*
	 * 
	 * Getters and Setters
	 * 
	 */

	public void setNodes(int nodes) {
		this.nodes = nodes;
	}

	public int getNodes() {
		return nodes;
	}

	public void setProcessors(int processors) {
		this.processors = processors;
	}

	public int getProcessors() {
		return processors;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getMemory() {
		return memory;
	}


	public int getNumPBS() {
		return numPBS;
	}
	
	public int getTotalPBS() {
		return totalPBS;
	}
	

}
