package existence;

import java.io.File;

public class CreateFile {
	
	public static int test (String input, String output) {
		
		File in = new File(input);
		File out = new File(output);
		
		if (in.exists() != true) {
//			System.out.println("Error: Input file " + input + " does not exist");
			return -1;
		}
		
		if (out.exists() != true) {
//			System.out.println("Need to create output file! (DNE)");
			return 1;
		}
		else {
			if (in.lastModified() > out.lastModified()) {
//				System.out.println("Need to create output file! (outdated)");
				return 1;
			}
			else {
//				System.out.println("Nothing to be done, all files up to date");
				return 0;
			}
		}
		
	}

}
