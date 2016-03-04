package viewerV1;
//Test class for reading in the different input forms
import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.text.SimpleDateFormat;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class ReadData {	
	
	private static ArrayList<Task> tasks; //All tasks in a log file
	private static ArrayList<Task> completedTasks; //tasks that have finished running
	private static String user; //The user creating the Monitor
	private static String clusterHost; //cluster host
	private static long wflowStart = 0; //Log file creation time
	private static String wflowUser; //User who submitted logfile jobs
	private static String logFile;
	private static String password = null;
	private static String keyPhrase;
	
	private static String keyFileLocation;
	
	//Getters and Setters
	public static void setKeyFileLocation(String keyfileLocation) {
		keyFileLocation = keyfileLocation;
	}
	
	public static String getKeyFileLocation() {
		return keyFileLocation;
	}

	public static void setUser(String username){
		user = username;
	}
	
	public static String getUser(){
		return user;
	}
	
	public static void setClusterHost(String cluster){
		clusterHost = cluster;
	}
	public static String getClusterHost(){
		return clusterHost;
	}
	
	public static void setPassword(String pword) {
		password = pword;
	}
	
	public static String getPassword(){
		return password;
	}
	
	public static void setKeyPhrase(String keyPhrase) {
		ReadData.keyPhrase = keyPhrase;
	}
	
	public static String getKeyPhrase() {
		return keyPhrase;
	}

	//Check if on cluster
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
	
public static int execute(String command) throws IOException{
		
		if (!onCluster()) {
			executeRemote(command);
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
	
	public static String executeOut(String command) throws IOException{
		
		if (!onCluster()) {
			return executeOutRemote(command);
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
	public static String executeOutRemote(String command) throws IOException{
		String hostname = clusterHost;
		String username = user;

		File keyfile = new File(keyFileLocation); // or "~/.ssh/id_rsa"
		String keyfilePass = keyPhrase; // will be ignored if not needed
		
		String output = null;
		
		/* Create a connection instance */

		Connection conn = new Connection(hostname);

		/* Now connect */

		conn.connect();

		/* Authenticate */

		boolean isAuthenticated;
		//Try to authenticate with keyfile first
		isAuthenticated = conn.authenticateWithPublicKey(username, keyfile, keyfilePass);
		if(!isAuthenticated){
			//if keyfile fails, then try to authenticate with password
			if(password!=null){
				isAuthenticated = conn.authenticateWithPassword(username, password);
			}
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
	
	public static void executeRemote(String command) throws IOException{
		String hostname = clusterHost;
		String username = user;

		File keyfile = new File(keyFileLocation); // or "~/.ssh/id_rsa"
		String keyfilePass = keyPhrase; // will be ignored if not needed
		
		/* Create a connection instance */

		Connection conn = new Connection(hostname);

		/* Now connect */

		conn.connect();

		/* Authenticate */

		boolean isAuthenticated;
		//Try to authenticate with keyfile first
		isAuthenticated = conn.authenticateWithPublicKey(username, keyfile, keyfilePass);
		if(!isAuthenticated){
			//if keyfile fails, then try to authenticate with password
			if(password!=null){
				isAuthenticated = conn.authenticateWithPassword(username, password);
			}
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
	}
	
	public void executeRemote(ArrayList<String> commands) throws IOException{
		
		String hostname = clusterHost;
		String username = user;

		File keyfile = new File(keyFileLocation); // or "~/.ssh/id_rsa"
		String keyfilePass = keyPhrase; // will be ignored if not needed

		Connection conn = new Connection(hostname);

		/* Now connect */

		conn.connect();

		/* Authenticate */

		boolean isAuthenticated;
		//Try to authenticate with keyfile first
		isAuthenticated = conn.authenticateWithPublicKey(username, keyfile, keyfilePass);
		if(!isAuthenticated){
			//if keyfile fails, then try to authenticate with password
			if(password!=null){
				isAuthenticated = conn.authenticateWithPassword(username, password);
			}
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
	
	// Read in data from a log file
	public static void ReadLog(Scanner s) {
		// See line 511 in PBS.java for form of job input

		//completedTasks = null;
			String temp = null;
			long workflowRun = 0;
			if (tasks == null){
				tasks = new ArrayList<Task>();
			}

			while (s.hasNextLine()) {// This loop executes for each job
				temp = s.nextLine();
				
				//Read Tasks info in log file
				if(temp.startsWith("$$")){
					//System.out.println("$$ symbol found");
					temp = s.nextLine();
					
					
					outer:
					while(!temp.startsWith("$$")){
						Task task = new Task();
						//read each task
						task.setJobName(temp.substring(0, temp.indexOf(",")));
						//Check if job is already in completedTasks
						for(int i =0;i<completedTasks.size();i++){
							if(task.getJobName().equals(completedTasks.get(i).getJobName())){
								temp = s.nextLine();
								continue outer;
							}
						}
						//create the task
						temp = temp.substring(temp.indexOf(",")+2);
						task.setJobID(temp.substring(0, temp.indexOf(",")));
						temp = temp.substring(temp.indexOf(",")+2);
						task.setUnitName(temp.substring(0,temp.indexOf(",")));
						temp = temp.substring(temp.indexOf(",")+2);
						task.setWFlowNode(temp.substring(0, temp.indexOf(",")));
						temp = temp.substring(temp.indexOf(",")+2);
						task.setNwflowNode(temp.substring(0, temp.indexOf(",")));
						temp = temp.substring(temp.indexOf(",")+2);
						task.setUser(temp.substring(0, temp.indexOf(",")));
						
						//ParentIDs arraylist
						temp = temp.substring(temp.indexOf(",")+2);
						if(temp.startsWith("[")){
							//ParentIDs is not null
							temp = temp.substring(1); //skip first '[' in arraylist
							
							if(!temp.startsWith("]")){
								//parentIDs exist
								String[] pjIDs = temp.substring(0,temp.indexOf("]")).split(",");
								for(int i=0;i<pjIDs.length;i++){
									task.addParentID(pjIDs[i]);
									//skip to next value
									temp = temp.substring(temp.indexOf(",")+2);
								}
							}
							
						}
						else{
							//skip to next value if ParentIDs is null
							temp = temp.substring(temp.indexOf(",")+2);
						}
						
						//if parentIDs is null, then Task sets them to null by default
		
						//longs queue/start/stop/elap/overdue time
						task.setQueueTime(Long.parseLong(temp.substring(0, temp.indexOf(","))));
						temp = temp.substring(temp.indexOf(",")+2);
						task.setStartTime(Long.parseLong(temp.substring(0, temp.indexOf(","))));
						temp = temp.substring(temp.indexOf(",")+2);
						task.setStopTime(Long.parseLong(temp.substring(0, temp.indexOf(","))));
						temp = temp.substring(temp.indexOf(",")+2);
						task.setElapTime(Long.parseLong(temp.substring(0, temp.indexOf(","))));
						temp = temp.substring(temp.indexOf(",")+2);
						task.setOverdueTime(Long.parseLong(temp.substring(0, temp.indexOf(","))));
						temp = temp.substring(temp.indexOf(",")+2);
						task.setExpectedRunTime(Long.parseLong(temp.substring(0,temp.indexOf(","))));
						temp = temp.substring(temp.indexOf(",")+2);
						
						//double Completed
						task.setCompleted(Double.parseDouble(temp.substring(0, temp.indexOf(","))));
						temp = temp.substring(temp.indexOf(",")+2);
						
						//int exitStatus
						task.setExitStatus(Integer.parseInt(temp.substring(0, temp.indexOf(","))));
						temp = temp.substring(temp.indexOf(",")+2);
						
						//boolean isStarted
						task.setStarted(Boolean.parseBoolean(temp.substring(0, temp.indexOf(","))));
						temp = temp.substring(temp.indexOf(",")+2);
						
						//extra job info
						task.setExtraInfo(temp);
						
						//add task to completedTasks list
						completedTasks.add(task);
						for(int i = 0;i<tasks.size();i++){
							if(tasks.get(i).getJobName().equals(task.getJobName())){
								//update task in tasks list
								tasks.get(i).updateTask(task);
								//break out of for loop
								break;
							}
						}
						//System.out.println("Task added to completedTasks");
						temp = s.nextLine();
					}
					continue;
				}
				if(temp.startsWith("##")){
					System.out.println("##########################");
					//New Workflow, ignore all previous entries/completedTasks
					completedTasks = new ArrayList<Task>();
					tasks = new ArrayList<Task>();
					//read beginning lines
					temp=s.nextLine();
					
					/*
					if (wflowStart == 0){ //Only update the wflowStart once
						wflowStart = Long.parseLong(temp.substring(temp.indexOf(":")+2));
						workflowRun = wflowStart;
					}
					else{
						workflowRun = Long.parseLong(temp.substring(temp.indexOf(":")+2));
					}
					*/
					
					//Update wflow start time
					wflowStart = Long.parseLong(temp.substring(temp.indexOf(":")+2));
					workflowRun = wflowStart;
					temp=s.nextLine();
					wflowUser = temp.substring(temp.indexOf(":")+2);
					temp=s.nextLine();
					
					continue;
				}
				if (temp.isEmpty()) {
					continue; // continue while loop if line is empty
				}

				String jobName = null;
				ArrayList<String> commands = null;
				String wfNode = null, nwfNode = null;
				String unit = null;
				ArrayList<String> inputs = null;
				ArrayList<String> outputs = null;
				ArrayList<String> parentIDs = null;
				String pbsID = null;
				long expectedTime = 0;
				int colon = temp.indexOf(":") + 2;

				// First line starts with Job Name
				temp = temp.substring(colon,temp.length());
				if(temp.indexOf(":") < 0){
					jobName = temp; // Get the job name

					temp = s.nextLine();
				}
				else{
					jobName = temp.substring(0, temp.indexOf(" "));
					temp = temp.substring(temp.indexOf(" ")+1);
				}
				
				//Go to next job if job is skipped (No useful info for now)
				if(jobName.equals("SKIPPED")){
					while(!temp.startsWith("@")){
						temp = s.nextLine();
					}
					continue;
				}				

				while (!temp.startsWith("@")) { // Loop through tags until you
					// reach @ symbol signifying end
					// of job

					colon = temp.indexOf(':') + 2; // index of where to begin
					// parsing line
					// Is the next line a Command?
					if (temp.startsWith("Command:")) {
						if (commands == null) {
							commands = new ArrayList<String>();
						}
						temp = temp.substring(colon);
						if(temp.indexOf(":")<0 && temp.indexOf("@") < 0){
							//go to next line
							commands.add(temp);
						}
						else{ //continue on same line
							commands.add(temp.substring(0, temp.indexOf("P")-1));
							System.out.println("Command on same line: " + temp.substring(0,temp.indexOf("P")-1));
							temp = temp.substring(temp.indexOf("P"));	
						}
					}
					
					// Is the next line a WorkflowNode?
					else if (temp.startsWith("WorkflowNode:")) {	
							temp = temp.substring(colon);
							wfNode = temp;
					}
					
					// Is the next line Unit?
					else if(temp.startsWith("Unit:")){
						temp = temp.substring(colon);
						unit = temp;
					}
					
					// Is the next line a Nested WorkflowNode?
					else if (temp.startsWith("NestWorkflowNode:")) {	
							temp = temp.substring(colon);
							nwfNode = temp;
					}

					// Is the next line an Input?
					else if (temp.startsWith("Input:")) {
						if (inputs == null) {
							inputs = new ArrayList<String>();
						}
						temp = temp.substring(colon);
						inputs.add(temp);
					}

					// Is the next line an Output?
					else if (temp.startsWith("Output:")) {
						if (outputs == null) {
							outputs = new ArrayList<String>();
						}
						temp = temp.substring(colon);
						outputs.add(temp);
					}

					// Is the next line a Parent ID?
					else if (temp.startsWith("Parent ID:")) {
						if (parentIDs == null) {
							parentIDs = new ArrayList<String>();
						}
						temp = temp.substring(colon);
						parentIDs.add(temp + ".lc4");
					}

					// Is the next line a PBS ID?
					else if (temp.startsWith("PBS ID:")) {
						temp = temp.substring(colon);
						if(temp.indexOf(":")<0 && temp.indexOf("@") < 0){
							//go to next line
							pbsID = temp;
						}
						else{ //continue on same line
							
							pbsID = (temp.substring(0, temp.indexOf(" ")));
							temp = temp.substring(temp.indexOf(" ")+1);
							System.out.println("PBS ID on same line: " + pbsID);
							System.out.println("Temp =" + temp);
						}
						if(!pbsID.endsWith(".lc4")){
							pbsID = pbsID.concat(".lc4");
						}						
					}
					
					else if(temp.startsWith("Expected Time:")){
						temp = temp.substring(colon);
						expectedTime = Long.parseLong(temp);
					}
					
					//Check if logfile is all on one line or separated
					if (temp.indexOf(":")<0 && temp.indexOf("@")<0){
						temp = s.nextLine();
					}
					else{
						continue;
					}
				}
				
				// Print everything out
				// Multiple arraylist items separated by commas
				
				System.out.println("WorkflowRun: " + workflowRun);
				System.out.println("User: " + wflowUser);
				System.out.println("Job Name: " + jobName);
				System.out.println("Unit name: " + unit);
				System.out.println("WorkflowNode: " + wfNode);
				System.out.println("NestWorkflowNode: " + nwfNode);
				System.out.println("Commands: " + commands);
				System.out.println("Inputs: " + inputs);
				System.out.println("Outputs: " + outputs);
				System.out.println("Parent IDs: " + parentIDs);
				System.out.println("PBS ID: " + pbsID);
				System.out.println("Run Time: " + expectedTime);
			
				// last line is an @ symbol
				int x = 0;
				//Update Task if already in tasks
				for(int i=0; i<tasks.size();i++){
					if(tasks.get(i).getJobName().equals(jobName)){
						tasks.get(i).setUser(wflowUser);
						tasks.get(i).setWFlowNode(wfNode);
						tasks.get(i).setNwflowNode(nwfNode);
						tasks.get(i).setJobID(pbsID);
						tasks.get(i).setParentJobIDs(parentIDs);
						tasks.get(i).setQueueTime(-1);
						tasks.get(i).setStartTime(-1);
						tasks.get(i).setStopTime(-1);
						tasks.get(i).setElapTime(-1);
						tasks.get(i).setExitStatus(-1);
						tasks.get(i).setCompleted(0);
						if(expectedTime!=0){
							tasks.get(i).setExpectedRunTime(expectedTime);
						}
						if(unit!=null && unit!="null"){
							tasks.get(i).setUnitName(unit);
						}						
						
						//Remove task from completed tasks if it is updated
						for(int j=0;j<completedTasks.size();j++){
							if(tasks.get(i).getJobName().equals(completedTasks.get(j).getJobName())){
								completedTasks.remove(j);
								break;
							}
						}
						x = 1;
					}
				}
				//Create new Task if not already in tasks
				if(x==0){
					Task task = new Task(jobName);
					task.setUser(wflowUser);
					task.setWFlowNode(wfNode);
					task.setNwflowNode(nwfNode);
					task.setJobID(pbsID);
					task.setParentJobIDs(parentIDs);
					
					if(unit!=null && unit!="null"){
						task.setUnitName(unit);
					}
					if(expectedTime!=0){
						task.setExpectedRunTime(expectedTime);
					}
					tasks.add(task);
				}
				
				System.out.println();
			}

			// Close input stream
			s.close();
	}

	//Parse a tracejob call
	public static void ReadTraceJob(Scanner s) {
		
			String temp = null;
			String jobID = null;
			String date = null, time = null; // Date and time for a specific
												// line
			ArrayList<Task> toAppend = null;
			long epoch;
			char state;
			String details = null;
			int index = 0;
			
			while (s.hasNextLine()) {
				temp = s.nextLine().trim();

				if (temp.isEmpty()) {
					continue;
				}

				if (temp.startsWith("Job:")) {
					temp = temp.substring(temp.indexOf(' ') + 1, temp.length());
					jobID = temp;
					System.out.println("\n" + jobID);
					continue;
				}
				
				//Determine which task to edit
				for (int j=0;j<tasks.size();j++){
					if(tasks.get(j).getJobID().equals(jobID)){
						index = j;
						break;
					}
				}

				// get date
				date = temp.substring(0, temp.indexOf(' '));
				temp = temp.substring(temp.indexOf(' ') + 1, temp.length());
				temp = temp.trim();
				// get time
				time = temp.substring(0, temp.indexOf(' '));
				temp = temp.substring(temp.indexOf(' ') + 1, temp.length());
				temp = temp.trim();
				// get char
				state = temp.substring(0, 1).toCharArray()[0];
				temp = temp.substring(temp.indexOf(' ') + 1, temp.length());
				temp = temp.trim();
				epoch = tasks.get(index).getEpoch(date + " " + time);

				//Parse the rest of the details for missing info from qstat
				if(temp.startsWith("queue=")){
					//job has been queued
					tasks.get(index).setQueueTime(epoch);
					tasks.get(index).setStartTime(epoch);
					tasks.get(index).setStopTime(epoch + tasks.get(index).getExpectedRunTime());
					
					/* This bit of code isn't working, but is a desired feature
					 * 
					//if job hasn't started yet and it has no dependencies, set start time to current time + 5mins
					if(tasks.get(index).getParentJobIDs()==null){
						tasks.get(index).setStartTime(System.currentTimeMillis()+(5*60*1000));
						tasks.get(index).setStopTime(tasks.get(index).getStartTime()+tasks.get(index).getExpectedRunTime());
					}
					*/
					
				}
				else if(temp.startsWith("Job")){
					//job has started
					tasks.get(index).setStartTime(epoch);
					//Set an expected stop time
					tasks.get(index).setStopTime(epoch + tasks.get(index).getExpectedRunTime());
					tasks.get(index).setStarted(true);
					if(temp.contains("deleted")){
						//Job deleted as result of dependency on job X (job ID)
						tasks.get(index).setStopTime(epoch);
						tasks.get(index).setCompleted(1.0);
						//Set extra job info
						tasks.get(index).setExtraInfo(temp.substring(temp.indexOf("Job")));
						if(toAppend == null){
							toAppend = new ArrayList<Task>();
						}
						toAppend.add(tasks.get(index));
					}
				}
				else if(temp.startsWith("user=") && temp.contains("Exit_status=")){
					//job has finished
					tasks.get(index).setStopTime(epoch);
					tasks.get(index).setCompleted(1.0);
					tasks.get(index).calcElapTime();					
					
					//get other useful info not found by qstat b/c job was finished
					//username
					tasks.get(index).setUser(temp.substring(5, temp.indexOf(" ")));
					temp = temp.substring(temp.indexOf(" "));
					//job name
					if(temp.contains("jobname")){
						temp = temp.substring(temp.indexOf("jobname"));
						tasks.get(index).setJobName(temp.substring(8,temp.indexOf(" ")));
					}
					//Check for errors
					if(temp.contains("Exit_status=")){
						int estat = temp.indexOf("Exit_status=") + 12;
						tasks.get(index).setExitStatus(Integer.parseInt(temp.substring(estat,estat+1)));
						//completedTasks.add(tasks.get(index));
						
						if(toAppend == null){
							toAppend = new ArrayList<Task>();
						}
						toAppend.add(tasks.get(index));
					}
					
				}
				else if(temp.startsWith("requestor=")){
					//killjobs was called on this job
					//example output: "requestor=pds3k@lc4.local"
					tasks.get(index).setStartTime(epoch);
					tasks.get(index).setStopTime(epoch);
					tasks.get(index).setCompleted(1.0);
					//Set extra job info
					tasks.get(index).setExtraInfo("Job killed by " + temp.substring(temp.indexOf("=")+1, temp.indexOf("@")));
					if(toAppend == null){
						toAppend = new ArrayList<Task>();
					}
					toAppend.add(tasks.get(index));
				}
				// get the rest
				details = temp;
				temp = temp.trim();

				System.out.println(date + " " + time + " " + state + " "
						+ details);
			}
			if(toAppend!=null){
				AppendTasks(toAppend, logFile);
			}
	}
	
	//Append Tasks to the end of a log file
	public static synchronized void AppendTasks(ArrayList<Task> toAppend, String filename){
		
		if(filename.endsWith(".txt")){
			//Append a local file
			try{
	        	// Create file 
	            FileWriter fstream = new FileWriter(filename,true);
	                BufferedWriter out = new BufferedWriter(fstream);
	            out.newLine();
	            //Start appended tasks symbol
	            out.write("$$");
	            out.newLine();
	            
	            //append tasks            
	            for(int i =0;i<toAppend.size();i++){
	            	completedTasks.add(toAppend.get(i));
	            	out.write(toAppend.get(i).print());
	            	out.newLine();
	            }
	            
	            out.write("$$");
	            
	            //Close the output stream
	            out.close();
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
		}
		else{
			//File is a log file. Append to files on ssh
			
			//Create string to append
			StringBuilder toWrite = new StringBuilder();
			toWrite.append("\n$$\n");
			//append tasks            
            for(int i =0;i<toAppend.size();i++){
            	completedTasks.add(toAppend.get(i));
            	toWrite.append(toAppend.get(i).print() + "\n");
            }
            toWrite.append("$$\n");
            
            String appendString = toWrite.toString();
			
			if (onCluster()) {
				if (filename != null) {
				
					File outFile = new File(filename);
				
					FileOutputStream outStream = null;
				
					try {
						outStream = new FileOutputStream(outFile, true);
					
						outStream.write(appendString.getBytes());
					
						outStream.close();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else {
				if (filename != null) {
					try {
						execute("/bin/echo -e '" + appendString.trim() + "' >> " + filename);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}

	//Retrieve info from qstat -a
	public static void ReadQstata(Scanner s) {
			
			String temp = null;
			// Job ID, Username, Queue, Jobname, SessID, NDS, TSK, Memory,
			// ReqTime, S, ElapTime
			String jobName = null, jobID = null;
			String userName = null, queue = null;
			int sessID, numNodes, numTasks;
			String memory = null, reqTime = null, elapTime = null;
			char state;

			// Ignore beginning lines
			if (s.hasNextLine()) {
				temp = s.nextLine();
			}
			while (!temp.startsWith("-")) { // skip over initial lines
				if (!s.hasNextLine()) {
					break;
				}
				temp = s.nextLine();
			}

			while (s.hasNextLine()) {
				// Parse each line
				temp = s.nextLine();
				// First string is the jobID
				if (temp.startsWith("-")) { // if value is not given
					while (temp.indexOf('-') == 0) {
						temp = temp.substring(1, temp.length());
					}
					jobID = null; // Default value
				} else {
					jobID = temp.substring(0, temp.indexOf(' '));
				}

				// Chop off characters before userName
				temp = temp.substring(temp.indexOf(' '), temp.length());
				temp = temp.trim(); // chop off white space
				if (temp.startsWith("-")) { // if value is not given
					while (temp.indexOf('-') == 0) {
						temp = temp.substring(1, temp.length());
					}
					userName = null; // Default value
				} else {
					// Get username
					userName = temp.substring(0, temp.indexOf(' '));
				}

				// Get queue
				temp = temp.substring(temp.indexOf(' '), temp.length());
				temp = temp.trim();
				if (temp.startsWith("-")) { // if value is not given
					temp.replaceAll("-", "");
					queue = null; // Default value
				} else {
					queue = temp.substring(0, temp.indexOf(' '));
				}

				// Get job name
				temp = temp.substring(temp.indexOf(' '), temp.length());
				temp = temp.trim();
				if (temp.startsWith("-")) { // if value is not given
					temp.replaceAll("-", "");
					jobName = null; // Default value
				} else {
					jobName = temp.substring(0, temp.indexOf(' '));
				}

				// Get session ID
				temp = temp.substring(temp.indexOf(' '), temp.length());
				temp = temp.trim();
				if (temp.startsWith("-")) { // if value is not given
					temp.replaceAll("-", "");
					sessID = 0; // Default value
				} else {
					sessID = Integer.parseInt(temp.substring(0, temp
							.indexOf(' ')));
				}

				// Get number of nodes requested
				temp = temp.substring(temp.indexOf(' '), temp.length());
				temp = temp.trim();
				if (temp.startsWith("-")) { // if value is not given
					temp.replaceAll("-", "");
					numNodes = 0; // Default value
				} else {
					numNodes = Integer.parseInt(temp.substring(0, temp
							.indexOf(' ')));
				}

				// Get number of tasks requested
				temp = temp.substring(temp.indexOf(' '), temp.length());
				temp = temp.trim();
				if (temp.startsWith("-")) { // if value is not given
					temp.replaceAll("-", "");
					numTasks = 0; // Default value
				} else {
					numTasks = Integer.parseInt(temp.substring(0, temp
							.indexOf(' ')));
				}

				// Get memory requested
				temp = temp.substring(temp.indexOf(' '), temp.length());
				temp = temp.trim();
				if (temp.startsWith("-")) { // if value is not given
					temp.replaceAll("-", "");
					memory = null; // Default value
				} else {
					memory = temp.substring(0, temp.indexOf(' '));
				}

				// Get CPU/Wall time requested
				temp = temp.substring(temp.indexOf(' '), temp.length());
				temp = temp.trim();
				if (temp.startsWith("-")) { // if value is not given
					temp.replaceAll("-", "");
					reqTime = null; // Default value
				} else {
					reqTime = temp.substring(0, temp.indexOf(' '));
				}

				// Get current state
				temp = temp.substring(temp.indexOf(' '), temp.length());
				temp = temp.trim();
				if (temp.startsWith("-")) { // if value is not given
					temp.replaceAll("-", "");
					state = 'X'; // Default value
				} else {
					state = temp.substring(0, temp.indexOf(' ')).charAt(0);
				}

				// Get CPU/Wall time used
				temp = temp.substring(temp.indexOf(' '), temp.length());
				temp = temp.trim();
				if (temp.startsWith("-")) { // if value is not given
					temp.replaceAll("-", "");
					elapTime = null; // Default value
				} else {
					elapTime = temp;
				}
				/*
				System.out.print(jobID + ", ");
				System.out.print(userName + ", ");
				System.out.print(queue + ", ");
				System.out.print(jobName + ", ");
				System.out.print(sessID + ", ");
				System.out.print(numNodes + ", ");
				System.out.print(numTasks + ", ");
				System.out.print(memory + ", ");
				System.out.print(reqTime + ", ");
				System.out.print(state + ", ");
				System.out.println(elapTime);
				 */
				for(int i = 0; i<tasks.size();i++){
					if (tasks.get(i).getJobName().equals(jobName)){
						tasks.get(i).setUser(userName);
					}
			}
			
			}
			s.close();
	}

	//have qstat -f return a string of important info from qstat -f not found by tracejob/logfile
	public static String ReadQstatf(Scanner s) {

		// Currently this separates each item by line. Need to figure out
		// which data is useful and which isn't
			String temp = null, temp2 = null;
			String jobID = null;
			String ret = ""; //String to return

			// Assume properly formatted qstat -f
			// First lines is Job ID
			temp = s.nextLine();
			jobID = temp.substring(temp.indexOf(':') + 2, temp.length());
			temp = jobID;

			// Read remaining lines
			while (s.hasNextLine()) {

				if (temp.isEmpty()) {
					break; // break out of loop if line is empty
				}
				
				//First check for resources_used
					while(temp.startsWith("resources_used") && s.hasNextLine()){
						if(temp.contains("cpupercent")){
							temp = temp.substring(temp.indexOf(" ")+3);
							ret = ret.concat("CPU Percent: " + temp + "\n");
						}
						else if(temp.contains("vmem")){
							temp = temp.substring(temp.indexOf(" ")+3);
							ret = ret.concat("Virtual Memory: " + temp + "\n");
						}
						else if(temp.contains("mem")){
							temp = temp.substring(temp.indexOf(" ")+3);
							ret = ret.concat("Memory: " + temp + "\n");
						}
						else if(temp.contains("ncpus")){
							temp = temp.substring(temp.indexOf(" ")+3);
							ret = ret.concat("Number of CPUs: " + temp + "\n");
						}
						temp = s.nextLine();
					}
					
				//Check Job State
				if(temp.startsWith("job_state") && s.hasNextLine()){
					temp = temp.substring(temp.indexOf(" ")+3);
					//Convert character to readable state
					if(temp.contains("R")){
						temp = "Running";
					}
					else if(temp.contains("Q")){
						temp = "Queued, eligible to run or be routed";
					}
					else if(temp.contains("W")){
						temp = "Waiting for its requested execution time to be reached";
					}
					else if(temp.contains("H")){
						temp = "Held";
					}
					else if(temp.contains("T")){
						temp = "Transition (being moved to a new location)";
					}
					else if(temp.contains("S")){
						temp = "Suspended";
					}
					else if(temp.contains("E")){
						temp = "Exiting after having run";
					}
					
					ret = ret.concat("Job State: " + temp + "\n");
					temp = s.nextLine();
				}
				
				//Get Queue
				if(temp.startsWith("queue") && s.hasNextLine()){
					temp = temp.substring(temp.indexOf(" ")+3);
					ret = ret.concat("Queue: " + temp + "\n");
				}
				

				// Variable List reads differently until "comment"
				// PBS_O_HOME, PBS_O_LOGNAME, PBS_O_PATH, PBS_O_MAIL,
				// PBS_O_SHELL, PBS_O_HOST, PBS_O_WORKDIR, PBS_O_SYSTEM, SHELL,
				// SSH_CLIENT, USER, MAIL, PATH, PWD, SHLVL, HOME, LOGNAME,
				// SSH_CONNECTION, _=, PBS_O_QUEUE
				if (temp.startsWith("Variable_List")) {
					//System.out.println(temp.substring(0, temp.indexOf('P')));
					temp = temp.substring(temp.indexOf('P'), temp.length());

					while (s.hasNextLine() && !temp.startsWith("comment =")) {
						// Remove empty spaces
						temp = temp.trim();

						// Get variables out of list
						while (temp.indexOf(',') >= 0 && temp.length() > 1) {
							//System.out.println(temp.substring(0, temp.indexOf(',')));
							temp = temp.substring(temp.indexOf(',') + 1, temp.length());
						}
						if (temp.indexOf(',') == temp.length()) { 
							// if last character is a comma
							temp = s.nextLine(); // Go to next line
						}
						else if (temp.contains("PBS_O_QUEUE")) { 
							// Last variable in Variable_List
							//System.out.println(temp);
							temp = s.nextLine(); 
							// temp will start with "comment", breakit it out of loop
						}
						else{ 
							// Need to concatenate
							temp = temp.concat(s.nextLine().trim());
						}
					}
				}

				if (s.hasNextLine()) {
					temp2 = s.nextLine();

					// if next line doesn't have an '=' char, concat with previous line
					if (temp2.indexOf('=') < 0 || !temp2.contains(" = ")) {
						temp2 = temp2.trim();
						temp = temp.concat(temp2);
					}
					// otherwise, move on to next line
					else {
						//System.out.println(temp);
						temp = temp2;
						temp = temp.trim();
					}
				}

			}
			//System.out.println(temp); // Print the last line

			//Close scanner
			s.close();
			return ret;
	}
	
	//return an arraylist of tasks
	public static ArrayList<Task> getTasks(){
		return tasks;
	}
	
	//Check bounds of unfinished tasks and fix those that aren't bounded
	public static void checkBounds(){
		Task temp = new Task();
		Task temp2 = new Task();
		for(int i=0;i<tasks.size();i++){
			
			//Initialize start time if not already initialized
			if(tasks.get(i).getStartTime()<0){
				tasks.get(i).setStartTime(tasks.get(i).getQueueTime());
				//Set expected stop time
				tasks.get(i).setStopTime(tasks.get(i).getQueueTime()+tasks.get(i).getExpectedRunTime());
			}
			//store the original task
			temp = tasks.get(i);
			
			//for all tasks after the current task
			for(int j=i+1;j<tasks.size();j++){
				temp2 = tasks.get(j);
				//for each parentJobID of the current task
				if(temp2.getParentJobIDs()!=null){
				for(int k=0;k<temp2.getParentJobIDs().size();k++){
					//if the parentJob of this task equals the original task, then fix its bounds
					if(temp2.getParentJobIDs().get(k).equals(temp.getJobID())){
						//Check if temp and temp2 have finished running
						if(temp2.getStartTime()>temp.getStopTime()){
							//Current task starts after its parent, correct
							break;
						}
						else{
							if(temp2.isStarted()){
								//don't adjust start time if job has already started running
								break;
							}
							//Set current task start time to one second after parent's stop time
							tasks.get(j).setStartTime(tasks.get(i).getStopTime()+1000);
							//Set expected stop time
							tasks.get(j).setStopTime(tasks.get(i).getStopTime()+1000 + tasks.get(j).getExpectedRunTime());
						}
						
					}
					
				} }
			}
			
		}
	}
	
	public static ArrayList<Task> checkBounds(ArrayList<Task> theTasks){
		Task temp = new Task();
		Task temp2 = new Task();
		for(int i=0;i<theTasks.size();i++){
			
			//Initialize start time if not already initialized
			if(theTasks.get(i).getStartTime()<0){
				theTasks.get(i).setStartTime(theTasks.get(i).getQueueTime());
				//Set expected stop time
				theTasks.get(i).setStopTime(theTasks.get(i).getQueueTime()+theTasks.get(i).getExpectedRunTime());
			}
			//store the original task
			temp = theTasks.get(i);
			
			//for all tasks after the current task
			for(int j=i+1;j<theTasks.size();j++){
				temp2 = theTasks.get(j);
				//for each parentJobID of the current task
				if(temp2.getParentJobIDs()!=null){
				for(int k=0;k<temp2.getParentJobIDs().size();k++){
					//if the parentJob of this task equals the original task, then fix its bounds
					if(temp2.getParentJobIDs().get(k).equals(temp.getJobID())){
						//Check if temp and temp2 have finished running
						if(temp2.getStartTime()>temp.getStopTime()){
							//Current task starts after its parent, correct
							break;
						}
						else{
							if(temp2.isStarted()){
								//don't adjust start time if job has already started running
								break;
							}
							//Set current task start time to one second after parent's stop time
							theTasks.get(j).setStartTime(theTasks.get(i).getStopTime()+1000);
							//Set expected stop time
							theTasks.get(j).setStopTime(theTasks.get(i).getStopTime()+1000 + theTasks.get(j).getExpectedRunTime());
						}
						
					}
					
				} }
			}
			
		}
		
		return theTasks;
	}
	
	//Run parsers to get job information
	public static void getData(String logfile){
		
		logfile = logfile.trim();
		//Add path to log file if not already included
		if(!logfile.contains("/") && !logfile.endsWith(".txt")){
			logfile = ".taverna-2.1.2/logs/".concat(logfile);
		}
		logFile = logfile;
		
		//Compare logfile to existing workflows in more efficient way
		completedTasks = new ArrayList<Task>();
		
		if(logfile == null){
			System.out.println("Log file was null");
			logfile = "LogFileEx3.txt";
		}
		//Look back correct number of days
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd"); //Get day of month
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM"); //Get month
		Date date = new Date();
		int day = Integer.parseInt(dateFormat.format(date)); //Get current day as int
		int month = Integer.parseInt(dateFormat2.format(date)); //Get current month as int
		
		int days = 10; //default days to look back for tracejob. Significant slowdown if days>20
		
		
		String command2 = "tracejob -n " + days + " ";
		String command3 = "less ";
		String y; //For the tracejob output files
		String inputFile = null;
		
		Scanner s, s3;
		
		if (logfile.endsWith(".txt")){ //Look for logfile in local directory for testing
			//Local txt file
			inputFile = logfile;
			try{
				File file = new File(inputFile);
				
				s = new Scanner(file);
				ReadLog(s); //Read logfile
				
				//update days to look back (after ReadLog is called)
				long startTime = wflowStart;
				Date endDate = new Date(startTime);
				int startday = Integer.parseInt(dateFormat.format(endDate));
				int startmonth = Integer.parseInt(dateFormat2.format(endDate));
				days = 31*(month-startmonth) + (day-startday) + 1;
				command2 = "tracejob -n " + days + " ";
				
				//Qstat -a is not necessary for now	
				/*
				//perform ssh command for qstat
				  String command1 = "qstat -a";
						String QstataFile = executeOut(command1);
						Scanner s2 = new Scanner(QstataFile);
									
					ReadQstata(s2); //Read qstat -a
					*/
					
					String jobs = null;
					
					outer:
					for(int i=0;i<tasks.size();i++){
						//Only call tracejobs on jobs that need the info
						
						//Check if a task is completed
							for(int j=0;j<completedTasks.size();j++){
								if(tasks.get(i).getJobName().equals(completedTasks.get(j).getJobName())){
									//if completed, don't call tracejob on it
									continue outer;
								}
							}		
							//if not completed, concat job to tracejob parameters
						if (jobs==null){
							jobs = tasks.get(i).getJobID();
						}
						else{
							jobs = jobs.concat(" " + tasks.get(i).getJobID());
						}
					}
					if(jobs!=null){
						y = executeOut(command2 + jobs);
						s3 = new Scanner(y);
						ReadTraceJob(s3);
					}					
					//AppendTasks(completedTasks, inputFile);
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		//Read workflow from file on directory in ssh
		else{
			try{
			//Look up file on ssh
			inputFile = executeOut(command3 + logFile);
			s = new Scanner(inputFile);
			ReadLog(s); //Read logfile
			
			long startTime = wflowStart;
			Date endDate = new Date(startTime);
			int startday = Integer.parseInt(dateFormat.format(endDate));
			int startmonth = Integer.parseInt(dateFormat2.format(endDate));
			days = 31*(month-startmonth) + (day-startday) + 1;
			command2 = "tracejob -n " + days + " ";
			
				String jobs = null;
				outer:
					for(int i=0;i<tasks.size();i++){
						//Only call tracejobs on jobs that need the info
						
						//Check if a task is completed
							for(int j=0;j<completedTasks.size();j++){
								if(tasks.get(i).getJobName().equals(completedTasks.get(j).getJobName())){
									//if completed, don't call tracejob on it
									continue outer;
								}
							}		
							//if not completed, concat job to tracejob parameters
						if (jobs==null){
							jobs = tasks.get(i).getJobID();
						}
						else{
							jobs = jobs.concat(" " + tasks.get(i).getJobID());
						}
					}
				if(jobs!=null){
					y = executeOut(command2 + jobs);
					s3 = new Scanner(y);
					ReadTraceJob(s3);
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		checkBounds();
	}

	public static void main(String args[]) {
		
	}
}
