package tavernaGrid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.Vector;

import tavernaPBS.Config;
import tavernaPBS.Job;

// this will be the superclass for various tools that interact with
// a grid (PBS, LFE, SGE etc)

// important is to get the basic functionality of interacting with a cluster
// into this interface as it will be used as the "front-end"

public abstract class Grid implements Serializable{
	
	private static final long serialVersionUID = 2581324987365L;
	// different number for each subclass?
	
	/*
	 * Cluster Parameters
	 */
	
	// definite universal commands
	private int defaultNodes;
	private int defaultProcessors;
	private String defaultMemory;
	private int defaultWallTime;	
	private String defaultFlag;	//instead of qsubflag
	// Might be behavior unique to PBS
	private String defaultDestination;	// queue or server to send job to
	private String defaultGroup;		// group membership
	private boolean block;				// "blocking" i.e. control is only returned to user when command is completed
	
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
	
	private String remoteFolder = ".tavernaPBS/"; // change to "tavernaGrid"?
	
	// login information
	private String clusterHost;
	private boolean loginGood;
	private String user;
	private String keyFileLocation;
	private String keyPhrase;
	private String password;
	
	private String monitorLocation = null;
	
	
	// no default constructor
	
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
	
	public abstract void merge(Grid other);
	

	
	/*
	 * 
	 * Getters and Setters
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

	public void setFlag(String defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	public String getFlag() {
		return defaultFlag;
	}

	public void setClusterHost(String clusterHost) {
		this.clusterHost = clusterHost;
	}

	public String getClusterHost() {
		return clusterHost;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setKeyFileLocation(String keyFileLocation) {
		this.keyFileLocation = keyFileLocation;
	}

	public String getKeyFileLocation() {
		return keyFileLocation;
	}

	public void setKeyPhrase(String keyPhrase) {
		this.keyPhrase = keyPhrase;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public void setMonitorLocation(String monitorLocation) {
		this.monitorLocation = monitorLocation;
	}

	public String getMonitorLocation() {
		return monitorLocation;
	}


	
	
}
