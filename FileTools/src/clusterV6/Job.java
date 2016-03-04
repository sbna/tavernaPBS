package clusterV6;

import java.util.ArrayList;



public class Job {
	
	private ArrayList<String> commands;
	private ArrayList<String> inputs;
	private ArrayList<String> outputs;
	
	private boolean clusterExec;
	
	public Job() {
		this.commands = null;
		this.inputs = null;
		this.outputs = null;
		
		this.clusterExec = true;
	}
	
	public Job(String command) {
		
		this.commands = new ArrayList<String>();
		this.commands.add(command);
		
		this.inputs = null;
		this.outputs = null;
		
		this.clusterExec = true;
		
	}
	
	public void addInput(String input) {
		
		if (this.inputs == null) {
			this.inputs = new ArrayList<String>();
		}
		
		this.inputs.add(input);
	}
	
	public void addOutput(String output) {
		
		if (this.outputs == null) {
			this.outputs = new ArrayList<String>();
		}
		
		this.outputs.add(output);
	}

	public void addCommand(String command) {
		
		if (this.commands == null) {
			this.commands = new ArrayList<String>();
		}
		
		this.commands.add(command);
	}
	
	public ArrayList<String> getCommands() {
		return commands;
	}

	public ArrayList<String> getInputs() {
		return inputs;
	}

	public ArrayList<String> getOutputs() {
		return outputs;
	}
	
	public void runOnCluster() {
		this.clusterExec = true;
	}
	
	public void runOnHeadNode() {
		this.clusterExec = false;
	}
	
	public boolean clusterRun() {
		return this.clusterExec;
	}
	

}
