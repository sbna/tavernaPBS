package clusterV5;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PBS head = new PBS();
		
//		int value = head.createTest("/net/cphg-nextgen1/cphg-nextgen1/projects/SWISS/Homo_sapiens_assembly18.fa", "/net/cphg-nextgen1/cphg-nextgen1/projects/SWISS/reads/1038JFM03_hg18_align_sorted_marked.calls");

		head.setUserName("mjl3p");
	
		//String out = head.executeOut("echo Hello");
		
		//System.out.println(out);
		
//		System.out.println(System.getProperty("user.name"));
	}

}
