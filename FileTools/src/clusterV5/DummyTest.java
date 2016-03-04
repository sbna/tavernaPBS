package clusterV5;

// this is a simple test to debug the submission process

public class DummyTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PBS head = new PBS();
		
		head.setUserName("mjl3p");
		
		try {
		
			head.startLog("DummyTest.log");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		head.blockOff();
		
		
		String headPBS = PBSConvert.fromPBS(head);
		
		// first node
		
		PBS node1 = PBSConvert.toPBS(headPBS);
		
		Job job1 = new Job("sleep 60");
		
		try {
			node1.doJob(job1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String node1PBS = PBSConvert.fromPBS(node1);
		
		// second node
		
		PBS node2 = PBSConvert.toPBS(node1PBS);
		
		Job job2 = new Job("sleep 75");
		
		try {
			node2.doJob(job2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String node2PBS = PBSConvert.fromPBS(node2);
		
		try {
			System.out.println(node2.executeOut("qstat -u mjl3p"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
