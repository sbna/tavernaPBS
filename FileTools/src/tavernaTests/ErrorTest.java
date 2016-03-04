package tavernaTests;

import tavernaPBS.*;

public class ErrorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PBS head = new PBS();
		
		head.setUser("MARK");
		
		String logName = "qsubErrorTest.log";
		
		// start the log
		try {
			head.startLog(logName);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		head.blockOff();
		head.cphgDefault();
		
		String outPBS = PBSConvert.fromPBS(head);
		
		/*
		 * WorkNode
		 */
		
		PBS in = PBSConvert.toPBS(outPBS);
		
		String command = "sleep 30";
		
		Job job = new Job(command);
		job.setQsubFlag("blah");
		
		try {
			in.doJob(job);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
