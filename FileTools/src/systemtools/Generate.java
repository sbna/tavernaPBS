package systemtools;

import java.util.ArrayList;


public class Generate {
	
	public static ArrayList<Float> numberList(double start, double end, double interval) {
		
		Double startD = start;
		Double endD = end;
		Double intervalD = interval;
		
		
		ArrayList<Float> numberArray = new ArrayList<Float>();
		
		if (start < end) {
			for (Double i = startD; i < end; i += intervalD) {
				numberArray.add(i.floatValue());
			}
		}
		else {
			for (Double i = startD; i > end; i -= intervalD) {
				numberArray.add(i.floatValue());
			}
		}
		
		numberArray.add(endD.floatValue());
		
		return numberArray;
		
	}
	
}
