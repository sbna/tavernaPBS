package viewerV1;

import com.jidesoft.gantt.DateGanttChartPane;
import com.jidesoft.gantt.DefaultGanttEntry;
import com.jidesoft.gantt.DefaultGanttEntryRelation;
import com.jidesoft.gantt.DefaultGanttEntryRenderer;
import com.jidesoft.gantt.DefaultGanttModel;
import com.jidesoft.gantt.GanttChart;
import com.jidesoft.gantt.GanttEntry;
import com.jidesoft.gantt.GanttEntryRelation;
import com.jidesoft.gantt.TaskBar;
import com.jidesoft.grid.*;
import com.jidesoft.plaf.GanttUIDefaultsCustomizer;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.plaf.basic.DefaultTaskBarPainter;
import com.jidesoft.range.TimeRange;
import com.jidesoft.scale.*;
import com.jidesoft.swing.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("serial")
public class MyProjectGanttChartDemo extends AbstractDemo {

    private DateGanttChartPane<DefaultGanttEntry<Date>> _ganttChartPane;
    //Added classes for UIDefaults
    private static MyTaskBarPainter tbp;

    private int _filterAppliedRowCount;
    private StyledLabel _filterTimesLabel;
    private static String logfile = null;
    private static MyProjectGanttChartDemo myDemo;
    private static boolean autoRefresh;
    private static Timer timer;
    private static long refreshDelay = 30*1000; //Default refresh rate of 30 seconds
    private static Point point;
        
    //Array list of tasks to populate gantt chart.
    private static ArrayList<Task> tasks;

    public MyProjectGanttChartDemo() {
    	tbp = new MyTaskBarPainter();
    }
    
    //Getters and setters    
    public void setTasks(ArrayList<Task> _tasks){
    	tasks = _tasks;
    }

    public static Timer getTimer() {
		return timer;
	}

	public static void setTimer(Timer _timer) {
		timer = _timer;
	}

