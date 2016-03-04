package viewerV1;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;

import ch.ethz.ssh2.Connection;

import com.jidesoft.plaf.LookAndFeelFactory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class InputFile extends JFrame {
    
	private static final long serialVersionUID = 1L;
	
	//Variables declaration
    private JLabel userLabel, serverLabel, logLabel;
	private JButton runButton, browseButton, selectButton;
    private JLabel runLabel, browseLabel;
    private JTextField userTextField, serverTextField, logTextField;
    private JFileChooser fc;
    private JList list;
    private JFrame listFrame;
    private DefaultListModel listModel;
    private static String user, server, password;
	private static InputFile inputFile;
    private static String keyFileLocation, keyphrase;
    private static final JDialog dialog = new JDialog(inputFile, "Loading", false);
	
	//Default constructor
    public InputFile() {
    	password = null;
    	user = "";
    	server = "lc4.itc.virginia.edu";
        initComponents();
    }
    
    //Constructor with more info
    public InputFile(String _user, String _server, String _password, String _keyFileLocation, String _keyphrase){
    	user = _user;
    	server = _server;
    	password = _password;
    	keyFileLocation = _keyFileLocation;
    	keyphrase = _keyphrase;
    	inputFile = this;
    	initComponents();
    }
    
    public void centerScreen(){
    	//Center startup screen
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	Dimension size = this.getSize();
    	int y = (screenSize.height/2) - (size.height/2);
    	int x = (screenSize.width/2) -(size.width/2);
    	this.setLocation(x,y);
    }    
    
    // This method is called from within the constructor to initialize the form
    private void initComponents() {
        userTextField = new JTextField(user);
        serverTextField = new JTextField(server);
        logTextField = new JTextField();
        userLabel = new JLabel();
        serverLabel = new JLabel();
        logLabel = new JLabel();
        runButton = new JButton();
        browseButton = new JButton();
        runLabel = new JLabel();
        browseLabel = new JLabel();
        
        //Set font sizes
        Font defaultFont = new Font("sansserif",Font.PLAIN, 14);
        userTextField.setFont(defaultFont);
        userLabel.setFont(defaultFont);
        serverTextField.setFont(defaultFont);
        serverLabel.setFont(defaultFont);
        logTextField.setFont(defaultFont);
        logLabel.setFont(defaultFont);
        runButton.setFont(defaultFont);
        runLabel.setFont(defaultFont);
        browseButton.setFont(defaultFont);
        browseLabel.setFont(defaultFont);
        

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("PBS Monitor");
        setJMenuBar(createMenuBar());

        //inputLabel.setText("/home/pds3k/CPHG/indelRealign.log");
        userLabel.setText("User name");
        serverLabel.setText("Host");
        logLabel.setText("Log file name");
        //serverTextField.setSize(120,10);
        logTextField.setSize(100, 10);
        
        //Run log file on enter
        logTextField.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent evt){
        		runButtonActionPerformed(evt);
        	}
        });

        //Run button
        runButton.setText("Run");
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        
        //Browse button
        browseButton.setText("Browse");
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        runLabel.setText("Run the log file in the text field");
        browseLabel.setText("Browse for a log file");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(userTextField,60,60,60)// GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(serverTextField,150,150,150)//) GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serverLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(logTextField,150,150,150)//, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(logLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(runButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(runLabel))
                    .addGroup(layout.createSequentialGroup()
                    	.addComponent(browseButton)
                    	.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseLabel)))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {runButton, userTextField, browseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(userTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(userLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(serverTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(logTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(logLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(runButton)
                    .addComponent(runLabel))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(browseButton)
                    .addComponent(browseLabel))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        pack();
    }

    public JMenuBar createMenuBar() {
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;

        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the help menu.
        menu = new JMenu("About");
        menu.setMnemonic(KeyEvent.VK_A);  
        

        //Help menu items
        menuItem = new JMenuItem("Help");
        menuItem.setMnemonic(KeyEvent.VK_H);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuActionPerformed(evt);
            }
        });
        menu.add(menuItem);
        menuBar.add(menu);

        return menuBar;
    }
    
    //Help menu item pressed
    private void menuActionPerformed(ActionEvent e) {
    	//Check name of menu item, for multiple menu items
		//JMenuItem source = (JMenuItem)(e.getSource());
		//String name = source.getText();
		
		//Bring up a new frame with help information
		createHelpFrame();
	}
    
    //Get help text from a method instead of a .html file
    private static String getHelpText(){
    	String ret = "<html>" +
"<h1><u>Help</u></h1>" + 
"<h3>What is this?</h3>" +
"<!--Need to break line right about.....................................................here...............................................................................and here...............................................................................and here-->" +
"<p>This is PBS Monitor. It is a way to view the current jobs running from a given<br> workflow in a graphical way. The resulting interface is known as a gantt chart, which<br> is helpful for show start/stop times and other useful information.</p>" +
"<h3>How do I start it?</h3>" +
"<p>1) First, enter in your user name that you normally use to log into the cluster. The<br> default cluster is lc4, but you can change that if you want to log into another server.</p><br>" +
"<p>2) Next, you need to select the log file that you wish to view. This can be done in<br> two ways. First, you can type in the name of the log file (with the .log extension)<br> and click Run. Or you can browse for the log file by clicking Browse.</p><br>" +
"<p>3) You can browse for the log file on your local machine or on the cluster. If you<br> are on your local machine, simply open up the log file. If you browse the cluster,<br> highlight the correct log file from the list and click Select.</p><br>" +
"<p>4) Once you have selected a log file, simply wait for the program to gather data<br> from your log file and its cluster jobs.</p>" +
"<h3>Why won't it work for me?</h3>" +
"<p>The problem could be connected to a number of issues. Here are a few potential<br> solutions:</p><br>" + 
"<p>1) Make sure you correctly entered your user id, cluster host, and log file name.</p><br>" +
"<p>2) If you type in the log file manually, make sure it has the proper extension (.log).</p><br>" + 
"<p>3) The Monitor will always test your connection before you can Run or Browse for a<br> log file. If you get a message saying that the program couldn't connect to ssh,<br> you can try to fix this by either changing your key_file location or manually inputting<br> your password.</p><br>" + 
"<p>4) If you select \"Change KeyFile\", enter in the absolute location of your \"id_rsa\"<br> file. You can also enter in a key phrase if you have one set up.</p><br>" +
"<p>5) If you select \"Input Password\", try to input your password and click OK. If the<br> connection is successful, then your password will be saved.</p><br>" +
"<p>6) If you can't find the log file when you browse the cluster, make sure it is in<br> your /home/user/.taverna-2.1.2/logs directory.</p><br>" +
"<p>7) If all else fails, post a question to the TavernaPBS sourforge forum page:<br> https://sourceforge.net/projects/tavernapbs/forums/forum/1153657</p>" +
"</html>";
		
	return ret;
    }
    
    private static void createHelpFrame() {
        //Create and set up the window.
        JFrame frame = new JFrame("Help");
        
        //Create the content-pane-to-be.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);

        //Create a scrolled text label with html code
        JLabel theLabel = new JLabel(getHelpText());
        theLabel.setFont(new Font("sansserif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(theLabel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        //scrollPane.getVerticalScrollBar().setBlockIncrement(180);
            
        //Add the text area to the content pane.
        contentPane.add(scrollPane, BorderLayout.CENTER);
        frame.setContentPane(contentPane);

        //Display the window.
        frame.setSize(570, 470); 
        frame.setVisible(true);
    }
    
    //Test the ssh connection
    public boolean testConnection(){

		File keyfile = new File(keyFileLocation); // or "~/.ssh/id_rsa"
		String keyfilePass = keyphrase; // will be ignored if not needed
		
		try
		{
			//Create a connection instance
			Connection conn = new Connection(server);
			conn.connect();

			// Authenticate
			boolean isAuthenticated;
			//Try to authenticate with keyfile first
			isAuthenticated = conn.authenticateWithPublicKey(user, keyfile, keyfilePass);
			if(isAuthenticated){
				return true;
			}
			else{
				//if keyfile fails, then try to authenticate with password
				if(password!=null){
					isAuthenticated = conn.authenticateWithPassword(user, password);
				}
			}
			if (!isAuthenticated){
				throw new IOException("Authentication failed.");
			}
			else{
				return true;
			}
				
		}catch(IOException e){
			//Could not connect
			e.printStackTrace();
		}  
    	return false;
    }
    
  //Check if log file exists
    public boolean testLogFile(String logfile){
		
		try
		{
			// Authenticate
			String test;
			test = ReadData.executeOutRemote("ls .taverna-2.1.2/logs");
			boolean isAuthenticated = test.contains(logfile);

			if (isAuthenticated == false){
				throw new IOException("Log file not found.");
			}
			else{
				return true;
			}
				
		}catch(IOException e){
			//Log file not found
			e.printStackTrace();
		}    
    	return false;
    }
    
    //Try to create the gantt chart from the given log file
    private void runButtonActionPerformed(ActionEvent evt) {

    	//Create Gantt chart from info in text fields
    	
    	if (logTextField.getText().isEmpty()){
    		runLabel.setText("Please input the log file name");
    	}
    	else if(!logTextField.getText().contains(".")){
    		runLabel.setText("Make sure log file has extension");
    	}
    	else{ //log file is properly formatted
    		
    		if (userTextField.getText().isEmpty()){
        		userLabel.setText("Please input your user name");
        		runLabel.setText("Cannot run. See above errors.");
        	}
        	if (serverTextField.getText().isEmpty()){
        		serverLabel.setText("Please input the host name");
        		runLabel.setText("Cannot run. See above errors.");
        	}
        	if(!userTextField.getText().isEmpty() && !serverTextField.getText().isEmpty()){
        		String input = logTextField.getText();
        		user = userTextField.getText();
        		server = serverTextField.getText();
		        runLabel.setText(input);
		        ReadData.setUser(user);
		        ReadData.setClusterHost(server);
		        
		        //test connection
		        if(!testConnection()){
		        	//Bring up warning dialog if connection fails
		        	Object[] options = {"Change KeyFile", "Input Password"};
		        	int n = JOptionPane.showOptionDialog(this,"Couldn't connect to ssh.\nHow would you like to resolve this?", "Error",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		        	if(n==JOptionPane.YES_OPTION){	
		        		//open keyfile change dialog box
		        		createKeyFilePane();
		        	}
		        	else if(n==JOptionPane.NO_OPTION){
		        		//Open login dialog box
		        		createLoginPane();
		        	}
		        	else{
		        		//close dialog box
		        	}
		        }
		        else{
		        	//if connection works, test if log file exists

		        	if(!testLogFile(input)){
		        		//Bring up warning dialog if log file doesn't exist
			        	JOptionPane.showMessageDialog(this,
	                            "Couldn't find log file.",
	                            "Error",
	                            JOptionPane.WARNING_MESSAGE);
		        	}
		        	else{
		        		
				        runProgram(input);
		        	}

		        }
		       
        	}       
    	}
       
    }
    
    //Login dialog box
    private void createLoginPane(){
    	JLabel jServerName = new JLabel("Host");
        JTextField serverName = new JTextField(server);
		JLabel jUserName = new JLabel("User Name");
        JTextField userName = new JTextField(user);
        JLabel jPassword = new JLabel("Password");
        JTextField passWord = new JPasswordField();
        Object[] ob = {jServerName, serverName, jUserName, userName, jPassword, passWord};
        int result = JOptionPane.showConfirmDialog(null, ob, "Please input password for PBS Monitor", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
        	server = serverName.getText();
            user = userName.getText();
            password = passWord.getText();
            //test connection with new info
            if(!testConnection()){
            	//Still unable to login.
				Object[] options = {"Change KeyFile", "Input Password"};
	        	int n = JOptionPane.showOptionDialog(this,"Still couldn't connect to ssh.\nHow would you like to resolve this?", "Error",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
	        	if(n==JOptionPane.YES_OPTION){	
	        		//open keyfile change dialog box
	        		createKeyFilePane();
	        	}
	        	else if(n==JOptionPane.NO_OPTION){
	        		//Open login dialog box
	        		createLoginPane();
	        	}
	        	else{
	        		//close dialog box
	        	}
            }
            else{
            	//Connection worked, save password
            	userTextField.setText(user);
            	serverTextField.setText(server);
            	ReadData.setPassword(password);
            	JOptionPane.showMessageDialog(this,
                        "Connection successful. Password saved.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    //Key file info dialog box
    private void createKeyFilePane(){
    	JLabel jKeyFileLocation = new JLabel("Key File location");
		JTextField keyfileLocation = new JTextField(keyFileLocation);
		JLabel jKeyPhrase = new JLabel("Key Phrase for detected Key");
		JTextField keyPhrase = new JPasswordField();
		Object[] ob = {jKeyFileLocation, keyfileLocation, jKeyPhrase, keyPhrase};
		int result = JOptionPane.showConfirmDialog(null, ob, "Please input key file info for PBS Monitor", JOptionPane.OK_CANCEL_OPTION);
		
		//TODO Add browse button to browse local machine for key file location
		/*
		 //Browse local machine
	            	fc = new JFileChooser();
	            	//FileChooserDemo FCD = new FileChooserDemo();
	            	int returnVal = fc.showOpenDialog(this);

	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                    File file = fc.getSelectedFile();
	                    //Run the file.
	            	    		String input = fc.getName(file); 
	            	    		//System.out.println(input);
	            	    		        runLabel.setText(input);
	            	    		        runProgram(input);
	                	}
		 */
		
		if (result == JOptionPane.OK_OPTION) {
			keyphrase = keyPhrase.getText();
			keyFileLocation = keyfileLocation.getText();
			
			//test connection again
			if(!testConnection()){
            	//Still unable to login.
				Object[] options = {"Change KeyFile", "Input Password"};
	        	int n = JOptionPane.showOptionDialog(this,"Still couldn't connect to ssh.\nHow would you like to resolve this?", "Error",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
	        	if(n==JOptionPane.YES_OPTION){	
	        		//open keyfile change dialog box
	        		createKeyFilePane();
	        	}
	        	else if(n==JOptionPane.NO_OPTION){
	        		//Open login dialog box
	        		createLoginPane();
	        	}
	        	else{
	        		//close dialog box
	        	}
	        }
            else{
            	//Connection worked, save keyfile location and keyphrase
            	ReadData.setKeyFileLocation(keyFileLocation);
            	ReadData.setKeyPhrase(keyphrase);
            	JOptionPane.showMessageDialog(this,
                        "Connection successful. Key File info saved.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
		}
    }
    
    //Browse local directory or files in /home/user/.taverna-2.1.2/logs/ directory
    private void browseButtonActionPerformed(ActionEvent evt) {
    	if(userTextField.getText().isEmpty() || serverTextField.getText().isEmpty()){
    		browseLabel.setText("Enter in user name and/or Host");
    	}
    	else{
    		//Pass user and server values on
    		user = userTextField.getText();
    		server = serverTextField.getText();
    		ReadData.setUser(user);
	        ReadData.setClusterHost(server);
	        
	      //test connection
	        if(!testConnection()){
	        	//Bring up warning dialog if connection fails
	        	Object[] options = {"Change KeyFile", "Input Password"};
	        	int n = JOptionPane.showOptionDialog(this,"Couldn't connect to ssh.\nHow would you like to resolve this?", "Error",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
	        	if(n==JOptionPane.YES_OPTION){	
	        		//open keyfile change dialog box
	        		createKeyFilePane();
	        	}
	        	else if(n==JOptionPane.NO_OPTION){
	        		//Open login dialog box
	        		createLoginPane();
	        	}
	        	else{
	        		//close dialog box
	        	}
	        }
	        else{
	        	//Connection successful, open Browse options
	    		Object[] options = {"Local Machine", "Cluster"};
	            int n = JOptionPane.showOptionDialog(this,"Where do you want to browse for the log file?", "Browse",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
	            if (n==JOptionPane.YES_OPTION){
	            	//Browse local machine
	            	fc = new JFileChooser();
	            	//FileChooserDemo FCD = new FileChooserDemo();
	            	int returnVal = fc.showOpenDialog(this);

	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                    File file = fc.getSelectedFile();
	                    //Run the file.
	            	    		String input = fc.getName(file); 
	            	    		//System.out.println(input);
	            	    		        runLabel.setText(input);
	            	    		        runProgram(input);
	                	}
	            }
	            else if (n==JOptionPane.NO_OPTION){
	            	//Browse the cluster
	            	createListFrame();
	            }
	            else{
	            	//Close frame
	            }
	        }
    	}		      
    }
 
    //Create clickable list of log files
    private void createListFrame() {
        //Instantiate cluster variables
		String clusterLocation = ".taverna-2.1.2/logs/";
		String command = "ls " + clusterLocation;
		
		//Get files in .taverna-2.1.2/logs/ directory
		String output;
		try {
			output = ReadData.executeOutRemote(command);

		//Parse log files from output
	    Scanner s = new Scanner(output);
	    String temp;
	    ArrayList<String> logs = new ArrayList<String>();
	    while(s.hasNextLine()){
	    	temp = s.nextLine();
	    	if(temp.endsWith(".log")){
	    		logs.add(temp);
	    	}
	    }
	    s.close();		
	    
		//Create list panel
		JPanel listPanel = new JPanel(new BorderLayout());
		listModel = new DefaultListModel();
		
		//Populate listModel
		for(int i =0; i<logs.size();i++){
			listModel.addElement(logs.get(i));
		}
		
		//Create the list and put it in a scroll pane.
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        //list.addListSelectionListener(listPanel);
        list.setVisibleRowCount(5);
        list.setFont(new Font("sansserif", Font.BOLD, 16));
        JScrollPane listScrollPane = new JScrollPane(list);
        
        //Create select button
        selectButton = new JButton("Select");
        selectButton.setActionCommand("Select");
        selectButton.addActionListener(new SelectListener());
        
      //Create a panel that uses BoxLayout.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(selectButton);
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        listPanel.add(listScrollPane, BorderLayout.CENTER);
        listPanel.add(buttonPane, BorderLayout.PAGE_END);
        
		//Create and set up the window.
        listFrame = new JFrame("Cluster Browse");
        
      //Create and set up the content pane.
        JComponent newContentPane = listPanel;
        newContentPane.setOpaque(true); //content panes must be opaque
        listFrame.setContentPane(newContentPane);
        
        //Set size of frame
        listFrame.pack();
        listFrame.setSize((listFrame.getSize().width*3)/2, (listFrame.getSize().height*3)/2);
        
      //Center the frame
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	Dimension size = listFrame.getSize();
    	int y = (screenSize.height/2) - (size.height/2);
    	int x = (screenSize.width/2) -(size.width/2);
    	listFrame.setLocation(x,y);

        //Display the window.
        listFrame.setVisible(true);
        
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    //Listen for select button being pressed
    class SelectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	if(list.getSelectedIndex() == -1){
        		//No item selected. Do nothing
        	}
        	else{
        		//Open up the file
        		
        		 int index = list.getSelectedIndex();
                 String file = (String) listModel.get(index);
                runLabel.setText(file);
 		        listFrame.setVisible(false);
 		        runProgram(file);
        	}  
        }
    }
    
  //This method is required by ListSelectionListener.
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {

            if (list.getSelectedIndex() == -1) {
            //No selection, disable fire button.
                selectButton.setEnabled(false);

            } else {
            //Selection, enable the fire button.
                selectButton.setEnabled(true);
            }
        }
    }
    
    public static void runProgram(String input){
    	//create loading window
    	Object[] options = {"Cancel"};
        final JOptionPane optionPane = new JOptionPane("Gathering job data...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.CANCEL_OPTION, null, options);

        dialog.setContentPane(optionPane);
        //Cannot 'X' out of this window to close it. It close automatically when the gantt chart is ready.
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        optionPane.addPropertyChangeListener(
	    new PropertyChangeListener() {
	        public void propertyChange(PropertyChangeEvent e) {
	            String prop = e.getPropertyName();
	            
	            //Listen for "Cancel" button being clicked
	            if (dialog.isVisible() && (e.getSource() == optionPane) && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
	                
	                dialog.setVisible(false);
	                //TODO If "Cancel" clicked, Kill loading process and go to Startup screen
	            }
	        }
	    });
        dialog.pack();
      //Center window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	Dimension size = dialog.getSize();
    	int y = (screenSize.height/2) - (size.height/2);
    	int x = (screenSize.width/2) -(size.width/2);
    	dialog.setLocation(x,y);
    	
    	if(inputFile!=null){
    		inputFile.setVisible(false);	
    	}
    	dialog.setVisible(true);        
    	
    	//Run the gantt chart
		MyProjectGanttChartDemo.runGantt(input);
    }
    
    public static void main(String args[]) {
    	
    	//First line is for JIDE license agreement. DO NOT REMOVE.
    	com.jidesoft.utils.Lm.verifyLicense("Aaron Mackey", "TavernaPBS", ".gfLqXDA3eUAP0XxgznnRj1d9syRojY1");
    	
    	//Check for command line arguments
    	if(args.length != 0){
    		
    		//Put arguments in format user password
    		String logfile = args[0];
    		user = args[1];
    		password = args[2];
    		server = args[3];
    		keyFileLocation = args[4];
    		keyphrase = args[5];
    		
    		//Set values for other classes
    		ReadData.setUser(user);
        	ReadData.setPassword(password);
        	ReadData.setClusterHost(server);
        	ReadData.setKeyFileLocation(keyFileLocation);
        	ReadData.setKeyPhrase(keyphrase);
        	
    		runProgram(logfile);    		
    	}
    	else{
    		EventQueue.invokeLater(new Runnable() {
                public void run() {   
                	
                	//Set default keyFileLocation and keyphrase
                	if (System.getProperty("os.name").contains("Windows")) {
            			
            			if (System.getProperty("os.name").contains("XP")) {
            				keyFileLocation = System.getProperty("user.home") + "\\My Documents\\id_rsa";
            			}
            			else {
            				keyFileLocation = System.getProperty("user.home") + "\\Documents\\id_rsa";
            			}
            		}
            		else {
            			keyFileLocation = System.getProperty("user.home") + "/.ssh/id_rsa";
            		}
            		ReadData.setKeyFileLocation(keyFileLocation);
            		keyphrase = "dummy";
            		ReadData.setKeyPhrase(keyphrase);
            		
            		//Create GUI
                	LookAndFeelFactory.installDefaultLookAndFeelAndExtension();
                	inputFile = new InputFile();
                	
                	//Center startup screen
                	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                	Dimension size = inputFile.getSize();
                	int y = (screenSize.height/2) - (size.height/2);
                	int x = (screenSize.width/2) -(size.width/2);
                	inputFile.setLocation(x,y);
                	
                	//Make screen visible
                	inputFile.setVisible(true);
                }
            }); 
    	}
    		 
    }
    
    //Getters and setters
    public static void setUser(String use) {
		user = use;
	}

	public static void setServer(String serve) {
		server = serve;
	}

	public static void setPassword(String pword) {
		password = pword;
	}

	public static void setKeyFileLocation(String keyfileLocation) {
		keyFileLocation = keyfileLocation;
	}

	public static void setKeyphrase(String keyPhrase) {
		keyphrase = keyPhrase;
	}

	public static JDialog getDialog() {
		return dialog;
	}  
}