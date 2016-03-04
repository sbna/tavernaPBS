package cluster;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

public class PBS {
	
	// default parameters for submitting a job
	private static int nodes = 1;
	private static int processors = 1;
	private static String memory = "4GB";
	private static int wallTime = 48;
	
	private static String destination = "cphg";
	private static String group = "CPHG";
	
	
	public static int submit (String submission) {
		
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		int exitVal = 0;
		
		Random rand = new Random();
		
		String jobName = "job" + rand.nextInt(10000);
		
		
		String qsubString = "echo 'cd $PWD; " + submission + "' | qsub -q " + destination 
															+ " -W group_list=" + group + ",block=true -V "
															+ " -l select=" + nodes + ":mem=" + memory + ":ncpus=" + processors
															+ " -N " + jobName + " -o " + jobName + ".out -e " + jobName + ".err -m e"; 
		
//		System.out.println(qsubString);
		
		try {
			proc = rt.exec(qsubString);
			
			exitVal = proc.waitFor();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// revert to defaults
		PBS.nodes = 1;
		PBS.processors = 1;
		PBS.memory = "4GB";
		PBS.wallTime = 48;
		PBS.destination = "cphg";
		PBS.group = "CPHG";
		
		
		return exitVal;
		
	}
	
	public static int submit (String submission, int nodes, int processors, String memory) {
		
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		int exitVal = 0;
		
		Random rand = new Random();
		
		String jobName = "job" + rand.nextInt(10000);
		
		
		String qsubString = "echo 'cd $PWD; " + submission + "' | qsub -q " + destination 
															+ " -W group_list=" + group + ",block=true -V "
															+ " -l select=" + nodes + ":mem=" + memory + ":ncpus=" + processors
															+ " -N " + jobName + " -o " + jobName + ".out -e " + jobName + ".err"; 
		
//		System.out.println(qsubString);
		
		try {
			proc = rt.exec(qsubString);
			
			exitVal = proc.waitFor();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return exitVal;
		
	}
	
	
	public static int execute (String submission) {
		// this function will simply execute the cluster submission AS IS
		
		Process proc = null;
		
		Runtime rt = Runtime.getRuntime();
		
		int exitVal = 0;
		
		
		try {
			
			
			proc = rt.exec(submission);
			
			exitVal = proc.waitFor();
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return exitVal;
	}
	
	// function creates a temporary script file and executes it on the cluster
	
	public static int submitScript(String submission) {
		
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		int exitVal = 0;
		
		Random rand = new Random();		
		String jobName = "job" + rand.nextInt(10000000);
		
		String location = System.getProperty("user.home") + "/.taverna-2.1.2/logs/";		
		File script = new File(location + jobName + ".sh");
		PrintWriter out = null;
				
		try {
			out = new PrintWriter(new FileWriter(script));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		out.println("#!/bin/sh");
		out.println("#PBS -q " + destination);
		out.println("#PBS -W group_list=" + group + ",block=true");
		out.println("#PBS -V");
		out.println("#PBS -l select=" + nodes + ":mem=" + memory + ":ncpus=" + processors);
		out.println("#PBS -N " + jobName);
		out.println("#PBS -o " + location + jobName + ".out");
		out.println("#PBS -e " + location + jobName + ".err");
		out.println();
		out.println("cd $PBS_O_WORKDIR");
		out.println(submission);
		
		out.close();
		
		String qsubString = "qsub " + script.getAbsolutePath();
		
//		System.out.println(qsubString);

		try {
			proc = rt.exec(qsubString);
			
			exitVal = proc.waitFor();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return exitVal;
		
	}
	
	public static int submitScript(String submission, int nodes, int processors, String memory) {
		// initialize runtime values
		Process proc = null;
		Runtime rt = Runtime.getRuntime();
		int exitVal = 0;
		
		Random rand = new Random();		
		String jobName = "job" + rand.nextInt(10000000);
		
		String location = System.getProperty("user.home") + "/.taverna-2.1.2/logs/";		
		File script = new File(location + jobName + ".sh");
		PrintWriter out = null;
				
		try {
			out = new PrintWriter(new FileWriter(script));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		out.println("#!/bin/sh");
		out.println("#PBS -q " + destination);
		out.println("#PBS -W group_list=" + group + ",block=true");
		out.println("#PBS -V");
		out.println("#PBS -l select=" + nodes + ":mem=" + memory + ":ncpus=" + processors);
		out.println("#PBS -N " + jobName);
		out.println("#PBS -o " + location + jobName + ".out");
		out.println("#PBS -e " + location + jobName + ".err");
		out.println();
		out.println("cd $PBS_O_WORKDIR");
		out.println(submission);
		
		out.close();
		
		String qsubString = "qsub " + script.getAbsolutePath();
		
//		System.out.println(qsubString);

		try {
			proc = rt.exec(qsubString);
			
			exitVal = proc.waitFor();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return exitVal;
	}
	



}
