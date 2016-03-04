package clusterV5;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class Config implements Serializable{
	
	private static final long serialVersionUID = 2582135298709L;
	
	// the location of the config file
	private String configFile;
	// The argument values
	private HashMap<String, String> arguments;
	// The timestamps of the arguments
	private HashMap<String, String> timeStamps;
	
	// local argument overrides
//	private HashMap<String, String> localArguments;
	
	// keeping track of recently checked arguments
	private Vector<String> checkedArguments; 
	
	private boolean updated;
	
	private String user;
	private String keyFileLocation;
	private String password;
	private String keyPhrase;
	
	private String clusterHost = "lc4.itc.virginia.edu";
	
	// default constructor
	public Config() {
		configFile = null;
		arguments = null;
		timeStamps = null;
		checkedArguments = null;
//		localArguments = null;
		
		updated = false;
		
		user = System.getProperty("user.name");
		
		this.password = null;
	}
	
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
	
	// set the user name to appease users who have different ones
	public void setUserName(String user) {
		this.user = user;
	}
	
	public void setKeyFileLocation(String keyFileLocation) {
		this.keyFileLocation = keyFileLocation;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	// were the variables recently updated?
	public boolean updated() {
		return this.updated;
	}
	
	// add an argument and its value, updating its timestamp if the argument value has changed
	public void setVar(String key, String value, String nodeName) {
		
		// create them if they dont exist
		if (arguments == null) {
			arguments = new HashMap<String, String>();
		}
		
		if (timeStamps == null) {
			timeStamps = new HashMap<String, String>();
		}
		
		// create the hashkey
		String hashKey = null;
		if (nodeName == null) {
			hashKey = key + "~global";
		}
		else {
			hashKey = key + "~" + nodeName;
		}
		
		// check to see if the key is already there
		if (arguments.containsKey(hashKey)) {
			// if the value has changed, update it
			if (!value.equals(arguments.get(hashKey))) {
				arguments.put(hashKey, value);
				Date now = new Date();
				timeStamps.put(hashKey, Long.toString(now.getTime()));
				this.updated = true;
			}
		}
		// otherwise just place into hashes
		else {
			arguments.put(hashKey, value);
			Date now = new Date();
			timeStamps.put(hashKey, Long.toString(now.getTime()));
			this.updated = true;
		}
	}
	
	
	// since all of the arguments should have been uploaded into memory, just overwrite the current file
	public boolean saveVars() throws IOException{
		
		// if there is no config file, dont save
		if (this.configFile == null) {
			return false;
		}
		
		// different actions depending if we are on the cluster
		if (this.onCluster()) {
			
			File file = new File(this.configFile);
			
			FileOutputStream out = null;
			
			try {
				out = new FileOutputStream(file);
				
				for (String key : arguments.keySet()) {
					String output = key + "\t" + arguments.get(key) + "\t" + timeStamps.get(key) + "\n";
					
					out.write(output.getBytes());
				}
				
				out.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		else {
			
			execute("rm -f " + this.configFile);
			execute("touch " + this.configFile);
			
			StringBuffer buff = new StringBuffer();
			
			for (String key : arguments.keySet()) {
				String output = key + "\t" + arguments.get(key) + "\t" + timeStamps.get(key) + "\n";
				buff.append(output);
			}
			
			execute("echo -e '" + buff.toString() + "' >> " + this.configFile);
		}
		
		// updates have been saved!
		this.updated = false;
		
		return true;
		
	}
	
	// load the arguments from a file
	public void loadArguments(String configFile) {
		this.configFile = configFile;
		
		this.arguments = new HashMap<String, String>();
		this.timeStamps = new HashMap<String, String>();
		this.checkedArguments = new Vector<String>();
		
		try {
			
			BufferedReader br = null;
			
			if (this.onCluster()) {
				File file = new File(this.configFile);
			
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			}
			else {
				
				String fileContents = executeOut("cat " + this.configFile);
				
				br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileContents.getBytes())));
			}
			
			while (br.ready()) {
				String line = br.readLine();
				
				String[] fields = line.split("\t");
				
				// if no output, exit
				if (fields.length == 1) {
					break;
				}
				
				String argument = fields[0];
				String argumentValue = fields[1];
				Date timeStamp = new Date(Long.valueOf(fields[2]));
				
				this.arguments.put(argument, argumentValue);
				this.timeStamps.put(argument, Long.toString(timeStamp.getTime()));
				
			}
			
			br.close();
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// no update, we just loaded it!
		this.updated = false;
	}
	
	
	// get the value for an argument, keep track of which argument was referenced
	public String getVar(String key, String nodeName) {
		
		String localKey = key + "~" + nodeName;
		String globalKey = key + "~global";
		
		if (this.arguments.containsKey(localKey)) {
			
			this.checkedArguments.add(localKey);
			
			return this.arguments.get(localKey);
		}
		else if (this.arguments.containsKey(globalKey)) {
			
			this.checkedArguments.add(globalKey);
			
			return this.arguments.get(globalKey);
		}
		else {
			return null;
		}
	}
	
	// get the timestamp of a particular argument
	public long getTimeStamp(String key) {
		
		return Long.valueOf(this.timeStamps.get(key));
	}
	
	// get all arguments that were recently referenced
	public Vector<String> getCheckedArguments() {
		
		return this.checkedArguments;
	}
	
	// clear all checked references
	public void clearChecked() {
		if (this.checkedArguments != null) {
			this.checkedArguments.clear();
		}
	}
	
	public int execute(String command) throws IOException{
		
		if (!(this.onCluster())) {
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
	
	public void executeRemote(String command) throws IOException{
		String hostname = new PBS().getClusterHost();
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
	
	public String executeOut(String command) throws IOException{
		
		if (!(this.onCluster())) {
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
	
	// this code was adapted from the PublicKeyAuthentication.java file in the ganymed examples
	public String executeOutRemote(String command) throws IOException{
		String hostname = new PBS().getClusterHost();
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

	public void setClusterHost(String clusterHost) {
		this.clusterHost = clusterHost;
	}

	public String getClusterHost() {
		return clusterHost;
	}

	public void setKeyPhrase(String keyPhrase) {
		this.keyPhrase = keyPhrase;
	}




}
