package systemtools;

import java.util.List;

public class Extract {
	
	// extract the absolute directory from an absolute file path
	
	public static String getDirectory(String absolutePath) {
		
		int index = absolutePath.lastIndexOf('/');
		
		return (absolutePath.substring(0, index) + '/');
		
	}
	
	// extract the absolute directory from a list of files (grabs the first directory it finds)
	public static String getDirectory(List<String> absolutePaths) {
		
		return getDirectory(absolutePaths.get(0));
	}
	
	// extract the file name from an absolute file path
	
	public static String getFile(String absolutePath) {
		
		int index = absolutePath.lastIndexOf('/');
		
		index++;
		
		return (absolutePath.substring(index));
	}
	
	// extract the file extension
	
	public static String getExtension(String absolutePath) {
		
		int index = absolutePath.lastIndexOf('.');
		
		return (absolutePath.substring(index));
	}

}
