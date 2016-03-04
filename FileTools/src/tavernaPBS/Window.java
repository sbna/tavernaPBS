package tavernaPBS;


import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Window {
	
	public static void error(String title, String message) {
		
		String[] messageLines = message.split("\n");
		
		JLabel[] labels = new JLabel[messageLines.length];
		
		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel(messageLines[i]);
		}
		
		JOptionPane.showMessageDialog(null, labels, title, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void warning(String title, String message) {
		
		String[] messageLines = message.split("\n");
		
		JLabel[] labels = new JLabel[messageLines.length];
		
		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel(messageLines[i]);
		}
		
		JOptionPane.showMessageDialog(null, labels, title, JOptionPane.WARNING_MESSAGE);
	}

}
