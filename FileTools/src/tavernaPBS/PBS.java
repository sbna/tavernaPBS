package tavernaPBS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;


public class PBS implements Serializable{
	
	private static final long serialVersionUID = 2582754687365L;
	
	// PBS parameters
	private int defaultNodes;
	private int defaultProcessors;
	private String defaultMemory;
	private int defaultWallTime;	
	private String defaultDestination;
	private String defaultGroup;	
	private String defaultAccountName;
	//private String defaultQsubFlag; // replaced with array
	private String defaultPath;		// variable to allow user to specify a PATH, can be added to or just replaced
									// setPath or add2Path
	private String defaultMailEvents;
	
	private Vector<String> defaultQsubFlags;
	
	// whether to use blocking or not 
	private boolean block;
	
	// list of previous jobIDs
	private Vector<String> prevJobIDs;
	
	// configuration object
	private Config config;
	
	// log information
	private String nodeName;
	private String nestedNode;
	private String unitName;
	private String fileName;
	private String logName;
	
	private String logDirectory;
	private String remoteRootDirectory;
//	private String remoteFolder = ".tavernaPBS/";	// see no reason to have this
	
	// login information
	private String clusterHost;
	private boolean loginGood;
	private String user;
	private String keyFileLocation;
//	private String keyPhrase;		// necessary? maybe there is at least a better way to ask for it
	private String password;	// might have to worry about security here ...
	
	private String monitorLocation = null;
	
	// default constructor
	public PBS() {
		
		// PBS parameters
		this.defaultNodes = 0;
		this.defaultProcessors = 0;
		this.defaultMemory = null;
		this.defaultWallTime = 0;
		this.defaultDestination = null;
		this.defaultGroup = null;
		this.defaultPath = null;
		//this.defaultQsubFlag = null;
		this.defaultQsubFlags = new Vector<String>();
		this.defaultMailEvents = "a"; // in line with default
		
		// blocking
		this.block = false;
		
		this.prevJobIDs = null;
		
		
		// log info
		this.nodeName = null;
		this.nestedNode = null;
		this.unitName = null;
		this.fileName = null;
		this.logName = null;
		this.logDirectory = null;
		
		// login information
//		this.clusterHost = "lc4.itc.Virginia.EDU";
		this.clusterHost = null;	// default is local?
		this.loginGood = false;
//		this.keyPhrase = "dummy";
		this.password = null;
		
		this.user = System.getProperty("user.name");
		
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
		
		// create configuration object
		this.config = new Config();
		this.config.setUserName(this.user);
		this.config.setKeyFileLocation(keyFileLocation);
		
	}
	
