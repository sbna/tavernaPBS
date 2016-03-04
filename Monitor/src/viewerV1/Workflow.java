package viewerV1;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

public class Workflow implements Serializable {

	/**
	 * Serializable class for saving a workflow
	 */
	private static final long serialVersionUID = -4141816618830809431L;
	private ArrayList<Task> tasks;
	private String firstJobName;
	private String firstJobID;
	private static String clusterHost = "lc4.itc.virginia.EDU";
	private static String user = "pds3k";
	
	public Workflow(){
		tasks = null;
		firstJobName = null;
		firstJobID = null;
	}
	
	//Create a workflow from an array list of tasks
	public Workflow(ArrayList<Task> tasks){
		this.tasks = new ArrayList<Task>();
		this.tasks = tasks;
		this.firstJobName = this.tasks.get(0).getJobName();
		String temp = this.tasks.get(0).getJobID();
		this.firstJobID = temp.substring(0,temp.indexOf('.'));
		//Collections.copy(this.tasks, tasks);
	}
	
	//Add a task
	public void addTask(Task task){
		this.tasks.add(task);
	}
	
	//Remove a task, return true if removed successfully
	public boolean removeTask(Task task){
		if (this.tasks.contains(task)){
			this.tasks.remove(task);
			return true;
		}
		else{
			return false;
		}
	}
	
	//Save a workflow object
	//TODO save workflow to file on ssh directory
	public void saveFile(String filename){
		try{
		// Write to disk with FileOutputStream
		FileOutputStream f_out = new 
			FileOutputStream(filename); // .data file

		// Write object with ObjectOutputStream
		ObjectOutputStream obj_out = new
			ObjectOutputStream (f_out);

		// Write object out to disk
		obj_out.writeObject(this);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//Save on ssh - Check to see if this works
		// if we are on cluster, use java tools to create log file
		if (onCluster()) {
		
			String location = System.getProperty("user.home") + "/.taverna-2.1.2/logs/";
		
			File file = new File(location + filename);
		
			filename = file.getAbsolutePath();
		
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
			
			// ensure directory exists
			String absoluteLocation = "/home/" + user + "/" + location;
			try{
			ReadData.execute("mkdir -p " + absoluteLocation);
			
			filename = location + filename;
			
			ReadData.execute("rm -f " + filename);
			ReadData.execute("touch " + filename);
			}catch(IOException e){
				e.printStackTrace();
			}
			
		}
	}
	
	//Read a workflow object
	public Workflow readFile(String filename){
		try{
		// Read from disk using FileInputStream
		FileInputStream f_in = new 
			FileInputStream(filename); // .data file

		// Read object using ObjectInputStream
		ObjectInputStream obj_in = 
			new ObjectInputStream (f_in);

		// Read an object
		Object obj = obj_in.readObject();

		if (obj instanceof Workflow)
		{
			// Cast object to a Workflow
			Workflow test = (Workflow) obj;
			return test;
		}
		
		}catch(Exception e){
			//TODO Create alternative if file doesn't exist
			e.printStackTrace();
		}
		Workflow bad = new Workflow();
		return bad;
	}
	
	//See if operating on cluster
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
	
	
	//Getters and Setters
	public ArrayList<Task> getTasks() {
		return tasks;
	}

	public void setTasks(ArrayList<Task> tasks) {
		this.tasks = tasks;
	}

	public String getFirstJobName() {
		return firstJobName;
	}

	public void setFirstJobName(String firstJobName) {
		this.firstJobName = firstJobName;
	}
	
	public String getFirstJobID() {
		return firstJobID;
	}

	public void setFirstJobID(String firstJobID) {
		this.firstJobID = firstJobID;
	}

	//Main for testing
	public static void main(String[] s){
		Workflow workflow;
		ArrayList<Task> arraylist = new ArrayList<Task>();
		Task a = new Task("one");
		Task b = new Task("two");
		arraylist.add(a);
		arraylist.add(b);
		
		workflow = new Workflow(arraylist);
		System.out.println(arraylist.toString());
		
		System.out.println(workflow.tasks.toString());
				
		workflow.saveFile("testFile1.data");
		Workflow test = new Workflow();
		test = test.readFile("testFile1.data");
		System.out.println(test.tasks.toString());
		
		for(int i=0;i<arraylist.size();i++){
			System.out.print(arraylist.get(i).getJobName());
			System.out.print(" " + workflow.tasks.get(i).getJobName());
			System.out.println(" " + test.tasks.get(i).getJobName());
		}
		
	}

}
