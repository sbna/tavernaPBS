package tavernaTests;

import tavernaPBS.*;

public class JobRunningTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		PBS head = new PBS();

		head.setUser("");
		head.setClusterHost("login.galileo.cineca.it");
		head.setPassword("");

		try {
			head.startLog("jobRunTest.log");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		head.cphgDefault();
		head.blockOff();

		String outPBS = PBSConvert.fromPBS(head);
		
		/*
		 * Run it!
		 */
		
		PBS in1 = PBSConvert.toPBS(outPBS);
		
		String command = "echo 60";
		
		Job job = new Job(command);
		
		job.setAccountName("cin_staff");
		job.setWallTime(1);
		
		try {
			in1.doJob(job);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		 * Re-Run it!
		 */
		
		PBS in2 = PBSConvert.toPBS(outPBS);
		
		try {
			in2.doJob(job);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
