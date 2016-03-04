package tavernaTests;

import tavernaPBS.Job;
import tavernaPBS.PBS;
import tavernaPBS.PBSConvert;

public class BasicOutputTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * Initialization
		 */
		
		PBS head = new PBS();
		
		head.setUser("mjl3p");
		
		String logName = "basicOutTest.log";
		
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
		job1.addCommand("echo THIS WAS THE FIRST NODE");
		
		String result1 = null;
		
		try {
			result1 = pbs1.doJobOut(job1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(result1);
		
		String outPBS1 = PBSConvert.fromPBS(pbs1);
		
		/*
		 * Second work node
		 */
		
		PBS pbs2 = PBSConvert.toPBS(outPBS1);
		
		String command2 = "sleep 45";
		
		Job job2 = new Job(command2);
		job2.addCommand("echo THIS WAS THE SECOND NODE");
		job2.addCommand("echo Yes, yes it was");
		
		String result2 = null;
		
		try {
			result2 = pbs2.doJobOut(job2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String outPBS2 = PBSConvert.fromPBS(pbs2);
		
		System.out.println(result2);

	}

}
