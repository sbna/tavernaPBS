package tavernaTests;

import tavernaPBS.*;

public class MergeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * Initialization
		 */
		
		PBS head = new PBS();
		
		head.setUser("mjl3p");
		
		String logName = "basicMergeTest.log";
		
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
		 * WorkNode1a
		 */
		
		PBS in1a = PBSConvert.toPBS(outPBS);
		
		String command1a = "echo I AM THE FIRST";
		
		Job job1a = new Job(command1a);

		try {
			in1a.doJob(job1a);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String outPBS1a = PBSConvert.fromPBS(in1a);
		
		/*
		 * WorkNode1b
		 */
		
		PBS in1b = PBSConvert.toPBS(outPBS1a);
		
		String command1b = "echo I AM THE SECOND NODE";
		
		Job job1b = new Job(command1b);

		try {
			in1b.doJob(job1b);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String outPBS1b = PBSConvert.fromPBS(in1b);
		
		/*
		 * WorkNode 2
		 */
		
		PBS in2 = PBSConvert.toPBS(outPBS);
		
		String command2 = "echo I AM A SEPERATE WORKNODE";
		
		Job job2 = new Job(command2);
		
		try {
			in2.doJob(job2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String outPBS2 = PBSConvert.fromPBS(in2);
		
		/*
		 * MergeNode
		 */
		
		PBS merge1 = PBSConvert.toPBS(outPBS1b);
		PBS merge2 = PBSConvert.toPBS(outPBS2);
		
		merge1.merge(merge2);
		
		String mergeCommand = "echo I AM THE MERGE NODE";
		
		Job job3 = new Job(mergeCommand);
		
		String result = null;
		
		try {
			result = merge1.doJobOut(job3);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(result);
		
		
	}

}
