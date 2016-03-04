package viewerV1;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.text.ParseException;
//import org.jfree.xml.ParseException;

public class Task implements Serializable {

	private static final long serialVersionUID = -7455187900286411916L;
	
	//Times are represented in epoch time in milliseconds
	private long queueTime; //Job submitted to cluster
	private long startTime; //Cluster begins running job
	private long stopTime; //Job completed running on cluster
	private long elapTime; //Time job has been running on cluster
	private long overdueTime; //The time at which job becomes overdue (-1 if not overdue yet)
	private long expectedRunTime; //How long a job is expected to run for
	private boolean isStarted; //Has job started running yet

	private double completed;
	private String jobName;
	private String jobID;
	private String unitName;
	private String wflowNode; //Workflow node
	private String nwflowNode; //Nested workflow
	private ArrayList<String> parentJobIDs;
	private String user;
	private int exitStatus; //Exit Status
	private String extraInfo; //This is for unique job statuses (killed/deleted jobs, etc.)
	
	private static SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private static long defaultExpectedRunTime = 5*60*1000; //Default expected job time of 5 minutes
	
	//Constructors
	public Task() {
		queueTime = -1;
		startTime = -1;
		stopTime = -1;
		elapTime = -1;
		overdueTime = -1;
		jobName = null;
		jobID = null;
		wflowNode = null;
		nwflowNode = null;
		user = null;
		parentJobIDs = null;
		exitStatus = -1;
		completed = 0.0;
		isStarted = false;
		expectedRunTime = defaultExpectedRunTime;
		extraInfo = "Empty";
		unitName = null;
	}

	public Task(String jobName) {
		this.jobName = jobName;

		queueTime = -1;
		startTime = -1;
		stopTime = -1;
		elapTime = -1;
		overdueTime = -1;
		jobID = null;
		wflowNode = null;
		nwflowNode = null;
		user = null;
		parentJobIDs = null;
		exitStatus = -1;
		completed = 0.0;
		isStarted = false;
		expectedRunTime = defaultExpectedRunTime;
		extraInfo = "Empty";
		unitName = null;
	}

	//Methods
	
	//Update current task with info from another task
	public void updateTask(Task task){
		
			queueTime = task.getQueueTime();
			startTime = task.getStartTime();
			stopTime = task.getStopTime();
			elapTime = task.getElapTime();
			overdueTime = task.getOverdueTime();
			jobID = task.getJobID();
			wflowNode = task.getWFlowNode();
			nwflowNode = task.getNwflowNode();
			user = task.getUser();
			parentJobIDs = task.getParentJobIDs();
			exitStatus = task.getExitStatus();
			completed = task.getCompleted();
			isStarted = task.isStarted();
			extraInfo = task.getExtraInfo();
			expectedRunTime = task.getExpectedRunTime();
			unitName = task.getUnitName();
	}
	
	//Add a parent ID to task
	public void addParentID(String parent) {
		if (parentJobIDs == null) {
			parentJobIDs = new ArrayList<String>();
		}
		parentJobIDs.add(parent);
	}
	
	//returns epoch time in milliseconds
	public long getEpoch(Date date){
		long epoch;
		epoch = date.getTime();
		return epoch;
	}
	
	//get epoch from a String in "MM/dd/yyyy HH:mm:ss" format
	public long getEpoch(String theDate){
		try{	
		long epoch;
		Date date = SDF.parse(theDate);
		epoch = date.getTime();
		return epoch;
		}catch(ParseException e){ //catch ParseException
			e.printStackTrace();
		}
		return -1; //if date is not formatted correctly
	}
	
	//get date from an epoch time in milliseconds
	public Date getDate(long epoch){
		Date date = new Date(epoch);
		return date;
	}
	
	//get the date in Simple Date Format
	public String getDateString(long epoch){
		Date date = new Date(epoch);
		String theDate = SDF.format(date);
		return theDate;
	}

	//print method for logfile appending format
	public String print() {
		String str = jobName + ", " + jobID + ", " + unitName + ", " + wflowNode + ", " + nwflowNode + ", " + user + ", " + parentJobIDs
				+ ", " + queueTime + ", " + startTime + ", " + stopTime + ", " + elapTime + ", " + overdueTime + ", " + expectedRunTime + ", " +
				completed + ", " + exitStatus + ", " + isStarted + ", " + extraInfo;
		return str;
	}
	
