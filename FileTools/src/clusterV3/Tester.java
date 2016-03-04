package clusterV3;

import java.util.ArrayList;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PBS head = new PBS();
		
//		head.startLog("test.txt");
		
//		head.printlnLog("Greetings Programs!");
		
//		String temp = PBSConvert.toSerString(head);
		
//		PBS head2 = PBSConvert.toPBS(temp);
		
//		head2.printlnLog("How you like me now?");
		
//		head.stopLog();
		
//		PBS node1 = head.clone();
		
//		node1.printlnLog("I am Node1!");
		
//		PBS node2 = head.clone();
		
//		node2.printlnLog("I am Node2!");
		
//		PBS node3 = head.clone();
		
//		node3.printlnLog("I am Node3!");
		
//		System.out.println("Node1 has the ID " + node1.getNumPBS() + " out of " + node1.getTotalPBS());
//		System.out.println("Node2 has the ID " + node2.getNumPBS() + " out of " + node2.getTotalPBS());
//		System.out.println("Node3 has the ID " + node3.getNumPBS() + " out of " + node3.getTotalPBS());
		
		
//		node3.stopLog();
		
		String command1 = "/h3/t1/users/ajm6q/bin/samtools index /net/cphg-nextgen1/cphg-nextgen1/projects/DGRP/test/DGRP-304_X.bam";
		String command2 = "/h3/t1/users/ajm6q/bin/samtools index /net/cphg-nextgen1/cphg-nextgen1/projects/DGRP/test/DGRP-360_X.bam";
		String command3 = "/h3/t1/users/ajm6q/bin/samtools index /net/cphg-nextgen1/cphg-nextgen1/projects/DGRP/test/DGRP-335_X.bam";
		String command4 = "java -Xmx4g -jar /h3/t1/users/mjl3p/Sting/dist/GenomeAnalysisTK.jar -T UnifiedGenotyper  -I /net/cphg-nextgen1/cphg-nextgen1/projects/DGRP/test/DGRP-208_X.bam -R /net/cphg-nextgen1/cphg-nextgen1/projects/DGRP/test/dmel.fa -varout /net/cphg-nextgen1/cphg-nextgen1/projects/DGRP/test/DGRP-208_X.calls";
		
		ArrayList<String> commands1 = new ArrayList<String>();
		commands1.add(command1);
		
		ArrayList<String> commands2 = new ArrayList<String>();
		commands2.add(command2);
		
		ArrayList<String> commands3 = new ArrayList<String>();
		commands3.add(command3);
		
		ArrayList<String> commands4 = new ArrayList<String>();
		commands4.add(command4);
		
		ArrayList<String> commandsAll = new ArrayList<String>();
		commandsAll.add(command1);
		commandsAll.add(command2);
		commandsAll.add(command3);
		commandsAll.add(command4);
		
		System.out.println(head.hasher(commands1));
		System.out.println(head.hasher(commands2));
		System.out.println(head.hasher(commands3));
		System.out.println(head.hasher(commands4));
		
		System.out.println(head.hasher(commandsAll));
		
		String jobName = "blah";
		
		String command = "qstat | grep " + jobName;
		
		System.out.println(command);
		
		
		

	}

}
