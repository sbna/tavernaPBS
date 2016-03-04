package clusterV4;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
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
	
	
	// set of commands that get executed at the end of the workflow (clean up commands and such)
	private ArrayList<String> delayedCommands;
	
	// whether to use blocking or not 
	private boolean block;
	
//	private static File file;
	
	private String fileName;
	private String logName;
	
	private String inputFile;
	private String outputFile;	
	private int create;
	
	private static String clusterHost = "lc4.itc.Virginia.EDU";
	
	
	
	// default constructor
	// adapted for ssh
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
		
		// if we are not running on elder, need to ensure that a local taverna folder exists
		
		if (!onCluster()) {
			String location = System.getProperty("user.home") + "/.taverna-2.1.2/";
		
			File directory = new File(location);
		
			directory.mkdir();
		}
		
		
	}
	
	public synchronized PBS clone() {
		
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
	
	public static boolean onCluster() {
		String hostName = null;
		
		try {
			InetAddress addr = InetAddress.getLocalHost();
			
			hostName = addr.getHostName();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (hostName.equals(clusterHost)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// adapted for ssh
	public void startLog(String logName) {
		
		this.logName = logName;
		
		// if we are on cluster, use java tools to create log file
		if (onCluster()) {
		
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
		}
		//otherwise remove and touch
		else {
			
			String location = ".taverna-2.1.2/logs/";
			
			this.fileName = location + logName;
			
			execute("rm -f " + this.fileName);
			execute("touch " + this.fileName);
			
		}
		
		
	}
	

	// adapted for ssh
	public synchronized void printlnLog(String echo) {
		
		
		if (onCluster()) {
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
		else {
			if (fileName != null) {
				execute("echo -e \"" + echo + "\" >> " + fileName);
			}
		}
		
	}
	
	public void blockOn() {
		block = true;
	}
	
	public void blockOff() {
		block = false;
	}
	
	
	// adapted for ssh
	public int createFile(String inputFile, String outputFile) {
		
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		
		if (onCluster()) {
			this.create = CreateFile.test(inputFile, outputFile);
		}
		else {
			
			String result1 = executeOut("date +%s -r " + inputFile);
			String result2 = executeOut("date +%s -r " + outputFile);
			
			if (result1.isEmpty()) {
				this.create = -1;
			}
			else if(result2.isEmpty()) {
				this.create = 1;
			}
			else {
				int inTime = Integer.parseInt(result1.trim());
				int outTime = Integer.parseInt(result2.trim());
				
				if (inTime > outTime) {
					this.create = 1;
				}
				else {
					this.create = 0;
				}
			}
			
		}
		
		return this.create;
		
	}
	
	// adapted for ssh
	public int createTest(String inputFile, String outputFile) {
		
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		
		if (onCluster()) {
			this.create = CreateFile.test(inputFile, outputFile);
		}
		else {
			
			String result1 = executeOut("date +%s -r " + inputFile);
			String result2 = executeOut("date +%s -r " + outputFile);
			
			if (result1.isEmpty()) {
				this.create = -1;
			}
			else if(result2.isEmpty()) {
				this.create = 1;
			}
			else {
				int inTime = Integer.parseInt(result1.trim());
				int outTime = Integer.parseInt(result2.trim());
				
				if (inTime > outTime) {
					this.create = 1;
				}
				else {
					this.create = 0;
				}
			}
			
		}
		
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
//		Process proc = null;
//		Runtime rt = Runtime.getRuntime();
		
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
		
		String scriptFile = null;
		
		// if on cluster, create script file locally
		if (onCluster()) {
		
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
			
			scriptFile = script.getAbsolutePath();
		}
		
		// else create script on cluster
		else {
			
			String location = ".taverna-2.1.2/logs/";
			scriptFile = location + jobName + ".sh";
			
			// delete what was there
			execute("rm -f " + scriptFile);
			// create the new file
			execute("touch " + scriptFile);
			
			// write to the shell script
			execute("echo -e \"#!/bin/sh\" >> " + scriptFile);
			execute("echo -e \"#PBS -q " + destination + "\" >> " + scriptFile);
		
			execute("echo -e \"#PBS -W group_list=" + group + "\" >> " + scriptFile);
		
			// deal with that whole block situation
			if (block) {
				execute("echo -e \"#PBS -W block=true\" >> " + scriptFile);
			}
			// list any job dependencies if they are running
			else {
				if (prevJobIDs != null) {
					for (int i = 0; i < prevJobIDs.size(); i++) {
						if (jobRunning(prevJobIDs.get(i))) {
							execute("echo -e \"#PBS -W depend=afterok:" + prevJobIDs.get(i) + "\" >> " + scriptFile);
						}
					}
				}

			}
			execute("echo -e \"#PBS -V\" >> " + scriptFile);
			execute("echo -e \"#PBS -l select=" + nodes + ":mem=" + memory + ":ncpus=" + processors + "\" >> " + scriptFile);
			execute("echo -e \"#PBS -N " + jobName + "\" >> " + scriptFile);
			execute("echo -e \"#PBS -o " + location + jobName + ".out\" >> " + scriptFile);
			execute("echo -e \"#PBS -e " + location + jobName + ".err\" >> " + scriptFile);
			execute("echo -e \" \" >> " + scriptFile);
			execute("echo -e  \"\\$PBS_O_WORKDIR\" >> " + scriptFile);
		
			for (int c = 0; c < commands.size(); c++) {
				execute("echo -e \"" + commands.get(c) + "\" >> " + scriptFile);
			}
			
		}
		
		String qsubString = "/opt/pbs/10.0.0.82981/bin/qsub " + scriptFile;
		
		String newJobID = executeOut(qsubString);
		
	
		
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
	// no ssh needed
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
	
	// adapted for ssh
	public boolean jobRunning(String jobName) {
		
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
		
		// lets try this with a list
		String[] commands = new String[3];
		
		if (onCluster()) {
			commands[0] = "/bin/sh";
			commands[1] = "-c";
			commands[2] = "qstat | grep " + jobName;
		}
		else {
			commands[0] = "ssh";
			commands[1] = clusterHost;
			commands[2] = "/opt/pbs/10.0.0.82981/bin/qstat | grep " + jobName;
		}
		
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
	
	// adapted for ssh
	public String getJobID(String jobName) {
		
		String jobID = null;
		
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
		
		// lets try this with a list
		String[] commands = new String[3];
		
		if (onCluster()) {
			commands[0] = "/bin/sh";
			commands[1] = "-c";
			commands[2] = "qstat | grep " + jobName;
		}
		else {
			commands[0] = "ssh";
			commands[1] = clusterHost;
			commands[2] = "/opt/pbs/10.0.0.82981/bin/qstat | grep " + jobName;
		}
		
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
//		Process proc = null;
//		Runtime rt = Runtime.getRuntime();
		
		
				
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
		
		String scriptFile = null;
		
		// if we are on cluster
		if (onCluster()) {
		
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
			
			scriptFile = script.getAbsolutePath();
		}
		// else remotely do things through ssh
		else {
			String location = ".taverna-2.1.2/logs/";
			scriptFile = location + jobName + ".sh";
			
			// delete what was there
			execute("rm -f " + scriptFile);
			// create the new file
			execute("touch " + scriptFile);
			
			// write to the shell script
			execute("echo -e \"#!/bin/sh\" >> " + scriptFile);
			execute("echo -e \"#PBS -q " + destination + "\" >> " + scriptFile);
		
			execute("echo -e \"#PBS -W group_list=" + group + "\" >> " + scriptFile);
		
			execute("echo -e \"#PBS -W block=true\" >> " + scriptFile);
			
			if (prevJobIDs != null) {
				for (int i = 0; i < prevJobIDs.size(); i++) {
					if (jobRunning(prevJobIDs.get(i))) {
						execute("echo -e \"#PBS -W depend=afterok:" + prevJobIDs.get(i) + "\" >> " + scriptFile);
					}
				}
			}

			
			execute("echo -e \"#PBS -V\" >> " + scriptFile);
			execute("echo -e \"#PBS -l select=1:mem=1GB:ncpus=1\" >> " + scriptFile);
			execute("echo -e \"#PBS -N " + jobName + "\" >> " + scriptFile);
			execute("echo -e \"#PBS -o " + location + jobName + ".out\" >> " + scriptFile);
			execute("echo -e \"#PBS -e " + location + jobName + ".err\" >> " + scriptFile);
			execute("echo -e \" \" >> " + scriptFile);
			execute("echo -e  \"\\$PBS_O_WORKDIR\" >> " + scriptFile);
			execute("echo -e \"echo Done!\" >> " + scriptFile);
			
		}
		
		String qsubString = "/opt/pbs/10.0.0.82981/bin/qsub " + scriptFile;
		
		int exitVal = execute(qsubString);
		
		if (prevJobIDs != null) {
			prevJobIDs.clear();
		}
		
		// do i clear the total job IDs?
		// add this one?
		
		prevJobIDs = null;
		
		
		
		return exitVal;
		
	}
	
	
	// adapted for ssh
	public static int execute(String command) {
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		int exitVal = 0;
		
		String[] commands = null;
			
		if (onCluster()) {
			
			commands = new String[3];
		
			commands[0] = "/bin/sh";
			commands[1] = "-c";
			commands[2] = command;
		}
		
		else {
			commands = new String[3];
			
			commands[0] = "ssh";
			commands[1] = clusterHost;
			commands[2] = command;
		}
		
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
			
		if (onCluster()) {
			
			commands = new String[3];
		
			commands[0] = "/bin/sh";
			commands[1] = "-c";
			commands[2] = command;
		}
		
		else {
			commands = new String[3];
			
			commands[0] = "ssh";
			commands[1] = clusterHost;
			commands[2] = command;
		}
		
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
				if (!this.prevJobIDs.contains(other.prevJobIDs.get(i))) {
					this.prevJobIDs.add(other.prevJobIDs.get(i));
				}
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
	
	// should work with ssh
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
	
	// adapted for ssh
	public ArrayList<String> getAllJobIDs() {
		
		ArrayList<String> jobIDs = new ArrayList<String>();
		
		try {
			
			BufferedReader br = null;
			
			if (onCluster()) {
				File file = new File(fileName);
			
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			}
			else {
				
				String fileContents = executeOut("cat " + fileName);
				
				br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileContents.getBytes())));
			}
			
			while (br.ready()) {
				String line = br.readLine();
				
				String find = "PBS ID: ";
				
				if (line.contains(find)) {
					jobIDs.add(line.substring(8));
				}
				
			}
			
			br.close();
			
			
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
	
	public static String getClusterHost() {
		return clusterHost;
	}

}