	public String getName() {
    	String date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(System.currentTimeMillis()));
        return "PBS Monitor: " + date;
    }

    public String getProduct() {
        return PRODUCT_NAME_GANTT_CHART;
    }

    @Override
    public String getDescription() {
        return "PBS Monitor";
    }

    @Override
    public Component getOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new JideBoxLayout(panel, JideBoxLayout.Y_AXIS, 5));

        JButton button = new JButton(new AbstractAction("Resize periods to fit") {
            public void actionPerformed(ActionEvent e) {
                _ganttChartPane.getGanttChart().autoResizePeriods(false);
            }
        });
        panel.add(button);

        button = new JButton(new AbstractAction("Resize periods to fit visible rows") {
            public void actionPerformed(ActionEvent e) {
                _ganttChartPane.getGanttChart().autoResizePeriods(true);
            }
        });
        panel.add(button);
        
        //Add button for inputting new log file, make old chart close upon opening new file
        button = new JButton(new AbstractAction("Add new Log file") {
            public void actionPerformed(ActionEvent e) {
                InputFile.main(null);
                tasks = null;
                _ganttChartPane = null;
                dispose(); //Doesn't completely tear down old gantt chart 
            }
        });
        panel.add(button);
        return panel;
    }
    
    //Methods for pressing menu buttons   
    public void resizePressed(){
    	//System.out.println("Resize periods to fit");
    	_ganttChartPane.getGanttChart().autoResizePeriods(false);
    }
    
    public void resizeVisiblePressed(){
    	//System.out.println("Resize periods to fit visible rows");
    	_ganttChartPane.getGanttChart().autoResizePeriods(true);
    }
    
    //Get a new log file
    public void newLogPressed(){
    	//System.out.println("Add new log button");
    	//InputFile.main(null);
    	
    	//Create new input file with same connection data
    	InputFile iFile = new InputFile(ReadData.getUser(), ReadData.getClusterHost(), ReadData.getPassword(), ReadData.getKeyFileLocation(), ReadData.getKeyPhrase());
    	
    	//Remove old gantt chart
        tasks = null;
        _ganttChartPane = null;
        iFile.centerScreen();
        iFile.setVisible(true);
        dispose(); //Doesn't completely tear down old gantt chart
    }
    
    //Manual refresh
    public void refreshPressed(){
    	//System.out.println("Refresh");
    	//Create new GanttChart object 	
    	if(!logfile.contains(".")){
    		//if the browse file returns just the file name w/o extension, add extension
    		logfile = logfile.concat(".txt");
    	}
    	timer.cancel();
    	timer = new Timer();
    	//System.out.println(logfile);
    	  EventQueue.invokeLater(new Runnable() {
            public void run() {
            	  //Try to add UIDefaults for new color
            	  myDemo = new MyProjectGanttChartDemo();
            	  GanttUIDefaultsCustomizer test = new GanttUIDefaultsCustomizer();
            	  UIDefaults defaults = new UIDefaults();
            	  defaults.put("TaskBar.painter", tbp);
            	  test.customize(defaults);
            	  
                  LookAndFeelFactory.addUIDefaultsCustomizer(test);
                  LookAndFeelFactory.installDefaultLookAndFeelAndExtension();
                  repaintFrame(myDemo);
                  
                //Resize periods to fit
                  myDemo._ganttChartPane.getGanttChart().autoResizePeriods(false);
              }
          });    	 
    }
    
    //Handle auto-refresh options
    public void autoRefreshOnPressed(){
    	//Only create new timer if not already on auto-refresh
    	if(!autoRefresh){
    		timer.cancel();
    		timer = new Timer();
    		timer.schedule(new RefreshTask(), 0, refreshDelay);
    	}
    	autoRefresh = true;
    }
    
    public void autoRefreshOffPressed(){
    	autoRefresh = false;
    	timer.cancel();
    }
    
    //Kill jobs from a log file
    public void killjobsPressed(){
    	try {
			String killjobs = ReadData.executeOutRemote("/h3/t1/users/mjl3p/killjobs " + ".taverna-2.1.2/logs/" + logfile);
			System.out.println(killjobs);
			
			JOptionPane.showMessageDialog(_ganttChartPane, "Killjobs run successfully", "Jobs Killed", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void helpMePressed(){
    	createHelpFrame();
    	//System.out.println("Help Me");
    }
    
    //Get html code for Help window on the gantt chart
    private static String getHelp2Text(){
    	String ret = "<html>" + 
"<h1><u>Gantt Help</u></h1>" + 
"<h3>What is this?</h3>" +
"<!--Need to break line right about.....................................................here...............................................................................and here...............................................................................and here-->" + 
"<p>This is a gantt chart created by PBS Monitor. It displays the status of current jobs<br> running on the cluster from a given workflow.</p>" +
"<h3>How do I read the gantt chart?</h3>" +
"<p>1) The gray bar at the top of the right-hand pane represents the workflow that this<br> chart was called with. It's length determines how long the jobs in the workflow<br> took to run or how long they are expected to run for.</p><br>" +
"<p>2) Each bar below the top bar represents a job. The color of the bar tells its status:</p>" +
"<ul>" +
"<li>Yellow = Queued/Waiting</li>" +
"<li>Green = Running normally</li>" +
"<li>Orange = Running/Completed but overdue</li>" +
"<li>Blue = Completed without errors</li>" +
"<li>Red = Completed with errors</li>" +
"<li>Magenta = Other (Right click on job and see \"Extra Job Info\")</li>" +
"</ul>" +
"<p>3) A black bar within a job represents its percent completed. The longer the bar,<br> the closer the job is to being finished. Queued jobs should not have a black bar<br> because they have not started running yet.</p><br>" + 
"<p>4) The length of a job bar represents the job's runtime or expected runtime if not<br> completed. If a job goes over the expected runtime, the bar will turn orange.</p><br>" +
"<p>5) The arrows signify job dependencies. A job with an arrow coming out of it is a<br> parent of the job it points to. I job cannot begin running until its parent(s) has<br> finished running.</p><br>" +
"<p>6) The table on the left has information related to each job's name, node origin,<br> runtime (expected or actual depending on completion), completion percentage,<br> start/stop times, and exit status</p>" +
"<h3>What can I do with this gantt chart?</h3>" +
"<p>1) If the chart is too small, you can resize the window and then click on Options<br> and then \"Resize periods to fit\". That should make the job bars longer.</p><br>" +
"<p>2) The gantt chart has an auto-refresh time of 30 seconds. You can turn this on/off<br> in the Options menu.</p><br>" +
"<p>3) If you want to refresh the display manually, you can click on Options->Manual<br> Refresh. This will bring up an updated gantt chart.</p><br>" +
"<p>4) If you want to see more information about a specific job, you can either double<br> click or right click on a job to bring up a window with more job details.</p><br>" + 
"<p>5) If you want to bring up an entirely new log file, you can click on Options->Add<br> new log file. This will send you back to the startup screen where you can input<br> new log file information.</p>" +
"</html>";	
    	return ret;
    }
    
    //Help window
    private static void createHelpFrame() {
        //Create and set up the window.
        JFrame frame = new JFrame("Gantt Help");
        
        //Create the content-pane-to-be.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);

        //Create a scrolled text label with html code
        JLabel theLabel = new JLabel(getHelp2Text());
        theLabel.setFont(new Font("sansserif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(theLabel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        //Add the text area to the content pane.
        contentPane.add(scrollPane, BorderLayout.CENTER);
        frame.setContentPane(contentPane);

        //Display the window.
        frame.setSize(570, 470);
        frame.setVisible(true);
    }
    
    //Refreshing the frame for auto-refresh
    class RefreshTask extends TimerTask{
    	public void run(){
    		//System.out.println("Refresh");
        	//Create new GanttChart object 	
        	if(!logfile.contains(".")){
        		//if the browse file returns just the file name w/o extension, add extension
        		logfile = logfile.concat(".txt");
        	}
        	timer.cancel();
        	timer = new Timer();
        	//System.out.println(logfile);
        	  EventQueue.invokeLater(new Runnable() {
                public void run() {
                	  //Try to add UIDefaults for new color
                	  myDemo = new MyProjectGanttChartDemo();
                	  GanttUIDefaultsCustomizer test = new GanttUIDefaultsCustomizer();
                	  UIDefaults defaults = new UIDefaults();
                	  defaults.put("TaskBar.painter", tbp);
                	  test.customize(defaults);
                	  
                      LookAndFeelFactory.addUIDefaultsCustomizer(test);
                      LookAndFeelFactory.installDefaultLookAndFeelAndExtension();
                      repaintFrame(myDemo);
                      
                    //Resize periods to fit
                      myDemo._ganttChartPane.getGanttChart().autoResizePeriods(false);
                  }
              });
    	}
    }
    
    //Create window to display detailed information for a specific job
    public void displayJobInfo(int row){
    	//Don't display anything for startTask
    	if(row == 0){
    		return;
    	}
    	int index = row-1;
    	Task task = tasks.get(index);
    	 	
    	try {
    		//Get Task info
    		String tInfo = task.printNeat();
    		
    		String temp = "";
    		//Get qstat -f data
			String qstatf = ReadData.executeOutRemote("qstat -f " + task.getJobID());
			//ignore qstat call if it is not available (job has finished running)
			if(qstatf.startsWith("qstat:") || qstatf.isEmpty()){
				qstatf = "Job not available on qstat\n";
			}
			else{
				//create scanner for qstat
				Scanner s1 = new Scanner(qstatf);
				qstatf = "Qstat -f Data\n\n" + ReadData.ReadQstatf(s1);
				s1.close();
			}
			
			//Get tracejob info
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd"); //Get day of month
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM"); //Get month
			int day = Integer.parseInt(dateFormat.format(new Date())); //Get current day as int
			int month = Integer.parseInt(dateFormat2.format(new Date())); //Get current month as int
			int startday = Integer.parseInt(dateFormat.format(new Date(task.getStartTime()))); //Get task start day as int
			int startmonth = Integer.parseInt(dateFormat2.format(new Date(task.getStartTime()))); //Get task start month as int
			int days = 31*(month-startmonth) + (day-startday) + 1; //Determine number of days to look back in tracejob
			String tracejob = ReadData.executeOutRemote("tracejob -n " + days + " " + task.getJobID());
			
			//create scanner for tracejob
			Scanner s2 = new Scanner(tracejob);
			temp = "";
			while(s2.hasNextLine()){
				temp = temp.concat(s2.nextLine() + "\n");
			}
			s2.close();
			tracejob = "Tracejob Data:\n" + temp;
			
			//Create frame
			JFrame frame = new JFrame(task.getJobName());
			
	        //Create the content-pane-to-be.
	        JPanel contentPane = new JPanel(new BorderLayout());
	        contentPane.setOpaque(true);
	        
	        //Set text of frame
	        //System.out.println(qstatf + "\n" + tracejob);
	        JTextArea theText = new JTextArea(tInfo + "\n" + qstatf + "\n" + tracejob);
	        theText.setFont(new Font("sansserif", Font.PLAIN, 14));
	        JScrollPane scrollPane = new JScrollPane(theText);
	        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
	        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
	        
	        //Add the text area to the content pane.
	        contentPane.add(scrollPane, BorderLayout.CENTER);
	        frame.setContentPane(contentPane);

	        //Display the window.
	        frame.setSize(450, 440);
	        frame.setVisible(true);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    //Create demo panel
    public Component getDemoPanel() {
    	//Change UI Default TaskBar.painter to MyTaskBarPainter
        UIDefaults uiDefaults = UIManager.getDefaults();
        uiDefaults.remove("TaskBar.painter");
        uiDefaults.put("TaskBar.painter",tbp);
       
    	//Filter search bar
        TaskTreeTableModel tableModel = new TaskTreeTableModel();
        tableModel.setValueAt("Test", 1, 1);
        final JPanel quickSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 2));
        QuickTableFilterField filterField = new QuickTableFilterField(tableModel);
        filterField.setObjectConverterManagerEnabled(true);
        quickSearchPanel.add(new JLabel("Filter:"));
        quickSearchPanel.add(filterField);
        
        JPanel tablePanel = new JPanel(new BorderLayout(2, 2));
        final JLabel label = new JLabel("Click on \"Auto Filter\" in option panel to enable AutoFilter feature");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        
        //Set display of filter tabs
        final SortableTreeTableModel<DefaultGanttEntry<Date>> sortableTreeTableModel =
                new SortableTreeTableModel<DefaultGanttEntry<Date>>(
                        new FilterableTreeTableModel<DefaultGanttEntry<Date>>(filterField.getDisplayTableModel()));
        sortableTreeTableModel.addIndexChangeListener(new IndexChangeListener() {
            public void indexChanged(IndexChangeEvent event) {
                if (event.getSource() instanceof FilterableTableModel && event.getType() == IndexChangeEvent.INDEX_CHANGED_EVENT && _filterTimesLabel != null) {
                    FilterableTableModel model = (FilterableTableModel) event.getSource();
                    _filterAppliedRowCount = model.retrieveFilterApplyRecords();
                    if (_filterAppliedRowCount != 0) {
                        StyledLabelBuilder.setStyledText(_filterTimesLabel, "Filter is applied on {" + _filterAppliedRowCount + ":f:blue} rows. {" + model.getRowCount() + ":f:blue} rows are left.");
                    }
                    else {
                        StyledLabelBuilder.setStyledText(_filterTimesLabel, "Filter in this level is cleared. {" + model.getRowCount() + ":f:blue} rows are shown up from the table model it wraps.");
                    }
                }
            }
        });

        //Set the date period of the chart side
        DefaultGanttModel<Date, DefaultGanttEntry<Date>> model = new DefaultGanttModel<Date, DefaultGanttEntry<Date>>();
        DatePeriod[] datePer = new DatePeriod[] {DatePeriod.HOUR_OF_DAY, DatePeriod.DAY_OF_WEEK, DatePeriod.MONTH};
        model.setScaleModel(new DateScaleModel(datePer));
        model.setTreeTableModel(sortableTreeTableModel);
        createTreeTableModel(model);
       
        _ganttChartPane = new DateGanttChartPane<DefaultGanttEntry<Date>>(model) {
            @Override
            protected JComponent createTableHeaderComponent() {
                JComponent component = super.createTableHeaderComponent();
                component.add(quickSearchPanel);
                return component;
            }
        };
        
        //Set my custom renderer for painting tasks different colors
        _ganttChartPane.getGanttChart().setDefaultEntryRenderer(new CustomGanttEntryRenderer());
    	
        
        //Changes the time frame on the right chart
        _ganttChartPane.getGanttChart().getScaleArea().setVisiblePeriods(Arrays.asList(
                DatePeriod.HOUR_OF_DAY, DatePeriod.DAY_OF_WEEK, DatePeriod.MONTH));

        TreeTable treeTable = _ganttChartPane.getTreeTable();
        _ganttChartPane.getGanttChart().getScaleArea().setTableCellRenderer(
                // note: cannot use a table header with table
                // a null table is not handled correctly in 6u3-6u13 Windows L&F
                // see http://bugs.sun.com/view_bug.do?bug_id=6777378
                new JideTable().getTableHeader().getDefaultRenderer());

        _ganttChartPane.getGanttChart().getScaleArea().addPopupMenuCustomizer(new VisiblePeriodsPopupMenuCustomizer<Date>());
        _ganttChartPane.getGanttChart().getScaleArea().addPopupMenuCustomizer(new ResizePeriodsPopupMenuCustomizer<Date>(_ganttChartPane.getGanttChart()));

        AutoFilterTableHeader header = new AutoFilterTableHeader(treeTable);
        header.setAutoFilterEnabled(true);
        treeTable.setTableHeader(header);

        treeTable.setDragEnabled(true);
        treeTable.setTransferHandler(new TreeTableTransferHandler());

        //Display correct labels on period tiers
        ScaleArea<Date> scaleArea = _ganttChartPane.getGanttChart().getScaleArea();
        DatePeriodConverter periodRenderer = new DatePeriodConverter("Hour",
                new SimpleDateFormat("HH"), new SimpleDateFormat("hhaa"));

        scaleArea.setPeriodConverter(DatePeriod.HOUR_OF_DAY, periodRenderer);
        scaleArea.setPreferredPeriodSize(new Dimension(50, 21));
        scaleArea.setVisiblePeriodCount(10);

        //TODO Make tree table uneditable
        treeTable.setEnabled(true); //Make false for perhaps temporary solution
        
        // do not clear selection when filtering
        treeTable.setClearSelectionOnTableDataChanges(false);
//        treeTable.getColumnModel().getColumn(0).setPreferredWidth(200);
//        treeTable.getColumnModel().getColumn(3).setPreferredWidth(60);
//        treeTable.getColumnModel().getColumn(4).setPreferredWidth(60);

        treeTable.expandAll();
        TableUtils.autoResizeAllColumns(treeTable);
        filterField.setTable(treeTable); 
        
        //Add MouseListener for selecting gantt entries
        _ganttChartPane.getGanttChart().addMouseListener(new MouseListener(){
        	JPopupMenu popup;
        	
			public void mouseClicked(MouseEvent e) {        
		        //Get point where mouse was clicked
				point = e.getPoint();
				int row = _ganttChartPane.getGanttChart().rowAtPoint(point);
				
				//listen for right click or double left click
				if((e.getButton()==1) && (e.getClickCount()>1)){
					//System.out.println("mouseClicked" + e.getButton() + ": Row " + row);
					
					//Bring up info window
					displayJobInfo(row);
				}	
			}
			public void mouseEntered(MouseEvent e) {
				//System.out.println("mouseEntered");
			}
			public void mouseExited(MouseEvent e) {
				//System.out.println("mouseExited");
			}
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
				//System.out.println("mousePressed");
			}
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
				//System.out.println("mouseReleased");
			}
			
			//Create popup menu for right click
			private void maybeShowPopup(MouseEvent e){
				point = e.getPoint();
				if(e.getButton() == 3){
					popup = new JPopupMenu();
			        JMenuItem menuItem = new JMenuItem("See job details");
			        menuItem.addActionListener(new ActionListener() {
			            public void actionPerformed(ActionEvent evt) {
			            	
			            	int row = _ganttChartPane.getGanttChart().rowAtPoint(point);
			            	popup.setVisible(false);
			            	popup = null;
			            	displayJobInfo(row);
			            }
			        });
			        popup.add(menuItem);
					//Listen for popup menu
					if(e.isPopupTrigger()){
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}
        });
        
        tablePanel.add(_ganttChartPane);

        JPanel infoPanel = new JPanel(new BorderLayout(3, 3));
        _filterTimesLabel = new StyledLabel("Not filtered yet.");
        infoPanel.add(_filterTimesLabel);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(new JideTitledBorder(new PartialEtchedBorder(PartialEtchedBorder.LOWERED, PartialSide.NORTH), "Filtered Row Count", JideTitledBorder.LEADING, JideTitledBorder.ABOVE_TOP),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JPanel panel = new JPanel(new BorderLayout(3, 3));
        panel.add(tablePanel);
        panel.add(infoPanel, BorderLayout.AFTER_LAST_LINE);

        //Maintain auto refresh timer state
        if(autoRefresh){
        	super.setAutoRefresh(true);
       	 timer.schedule(new RefreshTask(), refreshDelay, refreshDelay);
        }
        else{
        	timer.cancel();
        	super.setAutoRefresh(false);
        }
        
        return panel;
    }

    private void createTreeTableModel(DefaultGanttModel<Date, DefaultGanttEntry<Date>> model) {
    	
                //My Range
                Calendar start = Calendar.getInstance();
                start.add(Calendar.DAY_OF_WEEK, -10);
                Calendar stop = Calendar.getInstance();
                stop.add(Calendar.DAY_OF_WEEK, -6);
                
                tasks = new ArrayList<Task>();
                
                //read logfile from location on ssh
                ReadData.getData(logfile);
                tasks = ReadData.getTasks();
                //Assuming only finish to start dependencies
                int relation = GanttEntryRelation.ENTRY_RELATION_FINISH_TO_START;            
                model.setRange(new TimeRange(start.getTime(), stop.getTime()));
                ArrayList<GanttTask> GTs = new ArrayList<GanttTask>();                
                long max = 0;
                long min = tasks.get(0).getStartTime();
                long queueMin = tasks.get(0).getQueueTime();
                double avg = 0;
                int totalExitStatus = 0;
                
                //Fix bounds on tasks
                for(int i=0;i<tasks.size();i++){
                	tasks.get(i).calcElapTime();
                }
                tasks = ReadData.checkBounds(tasks);

                //Create a GanttTask for each task in workflow
                for (int i=0;i<tasks.size();i++){
                	Task task = tasks.get(i);
                	if (task.getExitStatus()!=0){
                		totalExitStatus = -1;
                	}
                	//Begin time is startTime, end time is stopTime
                	GanttTask theTask = new GanttTask(task.getJobName(), task.getJobID(), task.getUnitName(), task.getWFlowNode(), task.convertLongToHMS(task.getElapTime()), task.getCompleted(), task.getDate(task.getStartTime()), task.getDate(task.getStopTime()), task.getExitStatus());
                	
                	//check if task is overdue
                	if(task.getOverdueTime()>0){
                		//task is overdue
                		//theTask = new GanttTask(task.getJobName(), "OVERDUE", "", task.getCompleted(), task.getDate(task.getOverdueTime()), task.getDate(task.getOverdueTime()), -1);
                		//GTs.add(theTask);
                		theTask.setOverdue(true);
                	}
                	
                	//Set extra job info if there is any
                	if(!task.getExtraInfo().equals("Empty")){
                		//System.out.println("Setting extra info");
                		theTask.set_extraInfo(task.getExtraInfo());
                	}
                	
                	//Add GanttTask to arraylist
                	GTs.add(theTask);
                	avg += task.getCompleted();	
                	
                	//update max and min times
                	if (tasks.get(i).getStopTime() > max){
                		max = task.getStopTime();
                	}
                	if((tasks.get(i).getStartTime() < min) && (tasks.get(i).isStarted())){
                		min = task.getStartTime();
                	}
                	if(tasks.get(i).getQueueTime()<queueMin){
                		queueMin = tasks.get(i).getQueueTime();
                	}
                }
                
              //Set range of chart to longest job time
                String logFileName = logfile;
                avg = avg/tasks.size();
                Task mainTask = new Task();
                //Elapsed time as a string
                String totalElapTime;
                if(avg == 1.0){
                	//All jobs in workflow have finished running
                	totalElapTime = mainTask.calcElapTime(queueMin, max);
                	//If all jobs are finished, make auto-refresh defaulted to off
                	autoRefresh = false;
                	super.setAutoRefresh(false);
                }
                else{
                	//Some jobs in workflow are still running
                	totalElapTime = mainTask.calcElapTime(queueMin, System.currentTimeMillis());
                }
                
                //Elapsed time as a long in ms
                //String wFlowDuration = "" + (System.currentTimeMillis() - tasks.get(0).getQueueTime());
                GanttTask startTask = new GanttTask(logFileName, "", "", tasks.get(0).getUser(), totalElapTime, avg, tasks.get(0).getDate(queueMin), tasks.get(0).getDate(max), totalExitStatus);
                
                //Adding dependencies
                for(int i=0;i<tasks.size();i++){
                	Task task = tasks.get(i);
                	//Add root jobs
                	if(task.getParentJobIDs() == null){
                		
                		//Make sure tasks and GTs are pointing to same job
                		boolean isAdded = false;
						int count = i;
						while(!isAdded){
							if(GTs.get(count).get_name().equals(task.getJobName())){
							startTask.addChild(GTs.get(count));
							
								isAdded = true;
							}
							else{
								count++;
							}
						}						
                		
                		//Add root's children next
                		String childID = task.getJobID();
                		for(int j=i+1;j<tasks.size();j++){ //for all tasks after the current task
                			
                			task = tasks.get(j);
                			
                			if(task.getParentJobIDs() == null){
                				continue;
                			}
                			else{ //if the task has parents
                				outer:
                				for(int k=0;k<task.getParentJobIDs().size();k++){ //for each parent
                					if (task.getParentJobIDs().get(k).equals(childID)){ //if the parent equals the current task
                						//Check if GTs.get(j) has already been added to startTask
                						for(int l=0;l<startTask.getChildrenCount();l++){
                							
                							//Need to sync tasks and GTs
                							isAdded = false;
                							count = j;
                							while(!isAdded){
                								if(GTs.get(count).get_name().equals(task.getJobName())){
                								//check if child is already added
                									if(startTask.getChildAt(l).equals(GTs.get(count))){
                        								//Child is already in Start Task. Don't add
                        								childID = task.getJobID();
                        								break outer;
                        							}
                									isAdded = true;
                								}
                								else{
                									count++;
                								}
                							}
                							
                						}
                						//Child has not already been added to Start Task
                						
                						//Make sure tasks and GTs are pointing to same job
                						 isAdded = false;
                						 count = j;
                						while(!isAdded){
                							if(GTs.get(count).get_name().equals(task.getJobName())){
                								startTask.addChild(GTs.get(count));
                								isAdded = true;
                							}
                							else{
                								count++;
                							}
                						}
                						childID = task.getJobID(); //update childID to current task
                						break outer;
                					}
                				}
                			}
                		}
                	}
                	else{ //get dependencies
                		//Loop through all ParentJobIDs of current task
                		for (int j=0;j<task.getParentJobIDs().size();j++){
                			int count = i-1; //index of Parent must be less than current index
                			Boolean isFound = false;
                			while(!isFound && count>=0){
                				//Loop through jobs in logfile to see if jobID matches ParentID of current task
                				if(tasks.get(count).getJobID().trim().equals(task.getParentJobIDs().get(j).trim())){
                					isFound = true;
                				}
                				else{
                					count--;
                				}
                			}
                			if(count > -1){
                				//Add the relation from count (parent) to i (current)
                    			model.getGanttEntryRelationModel().addEntryRelation(new DefaultGanttEntryRelation<DefaultGanttEntry<Date>>(GTs.get(count), GTs.get(i), relation));
                			}
                		}
                	}
                }
                model.addGanttEntry(startTask); 
                
                //Put tasks in same order as they appear on gantt chart
                //TODO Not necessary to sort anymore since task order doesn't matter for colors
                ArrayList<Task> tempTasks = new ArrayList<Task>();
                outer:
                for(int i=0;i<startTask.getChildrenCount();i++){
                	//Start in order of gantt chart jobs
                	GanttTask GTtemp = (GanttTask) startTask.getChildAt(i);
                	
                	for(int j=0;j<tasks.size();j++){
                		//Find corresponding task for gantt chart job
                    	if(GTtemp.get_name().equals(tasks.get(j).getJobName())){
                    		Task temp = new Task(tasks.get(j).getJobName());
                    		temp.updateTask(tasks.get(j));
                    		
                    		//Add task to ArrayList
                    		tempTasks.add(temp);
                    		continue outer;
                    	}
                    }
                }
                //Reassign tasks
                tasks = tempTasks;
                return;
    }

    //Customize Tree Table
    private static class TaskTreeTableModel extends TreeTableModel<DefaultGanttEntry<Date>> implements StyleModel {
        private static final long serialVersionUID = 3589523753024111735L;

        public int getColumnCount() {
            return 9;
        }

        @Override
        public Class<?> getCellClassAt(int rowIndex, int columnIndex) {
            return getColumnClass(columnIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class; //name
                case 1:
                	return String.class; //ID
                case 2:
                	return String.class; //unit
                case 3:
                    return String.class; //workflow node
                case 4:
                    return String.class; //duration
                case 5:
                    return String.class; //work
                case 6:
                    return String.class; //start
                case 7:
                    return String.class; //finish
                case 8:
                	return String.class; //Exit Status
            }
            return Object.class;
        }

        //Column Names
        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Job Name";
                case 1:
                	return "Job ID";
                case 2:
                	return "Unit Name";
                case 3:
                    return "Workflow Node";
                case 4:
                    return "Duration";
                case 5:
                    return "Work";
                case 6:
                    return "Start";
                case 7:
                    return "Finish";
                case 8:
                	return "Exit";
            }
            return null;
        }
        
        static CellStyle BOLD = new CellStyle();

        static {
            BOLD.setFontStyle(Font.BOLD);
        }

        //Determine cell style at specific rows/cols
        public CellStyle getCellStyleAt(int rowIndex, int columnIndex) {
            Row row = getRowAt(rowIndex);
            //if parent ganttTask
            if (row.getParent() == getRoot() || (row instanceof ExpandableRow && ((ExpandableRow) row).hasChildren())) {
                return BOLD;
            }
            return null;
        }

        public boolean isCellStyleOn() {
            return true;
        }
    }

    //Changed name of class from "Task" to "GanttTask" to avoid confusion with my created Task object
    public static class GanttTask extends DefaultGanttEntry<Date> {
    	private String _name;
    	private String _jobID;
    	private String _unit;
    	private String _node;
    	private String _duration;
        private double _work;
        private Date _startTime;
        private Date _endTime;
        private static SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");
        private int _exitStatus;  
        private boolean isOverdue;
        private String _extraInfo;

		//Change completed to user
        public GanttTask(String name, String jobID, String unit, String node, String duration, double work, Date startTime, Date endTime, int exitStatus) {
            super(name, Date.class, new TimeRange(startTime, endTime), work);
            _name = name;
            _jobID = jobID;
            if(unit==null){
            	_unit = "";
            }
            else if(unit.equals("null")){
            	_unit = "";
            }
            else{
            	_unit = unit;
            }
            _node = node;
            _duration = duration;
            _work = work;
            _startTime = startTime;
            _endTime = endTime;
            _exitStatus = exitStatus;
            isOverdue = false;
            _extraInfo = "Empty";
        }

        //Getters and setters
        public String get_name() {
			return _name;
		}        
		public boolean isOverdue() {
			return isOverdue;
		}
		public void setOverdue(boolean isOverdue) {
			this.isOverdue = isOverdue;
		}
		public String get_duration() {
			return _duration;
		}
		public double get_work() {
			return _work;
		}
		public int get_exitStatus() {
			return _exitStatus;
		}
		public String get_extraInfo() {
			return _extraInfo;
		}
		public void set_extraInfo(String extraInfo) {
			_extraInfo = extraInfo;
		}

		@Override
        protected int getActualColumnIndex(int column) {
            switch (column) {
                case COLUMN_NAME:
                    return 0;
                case COLUMN_COMPLETION:
                    return 5;
                case COLUMN_RANGE_START:
                    return 6;
                case COLUMN_RANGE_END:
                    return 7;
            }
            return super.getActualColumnIndex(column);
        }

        @Override
        public Object getValueAt(int columnIndex) {
            switch (columnIndex) {
            	case 0:
            		return _name;
            	case 1:
        			return _jobID;
            	case 2:
            		return _unit;
            	case 3:
            		return _node;
                case 4:
                    return _duration;
                case 5:
                    return Long.toString(Math.round(_work*100))+"%";
                case 6:
                    return SDF.format(_startTime);
                case 7:
                    return SDF.format(_endTime);
                case 8:
                	return _exitStatus;
                default:
                    return super.getValueAt(columnIndex);
            }
        }

        @Override
        public void setValueAt(Object value, int columnIndex) {
            switch (columnIndex) {
	            case 0:
	                _name = (String) value;
	                break;
	            case 1:
	                _jobID = (String) value;
	                break;
	            case 2:
	                _unit = (String) value;
	                break;
	            case 3:
	                _node = (String) value;
	                break;
            	case 4:
                    _duration = (String) value;
                    break;
                case 5:
                    _work = (Double) value;
                    break;
                case 6:
                    _startTime = (Date) value;
                    break;
                case 7:
                    _endTime = (Date) value;
                    break;
                case 8:
                	_exitStatus = (Integer) value;
                	break;
                default:
                    super.setValueAt(value, columnIndex);
                    break;
            }
        }
       
    }
    
    //Custom renderer to change colors of gantt entries regardless of order
    private static class CustomGanttEntryRenderer extends DefaultGanttEntryRenderer {
    	//DefaultGanttEntryRenderer extends Taskbar
       
    	@Override
        public Component getGanttEntryRendererComponent(GanttChart<?, ?> chart, GanttEntry<?> entry, boolean isSelected, boolean hasFocus, int row, Insets insets) {

            TaskBar taskBar = (TaskBar)super.getGanttEntryRendererComponent(chart, entry, isSelected, hasFocus, row, insets);

            GanttTask t = (GanttTask)entry;
            
            if(t._work == 1){
            	//job is complete
            	if(t._exitStatus!=0){
            		//job completed with errors is red
            		putClientProperty("Color", Color.RED);
                	//Error color takes precedence over overdue color
                }
            	else{
            		putClientProperty("Color", new Color(1,115,255)); //default completed job color is light blue
            	}            	
            }
            else if(t._work == 0){
            	//job has not begun running
            	putClientProperty("Color", Color.YELLOW); //default queued job color is yellow
            }
            else{
            	//job is running
            	putClientProperty("Color", Color.GREEN); //default running job color is green
            }
            
            if(t.isOverdue && !(t._work==1 && t._exitStatus!=0)){
				//if task is overdue and it doesn't have errors, set color to dark Orange (default orange too close to yellow)
				putClientProperty("Color", new Color(255, 124, 1));
			}
            
            if(!t.get_extraInfo().equals("Empty")){
            	//System.out.println(t.get_name() + " " + t.get_extraInfo());
            	//if task was deleted, give it a unique color
            	putClientProperty("Color", Color.MAGENTA);
            }
            //Add extra info to taskbar object
            putClientProperty("Extra", t.get_extraInfo());
            
            return taskBar;
        }
    }

    
    //Override DefaultTaskBarPainter to change color of gantt entries
    public class MyTaskBarPainter extends DefaultTaskBarPainter{
    	
    	//@Override
    	   public void paintTask(Graphics2D graphics_p, TaskBar tasbar_p, Rectangle rect_p,
    	      Insets insets_p, Color color_p, Color bordercolor_p, Color percentcolor_p) {  
    	       //try to change color of gantt entries
    		   Color theColor, workColor;
    		   
    		   //set percent color
    		   workColor = new Color(1,1,1); //make percent bar completely black (visible against all other colors)
    		   
    		   //Get the color from the taskBar client property "Color"
    		   if (tasbar_p.getClientProperty("Color")!=null){
    			   theColor = (Color) tasbar_p.getClientProperty("Color");
    		   }
    		   else{
    			   //Color bar black (dark gray) if cannot find client property
    			   theColor = Color.BLACK;
    		   }
    		   
    	       super.paintTask(graphics_p, tasbar_p, rect_p, insets_p, theColor, bordercolor_p, workColor);
    	   }
    	   
    	   //@Override
    	   public void paintMilestone(Graphics2D graphics_p, TaskBar tasbar_p, Rectangle rect_p,
    	    	      Insets insets_p, Color color_p, Color bordercolor_p, Color percentcolor_p){
    		   
    		   //Change color of milestones for jobs killed in different ways
    		   Color theColor = color_p;
    		   String extra = (String) tasbar_p.getClientProperty("Extra");
    		   if(extra.startsWith("Job killed by")){
    			   //If job killed by user, paint it gray
    			   theColor = Color.GRAY;
    		   }
    		   else{
    			   //else job killed by PBS, paint it red
    			   theColor = Color.RED;
    		   }
    		   super.paintMilestone(graphics_p, tasbar_p, rect_p, insets_p, theColor, bordercolor_p, percentcolor_p);
    	   }
    }
    
    public static void runGantt(String s){
    	
    	if(!s.contains(".")){
    		//if the browse file returns just the file name w/o extension, add extension
    		s = s.concat(".txt");
    	}
    	autoRefresh = true;
    	timer = new Timer();
    	logfile = s;
    	System.out.println(logfile);
    	  EventQueue.invokeLater(new Runnable() {
              public void run() {
            	  //Try to add UIDefaults for new color
            	  myDemo = new MyProjectGanttChartDemo();
            	  
                  LookAndFeelFactory.addUIDefaultsCustomizer(new GanttUIDefaultsCustomizer());
                  LookAndFeelFactory.installDefaultLookAndFeelAndExtension();
                  showAsFrame(myDemo);
                  
                  //Close loading window
                  InputFile.getDialog().setVisible(false);
                  
                  //Resize periods to fit
                  myDemo._ganttChartPane.getGanttChart().autoResizePeriods(false); 
              }
          });	  
    }
    
    public static void main(String[] s) {
    	
    }

}