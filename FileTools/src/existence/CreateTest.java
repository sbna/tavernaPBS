package existence;

public class CreateTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int value = CreateFile.test("/Users/mjl3p/DGRP/taverna_test/DGRP-313_3L_1.bam", "/Users/mjl3p/DGRP/taverna_test/DGRP-313_3L_1.bam.bai");
		
		System.out.println(value);

	}

}
