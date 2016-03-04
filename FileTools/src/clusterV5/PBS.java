package clusterV5;

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
import java.util.Date;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import existence.CreateFile;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class PBS implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2582752365L;
	// default parameters for submitting a job
	private int defaultNodes = 1;
	private int defaultProcessors = 1;
	private String defaultMemory = "4GB";
	private int defaultWallTime = 48;	
	private String defaultDestination = "cphg";
	private String defaultGroup = "CPHG";
	
	private String defaultQsubFlag = null;
	
	private String unitName = null;
	
	private int numPBS;
	private static int totalPBS = 0;
	
	private Vector<String> prevJobIDs;
	
	private String monitorLocation = null;
	
	// set of commands that get executed at the end of the workflow (clean up commands and such)
	private Vector<String> delayedCommands;
	private Vector<Job> delayedJobs;
	
	// whether to use blocking or not 
	private boolean block;
	
	private String fileName;
	private String logName;
	
	private String clusterHost = "lc4.itc.Virginia.EDU";
	private boolean loginGood;
	// info for ssh
	private String user;
	private String keyFileLocation;
	private String keyPhrase;
	private String password;
	
	private Config config;
	
	private String nodeName;
	private String nestedNode;
	
	
	// This is the default constructor and should really only be run for the initial "head" node
	
	public PBS() {
		
		prevJobIDs = null;
		delayedCommands = null;
		
		block = true;
		
		fileName = null;
		logName = null;
		
		numPBS = 0;
		
		user = System.getProperty("user.name");
		
		if (System.getProperty("os.name").contains("Windows")) {
			
			if (System.getProperty("os.name").contains("XP")) {
				keyFileLocation = System.getProperty("user.home") + "\\My Documents\\id_rsa";
			}
			else {
				keyFileLocation = System.getProperty("user.home") + "\\Documents\\id_rsa";
			}
		}
		else {
			keyFileLocation = System.getProperty("user.home") + "/.ssh/id_rsa";
		}
		
		// set password to null
		this.password = null;
		
		// create configuration object
		this.config = new Config();
		this.config.setUserName(this.user);
		this.config.setKeyFileLocation(keyFileLocation);
		
		this.nodeName = null;
		this.setNestedNode(null);
		
		this.loginGood = false;
		
		this.keyPhrase = "dummy";
		
		
	}
	
	// this allows the user to override the username
	// useful in cases where there is a discrepancy between the username on the local machine
	// and the cluster
	
	public void setUserName(String userName) {
		this.user = userName;
		
		this.config.setUserName(userName);
	}
	
	// this allows the user to override the location of the keyfile
	
	public void setKeyFileLocation(String keyFileLocation) {
		this.keyFileLocation = keyFileLocation;
		
		this.config.setKeyFileLocation(keyFileLocation);
	}
	
	// this will create a new PBS object, making sure to retain cluster job dependencies
	// and all other settings
	// Note: This method is to be used to create ALL PBS objects (except the head node)
	
	public synchronized PBS clone() {
		
		PBS clone = new PBS();
		
		clone.defaultNodes = this.defaultNodes;
		clone.defaultProcessors = this.defaultProcessors;
		clone.defaultMemory = this.defaultMemory;
		clone.defaultWallTime = this.defaultWallTime;
		clone.defaultDestination = this.defaultDestination;
		clone.defaultGroup = this.defaultGroup;
		
		totalPBS++;
		
		clone.numPBS = totalPBS;
		
		// copy over job IDs
		if (prevJobIDs == null) {
			clone.prevJobIDs = this.prevJobIDs;
		}
		else {
			clone.prevJobIDs = new Vector<String>();
			
			for (int i = 0; i < this.prevJobIDs.size(); i++) {
				clone.prevJobIDs.add(this.prevJobIDs.get(i));
			}
		}
		
		clone.delayedCommands = this.delayedCommands;
		clone.delayedJobs = this.delayedJobs;

		clone.block = this.block;
		
		clone.fileName = this.fileName;
		clone.logName = this.logName;
		
		clone.config = this.config;
		
		return clone;
	}
	
	// this function checks to see if we are on the cluster
	
	public boolean onCluster() {
		String hostName = null;
		
		try {
			InetAddress addr = InetAddress.getLocalHost();
			
			hostName = addr.getHostName();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (hostName.equalsIgnoreCase(this.clusterHost)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// creates the log file on the cluster	
	public void startLog(String logName) throws IOException{
		
		this.logName = logName;
		
		// if we are on cluster, use java tools to create log file
		if (onCluster()) {
		
			String location = System.getProperty("user.home") + "/.taverna-2.1.2/logs/";
		
			File file = new File(location + logName);
		
			this.fileName = file.getAbsolutePath();
		
			FileOutputStream out = null;
		
			try {
				out = new FileOutputStream(file, true);
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
			
			// perform a login check
//			this.loginCheck();
			
			String location = ".taverna-2.1.2/logs/";
			
			// ensure directory exists
			String absoluteLocation = "/home/" + user + "/" + location;
			
			
			execute("mkdir -p " + absoluteLocation);
			
			this.fileName = location + logName;
			
//			execute("rm -f " + this.fileName);
			execute("touch " + this.fileName);
			
		}
		
		// print initial information
		StringBuffer sb = new StringBuffer();
		
		Date now = new Date();
		
		sb.append("#################################################\n");
		sb.append("WorkflowRun: " + now.getTime() + "\n");
		sb.append("User: " + this.user + "\n");
		sb.append("-------------------------------------------------\n");
		
		printlnLog(sb.toString());
		
		
	}
	
	// checks to see if user can access server (with key file), if not, prompts user for name and password
	
	public boolean loginCheck() throws IOException {
		
		try {
			this.executeRemote("/bin/echo HI");
		}
		catch (IOException e) {
			
			if (new File(this.keyFileLocation).canRead()) {
				this.keyPhrasePrompt();
				
				try {
					this.executeRemote("/bin/echo HI");
					
					this.config.setKeyPhrase(this.keyPhrase);
					
					this.password = null;
					this.config.setPassword(this.password);
				}
				catch (IOException ie) {
					
					this.passwordPrompt();
			        
			        this.executeRemote("/bin/echo HI");
			        
			        this.config.setUserName(this.user);
			        this.config.setPassword(this.password);
			        this.config.setPassword(this.clusterHost);
				}
			}
			else {
				this.passwordPrompt();
	        
				this.executeRemote("/bin/echo HI");
	        
				this.config.setUserName(this.user);
				this.config.setPassword(this.password);
				this.config.setClusterHost(this.clusterHost);
			}
		}
		
		this.loginGood = true;
		
		return this.loginGood;
		
	}
	
	public void passwordPrompt() {
		JLabel jUserName = new JLabel("User Name");
        JTextField userName = new JTextField(this.user);
        JLabel jPassword = new JLabel("Password");
        JTextField password = new JPasswordField();
        JLabel jHostName = new JLabel("Hostname");
        JTextField hostName = new JTextField(this.clusterHost);
        Object[] ob = {jUserName, userName, jPassword, password, jHostName, hostName};
        int result = JOptionPane.showConfirmDialog(null, ob, "Please input password for TavernaPBS", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            this.user = userName.getText();
            this.password = password.getText();
            this.clusterHost = hostName.getText();
        }
	}
	
	public void keyPhrasePrompt() {
		JLabel jKeyPhrase = new JLabel("Key Phrase for detected Key");
		JTextField keyPhrase = new JPasswordField();
		Object[] ob = {jKeyPhrase, keyPhrase};
		int result = JOptionPane.showConfirmDialog(null, ob, "Please input key phrase for TavernaPBS", JOptionPane.OK_CANCEL_OPTION);
		
		if (result == JOptionPane.OK_OPTION) {
			this.keyPhrase = keyPhrase.getText();
		}
	}
	
	// prints a string to the log file
	
	public synchronized void printlnLog(String echo) throws IOException{
		
		
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
				execute("/bin/echo -e '" + echo.trim() + "' >> " + fileName);
			}
		}
		
	}
	
	// turn blocking on
	
	public void blockOn() {
		block = true;
	}
	
	// turn blocking off
	
	public void blockOff() {
		block = false;
	}
	
	// load the configuration file
	public void loadConfig(String configFile) throws IOException{
		String location = ".taverna-2.1.2/conf/";
		
		// ensure directory exists
		String absoluteLocation = "/home/" + user + "/" + location;
		execute("mkdir -p " + absoluteLocation);
		
		
		this.config.loadArguments(absoluteLocation + "/" + configFile);
	}
	
	public boolean updatedConfig() {
		return this.config.updated();
	}
	
	// save the configuration file
	public boolean saveConfig() throws IOException{
		return this.config.saveVars();
	}
	
	public void setVar(String argument, String argumentValue) {
		
		if (this.nestedNode == null) {		
			this.config.setVar(argument, argumentValue, this.nodeName);
		}
		else {
			this.config.setVar(argument, argumentValue, this.nestedNode + ":" + this.nodeName);
		}
	}
	
	// this function checks to see if a file needs to be created (i.e. this part of the workflow needs to be run)
	// returns -1 if the input data has not been created
	// returns 1 if the output data does not exist or is older than the input data
	// returns 0 if nothing needs to be done
	
	public int createTest(String inputFile, String outputFile) throws IOException{
		
		int create;

		if (onCluster()) {
			create = CreateFile.test(inputFile, outputFile);
		}
		else {
			
			String result1 = executeOut("date +%s -r " + inputFile);
			String result2 = executeOut("date +%s -r " + outputFile);
			
			if (result1.isEmpty()) {
				create = -1;
			}
			else if(result2.isEmpty()) {
				create = 1;
			}
			else {
				int inTime = Integer.parseInt(result1.trim());
				int outTime = Integer.parseInt(result2.trim());
				
				if (inTime > outTime) {
					create = 1;
				}
				else {
					create = 0;
				}
			}
			
		}
		
		return create;
		
	}
	
	// check to see if configuration is newer than the input
	public int configTest(long configTime, String inputFile) throws IOException{
		
		int create;
		
		String inputTime = executeOut("date +%s -r " + inputFile);
		
		if (inputTime.isEmpty()) {
			create = -1;
		}
		else {
			if (configTime > (Long.parseLong(inputTime.trim())*1000)) {
				create = 1;
			}
			else {
				create = 0;
			}
		}
		
		return create;
	}
	
	// for the purposes of testing, this command will just log a job, placing its command/input/outputs
	// into the logfile without executing it
	
	public void justLogJob(Job job) throws IOException{
		StringBuilder buff = new StringBuilder();
		
		
		buff.append("Job Name: NOT EXECUTED\n");
		
		for (int c = 0; c < job.getCommands().size(); c++) {
			buff.append("Command: " + job.getCommands().get(c) + "\n");
		}
		for (int i = 0; i < job.getInputs().size(); i++) {
			buff.append("Input: " + job.getInputs().get(i) + "\n");
		}
		for (int o = 0; o < job.getOutputs().size(); o++) {
			buff.append("Output: " + job.getOutputs().get(o) + "\n");
		}
		buff.append("PBS ID: N/A\n");
		buff.append("@\n");
		
		String logString = buff.toString();
		
		printlnLog(logString);
	}
	
	// the main function, this will execute a job on the cluster

	public void doJob(Job job) throws IOException{
		
		// first check to see if job needs to be executed
		int runIt = 0;
		
		// check to see if newer inputs necessitate rerunning the job
		if ((job.getInputs() != null) && (job.getOutputs() != null)) {
			
			for (int i = 0; i < job.getInputs().size(); i++) {
				for (int j = 0; j < job.getOutputs().size(); j++) {
					if (createTest(job.getInputs().get(i), job.getOutputs().get(j)) != 0) {
						runIt = 1;
					}
				}
			}
			
		}
		else {
			runIt = 1;
		}
		
		// if accessed configuration arguments are new, rerun the job
		if (runIt == 0) {
			Vector<String> checkedArguments = this.config.getCheckedArguments();
			
			if ((checkedArguments != null) && (job.getOutputs() != null)) {
			
				for (int i = 0; i < checkedArguments.size(); i++) {
					for (int j = 0; j < job.getOutputs().size(); j++) {
						if (configTest(this.config.getTimeStamp(checkedArguments.get(i)), job.getOutputs().get(j)) != 0) {
							runIt = 1;
						}
					}
				}
			}
			
				
		}
		
		// reset accessed configuration arguments
		this.config.clearChecked();
		
		// if no work needs to be done, log this and skip the job
		if (runIt == 0) {
			// skip the job
			StringBuilder buff = new StringBuilder();
			
			
			buff.append("Job Name: SKIPPED\n");
			
			if (this.nestedNode != null) {
				buff.append("NestWorkflowNode: " + this.nestedNode + "\n");
			}
			
			buff.append("WorkflowNode: " + this.nodeName + "\n");
			
			if (job.getUnitName() != null) {
				buff.append("Unit: " + job.getUnitName() + "\n");
			}
			else if (this.unitName != null) {
				buff.append("Unit: " + this.unitName + "\n");
			}
			
			for (int c = 0; c < job.getCommands().size(); c++) {
				buff.append("Command: " + job.getCommands().get(c) + "\n");
			}
			if (job.getInputs() != null) {
				for (int i = 0; i < job.getInputs().size(); i++) {
					buff.append("Input: " + job.getInputs().get(i) + "\n");
				}
			}
			if (job.getOutputs() != null) {
				for (int o = 0; o < job.getOutputs().size(); o++) {
					buff.append("Output: " + job.getOutputs().get(o) + "\n");
				}
			}
			buff.append("PBS ID: N/A\n");
			if (job.getExpectedTime() != -1) {
				buff.append("Expected Time: " + (job.getExpectedTime()+60000) + "\n");
			}
			buff.append("@\n");
			
			String logString = buff.toString();
			
			printlnLog(logString);
			
			return;
		}
		
		String jobName = hasher(job.getCommands());
		
		// check to see if job is running
		if (jobRunning(jobName)) {
			
			// if that is the case, add to previous jobs list
			if (this.prevJobIDs == null) {
				this.prevJobIDs = new Vector<String>();
			}
			
			String prevJobID = getJobID(jobName);
			

			// log the job
			
			// log what was submitted
			
			StringBuilder buff = new StringBuilder();
			
			
			buff.append("Job Name: " + jobName + "\n");
			
			
			if (this.nestedNode != null) {
				buff.append("NestWorkflowNode: " + this.nestedNode + "\n");
			}
			
			buff.append("WorkflowNode: " + this.nodeName + "\n");
			
			if (job.getUnitName() != null) {
				buff.append("Unit: " + job.getUnitName() + "\n");
			}
			else if (this.unitName != null) {
				buff.append("Unit: " + this.unitName + "\n");
			}
	
			
			for (int c = 0; c < job.getCommands().size(); c++) {
				buff.append("Command: " + job.getCommands().get(c) + "\n");
			}
			if (job.getInputs() != null) {
				for (int i = 0; i < job.getInputs().size(); i++) {
					buff.append("Input: " + job.getInputs().get(i) + "\n");
				}
			}
			if (job.getOutputs() != null) {
				for (int o = 0; o < job.getOutputs().size(); o++) {
					buff.append("Output: " + job.getOutputs().get(o) + "\n");
				}
			}
			
			if (prevJobIDs != null) {
				for (int j = 0; j < prevJobIDs.size(); j++) {
					buff.append("Parent ID: " + prevJobIDs.get(j) + "\n");
				}
			}
			
			buff.append("PBS ID: " + prevJobID + "\n");
			if (job.getExpectedTime() != -1) {
				buff.append("Expected Time: " + (job.getExpectedTime()+60000) + "\n");
			}
			buff.append("@\n");
			
			String logString = buff.toString();
			
			printlnLog(logString);
			
			// add to prev job IDs after clearing them
			
			this.prevJobIDs.clear();
			
			if (prevJobID != "N/A") {
				this.prevJobIDs.add(prevJobID);
			}
			
			
			return;
		}
		
		// otherwise LETS DO THIS!!!
		// if on cluster, create script file locally
		
		// touch any already generated output files to reflect changes to later nodes
		if (job.getOutputs() != null) {
			
			for (int i = 0; i < job.getOutputs().size(); i++) {
				execute("touch -c " + job.getOutputs().get(i));
			}
			
		}
		
		
		String scriptFile;
		
		// set the PBS script parameters
		String destination;
		if (job.getDestination() != null) {
			destination = job.getDestination();
		}
		else {
			destination = this.defaultDestination;
		}
		
		String group;
		if (job.getGroup() != null) {
			group = job.getGroup();
		}
		else {
			group = this.defaultGroup;
		}
		
		int nodes;
		if (job.getNodes() != 0) {
			nodes = job.getNodes();
		}
		else {
			nodes = this.defaultNodes;
		}
		
		String memory;
		if (job.getMemory() != null) {
			memory = job.getMemory();
		}
		else {
			memory = this.defaultMemory;
		}
		
		int processors;
		if (job.getProcessors() != 0) {
			processors = job.getProcessors();
		}
		else {
			processors = this.defaultProcessors;
		}
		String qsubFlag;
		if (job.getQsubFlag() != null) {
			qsubFlag = job.getQsubFlag();
		}
		else {
			qsubFlag = this.defaultQsubFlag;
		}
		
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
			out.println("#PBS -W umask=33");
		
			// deal with that whole block situation
			if (block) {
				out.println("#PBS -W block=true");
			}
			// list any job dependencies if they are running
			else {
				if (prevJobIDs != null) {
					out.print("#PBS -W depend=afterok");
					for (int i = 0; i < prevJobIDs.size(); i++) {
						if (jobRunning(prevJobIDs.get(i))) {
//							out.println("#PBS -W depend=afterok:" + prevJobIDs.get(i));
							out.print(":" + prevJobIDs.get(i));
						}
						
					}
					out.println();
				}

			}
			out.println("#PBS -V");
			out.println("#PBS -l select=" + nodes + ":mem=" + memory + ":ncpus=" + processors);
			out.println("#PBS -N " + jobName);
			out.println("#PBS -o " + location + jobName + ".out");
			out.println("#PBS -e " + location + jobName + ".err");
			
			if (qsubFlag != null) {
				out.println("#PBS " + qsubFlag);
			}
			
			out.println();
			out.println("cd $PBS_O_WORKDIR");
			
			// add delay
			out.println("sleep 60");
		
			for (int c = 0; c < job.getCommands().size(); c++) {
				out.println(job.getCommands().get(c));
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
			execute("/bin/echo -e \"#!/bin/sh\" >> " + scriptFile);
			execute("/bin/echo -e \"#PBS -q " + destination + "\" >> " + scriptFile);
		
			execute("/bin/echo -e \"#PBS -W group_list=" + group + "\" >> " + scriptFile);
			execute("/bin/echo -e \"#PBS -W umask=33\" >> " + scriptFile);
		
			// deal with that whole block situation
			if (block) {
				execute("/bin/echo -e \"#PBS -W block=true\" >> " + scriptFile);
			}
			// list any job dependencies if they are running
			else {
				if (prevJobIDs != null) {
					
					StringBuffer dependJobs = new StringBuffer();
					
					for (int i = 0; i < prevJobIDs.size(); i++) {
						if (jobRunning(prevJobIDs.get(i))) {
//							execute("/bin/echo -e \"#PBS -W depend=afterok:" + prevJobIDs.get(i) + "\" >> " + scriptFile);
							dependJobs.append(":" + prevJobIDs.get(i));
						}
					}
					
					if (!dependJobs.toString().equals("")) {
						execute("/bin/echo -e \"#PBS -W depend=afterok" + dependJobs.toString() + "\" >> " + scriptFile);
					}
				}

			}
			execute("/bin/echo -e \"#PBS -V\" >> " + scriptFile);
			execute("/bin/echo -e \"#PBS -l select=" + nodes + ":mem=" + memory + ":ncpus=" + processors + "\" >> " + scriptFile);
			execute("/bin/echo -e \"#PBS -N " + jobName + "\" >> " + scriptFile);
			execute("/bin/echo -e \"#PBS -o " + location + jobName + ".out\" >> " + scriptFile);
			execute("/bin/echo -e \"#PBS -e " + location + jobName + ".err\" >> " + scriptFile);
			execute("/bin/echo -e \" \" >> " + scriptFile);
			execute("/bin/echo -e  \"\\$PBS_O_WORKDIR\" >> " + scriptFile);
			
			// add delay
			execute("/bin/echo -e \"sleep 60\" >> " + scriptFile);
		
			for (int c = 0; c < job.getCommands().size(); c++) {
				execute("/bin/echo -e \'" + job.getCommands().get(c) + "\' >> " + scriptFile);
			}
			
		}
		
		
		// submit job
		String qsubString = "qsub " + scriptFile;
		
		String newJobID = executeOut(qsubString);
		
		// chop off the ".lc4" part
		int loc = newJobID.indexOf('.');
		
		String jobID = newJobID.substring(0, loc);
		
		// log what was submitted
		
		StringBuilder buff = new StringBuilder();
		
		
		buff.append("Job Name: " + jobName + "\n");
		
		if (this.nestedNode != null) {
			buff.append("NestWorkflowNode: " + this.nestedNode + "\n");
		}
		
		buff.append("WorkflowNode: " + this.nodeName + "\n");
		
		if (job.getUnitName() != null) {
			buff.append("Unit: " + job.getUnitName() + "\n");
		}
		else if (this.unitName != null) {
			buff.append("Unit: " + this.unitName + "\n");
		}
		
		for (int c = 0; c < job.getCommands().size(); c++) {
			buff.append("Command: " + job.getCommands().get(c) + "\n");
		}
		if (job.getInputs() != null) {
			for (int i = 0; i < job.getInputs().size(); i++) {
				buff.append("Input: " + job.getInputs().get(i) + "\n");
			}
		}
		if (job.getOutputs() != null) {
			for (int o = 0; o < job.getOutputs().size(); o++) {
				buff.append("Output: " + job.getOutputs().get(o) + "\n");
			}
		}
		
		if (prevJobIDs != null) {
			for (int j = 0; j < prevJobIDs.size(); j++) {
				buff.append("Parent ID: " + prevJobIDs.get(j) + "\n");
			}
		}
		
		buff.append("PBS ID: " + newJobID.trim() + "\n");
		
		if (job.getExpectedTime() != -1) {
			buff.append("Expected Time: " + (job.getExpectedTime()+60000) + "\n");
		}
		
		buff.append("@\n");
		
		String logString = buff.toString();
		
		printlnLog(logString);
		
		// empty out old jobIDs and save the new one
		
		if (!block) {
			
			if (prevJobIDs == null) {
				prevJobIDs = new Vector<String>();
			}
			else {
				prevJobIDs.clear();
			}
		
			prevJobIDs.add(jobID);
		}
		
		
		
	}
	

	
	// returns a hash string for qsub use
	
	public String hasher(Vector<String> commands) {
		
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
	
/*	public boolean jobRunning(String jobName) {
		
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
			commands[1] = user + '@' + clusterHost;
			commands[2] = "qstat | grep " + jobName;
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
		
	} */
	
	public boolean jobRunning(String jobName) throws IOException{
		
		String result = this.executeOut("qstat | grep " + jobName);
		
		if (result.isEmpty()) {
			return false;
		}
		else {
			return true;
		}
		
	}
	
	// given the job name, extract the jobID from qstat (without the .lc4)
	
/*	public String getJobID(String jobName) {
		
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
			commands[1] = user + '@' + clusterHost;
			commands[2] = "qstat | grep " + jobName;
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
	} */
	
	public String getJobID(String jobName) throws IOException{
		
		String jobID = null;
		
		String result = this.executeOut("qstat | grep " + jobName);
		
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
	
	// NOTE: This could be more robust (i.e. polling qstat)
	
	public int jobWait() throws IOException{
		
		if (prevJobIDs == null) {
			return 0;
		}
		
		if (prevJobIDs.isEmpty()) {
			return 0;
		}
				
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
			out.println("#PBS -q " + defaultDestination);		
			out.println("#PBS -W group_list=" + defaultGroup);
			out.println("#PBS -W block=true");
			// print out all dependencies
			if (prevJobIDs != null) {
				out.print("#PBS -W depend=afterok");
				for (int i = 0; i < prevJobIDs.size(); i++) {
					if (jobRunning(prevJobIDs.get(i))) {
//						out.println("#PBS -W depend=afterok:" + prevJobIDs.get(i));
						out.print(":" + prevJobIDs.get(i));
					}
					
				}
				out.println();
			}
			out.println("#PBS -V");
			out.println("#PBS -l select=1:mem=1GB:ncpus=1");
			out.println("#PBS -N " + jobName);
			out.println("#PBS -o " + location + jobName + ".out");
			out.println("#PBS -e " + location + jobName + ".err");
			out.println();
			out.println("cd $PBS_O_WORKDIR");
			// essentially a dummy command
			out.println("/bin/echo \"Done!\"");
		
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
			execute("/bin/echo -e \"#!/bin/sh\" >> " + scriptFile);
			execute("/bin/echo -e \"#PBS -q " + defaultDestination + "\" >> " + scriptFile);
		
			execute("/bin/echo -e \"#PBS -W group_list=" + defaultGroup + "\" >> " + scriptFile);
		
			execute("/bin/echo -e \"#PBS -W block=true\" >> " + scriptFile);
			
			if (prevJobIDs != null) {
				
				StringBuffer dependJobs = new StringBuffer();
				
				for (int i = 0; i < prevJobIDs.size(); i++) {
					if (jobRunning(prevJobIDs.get(i))) {
//						execute("/bin/echo -e \"#PBS -W depend=afterok:" + prevJobIDs.get(i) + "\" >> " + scriptFile);
						dependJobs.append(":" + prevJobIDs.get(i));
					}
				}
				
				if (!dependJobs.toString().equals("")) {
					execute("/bin/echo -e \"#PBS -W depend=afterok" + dependJobs.toString() + "\" >> " + scriptFile);
				}
			}
			
			execute("/bin/echo -e \"#PBS -V\" >> " + scriptFile);
			execute("/bin/echo -e \"#PBS -l select=1:mem=1GB:ncpus=1\" >> " + scriptFile);
			execute("/bin/echo -e \"#PBS -N " + jobName + "\" >> " + scriptFile);
			execute("/bin/echo -e \"#PBS -o " + location + jobName + ".out\" >> " + scriptFile);
			execute("/bin/echo -e \"#PBS -e " + location + jobName + ".err\" >> " + scriptFile);
			execute("/bin/echo -e \" \" >> " + scriptFile);
			execute("/bin/echo -e  \"\\$PBS_O_WORKDIR\" >> " + scriptFile);
			execute("/bin/echo -e \"/bin/echo Done!\" >> " + scriptFile);
			
		}
		
		String qsubString = "qsub " + scriptFile;
		
		int exitVal = execute(qsubString);
		
		if (prevJobIDs != null) {
			prevJobIDs.clear();
		}
		
		prevJobIDs = null;
		
		return exitVal;
		
	}
	
	
	// adapted for ssh
/*	public int execute(String command) {
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
			commands[1] = user + '@' + clusterHost;
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
	*/
	public int execute(String command) throws IOException{
		
		if (!onCluster()) {
			if (!this.loginGood) {
				this.loginCheck();
			}
			
			this.executeRemote(command);
			return 0;
		}
		
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		int exitVal = 0;
		
		String[] commands = null;
			
			
		commands = new String[3];
		
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
	
	//adapted for ssh
/*	public String executeOut(String command) {
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
			commands[1] = user + '@' + clusterHost;
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
	} */
	
	public String executeOut(String command) throws IOException{
		
		if (!onCluster()) {
			
			if (!this.loginGood) {
				this.loginCheck();
			}
			
			return this.executeOutRemote(command);
		}
		
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		
		
		String[] commands = null;
		
		String result = null;	
			
		commands = new String[3];
		
		commands[0] = "/bin/sh";
		commands[1] = "-c";
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
	
	// test out the ganymed ssh interface
	// this code was adapted from the PublicKeyAuthentication.java file in the ganymed examples
	public String executeOutRemote(String command) throws IOException{
		String hostname = clusterHost;
		String username = this.user;

		File keyfile = new File(this.keyFileLocation); // or "~/.ssh/id_dsa"
		String keyfilePass = this.keyPhrase; // will be ignored if not needed
		
		String output = null;
		
		
		/* Create a connection instance */

		Connection conn = new Connection(hostname);

		/* Now connect */

		conn.connect();

		/* Authenticate */

		boolean isAuthenticated = false;
		
		if (this.password == null) {
			isAuthenticated = conn.authenticateWithPublicKey(username, keyfile, keyfilePass);
		}
		else {
			isAuthenticated = conn.authenticateWithPassword(username, this.password);
		}

		if (isAuthenticated == false)
			throw new IOException("Authentication failed.");

		/* Create a session */

		Session sess = conn.openSession();

		sess.execCommand(command);

		InputStream stdout = new StreamGobbler(sess.getStdout());

		BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
			
		StringBuffer sb = new StringBuffer();

		while (true)
		{
			String line = br.readLine();
			if (line == null)
				break;
			sb.append(line + "\n");
		}
			
		output = sb.toString();

		/* Close this session */
			
		sess.close();

		/* Close the connection */

		conn.close();

		
		
		return output;
	}
	
	public void executeRemote(String command) throws IOException{
		String hostname = clusterHost;
		String username = this.user;

		File keyfile = new File(this.keyFileLocation); // or "~/.ssh/id_dsa"
		String keyfilePass = this.keyPhrase; // will be ignored if not needed
		
		
		/* Create a connection instance */

		Connection conn = new Connection(hostname);

		/* Now connect */

		conn.connect();

		/* Authenticate */

		boolean isAuthenticated = false;
		
		if (this.password == null) {
			isAuthenticated = conn.authenticateWithPublicKey(username, keyfile, keyfilePass);
		}
		else {
			isAuthenticated = conn.authenticateWithPassword(username, this.password);
		}

		if (isAuthenticated == false)
			throw new IOException("Authentication failed.");

		/* Create a session */

		Session sess = conn.openSession();

		sess.execCommand(command);

			/* Close this session */
			
		sess.close();

			/* Close the connection */

			conn.close();

		
//		catch (IOException e)
//		{
//			e.printStackTrace(System.err);
//			System.exit(2);
//		}
	}
	
	public void executeRemote(ArrayList<String> commands) throws IOException{
		
		String hostname = clusterHost;
		String username = this.user;

		File keyfile = new File(this.keyFileLocation); // or "~/.ssh/id_dsa"
		String keyfilePass = this.keyPhrase; // will be ignored if not needed
		


		Connection conn = new Connection(hostname);

		/* Now connect */

		conn.connect();

		/* Authenticate */

		boolean isAuthenticated = false;
		
		if (this.password == null) {
			isAuthenticated = conn.authenticateWithPublicKey(username, keyfile, keyfilePass);
		}
		else {
			isAuthenticated = conn.authenticateWithPassword(username, this.password);
		}

		if (isAuthenticated == false)
			throw new IOException("Authentication failed.");

		/* Create a session for each command*/
			
		for (int i = 0; i < commands.size(); i++) {
			
			Session sess = conn.openSession();

			sess.execCommand(commands.get(i));

			/* Close this session */
			
			sess.close();
		}

		/* Close the connection */

		conn.close();

		

		
	}

	// merge a different PBS object with the current one
	// important for having more than one job ID
	public void merge(PBS other) {
		
		/*
		 * Other information to transfer over?
		 */
		
		// transfer jobs over
		if (this.prevJobIDs == null) {
			this.prevJobIDs = new Vector<String>();
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
			this.delayedCommands = new Vector<String>();
		}
		
		if (other.delayedCommands != null) {
			for (int i = 0; i < other.delayedCommands.size(); i++) {
				this.delayedCommands.add(other.delayedCommands.get(i));
			}
		}
		
		this.unitName = null;
		
	}
	
	public void delayJob(Job job) {
		
		if (this.delayedJobs == null) {
			this.delayedJobs = new Vector<Job>();
		}
		
		this.delayedJobs.add(job);
	}
	

	// function to execute all "delayed jobs"
	// WARNING: This should only be called AFTER all cluster jobs have run

	public boolean cleanUpJobs() throws IOException{
		
		if (this.prevJobIDs != null) {
			return false;
		}
		
		if (this.delayedJobs == null) {
			return true;
		}
		
		if (this.delayedJobs.isEmpty()) {
			return true;
		}
		
		printlnLog("Delayed Jobs:\n");
		
		for (int i = 0; i < this.delayedJobs.size(); i++) {
			
			
			int exitVal = 0;
			
			exitVal = execute(delayedJobs.get(i).getCommands().get(0));
			
			if (exitVal == 0) {
				printlnLog("Executed Successfully\n"); 
			}
			else {
				printlnLog("Error in execution. Code: " + exitVal + "\n");
			}
			
		}
			
		return true;
	}
	


	
	// function to get all job IDs from the log file
	
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
	
	// function to kill all jobs that were created by this workflow
	
	public void killAllJobs() throws IOException{
		
		ArrayList<String> jobIDs = getAllJobIDs();
		
		if (jobIDs.size() == 0) {
			return;
		}
		
		for (int i = 0; i < jobIDs.size(); i++) {
			execute("qdel " + jobIDs.get(i));
		}
		
	}
	
	// function to get a list of files on the server
	
	public String[] getRemoteFiles(String directory, String extension) throws IOException{
		
		String command = null;
		
		if (directory.endsWith("/")) {
			command = "ls " + directory + "*" + extension;
		}
		else {
			command = "ls " + directory + "/*" + extension;
		}
		
		String result = executeOut(command);
		
		return result.split("\n");
	}
	
	// function to load PBSMonitor.jar from a local machine
	public void loadMonitor(String logfile){
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		String location = monitorLocation;
		if(location == null){
			location = "empty";
		}
		JFrame theFrame = new JFrame();
		
			//Don't let the user select the wrong file
			while(!location.endsWith("Monitor.jar")){
				JOptionPane.showMessageDialog(theFrame, "Please specify the location of PBSMonitor.jar.", "Incorrect File", JOptionPane.WARNING_MESSAGE);
				System.out.println("Not the correct file");
				//Bring up a file browser to select the monitor location
				JFileChooser fc = new JFileChooser();
		    	int returnVal = fc.showOpenDialog(theFrame);
		    	
		    	//Allow the user to close the filechooser
		    	if(returnVal == JFileChooser.CANCEL_OPTION){
		    		break;
		    	}
		    	//Listen for file selected
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		    	    location = file.getAbsolutePath();
		        }
			}
		
		String[] commands = new String[3];
		//String result = null;
		
		//Different command syntax based on OS
		if (System.getProperty("os.name").contains("Windows")) {
			//Windows commands
			commands[0] = "cmd";
			commands[1] = "/c";
			
			//Windows location
			
		}
		else {
			//Linux commands
			commands[0] = "/bin/sh";
			commands[1] = "-c";
			
			//Linux location
			
		}
		
		String command = "java -jar " + location;
		
		//Check login values to make sure they exist
		if(user==null){
			user = "U.Empty";
		}
		if(password==null){
			password = "P.Empty";
		}
		if(clusterHost==null){
			clusterHost = "CH.Empty";
		}
		if(keyFileLocation==null){
			keyFileLocation = "KF.Empty";
		}
		if(keyPhrase==null){
			keyPhrase = "KP.Empty";
		}
		
		//Add login arguments to the command
		command = command.concat(" " + logfile);
		command = command.concat(" " + user);
		command = command.concat(" " + password);
		command = command.concat(" " + clusterHost);
		command = command.concat(" " + keyFileLocation);
		command = command.concat(" " + keyPhrase);
		
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
			//result = sb.toString();		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	
	/*
	 * 
	 * Getters and Setters
	 * 
	 */
	
	public void setMonitorLocation(String location) {
		this.monitorLocation = location;
	}
	public void setNodes(int nodes) {
		this.defaultNodes = nodes;
	}

	public int getNodes() {
		return defaultNodes;
	}

	public void setProcessors(int processors) {
		this.defaultProcessors = processors;
	}

	public int getProcessors() {
		return defaultProcessors;
	}

	public void setMemory(String memory) {
		this.defaultMemory = memory;
	}

	public String getMemory() {
		return defaultMemory;
	}

	public void setWallTime(int wallTime) {
		this.defaultWallTime = wallTime;
	}
	
	public int getWallTime() {
		return this.defaultWallTime;
	}
	
	public void setGroup(String group) {
		this.defaultGroup = group;
	}
	
	public String getGroup() {
		return this.defaultGroup;
	}
	
	public void setDestination(String destination) {
		this.defaultDestination = destination;
	}
	
	public String getDestination() {
		return this.defaultDestination;
	}
	
	public void setQsubFlag(String qsubFlag) {
		this.defaultQsubFlag = qsubFlag;
	}
	
	public String getQsubFlag() {
		return this.defaultQsubFlag;
	}

	public int getNumPBS() {
		return numPBS;
	}
	
	public int getTotalPBS() {
		return totalPBS;
	}
	
	public void setClusterHost(String clusterHost) {
		this.clusterHost = clusterHost;
		
		this.loginGood = false;
	}
	
	public String getClusterHost() {
		return this.clusterHost;
	}
	
	// get a configuration argument
	public String getVar(String argument) {
		
		if (this.nestedNode == null) {
			return this.config.getVar(argument, this.nodeName);
		}
		else {
			return this.config.getVar(argument, this.nestedNode + ":" + this.nodeName);
		}
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNestedNode(String nestedNode) {
		this.nestedNode = nestedNode;
	}

	public String getNestedNode() {
		return nestedNode;
	}
	
	public void setKeyPhrase(String keyPhrase) {
		this.keyPhrase = keyPhrase;
		
		this.config.setKeyPhrase(keyPhrase);
	}
	
	public void setPassword(String password) {
		this.password = password;
		
		this.config.setPassword(password);
	}
	
	public String getUserName() {
		return this.user;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getUnitName() {
		return unitName;
	}

}
