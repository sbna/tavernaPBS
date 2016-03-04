package tavernaPBS;

import java.util.List;

public class Merge {

	// merge a list of strings into one, space-separated string
	public static String fileList(List<String> files) {
		
		if (files == null) {
			return "";
		}
		
		StringBuffer temp = new StringBuffer();
		
		if (files.isEmpty()) {
			return "";
		}
		else {
			temp.append(files.get(0));
		}
		
		for (int i = 1; i < files.size(); i++) {
			temp.append(" " + files.get(i));
		}
		
		return temp.toString();
	}
	
	// merge a list of strings into one string with the given spacer used as a divider
	public static String fileList(List<String> files, String spacer) {
		if (files == null) {
			return "";
		}
		
		StringBuffer temp = new StringBuffer();
		
		if (files.isEmpty()) {
			return "";
		}
		else {
			temp.append(files.get(0));
		}
		
		for (int i = 1; i < files.size(); i++) {
			temp.append(spacer + files.get(i));
		}
		
		return temp.toString();
	}
	
	// merge a list of PBS objects into one PBS object
	public static PBS pbsList(List<String> pbs) {
		
		if (pbs == null) {
			return null;
		}
		
		PBS head;
		
		if (pbs.isEmpty()) {
			return null;
		}
		else {
			head = PBSConvert.toPBS(pbs.get(0));
		}
		
		for (int i = 1; i < pbs.size(); i++) {
			head.merge(PBSConvert.toPBS(pbs.get(i)));
		}
		
		return head;
		
	}
}