	//print method for neatly displaying Task info
	public String printNeat(){
		String str = "Information for job: " + jobName + "\n\n";
		str = str.concat("Job ID: " + jobID + "\n");
		str = str.concat("User: " + user + "\n");
		str = str.concat("Unit name: " + unitName + "\n");
		str = str.concat("Workflow Node: " + wflowNode + "\n");
		str = str.concat("Nested Workflow Node: " + nwflowNode + "\n");
		str = str.concat("Parent Job IDs: " + parentJobIDs + "\n");
		str = str.concat("Date submitted: " + convertEpochToMDY(queueTime) + "\n");
		
		//For times, need to check if they are available/accurate
		//queue time
		if(queueTime==-1){
			str = str.concat("Queue Time: Job has not been queued yet\n");
		}
		else{
			str = str.concat("Queue time: " + convertEpochToHMS(queueTime) + "\n");
			
			//start time
			if(!isStarted){
				//Job hasn't started running
				str = str.concat("Start Time: Job has not started yet\n");
				str = str.concat("Expected Run Time: " + convertLongToHMS(expectedRunTime) + "\n");
				str = str.concat("Elapsed Time (Queue-to-Current time): " + convertLongToHMS(elapTime) + "\n");
			}
			else{
				str = str.concat("Start Time: " + convertEpochToHMS(startTime) + "\n");
				
				//stop time
				if(completed!=1){
					//Job is still running
					str = str.concat("Stop Time: Job has not finished running yet\n");
					str = str.concat("Expected Run Time: " + convertLongToHMS(expectedRunTime) + "\n");
					str = str.concat("Elapsed Time (Start-to-Current time): " + convertLongToHMS(elapTime) + "\n");
					//check if overdue
					str = str.concat("Is job Overdue? ");
					if(overdueTime==-1){
						str = str.concat("No\n");
					}
					else{
						str = str.concat("Yes, overdue at " + convertEpochToHMS(overdueTime) + "\n");
					}
				}
				else{
					//Job completed running
					str = str.concat("Stop Time: " + convertEpochToHMS(stopTime) + "\n");
					str = str.concat("Expected Run Time: " + convertLongToHMS(expectedRunTime) + "\n");
					str = str.concat("Elapsed Time (Start-to-Stop): " + convertLongToHMS(elapTime) + "\n");
					//check if overdue
					str = str.concat("Was job Overdue? ");
					if(overdueTime==-1){
						str = str.concat("No\n");
					}
					else{
						str = str.concat("Yes, overdue at " + convertEpochToHMS(overdueTime) + "\n");
					}
				}
			}
			str = str.concat("Percent Completed: " + Long.toString(Math.round(completed*100)) + "%\n");
			if(completed == 1){
				str = str.concat("Exit Status: " + exitStatus + "\n");
			}
			
		}
		
		if(!extraInfo.equals("Empty")){
			str = str.concat("Extra Job Info: " + extraInfo + "\n");
		}
		return str;
	}
	