	// this function checks to see if we are on the cluster	
	public boolean onCluster() {
		
		if (this.clusterHost == null) {
			return false;
		}
		
		// override: if clusterhost is "local", you are on the cluster
		if (this.clusterHost.equals("local")) {
			return true;
		}
		
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
	
	// merge a different PBS object with the current one
	// important for having more than one job ID
	public void merge(PBS other) {
		
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
		
		//this.unitName = null;
		
	}
	
	/*
	 * 
	 * Log Functions
	 * 
	 */
	
	// creates the log file on the cluster	
	public void startLog(String logName) throws IOException{
		
		if (!onCluster()) {
			if (!this.loginGood) {
				this.loginCheck();
			}
		}
		// ensure login
		//this.loginCheck();
		
		this.logName = logName;
		
		// if no given log directory, use home directory
		if (this.logDirectory == null) {
			this.logDirectory = this.executeOut("echo $HOME").trim();
		}
		
		// ensure that the log directory ends with "/"
		if (!this.logDirectory.endsWith("/")) {
			this.logDirectory = this.logDirectory + "/";
		}
		
		this.remoteRootDirectory = this.logDirectory + "tavernaPBS/";
			
		// ensure directories exist
		String absoluteLogLocation = this.remoteRootDirectory + "logs/";
		String absoluteScriptLocation = this.remoteRootDirectory + "scripts/";
		String absoluteConfigLocation = this.remoteRootDirectory + "config/";
			
		StringBuffer sb1 = new StringBuffer();
					
		sb1.append("mkdir -p " + this.remoteRootDirectory + "\n");
		sb1.append("mkdir -p " + absoluteLogLocation + "\n");
		sb1.append("mkdir -p " + absoluteScriptLocation + "\n");
		sb1.append("mkdir -p " + absoluteConfigLocation + "\n");
		
		execute(sb1.toString());
			
		this.fileName = absoluteLogLocation + logName;

		execute("touch " + this.fileName);
		
		// print initial information
		StringBuffer sb2 = new StringBuffer();
		
		Date now = new Date();
		
		sb2.append("#################################################\n");
		sb2.append("WorkflowRun: " + now.getTime() + "\n");
		sb2.append("User: " + this.user + "\n");
		sb2.append("-------------------------------------------------\n");
		
		printlnLog(sb2.toString());				
	}
	
	public void startLog() throws IOException{
		
		String defaultLog = "default.log";
		
		this.startLog(defaultLog);
		
	}
	
	// prints to the log file
	private synchronized void printlnLog(String echo) throws IOException{
		
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
	
	// log the information for a job
	
	private synchronized void logJob(String jobName, String jobID, Job job) throws IOException{
		
		StringBuffer buff = new StringBuffer();
		
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
		
		buff.append("PBS ID: " + jobID + "\n");
		if (job.getExpectedTime() != -1) {
			buff.append("Expected Time: " + (job.getExpectedTime()+60000) + "\n");
		}
		buff.append("@\n");
		
		this.printlnLog(buff.toString());
	}
	
	/*
	 * 
	 * Config Functions
	 * 
	 */
	
	// load the configuration file
	public void loadConfig(String configFile) throws IOException{
		
		// if startLog has not been run, ensure that it is
		if (this.remoteRootDirectory == null) {
			startLog();
		}
		
		String absoluteConfigLocation = this.remoteRootDirectory + "config/";
		execute("mkdir -p " + absoluteConfigLocation);
		
		this.config.loadArguments(absoluteConfigLocation + "/" + configFile);
	}
	
	// was the configuration recently updated?
	public boolean updatedConfig() {
		return this.config.updated();
	}
	
	// save the configuration file
	public boolean saveConfig() throws IOException{
		return this.config.saveVars();
	}
	
	// set a configuration variable
	public void setVar(String argument, String argumentValue) {
		
		if (this.nestedNode == null) {		
			this.config.setVar(argument, argumentValue, this.nodeName);
		}
		else {
			this.config.setVar(argument, argumentValue, this.nestedNode + ":" + this.nodeName);
		}
	}
	
	// get a configuration variable
	public String getVar(String argument) {
		
		if (this.config == null) {
			return null;
		}
		
		if (this.nestedNode == null) {
			return this.config.getVar(argument, this.nodeName);
		}
		else {
			return this.config.getVar(argument, this.nestedNode + ":" + this.nodeName);
		}
	}
	
	/*
	 * 
	 * Login functions
	 * 
	 */
	
	// check to ensure user can login, display prompts for login info if login fails
	public boolean loginCheck() throws IOException {
		
		try {
			this.executeRemote("/bin/echo HI");
		}
		catch (IOException e) {
			
/*			if (new File(this.keyFileLocation).canRead()) {
				this.keyPhrasePrompt();
				
				try {
					this.executeRemote("/bin/echo HI");
					
//					this.config.setKeyPhrase(this.keyPhrase);
					
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
			else { */
			this.passwordPrompt();
	        
			this.executeRemote("/bin/echo HI");
	        
			this.config.setUserName(this.user);
			this.config.setPassword(this.password);
			this.config.setClusterHost(this.clusterHost);
			
		}
		
		this.loginGood = true;
		
		return this.loginGood;
		
	}
	
	// prompt to get password from user
	private void passwordPrompt() {
		JLabel jUserName = new JLabel("User Name");
        JTextField userName = new JTextField(this.user);
        JLabel jHostName = new JLabel("Hostname");
        JTextField hostName = new JTextField(this.clusterHost);
        JLabel jPassword = new JLabel("Password");
        JTextField password = new JPasswordField();
        Object[] ob = {jUserName, userName, jHostName, hostName, jPassword, password};
        int result = JOptionPane.showConfirmDialog(null, ob, "Please input password for TavernaPBS", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            this.user = userName.getText();
            this.password = password.getText();
            this.clusterHost = hostName.getText();
        }
	}
	
	// prompt to get keyphrase from user
/*	private void keyPhrasePrompt() {
		JLabel jKeyPhrase = new JLabel("Key Phrase for detected Key");
		JTextField keyPhrase = new JPasswordField();
		Object[] ob = {jKeyPhrase, keyPhrase};
		int result = JOptionPane.showConfirmDialog(null, ob, "Please input key phrase for TavernaPBS", JOptionPane.OK_CANCEL_OPTION);
		
		if (result == JOptionPane.OK_OPTION) {
			this.keyPhrase = keyPhrase.getText();
		}
	} */
	
	/*
	 * 
	 * Job Execution Functions
	 * 
	 */
	
	// turn blocking on
	public void blockOn() {
		this.block = true;
	}
	
	// turn blocking off
	public void blockOff() {
		this.block = false;
	}
	
	// compare timestamps between the two files
	// -1 if input does not exist
	// 0 if input is older than output
	// 1 if input is newer than output
	public int timeStampCheck(String inputFile, String outputFile) throws IOException{
		
		int runJob;
		
		String result1 = executeOut("date +%s -r " + inputFile);
		String result2 = executeOut("date +%s -r " + outputFile);
		
		if ((result1.startsWith("date:")) || result1.trim().equals("")) {
			runJob = -1;
		}
		else if((result2.startsWith("date:")) || result2.trim().equals("")) {
			runJob = 1;
		}
		
		else {
			int inTime = Integer.parseInt(result1.trim());
			int outTime = Integer.parseInt(result2.trim());
			
			if (inTime > outTime) {
				runJob = 1;
			}
			else {
				runJob = 0;
			}
		}
		
		return runJob;
		
	}
	
	// compare a timestamp to an input file
	// -1 if input does not exist
	// 0 if input is older than output
	// 1 if input is newer than output
	private int timeStampCheck(long configTime, String inputFile) throws IOException{
		
		int runJob;
		
		String inputTime = executeOut("date +%s -r " + inputFile);
		
		if (inputTime.startsWith("date:")) {
			runJob = -1;
		}
		else {
			if (configTime > (Long.parseLong(inputTime.trim())*1000)) {
				runJob = 1;
			}
			else {
				runJob = 0;
			}
		}
		
		return runJob;
	}
	
	// function to set defaults for CPHG use (only can be used by members with CPHG group access)
	public void cphgDefault() {
		this.defaultNodes = 1;
		this.defaultProcessors = 1;
		this.defaultMemory = "4GB";
		this.defaultWallTime = 48;
		this.defaultDestination = "cphg";
		this.defaultGroup = "CPHG";
	}
	
	// returns a hash string for qsub use
	private String hasher(Vector<String> commands) {
		
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
	
	// check to see if job is running
	private boolean jobRunning(String jobName) throws IOException{
		
		String result = this.executeOut("qstat | grep " + jobName);
		
		if (result.isEmpty()) {
			return false;
		}
		else {
			return true;
		}
		
	}
	
	private boolean jobIDRunning(String jobID) throws IOException{
		
		String result = this.executeOut("qstat " + jobID);
		
		if (result.startsWith("qstat:" )) {
			return false;
		}
		else {
			return true;
		}
		
	}
	
	private String getJobID(String jobName) throws IOException{
		
		String jobID = null;
		
		String result = this.executeOut("qstat -f | grep -B 1 " + jobName);
		
		if (result.isEmpty()) {
			jobID = "N/A";
		}
		else {

			// extract sequence
			String[] fields = result.split(" ");
			
			jobID = fields[2];
		}
		
		
		return jobID;
	}
	
	// this function executes a job on PBS (if necessary)
	public synchronized void doJob(Job job) throws IOException, Exception{
		
		int runJob = 0; // variable to determine if job needs to be run		
		String jobName = this.hasher(job.getCommands());
		
		/*
		 * If job is currently running, log info and exit
		 */
		
		if (this.jobRunning(jobName)) {
			String jobID = this.getJobID(jobName);
			
			this.logJob(jobName, jobID, job);
			
			// add to prev job IDs after clearing them
			
			if (this.prevJobIDs == null) {
				this.prevJobIDs = new Vector<String>();
			}
			else {
				this.prevJobIDs.clear();
			}
			
			if (jobID != "N/A") {
				this.prevJobIDs.add(jobID);
			}
			
			return;
		}
		
		
		/*
		 * Check to see if any input is newer than any output (or outputs are not created)
		 */
		
		if ((job.getInputs() != null) && (job.getOutputs() != null)) {
			
			for (int i = 0; i < job.getInputs().size(); i++) {
				for (int j = 0; j < job.getOutputs().size(); j++) {
					if (this.timeStampCheck(job.getInputs().get(i), job.getOutputs().get(j)) != 0) {
						runJob = 1;
					}
				}
			}
			
		}
		else {
			runJob = 1;
		}
		
		/*
		 * Check to see if a configuration parameter is newer than the input file
		 */
		
		if (runJob == 0) {
			Vector<String> checkedArguments = this.config.getCheckedArguments();
			
			if ((checkedArguments != null) && (job.getOutputs() != null)) {
			
				for (int i = 0; i < checkedArguments.size(); i++) {
					for (int j = 0; j < job.getOutputs().size(); j++) {
						if (this.timeStampCheck(this.config.getTimeStamp(checkedArguments.get(i)), job.getOutputs().get(j)) != 0) {
							runJob = 1;
						}
					}
				}
			}			
		}
		// reset accessed configuration arguments
		this.config.clearChecked();
		
		/*
		 * If job is being skipped, log info and exit
		 */
		
		if (runJob == 0) {
			
			this.logJob("SKIPPED", "N/A", job);
			
			return;
		}
		
		
		/*
		 * Create the PBS script
		 */
		StringBuffer script = this.pbsScript(jobName, job);
		
		// set blocking if blocking is true
		if (this.block) {
			script.append("#PBS -W block=true");
		}
		// enter working directory
		script.append("\ncd $PBS_O_WORKDIR\n");
		// set a delay
		script.append("sleep 60\n");
		// enter commands
		for (int i = 0; i < job.getCommands().size(); i++) {
			script.append(job.getCommands().get(i) + "\n");
		}
		
		// create script
		String absoluteScriptLocation = this.remoteRootDirectory + "scripts/";
		String scriptFile = absoluteScriptLocation + jobName + ".sh";
		
		this.execute("/bin/echo -e \'" + script.toString() + "\' > " + scriptFile);
		
		/*
		 * Run the PBS script
		 */
		
		String newJobID = this.executeOut("qsub " + scriptFile).trim();
		
		// error handling
		
		if (newJobID.startsWith("qsub")) {
			Window.error("QSUB ERROR", "Error for Job " + jobName + "\n" + newJobID);
			throw new Exception("Error for Job " + jobName + " :\n" + newJobID);
		}
		
		String jobID = newJobID;
		
		// log the job
		this.logJob(jobName, jobID, job);
		
		// empty out old jobIDs and save the new one
		
		if (!block) {
			
			if (this.prevJobIDs == null) {
				this.prevJobIDs = new Vector<String>();
			}
			else {
				this.prevJobIDs.clear();
			}
		
			this.prevJobIDs.add(jobID);
		}
		
		// touch all the output files
		if (job.getOutputs() != null) {
			for (int i = 0; i < job.getOutputs().size(); i++) {
				this.execute("touch -c " + job.getOutputs().get(i));
			}
		}
	}
	
	// wait for all jobs to complete
	public int jobWait() throws IOException, Exception{
		
		if (prevJobIDs == null) {
			return 0;
		}
		
		if (prevJobIDs.isEmpty()) {
			return 0;
		}
		
		String jobName;
		
		Vector<String> toBeHashed = new Vector<String>();
		toBeHashed.add(this.logName);
		toBeHashed.add(this.user);
		
		jobName = this.hasher(toBeHashed).replace("J", "W");
		
		// create script
		StringBuffer script = new StringBuffer();
		
		script.append("#!/bin/bash\n");
		script.append("#PBS -W depend=afterok");
		for (int i = 0; i < prevJobIDs.size(); i++) {
			if (jobIDRunning(prevJobIDs.get(i))) {
				script.append(":" + prevJobIDs.get(i));
			}	
		}
		script.append("\n");
		script.append("#PBS -W block=true\n");
		
		// set jobname, output, and error
		script.append("#PBS -N " + jobName + "\n");
				
		String absoluteScriptLocation = this.remoteRootDirectory + "scripts/";
		
		script.append("#PBS -o " + absoluteScriptLocation + jobName + ".out\n");
		script.append("#PBS -e " + absoluteScriptLocation + jobName + ".err\n");
		
		
		// enter working directory
		script.append("\ncd $PBS_O_WORKDIR\n");
		// dummy command
		script.append("echo LETS GO HOKIES\n");
		
		// create script
		String scriptFile = absoluteScriptLocation + jobName + ".sh";
		
		this.execute("/bin/echo -e \'" + script.toString() + "\' > " + scriptFile);
		
		String newJobID = this.executeOut("qsub " + scriptFile).trim();
		
		// error handling
		
		if (newJobID.startsWith("qsub")) {
			Window.error("QSUB ERROR", "Error for Job " + jobName + "\n" + newJobID);
			throw new Exception("Error for Job " + jobName + " :\n" + newJobID);
		}
		
		// clear previous jobIDs		
		if (this.prevJobIDs != null) {
			this.prevJobIDs.clear();
		}
			
		
		return 0;
		
	}
	
	// do a job and then capture and return output
	public String doJobOut(Job job) throws IOException{
		
		String jobName = this.hasher(job.getCommands());
		
		StringBuffer script = this.pbsScript(jobName, job);
		// turn on blocking
		script.append("#PBS -W block=true\n");
		
		// enter working directory
		script.append("\ncd $PBS_O_WORKDIR\n");
		// enter commands
		for (int i = 0; i < job.getCommands().size(); i++) {
			script.append(job.getCommands().get(i) + "\n");
		}
		
		// create script
		String absoluteScriptLocation = this.remoteRootDirectory + "scripts/";
		String scriptFile = absoluteScriptLocation + jobName + ".sh";
		
		this.execute("/bin/echo -e \'" + script.toString() + "\' > " + scriptFile);
		
		// log the job
		this.logJob(jobName, "OUT", job);
		
		String executeResults = this.executeOut("qsub " + scriptFile);
		
		String outputFile = absoluteScriptLocation + jobName + ".out";
		
		String catOut = this.executeOut("cat " + outputFile);
		
		// clear previous jobIDs		
		if (this.prevJobIDs != null) {
			this.prevJobIDs.clear();
		}
		
		// chop off first few lines of output
		String[] catArray = catOut.split("\n");
		
		if (catArray.length > 6) {
			StringBuffer output = new StringBuffer();
			
			for (int i = 7; i < catArray.length; i++) {
				output.append(catArray[i] + "\n");
			}
			
			return output.toString();
		}
		else {
			return catOut;
		}
	
		
	}
	
	// create a script to submit to PBS
	private StringBuffer pbsScript(String jobName, Job job) throws IOException{
		
		StringBuffer script = new StringBuffer();
		
		script.append("#!/bin/bash\n");
		
		// set destination
		String destination = job.getDestination();
		if (destination == null) {
			destination = this.defaultDestination;
		}
		if (destination != null) {
			script.append("#PBS -q " + destination + "\n");
		}
		
		// set group
		String group = job.getGroup();
		if (group == null) {
			group = this.defaultGroup;
		}
		if (group != null) {
			script.append("#PBS -W group_list=" + group + "\n");
		}
		
		// set processors
		int processors = job.getProcessors();
		if (processors == 0) {
			processors = this.defaultProcessors;
		}
		if (processors != 0) {
			script.append("#PBS -l ncpus=" + processors + "\n");
		}
		
		// set memory
		String memory = job.getMemory();
		if (memory == null) {
			memory = this.defaultMemory;
		}
		if (memory != null) {
			script.append("#PBS -l mem=" + memory + "\n");
		}
		
		// set nodes
		int nodes = job.getNodes();
		if (nodes == 0) {
			nodes = this.defaultNodes;
		}
		if (nodes != 0) {
			script.append("#PBS -l nodes=" + nodes + "\n");
		}
		
		// set walltime
		int wallTime = job.getWallTime();
		if (wallTime == 0) {
			wallTime = this.defaultWallTime;
		}
		if (wallTime != 0) {
			script.append("#PBS -l walltime=" + wallTime + ":00:00\n");
		}
		
		// set path
		String path = job.getPath();
		if (path == null) {
			path = this.defaultPath;
		}
		if (path != null) {
			script.append("#PBS -v PATH=" + path + "\n");
		}
		
		// set mail events
		String mailEvents = job.getMailEvents();
		if (mailEvents == null) {
			mailEvents = this.defaultMailEvents;
		}
		if (mailEvents != null) {
			script.append("#PBS -m " + mailEvents + "\n");
		}
		
		
		// set qsubflag
//		String qsubFlag = job.getQsubFlag();
//		if (qsubFlag == null) {
//			qsubFlag = this.defaultQsubFlag;
//		}
//		if (qsubFlag != null) {
//			script.append("#PBS " + qsubFlag + "\n");
//		}
		
		// first set default qsub flags
		if (this.defaultQsubFlags != null) {
			
			for (int i = 0; i < this.defaultQsubFlags.size(); i++) {
				script.append("#PBS " + this.defaultQsubFlags.get(i) + "\n");
			}
			
		}
		
		// then set job qsub flags
		if (job.getQsubFlags() != null) {
			
			for (int i = 0; i < job.getQsubFlags().size(); i++) {
				script.append("#PBS " + job.getQsubFlags().get(i) + "\n");
			}
			
		}
		
		// set job dependencies
		if (prevJobIDs != null) {
			script.append("#PBS -W depend=afterok");
			for (int i = 0; i < prevJobIDs.size(); i++) {
				if (jobIDRunning(prevJobIDs.get(i))) {
					script.append(":" + prevJobIDs.get(i));
				}
				
			}
			script.append("\n");
		}
		

		
		// set jobname, output, and error
		script.append("#PBS -N " + jobName + "\n");
				
		String absoluteScriptLocation = this.remoteRootDirectory + "scripts/";
		
		script.append("#PBS -o " + absoluteScriptLocation + jobName + ".out\n");
		script.append("#PBS -e " + absoluteScriptLocation + jobName + ".err\n");
		
		// additional PBS commands
		// change this to 002?
		script.append("#PBS -W umask=33\n");
		
		// set account name
		String accountName = job.getAccountName();
		if (accountName == null) {
			accountName = this.defaultAccountName;
		}
		if (accountName != null) {
			script.append("#PBS -A " + accountName + "\n");
		}
		
		
		return script;
	}
	
	/*
	 * 
	 * Additional Utilities
	 * 
	 */
	
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
//		if(keyPhrase==null){
//			keyPhrase = "KP.Empty";
//		}
		
		//Add login arguments to the command
		command = command.concat(" " + logfile);
		command = command.concat(" " + user);
		command = command.concat(" " + password);
		command = command.concat(" " + clusterHost);
		command = command.concat(" " + keyFileLocation);
//		command = command.concat(" " + keyPhrase);
		
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
	
	// get a list of files on the cluster
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
	
	// get a list of subdirectories
	public String[] getRemoteDirectories(String directory, String endPattern) throws IOException{
		
		String command = null;
		
		if (directory.endsWith("/")) {
			command = "ls -d " + directory + "*" + endPattern + "/";
		}
		else {
			command = "ls -d " + directory + "/*" + endPattern + "/";
		}
		
		String result = executeOut(command);
		
		return result.split("\n");
	}
	
	/*
	 * 
	 * Execute external commands functions
	 * 
	 */
	
	// execute a command
	private int execute(String command) throws IOException{
		
		if (!onCluster()) {
			if (!this.loginGood) {
				this.loginCheck();
			}
			
			return this.executeRemote(command);
			
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
	
	// execute a command on the remote server
	private int executeRemote(String command) throws IOException{
		
		int exitVal = 0;
		
		String hostname = clusterHost;
		String username = this.user;

		File keyfile = new File(this.keyFileLocation); // or "~/.ssh/id_dsa"
//		String keyfilePass = this.keyPhrase; // will be ignored if not needed
		
		String keyfilePass = "dummy";
		
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
		
		return exitVal;

	}
	
	// execute a command, capturing and returning the output
	private String executeOut(String command) throws IOException{
		
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
	
	// execute a command on the remote server, capturing and returning the output
	private String executeOutRemote(String command) throws IOException{
		String hostname = clusterHost;
		String username = this.user;

		File keyfile = new File(this.keyFileLocation); // or "~/.ssh/id_dsa"
	//	String keyfilePass = this.keyPhrase; // will be ignored if not needed
		String keyfilePass = "dummy";
		
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
		
		
		// if we have an error, return error string instead of output
		if (sb.toString().isEmpty()) {
			
			InputStream stderr = new StreamGobbler(sess.getStderr());
			
			BufferedReader errBuff = new BufferedReader(new InputStreamReader(stderr));
			
			StringBuffer errSb = new StringBuffer();
			
			while (true)
			{
				String line = errBuff.readLine();
				if (line == null)
					break;
				errSb.append(line + "\n");
			}
			
			return errSb.toString();
			
		}

		/* Close this session */
			
		sess.close();

		/* Close the connection */

		conn.close();

		
		output = sb.toString();
		
		return output;
	}
	
	// send a large string to a remote server and write to a file
	// if on server, just write to file
	public void writeToRemoteFile(String data, String remoteFileName) throws IOException{
		
		// if on the cluster, just write the file
		
		if (this.onCluster()) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(remoteFileName));
				out.write(data);
				out.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			return;
		}
		
		// write to local file
		
		String fileName = Extract.getFile(remoteFileName);
		String remoteDirectory = Extract.getDirectory(remoteFileName);
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write(data);
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// send file
		
		
		this.sendFileToRemote(fileName, remoteDirectory);
		
		// delete local file
		File tempFile = new File(fileName);
		
		tempFile.delete();
	}
	
	
	
	// send file to remote server
	// do nothing if on cluster
	public void sendFileToRemote(String fileLocation, String remoteFolder) throws IOException{
		
		if (this.onCluster()) {
			return;
		}
		
		String hostname = clusterHost;
		String username = this.user;

		File keyfile = new File(this.keyFileLocation); // or "~/.ssh/id_dsa"
//		String keyfilePass = this.keyPhrase; // will be ignored if not needed
		
		String keyfilePass = "dummy";
		
		
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
		
		// create scp session
		SCPClient scp = new SCPClient(conn);
		
		scp.put(fileLocation, remoteFolder);
		
		conn.close();
	}

	
	/*
	 * 
	 * Getters and setters
	 * 
	 */

	public void setNodes(int defaultNodes) {
		this.defaultNodes = defaultNodes;
	}

	public int getNodes() {
		return defaultNodes;
	}

	public void setProcessors(int defaultProcessors) {
		this.defaultProcessors = defaultProcessors;
	}

	public int getProcessors() {
		return defaultProcessors;
	}

	public void setMemory(String defaultMemory) {
		this.defaultMemory = defaultMemory;
	}

	public String getMemory() {
		return defaultMemory;
	}

	public void setWallTime(int defaultWallTime) {
		this.defaultWallTime = defaultWallTime;
	}

	public int getWallTime() {
		return defaultWallTime;
	}

	public void setDestination(String defaultDestination) {
		this.defaultDestination = defaultDestination;
	}

	public String getDestination() {
		return defaultDestination;
	}

	public void setGroup(String defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	public String getGroup() {
		return defaultGroup;
	}
	
	public void setAccountName(String defaultAccountName) {
		this.defaultAccountName = defaultAccountName;
	}

	public String getAccountName() {
		return defaultAccountName;
	}
	
	public void addQsubFlag(String qsubFlag) {
		if (this.defaultQsubFlags == null) {
			this.defaultQsubFlags = new Vector<String>();
		}
		
		this.defaultQsubFlags.add(qsubFlag);
	}

	public void setQsubFlag(String defaultQsubFlag) {
//		this.defaultQsubFlag = defaultQsubFlag;
		
		this.addQsubFlag(defaultQsubFlag);
	}

	public Vector<String> getQsubFlags() {
		return this.defaultQsubFlags;
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

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getUnitName() {
		return unitName;
	}

	public boolean isLoginGood() {
		return loginGood;
	}

	public void setClusterHost(String clusterHost) {
		this.clusterHost = clusterHost;
		
		this.config.setClusterHost(clusterHost);
		
		this.loginGood = false;
	}

	public String getClusterHost() {
		return clusterHost;
	}

	public void setKeyFileLocation(String keyFileLocation) {
		this.keyFileLocation = keyFileLocation;
	}

	public String getKeyFileLocation() {
		return keyFileLocation;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setMonitorLocation(String monitorLocation) {
		this.monitorLocation = monitorLocation;
	}

	public String getMonitorLocation() {
		return monitorLocation;
	}

	public void setPath(String defaultPath) {
		this.defaultPath = defaultPath;
	}

	public String getPath() {
		return defaultPath;
	}

	public void add2Path(String addPath) {
		if (this.defaultPath == null) {
			this.setPath(addPath);
		}
		else {
			String newPath = this.defaultPath + ":" + addPath;
			this.setPath(newPath);
		}
	}

	public void setLogDirectory(String logDirectory) {
		this.logDirectory = logDirectory;
	}

	public String getLogDirectory() {
		return logDirectory;
	}
	
	public String getRemoteRootDirectory(){
		return this.remoteRootDirectory;
	}

	public void setMailEvents(String defaultMailEvents) {
		this.defaultMailEvents = defaultMailEvents;
	}

	public String getMailEvents() {
		return defaultMailEvents;
	}

}
