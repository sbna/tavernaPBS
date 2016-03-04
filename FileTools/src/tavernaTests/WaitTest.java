package tavernaTests;

import tavernaPBS.Job;
import tavernaPBS.PBS;
import tavernaPBS.PBSConvert;

public class WaitTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		/*
		 * Initialization
		 */
		
		PBS head = new PBS();
		
		head.setUser("mjl3p");
		
		String logName = "waitTest.log";
		
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
		
		String command2 = "sleep 75";
		
		Job job2 = new Job(command2);
		
		try {
			pbs2.doJob(job2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String outPBS2 = PBSConvert.fromPBS(pbs2);
		
		System.out.println("Finished submitting job2");
		
		/*
		 * Third work node
		 */
		
		PBS pbs3 = PBSConvert.toPBS(outPBS2);
		
		String command3 = "sleep 10";
		
		Job job3 = new Job(command3);
		
		try {
			pbs3.doJob(job3);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String outPBS3 = PBSConvert.fromPBS(pbs3);
		
		System.out.println("Finished submitting job2");
		
		PBS wait = PBSConvert.toPBS(outPBS3);
		
		int exitVal = 0;
		
		try {
			exitVal = wait.jobWait();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(exitVal);
		

	}

}
