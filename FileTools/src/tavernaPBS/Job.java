package tavernaPBS;

import java.util.Vector;
import java.util.List;

public class Job {
	
	// main job contents
	private Vector<String> commands;
	private Vector<String> inputs;
	private Vector<String> outputs;
	
	// PBS parameters
	private int processors;
	private int nodes;
	private String memory;
	private int wallTime;
	private String destination;
	private String group;
	private String path;
//	private String qsubFlag;		// additional data to be added to submission script // replaced
	private Vector<String> qsubFlags;
	private String mailEvents;

	// additional data
	private int expectedTime; // in milliseconds
	private String unitName;
	
	// constructor (requires at least one command)
	public Job(String command) {
		
		this.commands = new Vector<String>();
		this.commands.add(command);
		
		this.inputs = null;
		this.outputs = null;
		
		// PBS parameters
		this.processors = 0;
		this.nodes = 0;
		this.memory = null;
		this.wallTime = 0;
		this.destination = null;
		this.group = null;
		this.path = null;
		this.mailEvents = null; // default
		
//		this.qsubFlag = null;
		this.qsubFlags = new Vector<String>();
		
		this.unitName = null;
		
		// expected time
		this.expectedTime = -1;
		
	}
	
	// add an input
	public void addInput(String input) {
		
		if (this.inputs == null) {
			this.inputs = new Vector<String>();
		}
		
		this.inputs.add(input);
	}
	
	// add a list of inputs
	public void addInput(List<String> inputs) {
		
		if (this.inputs == null) {
			this.inputs = new Vector<String>();
		}
		
		this.inputs.addAll(inputs);
		
	}
	
	// add an output
	public void addOutput(String output) {
		
		if (this.outputs == null) {
			this.outputs = new Vector<String>();
		}
		
		this.outputs.add(output);
	}
	
	// add a list of outputs
	public void addOutput(List<String> outputs) {
		
		if (this.outputs == null) {
			this.outputs = new Vector<String>();
		}
		
		this.outputs.addAll(outputs);
	}

	// add a command
	public void addCommand(String command) {
		
		this.commands.add(command);
	}
	
	
	/*
	 * Getters and Setters
	 */
	
	public Vector<String> getCommands() {
		return commands;
	}
	public Vector<String> getInputs() {
		return inputs;
	}
	public Vector<String> getOutputs() {
		return outputs;
	}
	public void setProcessors(int processors) {
		this.processors = processors;
	}
	public int getProcessors() {
		return processors;
	}
	public void setNodes(int nodes) {
		this.nodes = nodes;
	}
	public int getNodes() {
		return nodes;
	}
	public void setWallTime(int wallTime) {
		this.wallTime = wallTime;
	}
	public int getWallTime() {
		return wallTime;
	}
	public void setMemory(String memory) {
		this.memory = memory;
	}
	public String getMemory() {
		return memory;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getDestination() {
		return destination;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getGroup() {
		return group;
	}
	
	public void addQsubFlag(String qsubFlag) {
		if (this.qsubFlags == null) {
			this.qsubFlags = new Vector<String>();
		}
		
		this.qsubFlags.add(qsubFlag);
	}
	
	public void setQsubFlag(String qsubFlag) {
		this.addQsubFlag(qsubFlag);
	}
	public Vector<String> getQsubFlags() {
		return qsubFlags;
	}
	
	// takes in a string of the format HH:MM:SS and converts it into milliseconds
	public void setExpectedTime(String timeString) {
		
		String[] fields = timeString.split(":");
		
		int milliTime = 0;
		
		try {
			milliTime = Integer.valueOf(fields[0])*3600000;
		
		
			if (fields.length > 1) {
				milliTime += Integer.valueOf(fields[1])*60000;
			}
			if (fields.length > 2) {
				milliTime += Integer.valueOf(fields[2])*1000;
			}
			if (fields.length > 3) {
				milliTime += Integer.valueOf(fields[3]);
			}
		}
		catch (NumberFormatException ne) {
			milliTime = -1;
		}
		
		this.expectedTime = milliTime;
		
	}
	
	public void setExpectedTime(int expectedTime) {
		this.expectedTime = expectedTime;
	}
	public int getExpectedTime() {
		return expectedTime;
	}
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	public String getUnitName() {
		return unitName;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	
	public void add2Path(String addPath) {
		if (this.path == null) {
			this.setPath(addPath);
		}
		else {
			String newPath = this.path + ":" + addPath;
			this.setPath(newPath);
		}
	}

	public void setMailEvents(String mailEvents) {
		this.mailEvents = mailEvents;
	}

	public String getMailEvents() {
		return mailEvents;
	}

}
