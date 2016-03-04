package clusterV5;

import java.util.Vector;
import java.util.List;



public class Job {
	
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
	
	private String unitName;
	
	private String qsubFlag;
	
	// additional data
	private int expectedTime; // in milliseconds
	
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
		
		this.qsubFlag = null;
		
		this.unitName = null;
		
		// expected time
		this.expectedTime = -1;
		
	}
	
	public void addInput(String input) {
		
		if (this.inputs == null) {
			this.inputs = new Vector<String>();
		}
		
		this.inputs.add(input);
	}
	
	public void addInput(List<String> inputs) {
		
		if (this.inputs == null) {
			this.inputs = new Vector<String>();
		}
		
		this.inputs.addAll(inputs);
		
	}
	
	public void addOutput(String output) {
		
		if (this.outputs == null) {
			this.outputs = new Vector<String>();
		}
		
		this.outputs.add(output);
	}
	
	public void addOutput(List<String> outputs) {
		
		if (this.inputs == null) {
			this.inputs = new Vector<String>();
		}
		
		this.outputs.addAll(outputs);
	}

	public void addCommand(String command) {
		
		this.commands.add(command);
	}
	
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

	public void setWallTime(int wallTime) {
		this.wallTime = wallTime;
	}

	public int getWallTime() {
		return wallTime;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getGroup() {
		return group;
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

	public int getExpectedTime() {
		return expectedTime;
	}

	public void setQsubFlag(String qsubFlag) {
		this.qsubFlag = qsubFlag;
	}

	public String getQsubFlag() {
		return qsubFlag;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getUnitName() {
		return unitName;
	}

}
