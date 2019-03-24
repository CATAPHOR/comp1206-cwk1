package comp1206.sushi.server;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

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
	private ArrayList<GenericPanel> panels;
	
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
		this.panels = new ArrayList<GenericPanel>();
		
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
		
		//create panels
		panels.add(new OrderPanel());
		panels.add(new DishPanel());
		panels.add(new IngredientPanel());
		panels.add(new SupplierPanel());
		panels.add(new StaffPanel());
		panels.add(new DronePanel());
		panels.add(new UserPanel());
		panels.add(new PostcodePanel());
		
		//add panels to tabbed pane
		tabbedPane.add("Orders", panels.get(0));
		tabbedPane.add("Dishes", panels.get(1));
		tabbedPane.add("Ingredients", panels.get(2));
		tabbedPane.add("Suppliers", panels.get(3));
		tabbedPane.add("Staff", panels.get(4));
		tabbedPane.add("Drones", panels.get(5));
		tabbedPane.add("Users", panels.get(6));
		tabbedPane.add("Postcodes", panels.get(7));
		
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
		
		//constructor, "sets stage" for inheriting classes' constructors
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
		
		public GenericPanel()
		{
			super();
			
			this.setLayout(new BorderLayout());
			
			this.scrollPane = new JScrollPane();
			
			this.buttonPanel = new JPanel();
			this.buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			this.buttons = new ArrayList<JButton>();
		}
		
		//refresh table contents
		protected void refresh()
		{
			//to be overridden
		}
		
		protected void tableGen(GenericTableModel m)
		{
			this.table = new JTable(m);
			this.table.setShowGrid(false);
			this.table.setDefaultEditor(Object.class, null);
			this.table.setFillsViewportHeight(true);
		}
		
		//method to add buttons to bottom of panel
		protected void addButtons(String[] names)
		{
			this.buttonPanel.setLayout(new GridLayout(0, names.length, 10, 10));

			for (int i = 0; i < names.length; i++)
			{
				this.buttons.add(new JButton(names[i]));
				this.buttonPanel.add(this.buttons.get(i));
			}
		}
		
		//initialises all (or most) gui components
		protected void guiInit()
		{
			this.scrollPane.getViewport().add(this.table);
			this.add(this.scrollPane, BorderLayout.CENTER);
			
			this.add(this.buttonPanel, BorderLayout.SOUTH);
		}
		
		//table model
		protected class GenericTableModel extends AbstractTableModel
		{
			protected String[] columnHeaders;
			protected ArrayList<Object[]> data = new ArrayList<Object[]>();
			
			public int getColumnCount() 
			{
				return this.columnHeaders.length;
			}
			
			public String getColumnName(int i)
			{
				return this.columnHeaders[i];
			}
			public int getRowCount()
			{
				return this.data.size();
			}

			public Object getValueAt(int i, int j) 
			{
				return this.data.get(i)[j];
			}
			
			public void setValueAt(Object o, int i, int j)
			{
				this.data.get(i)[j] = o;
				this.fireTableCellUpdated(i, j);
			}
			
			public boolean isCellEditable(int i, int j)
			{
				return false;
			}
		}
	}
	
	private class OrderPanel extends GenericPanel
	{
		public OrderPanel()
		{
			super();
			this.addButtons(new String[] {"Complete", "Delete"});
			
			this.tableGen(new OrderTableModel());
			this.guiInit();
			
			//DELETE function
			this.buttons.get(1).addActionListener(e -> 
			{ 
				try
				{
					if (this.table.getSelectedRow() == -1)
					{
						throw new ArrayIndexOutOfBoundsException();
					}
					
					int r[] = this.table.getSelectedRows();
					int index = 0;
					while (index < r.length)
					{
						server.removeOrder(server.getOrders().get(r[index++]));
						
						for (int i = index; i < r.length; i++)
						{
							r[i] -= 1;
						}
					}
					ServerWindow.this.refreshAll();
				}
				catch (UnableToDeleteException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
						    "No entry selected.",
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			});
		}
		
		//refresh table contents
		protected void refresh()
		{
			try
			{
				int[] r = this.table.getSelectedRows();
				this.table.setModel(new OrderTableModel());
				if (r.length > 0)
				{
					for (int i : r)
					{
						this.table.addRowSelectionInterval(i, i);
					}
				}
			}
			catch (IllegalArgumentException ex)
			{
				
			}
		}
		
		//populate table with orders from MockServer
		protected class OrderTableModel extends GenericTableModel
		{
			public OrderTableModel()
			{
				this.columnHeaders = new String[] {"Name", "Distance", "Cost", "Status"};
				for (Order order : ServerWindow.this.server.getOrders())
				{
					this.data.add(new Object[] {order.getName(), server.getOrderDistance(order), 
							server.getOrderCost(order), server.getOrderStatus(order)});
				}
			}
		}
	}
	
	// TODO need to implement get recipe/set recipe
	private class DishPanel extends GenericPanel
	{
		public DishPanel()
		{
			super();
			this.addButtons(new String[] {"Add", "Edit", "Delete"});
			
			this.tableGen(new DishTableModel());
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
			
			//DELETE function
			this.buttons.get(2).addActionListener(e -> 
			{ 
				try
				{
					if (this.table.getSelectedRow() == -1)
					{
						throw new ArrayIndexOutOfBoundsException();
					}
					
					int r[] = this.table.getSelectedRows();
					int index = 0;
					while (index < r.length)
					{
						server.removeDish(server.getDishes().get(r[index++]));
						
						for (int i = index; i < r.length; i++)
						{
							r[i] -= 1;
						}
					}
					ServerWindow.this.refreshAll();
				}
				catch (UnableToDeleteException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
						    "No entry selected.",
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			});
		}
		
		//refresh table contents
		protected void refresh()
		{
			try
			{
				int[] r = this.table.getSelectedRows();
				this.table.setModel(new DishTableModel());
				if (r.length > 0)
				{
					for (int i : r)
					{
						this.table.addRowSelectionInterval(i, i);
					}
				}
			}
			catch (IllegalArgumentException ex)
			{
				
			}
		}
		
		//populate table with orders from MockServer
		protected class DishTableModel extends GenericTableModel
		{
			public DishTableModel()
			{
				this.columnHeaders = new String[] {"Name", "Description", "Price", "Restock Amount", 
						"Restock Threshold", "Stock"};
				for (Dish dish : ServerWindow.this.server.getDishes())
				{
					this.data.add(new Object[] {dish.getName(), dish.getDescription(), 
							dish.getPrice(), dish.getRestockAmount(), dish.getRestockThreshold(), 
							server.getDishStockLevels().get(dish)});
				}
			}
		}
	}
	
	private class IngredientPanel extends GenericPanel
	{
		public IngredientPanel()
		{
			super();
			this.addButtons(new String[] {"Add", "Edit", "Delete"});
			
			this.tableGen(new IngredientTableModel());
			this.guiInit();
			
			//DELETE function
			this.buttons.get(2).addActionListener(e -> 
			{ 
				try
				{
					if (this.table.getSelectedRow() == -1)
					{
						throw new ArrayIndexOutOfBoundsException();
					}
					
					int r[] = this.table.getSelectedRows();
					int index = 0;
					while (index < r.length)
					{
						//check if ingredient in use by dish
						for (Dish dish : server.getDishes())
						{
							if (dish.getRecipe().containsKey(server.getIngredients().get(r[index])))
							{
								throw new UnableToDeleteException("Ingredient " + 
										server.getIngredients().get(r[index]).getName() + " used by Dish " +
										dish.getName() + ".");
							}
						}
						
						server.removeIngredient(server.getIngredients().get(r[index++]));
						
						for (int i = index; i < r.length; i++)
						{
							r[i] -= 1;
						}
					}
					ServerWindow.this.refreshAll();
				}
				catch (UnableToDeleteException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
						    "No entry selected.",
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			});
		}
		
		//refresh table contents
		protected void refresh()
		{
			try
			{
				int[] r = this.table.getSelectedRows();
				this.table.setModel(new IngredientTableModel());
				if (r.length > 0)
				{
					for (int i : r)
					{
						this.table.addRowSelectionInterval(i, i);
					}
				}
			}
			catch (IllegalArgumentException ex)
			{
				
			}
		}
				
		//populate table with orders from MockServer
		protected class IngredientTableModel extends GenericTableModel
		{
			public IngredientTableModel()
			{
				this.columnHeaders = new String[] {"Name", "Unit", "Supplier", "Restock Amount",
						"Restock Threshold", "Stock"};
				for (Ingredient ingredient : ServerWindow.this.server.getIngredients())
				{
					this.data.add(new Object[] {ingredient.getName(), ingredient.getUnit(),
							ingredient.getSupplier().getName(), ingredient.getRestockAmount(), 
							ingredient.getRestockThreshold(), server.getIngredientStockLevels().get(ingredient)});
				}
			}
		}
	}
	
	private class SupplierPanel extends GenericPanel
	{
		public SupplierPanel()
		{
			super();
			this.addButtons(new String[] {"Add", "Edit", "Delete"});
			
			this.tableGen(new SupplierTableModel());
			this.guiInit();
			
			//DELETE function
			this.buttons.get(2).addActionListener(e -> 
			{ 
				try
				{
					if (this.table.getSelectedRow() == -1)
					{
						throw new ArrayIndexOutOfBoundsException();
					}
					
					int r[] = this.table.getSelectedRows();
					int index = 0;
					while (index < r.length)
					{
						server.removeSupplier(server.getSuppliers().get(r[index++]));
						
						for (int i = index; i < r.length; i++)
						{
							r[i] -= 1;
						}
					}
					ServerWindow.this.refreshAll();
				}
				catch (UnableToDeleteException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
						    "No entry selected.",
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			});
		}
		
		//refresh table contents
		protected void refresh()
		{
			try
			{
				int[] r = this.table.getSelectedRows();
				this.table.setModel(new SupplierTableModel());
				if (r.length > 0)
				{
					for (int i : r)
					{
						this.table.addRowSelectionInterval(i, i);
					}
				}
			}
			catch (IllegalArgumentException ex)
			{
				
			}
		}

		//populate table with orders from MockServer
		protected class SupplierTableModel extends GenericTableModel
		{
			public SupplierTableModel()
			{
				this.columnHeaders = new String[] {"Name", "Postcode", "Distance"};
				for (Supplier supplier : ServerWindow.this.server.getSuppliers())
				{
					this.data.add(new Object[] {supplier.getName(), supplier.getPostcode(),
							server.getSupplierDistance(supplier)});
				}
			}
		}
	}
	
	private class StaffPanel extends GenericPanel
	{
		public StaffPanel()
		{
			super();
			this.addButtons(new String[] {"Add", "Delete"});
			
			this.tableGen(new StaffTableModel());
			this.guiInit();
			
			//DELETE function
			this.buttons.get(1).addActionListener(e -> 
			{ 
				try
				{
					if (this.table.getSelectedRow() == -1)
					{
						throw new ArrayIndexOutOfBoundsException();
					}
					
					int r[] = this.table.getSelectedRows();
					int index = 0;
					while (index < r.length)
					{
						server.removeStaff(server.getStaff().get(r[index++]));
						
						for (int i = index; i < r.length; i++)
						{
							r[i] -= 1;
						}
					}
					ServerWindow.this.refreshAll();
				}
				catch (UnableToDeleteException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
						    "No entry selected.",
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			});
		}
		
		//refresh table contents
		protected void refresh()
		{
			try
			{
				int[] r = this.table.getSelectedRows();
				this.table.setModel(new StaffTableModel());
				if (r.length > 0)
				{
					for (int i : r)
					{
						this.table.addRowSelectionInterval(i, i);
					}
				}
			}
			catch (IllegalArgumentException ex)
			{
				
			}
		}
				
		//populate table with orders from MockServer
		protected class StaffTableModel extends GenericTableModel
		{
			public StaffTableModel()
			{
				this.columnHeaders = new String[] {"Name", "Status", "Fatigue"};
				for (Staff staff : ServerWindow.this.server.getStaff())
				{
					this.data.add(new Object[] {staff.getName(), server.getStaffStatus(staff), staff.getFatigue()});
				}
			}
		}
	}
	
	private class DronePanel extends GenericPanel
	{
		public DronePanel()
		{
			super();
			this.addButtons(new String[] {"Add", "Delete"});
			
			this.tableGen(new DroneTableModel());
			this.guiInit();
			
			//DELETE function
			this.buttons.get(1).addActionListener(e -> 
			{ 
				try
				{
					if (this.table.getSelectedRow() == -1)
					{
						throw new ArrayIndexOutOfBoundsException();
					}
					
					int r[] = this.table.getSelectedRows();
					int index = 0;
					while (index < r.length)
					{
						server.removeDrone(server.getDrones().get(r[index++]));
						
						for (int i = index; i < r.length; i++)
						{
							r[i] -= 1;
						}
					}
					ServerWindow.this.refreshAll();
				}
				catch (UnableToDeleteException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
						    "No entry selected.",
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			});
		}
		
		//refresh table contents
		protected void refresh()
		{
			try
			{
				int[] r = this.table.getSelectedRows();
				this.table.setModel(new DroneTableModel());
				if (r.length > 0)
				{
					for (int i : r)
					{
						this.table.addRowSelectionInterval(i, i);
					}
				}
			}
			catch (IllegalArgumentException ex)
			{
				
			}
		}
				
		//populate table with orders from MockServer
		protected class DroneTableModel extends GenericTableModel
		{
			public DroneTableModel()
			{
				this.columnHeaders = new String[] {"Name", "Source", "Destination", "Progress", "Speed", "Status"};
				for (Drone drone : ServerWindow.this.server.getDrones())
				{
					this.data.add(new Object[] {drone.getName(), server.getDroneSource(drone),
							server.getDroneDestination(drone), server.getDroneProgress(drone),
							server.getDroneSpeed(drone), server.getDroneStatus(drone)});
				}
			}
		}
	}
	
	private class UserPanel extends GenericPanel
	{
		public UserPanel()
		{
			super();
			this.addButtons(new String[] {"Delete"});
			
			this.tableGen(new UserTableModel());
			this.guiInit();
			
			//DELETE function
			this.buttons.get(0).addActionListener(e -> 
			{ 
				try
				{
					if (this.table.getSelectedRow() == -1)
					{
						throw new ArrayIndexOutOfBoundsException();
					}
					
					int r[] = this.table.getSelectedRows();
					int index = 0;
					while (index < r.length)
					{
						server.removeUser(server.getUsers().get(r[index++]));
						
						for (int i = index; i < r.length; i++)
						{
							r[i] -= 1;
						}
					}
					ServerWindow.this.refreshAll();
				}
				catch (UnableToDeleteException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
						    "No entry selected.",
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			});
		}
		
		//refresh table contents
		protected void refresh()
		{
			try
			{
				int[] r = this.table.getSelectedRows();
				this.table.setModel(new UserTableModel());
				if (r.length > 0)
				{
					for (int i : r)
					{
						this.table.addRowSelectionInterval(i, i);
					}
				}
			}
			catch (IllegalArgumentException ex)
			{
				
			}
		}
				
		//populate table with orders from MockServer
		protected class UserTableModel extends GenericTableModel
		{
			public UserTableModel()
			{
				this.columnHeaders = new String[] {"Name", "Distance", "Postcode"};
				for (User user : ServerWindow.this.server.getUsers())
				{
					this.data.add(new Object[] {user.getName(), user.getDistance(), user.getPostcode()});
				}
			}
		}
	}
	
	private class PostcodePanel extends GenericPanel
	{
		public PostcodePanel()
		{
			super();
			this.addButtons(new String[] {"Add", "Delete"});
			
			this.tableGen(new PostcodeTableModel());
			this.guiInit();
			
			//DELETE function
			this.buttons.get(1).addActionListener(e -> 
			{ 
				try
				{
					if (this.table.getSelectedRow() == -1)
					{
						throw new ArrayIndexOutOfBoundsException();
					}
					
					int r[] = this.table.getSelectedRows();
					int index = 0;
					while (index < r.length)
					{
						//check if postcode in use by supplier
						for (Supplier supplier : server.getSuppliers())
						{
							if (supplier.getPostcode() == server.getPostcodes().get(r[index]))
							{
								throw new UnableToDeleteException("Postcode " + 
										server.getPostcodes().get(r[index]).getName() + " used by Supplier " +
										supplier.getName() + ".");
							}
						}
						
						server.removePostcode(server.getPostcodes().get(r[index++]));
						
						for (int i = index; i < r.length; i++)
						{
							r[i] -= 1;
						}
					}
					ServerWindow.this.refreshAll();
				}
				catch (UnableToDeleteException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
						    ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(ServerWindow.this,
						    "No entry selected.",
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			});
		}
		
		//refresh table contents
		protected void refresh()
		{
			try
			{
				int[] r = this.table.getSelectedRows();
				this.table.setModel(new PostcodeTableModel());
				if (r.length > 0)
				{
					for (int i : r)
					{
						this.table.addRowSelectionInterval(i, i);
					}
				}
			}
			catch (IllegalArgumentException ex)
			{
				
			}
		}
				
		//populate table with orders from MockServer
		protected class PostcodeTableModel extends GenericTableModel
		{
			public PostcodeTableModel()
			{
				this.columnHeaders = new String[] {"Name", "Latitude / Longitude", "Distance"};
				for (Postcode postcode : ServerWindow.this.server.getPostcodes())
				{
					this.data.add(new Object[] {postcode.getName(), 
							(String) (postcode.getLatLong().get("lat") + " / " + postcode.getLatLong().get("lon")), 
							postcode.getDistance()});
				}
			}
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
		for (GenericPanel panel : this.panels)
		{
			panel.refresh();
		}
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
