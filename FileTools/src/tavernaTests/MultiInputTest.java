package tavernaTests;

import tavernaPBS.*;
import java.util.ArrayList;

public class MultiInputTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String logName = "multiInputTest.log";
		
		

		ArrayList<String> inputs = new ArrayList<String>();

		inputs.add("input1");
		inputs.add("input2");
		inputs.add("input3");
		
		/*
		 * Init Node
		 */
		
		PBS head = new PBS();

		head.setUser("mjl3p");

		try {
			head.startLog(logName);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		head.cphgDefault();
		head.blockOff();

		String outPBS = PBSConvert.fromPBS(head);
		
		/*
		 * Loop the worknode
		 */
		
		for (int i = 0; i < inputs.size(); i++) {
			// recreate PBS object
			PBS in = PBSConvert.toPBS(outPBS);

			in.setUnitName(inputs.get(i));

			// command string to be executed
			String command = "echo " + inputs.get(i) + " in node1";

			// create job object to be run on cluster
			Job job = new Job(command);
			job.addCommand("sleep 15");

			// do the job on the cluster
			try {
				in.doJob(job);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