	//Get elapsed run time of job and return String value in Simple Date Format
	public String calcElapTime(){
		String ret = null;
		
		//Get elapTime in milliseconds
		if (queueTime < 0){ //Job has not been submitted yet
			elapTime = -1;
			setCompleted(0.0);
		}
		else if (startTime <0){ //job has not started yet
			//elapTime = System.currentTimeMillis() - queueTime;
			elapTime = 0;
			//setStartTime(queueTime);
			setCompleted(0.0);
		}
		else{ //Job has a start time. Stop time is set - expected or actual
			
			if(getCompleted() == 1){
				//Job has completed
				elapTime = stopTime - startTime;
				setCompleted(1.0);
				
				//Check if job was overdue before it finished
				if((overdueTime == -1) && (startTime+expectedRunTime < stopTime)){
					setOverdueTime(startTime+expectedRunTime);
				}
			}
			else if(!isStarted()){
				//Job has been given a start time for display purposes, but has not technically started running on cluster
				elapTime = 0;
				setCompleted(0.0);
			}
			else{
				//Job has not completed, but has started
				
				//Check if job is overdue
				if((overdueTime == -1) && (System.currentTimeMillis() > stopTime)){
					setOverdueTime(startTime+expectedRunTime);
				}
					
				//Add five minutes to expected job time until it is later than current time
				long timeToAdd = 300000;
				while(System.currentTimeMillis() >= stopTime){
					setStopTime(stopTime+timeToAdd);
				}
					
				//Calculate completion percentage
				long time = System.currentTimeMillis()-startTime;
				double currTime = (double)(time/1000);
				double totalTime = (double)((stopTime-startTime)/1000);
				double percent = (currTime/totalTime);
				if(percent<0){
					//Percent can't be negative
					percent = 0;
				}
				setCompleted(percent);
				elapTime = System.currentTimeMillis()-startTime;			
			}		
		}
		
		//Convert elapTime to HH:mm:ss
		if (elapTime<0){
			ret = null;
		}
		else{
			//Format time for parsing
		    ret =  convertLongToHMS(elapTime);  
		}
		return ret;
	}
	
	//Calculate an elapsed time from start and stop times
	public String calcElapTime(long start, long stop){
		String ret = null;
		
		//Get elapTime in milliseconds
			elapTime = stop - start;

		//Convert elapTime to HH:mm:ss
		if (elapTime<0){
			ret = null;
		}
		else{
			//Format time for parsing
		    ret =  convertLongToHMS(elapTime);    
		}
		
		return ret;
	}
	
	//Convert long time into readable HH:mm:ss string
	public String convertLongToHMS(long l){
		String ret = null;
		String format = String.format("%%0%dd", 2);
		long time = l/1000;
		String seconds = String.format(format, time%60);
		String minutes = String.format(format, (time % 3600) / 60);  
	    String hours = String.format(format, time / 3600);
	    ret =  hours + ":" + minutes + ":" + seconds;  
	    
	    return ret;
	}
	
	public String convertEpochToHMS(long epoch){
		String ret = null;
		Date date = new Date(epoch);
		SimpleDateFormat HMS = new SimpleDateFormat("HH:mm:ss");
		ret = HMS.format(date);
		
		return ret;		
	}
	
	public String convertEpochToMDY(long epoch){
		String ret = null;
		Date date = new Date(epoch);
		SimpleDateFormat HMS = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
		ret = HMS.format(date);
		
		return ret;		
	}
	

	// Getters and Setters for instance variables
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getStopTime() {
		return stopTime;
	}

	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}

	public long getQueueTime() {
		return queueTime;
	}

	public void setQueueTime(long queueTime) {
		this.queueTime = queueTime;
	}

	public long getElapTime() {
		return elapTime;
	}

	public void setElapTime(long elapTime) {
		this.elapTime = elapTime;
	}
	
	public long getExpectedRunTime() {
		return expectedRunTime;
	}

	public void setExpectedRunTime(long expectedRunTime) {
		//Add a 5 second padding to expectedRunTime to allow for insignificant delays
		
		this.expectedRunTime = expectedRunTime + (5*1000);
	}

	public long getOverdueTime() {
		return overdueTime;
	}

	public void setOverdueTime(long overdueTime) {
		this.overdueTime = overdueTime;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobID() {
		return jobID;
	}

	public void setJobID(String jobID) {
		this.jobID = jobID;
	}
	
	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getWFlowNode() {
		return wflowNode;
	}

	public void setWFlowNode(String wflowNode) {
		this.wflowNode = wflowNode;
	}
	

	public String getNwflowNode() {
		return nwflowNode;
	}

	public void setNwflowNode(String nwflowNode) {
		this.nwflowNode = nwflowNode;
	}

	public ArrayList<String> getParentJobIDs() {
		return parentJobIDs;
	}

	public void setParentJobIDs(ArrayList<String> parentJobIDs) {
		this.parentJobIDs = parentJobIDs;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	public int getExitStatus() {
		return exitStatus;
	}

	public void setExitStatus(int errors) {
		this.exitStatus = errors;
	}
	
	public double getCompleted() {
		return completed;
	}
	
	public void setCompleted(double completed) {
		this.completed = completed;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}

}
