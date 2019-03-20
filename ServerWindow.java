package comp1206.sushi.server;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

import comp1206.sushi.common.*;
import comp1206.sushi.server.ServerInterface.UnableToDeleteException;

/**
 * Provides the Sushi Server user interface
 *
 */
public class ServerWindow extends JFrame implements UpdateListener 
{

	private static final long serialVersionUID = -4661566573959270000L;
	private ServerInterface server;
	private JPanel contentPane;
	
	/**
	 * Create a new server window
	 * @par	am server instance of server to interact with
	 */
	public ServerWindow(ServerInterface server) 
	{
		super("Sushi Server");
		this.server = server;
		this.setTitle(server.getRestaurantName() + " Server");
		server.addUpdateListener(this);
		
		//Display window
		setSize(800,600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//set top level content pane
		this.contentPane = new JPanel();
		this.setContentPane(contentPane);
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		
		//tabbed pane
		JTabbedPane tabbedPane = new JTabbedPane();
		
		//testing dummy data
		Object[][] orderData = {{"test", 12, 43, "Pending"}, {"test2", 122, 433, "Ready"}};
		Object[][] dishData = {{"test", "test", 43, 10, 1, 12}, 
				{"test2", "test2", 122, 433, 123, 14}};
		Object[][] ingredientData = {{"test", "kg", "test", 10, 1}, 
				{"test2", "kg", "test2", 433, 123}};
		Object[][] supplierData = {{"test", "test", 10}, 
				{"test2", "test2", 433}};
		Object[][] staffData = {{"test", "test", "Tired"}, 
				{"test2", "test2", "Exhausted"}};
		Object[][] droneData = {{"test", 10, "Flying"}, 
				{"test2", 23, "Idle"}};
		Object[][] userData = {{"test", "test", "test"}, 
				{"test2", "test2", "test2"}};
		Object[][] postcodeData = {{"test", "0/0", 3}, 
				{"test2", "1/1", 4}};
		
		
		GenericPanel orderPanel = new OrderPanel(orderData);
		GenericPanel dishPanel = new DishPanel(dishData);
		GenericPanel ingredientPanel = new IngredientPanel(ingredientData);
		GenericPanel supplierPanel = new SupplierPanel(supplierData);
		GenericPanel staffPanel = new StaffPanel(staffData);
		GenericPanel dronePanel = new DronePanel(droneData);
		GenericPanel userPanel = new UserPanel(userData);
		GenericPanel postcodePanel = new PostcodePanel(postcodeData);
		
		tabbedPane.add("Orders", orderPanel);
		tabbedPane.add("Dishes", dishPanel);
		tabbedPane.add("Ingredients", ingredientPanel);
		tabbedPane.add("Suppliers", supplierPanel);
		tabbedPane.add("Staff", staffPanel);
		tabbedPane.add("Drones", dronePanel);
		tabbedPane.add("Users", userPanel);
		tabbedPane.add("Postcodes", postcodePanel);
		
		this.contentPane.add(tabbedPane);
		setVisible(true);
		
		//Start timed updates
		startTimer();
	}
	
	/*
	 * PANEL CLASSES
	 */
	
	private class GenericPanel extends JPanel
	{
		protected JTable table;
		protected Object[][] data;
		
		protected JScrollPane scrollPane;
		protected ArrayList<JButton> buttons;
		protected JPanel buttonPanel;
		
		public GenericPanel(Object[][] data)
		{
			super();
			
			this.setLayout(new BorderLayout());
			this.data = data;
			
			this.scrollPane = new JScrollPane();
			
			this.buttonPanel = new JPanel();
			this.buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			this.buttons = new ArrayList<JButton>();
		}
		
		protected void tableGen(String[] header)
		{
			this.table = new JTable(this.data, header);
			this.table.setShowGrid(false);
			this.table.setDefaultEditor(Object.class, null);
			this.table.setFillsViewportHeight(true);
		}
		
		protected void addButtons(String[] names)
		{
			this.buttonPanel.setLayout(new GridLayout(0, names.length, 10, 10));
			
			for (String name : names)
			{
				this.buttons.add(new JButton(name));
			}
			for (JButton button : this.buttons)
			{
				this.buttonPanel.add(button);
			}
		}
		
		protected void guiInit()
		{
			this.scrollPane.getViewport().add(this.table);
			this.add(this.scrollPane, BorderLayout.CENTER);
			
			this.add(this.buttonPanel, BorderLayout.SOUTH);
		}
	}
	
	private class OrderPanel extends GenericPanel
	{
		public OrderPanel(Object[][] data)
		{
			super(data);
			this.addButtons(new String[] {"Complete", "Delete"});
			
			this.tableGen(new String[] {"Name", "Distance", "Cost", "Status"});
			this.guiInit();
		}
	}
	
	private class DishPanel extends GenericPanel
	{
		public DishPanel(Object[][] data)
		{
			super(data);
			this.addButtons(new String[] {"Add", "Edit", "Delete"});
			
			this.tableGen(new String[] {"Name", "Description", "Price", "Restock Amount", "Restock Threshold", "Stock"});
			this.guiInit();
			
			//example of how to implement edit
			this.buttons.get(1).addActionListener(e -> { String s = (String) JOptionPane.showInputDialog(
                    ServerWindow.this,
                    "FieldN:",
                    "Edit dish:",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new String[] {"test", "test2"},
                    "ham"); });
		}
	}
	
	private class IngredientPanel extends GenericPanel
	{
		public IngredientPanel(Object[][] data)
		{
			super(data);
			this.addButtons(new String[] {"Add", "Edit", "Delete"});
			
			this.tableGen(new String[] {"Name", "Unit", "Supplier", "Restock Amount", "Restock Threshold"});
			this.guiInit();
		}
	}
	
	private class SupplierPanel extends GenericPanel
	{
		public SupplierPanel(Object[][] data)
		{
			super(data);
			this.addButtons(new String[] {"Add", "Edit", "Delete"});
			
			this.tableGen(new String[] {"Name", "Postcode", "Distance"});
			this.guiInit();
		}
	}
	
	private class StaffPanel extends GenericPanel
	{
		public StaffPanel(Object[][] data)
		{
			super(data);
			this.addButtons(new String[] {"Add", "Delete"});
			
			this.tableGen(new String[] {"Name", "Status", "Fatigue"});
			this.guiInit();
		}
	}
	
	private class DronePanel extends GenericPanel
	{
		public DronePanel(Object[][] data)
		{
			super(data);
			this.addButtons(new String[] {"Add", "Delete"});
			
			this.tableGen(new String[] {"Name", "Speed", "Status"});
			this.guiInit();
		}
	}
	
	private class UserPanel extends GenericPanel
	{
		public UserPanel(Object[][] data)
		{
			super(data);
			this.addButtons(new String[] {"Delete"});
			
			this.tableGen(new String[] {"Name", "Address", "Postcode"});
			this.guiInit();
		}
	}
	
	private class PostcodePanel extends GenericPanel
	{
		public PostcodePanel(Object[][] data)
		{
			super(data);
			this.addButtons(new String[] {"Add", "Delete"});
			
			this.tableGen(new String[] {"Name", "Latitude/Longitude", "Distance"});
			this.guiInit();
		}
	}
	
	/**
	 * Start the timer which updates the user interface based on the given interval to update all panels
	 */
	public void startTimer() 
	{
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);     
        int timeInterval = 5;
        
        scheduler.scheduleAtFixedRate(() -> refreshAll(), 0, timeInterval, TimeUnit.SECONDS);
	}
	
	/**
	 * Refresh all parts of the server application based on receiving new data, calling the server afresh
	 */
	public void refreshAll() 
	{
		
	}
	
	@Override
	/**
	 * Respond to the model being updated by refreshing all data displays
	 */
	public void updated(UpdateEvent updateEvent) 
	{
		refreshAll();
	}
	
}
