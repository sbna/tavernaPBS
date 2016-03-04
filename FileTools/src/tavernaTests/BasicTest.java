package tavernaTests;

import tavernaPBS.*;

public class BasicTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		/*
		 * Initialization
		 */
		
		PBS head = new PBS();
		
		head.setUser("mjl3p");
		
		String logName = "basicTest.log";
		
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
		 * First work node
		 */
		
		PBS pbs1 = PBSConvert.toPBS(outPBS);
		
		String command1 = "sleep 60";
		
		Job job1 = new Job(command1);
		
		try {
			pbs1.doJob(job1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Finished submitting job1");
		
		String outPBS1 = PBSConvert.fromPBS(pbs1);
		
		/*
		 * Second work node
		 */
		
		PBS pbs2 = PBSConvert.toPBS(outPBS1);
		
		String command2 = "sleep 45";
		
		Job job2 = new Job(command2);
		
		try {
			pbs2.doJob(job2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String outPBS2 = PBSConvert.fromPBS(pbs2);
		
		System.out.println("Finished submitting job2");
	}

}
