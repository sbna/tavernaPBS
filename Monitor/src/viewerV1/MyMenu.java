package viewerV1;
import java.awt.*;
import java.awt.event.*;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.ImageIcon;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JFrame;

public class MyMenu implements ActionListener{
    JTextArea output;
    JScrollPane scrollPane;
    MyProjectGanttChartDemo _demo;
    JRadioButtonMenuItem refreshOn, refreshOff;
    
    public MyMenu(Demo demo){
    	_demo = (MyProjectGanttChartDemo) demo;
    }
    public MyMenu(){
    	//default constructor
    }
    
    //Getters and setters
    public JRadioButtonMenuItem getRefreshOn() {
		return refreshOn;
	}
	public JRadioButtonMenuItem getRefreshOff() {
		return refreshOff;
	}

    public JMenuBar createMenuBar() {
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;

        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        menu = new JMenu("Options");
        menu.setMnemonic(KeyEvent.VK_O);  
        menuBar.add(menu);

        //a group of JMenuItems
        menuItem = new JMenuItem("Resize periods to fit");
        menuItem.setMnemonic(KeyEvent.VK_R);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Resize periods to fit visible rows");
        menuItem.setMnemonic(KeyEvent.VK_V);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Get new log file");
        menuItem.setMnemonic(KeyEvent.VK_A);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Manual Refresh");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        //Add Radio buttons for Auto refresh on/off
        ButtonGroup group = new ButtonGroup();

        refreshOn = new JRadioButtonMenuItem("Auto-Refresh On");
        refreshOn.setSelected(true);
        refreshOn.setMnemonic(KeyEvent.VK_N);
        refreshOn.addActionListener(this);
        group.add(refreshOn);
        menu.add(refreshOn);

        refreshOff = new JRadioButtonMenuItem("Auto-Refresh Off");
        refreshOff.setMnemonic(KeyEvent.VK_F);
        refreshOff.addActionListener(this);
        group.add(refreshOff);
        menu.add(refreshOff);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Killjobs");
        menuItem.setMnemonic(KeyEvent.VK_K);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        //Add a help feature for this menu for the gantt chart display
        
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);  
        menuBar.add(menu);

        menuItem = new JMenuItem("Help Me");
        menuItem.setMnemonic(KeyEvent.VK_M);
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        /*
        //a group of check box menu items
        menu.addSeparator();
        cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
        cbMenuItem.setMnemonic(KeyEvent.VK_C);
        menu.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Another one");
        cbMenuItem.setMnemonic(KeyEvent.VK_H);
        menu.add(cbMenuItem);

        //a submenu
        menu.addSeparator();
        submenu = new JMenu("A submenu");
        submenu.setMnemonic(KeyEvent.VK_S);

        menuItem = new JMenuItem("An item in the submenu");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_2, ActionEvent.ALT_MASK));
        submenu.add(menuItem);

        menuItem = new JMenuItem("Another item");
        submenu.add(menuItem);
        menu.add(submenu);
        
        //Build second menu in the menu bar.
        menu = new JMenu("Another Menu");
        menu.setMnemonic(KeyEvent.VK_N);
        menu.getAccessibleContext().setAccessibleDescription(
                "This menu does nothing");
        menuBar.add(menu);
        */

        return menuBar;
    }

	public Container createContentPane() {
        //Create the content-pane-to-be.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);

        //Create a scrolled text area.
        output = new JTextArea(5, 30);
        output.setEditable(false);
        scrollPane = new JScrollPane(output);

        //Add the text area to the content pane.
        contentPane.add(scrollPane, BorderLayout.CENTER);

        return contentPane;
    }

    // Returns an ImageIcon, or null if the path was invalid.
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = MyMenu.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        MyMenu demo = new MyMenu();
        frame.setJMenuBar(demo.createMenuBar());
        frame.setContentPane(demo.createContentPane());

        //Display the window.
        frame.setSize(450, 260);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
		String name = source.getText();
		
		//Determine correct action based on source
		if(name.equals("Resize periods to fit")){
			_demo.resizePressed();
		}
		else if(name.equals("Resize periods to fit visible rows")){
			_demo.resizeVisiblePressed();
		}
		else if(name.equals("Get new log file")){
			_demo.newLogPressed();
		}

		else if(name.equals("Auto-Refresh On")){
			_demo.autoRefreshOnPressed();
		}
		else if(name.equals("Auto-Refresh Off")){
			_demo.autoRefreshOffPressed();
		}
		else if(name.equals("Manual Refresh")){
			_demo.refreshPressed();
		}
		
		else if(name.equals("Killjobs")){
			_demo.killjobsPressed();
		} 
		
		else if(name.equals("Help Me")){
			_demo.helpMePressed();
		} 
		
		
	}
}