package comp1206.sushi.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
					JOptionPane.showMessageDialog(null,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(null,
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
	
	private class DishPanel extends GenericPanel
	{	
		public DishPanel()
		{
			super();
			this.addButtons(new String[] {"Add", "Edit", "Delete"});
			
			this.tableGen(new DishTableModel());
			this.guiInit();
			
			//ADD function
			this.buttons.get(0).addActionListener(e -> 
			{
				JTextField name = new JTextField();
				JTextField description = new JTextField();
				JTextField price = new JTextField();
				JTextField restockThreshold = new JTextField();
				JTextField restockAmount = new JTextField();
				
				Object[] message = {
				    "Dish name:", name,
				    "Description:", description,
				    "Price:", price,
				    "Restock Threshold", restockThreshold,
				    "Restock Amount", restockAmount
				};
	
				int option = JOptionPane.showConfirmDialog(null, message, "Add new dish:", 
						JOptionPane.OK_CANCEL_OPTION);
				
				if (option == JOptionPane.OK_OPTION) 
				{
				    if (name.getText() != null && name.getText().length() > 0) 
				    {
				    	try
				    	{
				    		for (Dish dish : server.getDishes())
				    		{
					    		if (name.getText().equals(dish.getName()))
					    		{
					    			throw new Exception("Dish " + name.getText() + " already exists.");
					    		}
				    		}
					    	
					    	int rAmount = Integer.parseInt(restockAmount.getText());
					    	int rThreshold = Integer.parseInt(restockThreshold.getText());
					    	int rprice = Integer.parseInt(price.getText());
					    	
					    	if (rAmount <= 0 || rThreshold <= 0 || rprice <= 0)
					    	{
					    		throw new Exception("Numerical values must be above 0.");
					    	}
					    	
					        server.addDish(name.getText(), description.getText(), rprice, rThreshold, rAmount);
					        ServerWindow.this.refreshAll();
				    	}
				    	catch (NumberFormatException ex)
				    	{
				    		JOptionPane.showMessageDialog(null,
									"Price and restock fields must hold valid numerical values.",
								    "Add Error",
								    JOptionPane.ERROR_MESSAGE);
				    	}
				    	catch (Exception ex)
				    	{
							JOptionPane.showMessageDialog(null,
									ex.getMessage(),
								    "Add Error",
								    JOptionPane.ERROR_MESSAGE);
				    	}
				    }
				}
			});
			
			//EDIT function
			this.buttons.get(1).addActionListener(e -> 
			{
				if (this.table.getSelectedRow() != -1)
				{
					Dish dish = server.getDishes().get(this.table.getSelectedRow());
					
					JTextField rThreshold = new JTextField(dish.getRestockThreshold().toString());
					JTextField rAmount = new JTextField(dish.getRestockAmount().toString());
					JButton recipeOpen = new JButton("View and Edit");
					
					recipeOpen.addActionListener(f ->
					{
						JTable recipeTable = new JTable(new RecipeTableModel(dish));
						recipeTable.setShowGrid(false);
						recipeTable.setFillsViewportHeight(true);
						JScrollPane recipeScroll = new JScrollPane();
						recipeScroll.getViewport().add(recipeTable);
						
						ArrayList<JButton> recipebuttons = new ArrayList<JButton>();
						recipebuttons.add(new JButton("Add"));
						recipebuttons.add(new JButton("Edit"));
						recipebuttons.add(new JButton("Delete"));
						
						JPanel buttonPanel = new JPanel();
						buttonPanel.setLayout(new FlowLayout());
						
						for (JButton button : recipebuttons)
						{
							buttonPanel.add(button);
						}
						
						//ADD function
						recipebuttons.get(0).addActionListener(g -> 
						{
							String[] ingredients = new String[server.getIngredients().size()];
							
							for (int i = 0; i < ingredients.length; i++)
							{
								ingredients[i] = server.getIngredients().get(i).getName();
							}
							
							JComboBox<String> ingredientChoice = new JComboBox<String>(ingredients);
							JTextField number = new JTextField();
							
							Object[] message = {
							    "Ingredient:", ingredientChoice,
							    "Number:", number
							};
				
							int option = JOptionPane.showConfirmDialog(null, message, "Add new ingredient to recipe:", 
									JOptionPane.OK_CANCEL_OPTION);
							
							if (option == JOptionPane.OK_OPTION) 
							{
						    	try
						    	{
						    		for (Ingredient ingredient : server.getRecipe(dish).keySet())
						    		{
							    		if (server.getIngredients().get(ingredientChoice.getSelectedIndex()) 
							    				== ingredient)
							    		{
							    			throw new Exception("Ingredient " + ingredient.getName() 
							    			+ " already exists in recipe.");
							    		}
						    		}
							    	
							    	int num = Integer.parseInt(number.getText());
							    	
							    	if (num <= 0)
							    	{
							    		throw new Exception("Numerical values must be above 0.");
							    	}
							    	
							    	Map<Ingredient, Number> entry = new HashMap<Ingredient, Number>();
							    	entry.put(server.getIngredients().get(ingredientChoice.getSelectedIndex()), num);
							    	
							        server.setRecipe(dish, entry);
							        recipeTable.setModel(new RecipeTableModel(dish));
						    	}
						    	catch (NumberFormatException ex)
						    	{
						    		JOptionPane.showMessageDialog(null,
											"Number field must hold valid numerical value.",
										    "Add Error",
										    JOptionPane.ERROR_MESSAGE);
						    	}
						    	catch (Exception ex)
						    	{
									JOptionPane.showMessageDialog(null,
											ex.getMessage(),
										    "Add Error",
										    JOptionPane.ERROR_MESSAGE);
						    	}
						    }
						});
						
						//EDIT function
						recipebuttons.get(1).addActionListener(g -> 
						{
							if (recipeTable.getSelectedRows().length > 0)
							{
								JTextField number = new JTextField(
										recipeTable.getValueAt(recipeTable.getSelectedRow(), 1).toString());
								Object[] message = {
										"Number:", number
								};
								
								int option = JOptionPane.showConfirmDialog(null, message, "Number of ingredient " + 
										recipeTable.getValueAt(recipeTable.getSelectedRow(), 0).toString()
										+ ":", JOptionPane.OK_CANCEL_OPTION);
								if (option == JOptionPane.OK_OPTION)
								{
									try
									{
										int num = Integer.parseInt(number.getText());
										
										if (num <= 0)
										{
											throw new Exception("Numerical values must be above 0.");
										}
										
										Ingredient ingredient = null;
										
										for (Ingredient i : server.getRecipe(dish).keySet())
										{
											if (i.getName().equals(recipeTable.getValueAt(recipeTable.getSelectedRow(), 0)))
											{
												ingredient = i;
											}
										}
										
										server.getRecipe(dish).put(ingredient, num);
										recipeTable.setModel(new RecipeTableModel(dish));
									}
									catch (NumberFormatException ex)
							    	{
							    		JOptionPane.showMessageDialog(null,
												"Number field must hold valid numerical value.",
											    "Edit Error",
											    JOptionPane.ERROR_MESSAGE);
							    	}
									catch (Exception ex)
									{
										JOptionPane.showMessageDialog(null,
											    ex.getMessage(),
											    "Edit Error",
											    JOptionPane.ERROR_MESSAGE);
									}
								}
							}
						});
						
						
						//DELETE function
						recipebuttons.get(2).addActionListener(g -> 
						{
							try
							{
								if (recipeTable.getSelectedRow() == -1)
								{
									throw new ArrayIndexOutOfBoundsException();
								}
								
								int r[] = recipeTable.getSelectedRows();
								int index = 0;
								while (index < r.length)
								{
									Ingredient ingredient = null;
									
									for (Ingredient i : server.getRecipe(dish).keySet())
									{
										if (i.getName().equals(recipeTable.getValueAt(r[index], 0)))
										{
											ingredient = i;
										}
									}
									
									if (ingredient == null)
									{
										throw new UnableToDeleteException("Ingredient to delete not found.");
									}
									
									server.removeIngredientFromDish(dish, 
											ingredient);
									
									index++;
								}
								recipeTable.setModel(new RecipeTableModel(dish));
							}
							catch (UnableToDeleteException ex)
							{
								JOptionPane.showMessageDialog(null,
									    ex.getMessage(),
									    "Delete Error",
									    JOptionPane.ERROR_MESSAGE);
							}
							catch (ArrayIndexOutOfBoundsException ex)
							{
								JOptionPane.showMessageDialog(null,
									    "No entry selected.",
									    "Delete Error",
									    JOptionPane.ERROR_MESSAGE);
							}
						});
						
						Object[] message = {
								recipeScroll, buttonPanel
							};
						
						JOptionPane.showMessageDialog(null, message, "Recipe of dish " + 
						dish.getName() + ":", JOptionPane.PLAIN_MESSAGE);
					});
					
					Object[] message = {
							"Recipe:", recipeOpen,
						    "Restock Threshold:", rThreshold,
						    "Restock Amount", rAmount
						};
					
					int option = JOptionPane.showConfirmDialog(null, message, "Edit dish " + 
					dish.getName() + ":", 
							JOptionPane.OK_CANCEL_OPTION);
					
					if (option == JOptionPane.OK_OPTION)
					{
						try
						{
							
							int restockThreshold = Integer.parseInt(rThreshold.getText());
							int restockAmount = Integer.parseInt(rAmount.getText());
							
							if (restockAmount <= 0 || restockThreshold <= 0)
					    	{
					    		throw new Exception("Restock values must be above 0.");
					    	}
							
							server.setRestockLevels(dish, restockThreshold, restockAmount);
							ServerWindow.this.refreshAll();
						}
						catch (NumberFormatException ex)
				    	{
				    		JOptionPane.showMessageDialog(null,
									"Restock fields must hold valid numerical values.",
								    "Edit Error",
								    JOptionPane.ERROR_MESSAGE);
				    	}
						catch (Exception ex)
						{
							JOptionPane.showMessageDialog(null,
									ex.getMessage(),
								    "Edit Error",
								    JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
			
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
					JOptionPane.showMessageDialog(null,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(null,
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
				this.columnHeaders = new String[] {"Name", "Description", "Price", "Restock Threshold", 
						"Restock Amount", "Stock"};
				for (Dish dish : ServerWindow.this.server.getDishes())
				{
					this.data.add(new Object[] {dish.getName(), dish.getDescription(), 
							dish.getPrice(), dish.getRestockThreshold(), dish.getRestockAmount(), 
							server.getDishStockLevels().get(dish)});
				}
			}
		}
		
		protected class RecipeTableModel extends GenericTableModel
		{
			public RecipeTableModel(Dish dish)
			{
				this.columnHeaders = new String[] {"Ingredient", "Number"};
				for (Ingredient ingredient : server.getRecipe(dish).keySet())
				{
					this.data.add(new Object[] {ingredient.getName(), server.getRecipe(dish).get(ingredient)});
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
			
			//ADD function
			this.buttons.get(0).addActionListener(e -> 
			{
				String[] suppliers = new String[server.getSuppliers().size()];
				
				for (int i = 0; i < suppliers.length; i++)
				{
					suppliers[i] = server.getSuppliers().get(i).getName();
				}
				
				JTextField name = new JTextField();
				JTextField units = new JTextField();
				JTextField restockAmount = new JTextField();
				JTextField restockThreshold = new JTextField();
				JComboBox<String> supplierChoice = new JComboBox<String>(suppliers);
				
				Object[] message = {
				    "Ingredient name:", name,
				    "Units:", units,
				    "Supplier:", supplierChoice,
				    "Restock Threshold", restockThreshold,
				    "Restock Amount", restockAmount
				};
	
				int option = JOptionPane.showConfirmDialog(null, message, "Add new ingredient:", 
						JOptionPane.OK_CANCEL_OPTION);
				
				if (option == JOptionPane.OK_OPTION) 
				{
				    if (name.getText() != null && name.getText().length() > 0) 
				    {
				    	try
				    	{
				    		for (Ingredient ingredient : server.getIngredients())
				    		{
					    		if (name.getText().equals(ingredient.getName()))
					    		{
					    			throw new Exception("Ingredient " + name.getText() + " already exists.");
					    		}
				    		}
				    		
					    	Supplier supplier = server.getSuppliers().get(supplierChoice.getSelectedIndex());
					    	
					    	if (units.getText() == null || units.getText().length() == 0)
					    	{
					    		throw new Exception("Must enter unit type.");
					    	}
					    	
					    	int rAmount = Integer.parseInt(restockAmount.getText());
					    	int rThreshold = Integer.parseInt(restockThreshold.getText());
					    	
					    	if (rAmount <= 0 || rThreshold <= 0)
					    	{
					    		throw new Exception("Restock values must be above 0.");
					    	}
					    	
					        server.addIngredient(name.getText(), units.getText(), supplier, rThreshold, rAmount);
					        ServerWindow.this.refreshAll();
				    	}
				    	catch (NumberFormatException ex)
				    	{
				    		JOptionPane.showMessageDialog(null,
									"Restock fields must hold valid numerical values.",
								    "Add Error",
								    JOptionPane.ERROR_MESSAGE);
				    	}
				    	catch (Exception ex)
				    	{
							JOptionPane.showMessageDialog(null,
									ex.getMessage(),
								    "Add Error",
								    JOptionPane.ERROR_MESSAGE);
				    	}
				    }
				}
			});
			
			//EDIT function
			this.buttons.get(1).addActionListener(e -> 
			{
				if (this.table.getSelectedRow() != -1)
				{
					Ingredient ingredient = server.getIngredients().get(this.table.getSelectedRow());
					
					String[] suppliers = new String[server.getSuppliers().size()];
					
					for (int i = 0; i < suppliers.length; i++)
					{
						suppliers[i] = server.getSuppliers().get(i).getName();
					}
					
					JComboBox<String> supplierChoice = new JComboBox<String>(suppliers);
					JTextField rThreshold = new JTextField(ingredient.getRestockThreshold().toString());
					JTextField rAmount = new JTextField(ingredient.getRestockAmount().toString());
					
					Object[] message = {
						    "Supplier:", supplierChoice,
						    "Restock Threshold:", rThreshold,
						    "Restock Amount", rAmount
						};
					
					supplierChoice.setSelectedIndex(server.getSuppliers().indexOf(ingredient.getSupplier()));
					
					int option = JOptionPane.showConfirmDialog(null, message, "Edit ingredient " + 
					ingredient.getName() + ":", 
							JOptionPane.OK_CANCEL_OPTION);
					
					if (option == JOptionPane.OK_OPTION)
					{
						try
						{
							Supplier supplier = server.getSuppliers().get(supplierChoice.getSelectedIndex());
							ingredient.setSupplier(supplier);
							
							int restockThreshold = Integer.parseInt(rThreshold.getText());
							int restockAmount = Integer.parseInt(rAmount.getText());
							
							if (restockAmount <= 0 || restockThreshold <= 0)
					    	{
					    		throw new Exception("Restock values must be above 0.");
					    	}
							
							server.setRestockLevels(ingredient, restockThreshold, restockAmount);
							ServerWindow.this.refreshAll();
						}
						catch (NumberFormatException ex)
				    	{
				    		JOptionPane.showMessageDialog(null,
									"Restock fields must hold valid numerical values.",
								    "Edit Error",
								    JOptionPane.ERROR_MESSAGE);
				    	}
						catch (Exception ex)
						{
							JOptionPane.showMessageDialog(null,
									ex.getMessage(),
								    "Edit Error",
								    JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
			
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
										server.getIngredients().get(r[index]).getName() + " used by dish " +
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
					JOptionPane.showMessageDialog(null,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(null,
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
				this.columnHeaders = new String[] {"Name", "Unit", "Supplier", "Restock Threshold",
						"Restock Amount", "Stock"};
				for (Ingredient ingredient : ServerWindow.this.server.getIngredients())
				{
					this.data.add(new Object[] {ingredient.getName(), ingredient.getUnit(),
							ingredient.getSupplier().getName(), ingredient.getRestockThreshold(), 
							ingredient.getRestockAmount(), server.getIngredientStockLevels().get(ingredient)});
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
			
			//ADD function
			this.buttons.get(0).addActionListener(e -> 
			{
				String[] postcodes = new String[server.getPostcodes().size()];
				
				for (int i = 0; i < postcodes.length; i++)
				{
					postcodes[i] = server.getPostcodes().get(i).getName();
				}
				
				JTextField name = new JTextField();
				JComboBox<String> postcodeChoice = new JComboBox<String>(postcodes);
				
				Object[] message = {
				    "Supplier name:", name,
				    "Postcode:", postcodeChoice
				};
	
				int option = JOptionPane.showConfirmDialog(null, message, "Add new supplier:", 
						JOptionPane.OK_CANCEL_OPTION);
				
				if (option == JOptionPane.OK_OPTION) 
				{
				    if (name.getText() != null && name.getText().length() > 0) 
				    {
				    	try
				    	{
					    	Postcode postcode = null;
					    	
					    	for (Postcode p : server.getPostcodes())
					    	{
					    		if (p.getName().equals(postcodeChoice.getSelectedItem()))
					    		{
					    			postcode = p;
					    		}
					    	}
					    	
					    	for (Supplier supplier : server.getSuppliers())
					    	{
					    		if (supplier.getName().equals(name.getText()) && 
					    				supplier.getPostcode() == postcode)
					    		{
					    			throw new Exception("Supplier already exists.");
					    		}
					    	}
					    	
					        server.addSupplier(name.getText(), postcode);
					        ServerWindow.this.refreshAll();
				    	}
				    	catch (Exception ex)
				    	{
							JOptionPane.showMessageDialog(null,
									ex.getMessage(),
								    "Add Error",
								    JOptionPane.ERROR_MESSAGE);
				    	}
				    }
				}
			});
			
			//EDIT function
			this.buttons.get(1).addActionListener(e -> 
			{
				if (this.table.getSelectedRow() != -1)
				{
					Supplier supplier = server.getSuppliers().get(this.table.getSelectedRow());
					
					String[] postcodes = new String[server.getPostcodes().size()];
					
					for (int i = 0; i < postcodes.length; i++)
					{
						postcodes[i] = server.getPostcodes().get(i).getName();
					}
					
					JComboBox<String> postcodeChoice = new JComboBox<String>(postcodes);
					Object[] message = {
						    "Postcode:", postcodeChoice
						};
					
					postcodeChoice.setSelectedIndex(server.getPostcodes().indexOf(supplier.getPostcode()));
					
					int option = JOptionPane.showConfirmDialog(null, message, "Edit supplier " + 
					supplier.getName() + ":", 
							JOptionPane.OK_CANCEL_OPTION);
					
					if (option == JOptionPane.OK_OPTION)
					{
						try
						{
							Postcode postcode = server.getPostcodes().get(postcodeChoice.getSelectedIndex());
							
							for (Supplier s : server.getSuppliers())
					    	{
					    		if (s != supplier && s.getName().equals(supplier.getName()) && 
					    				s.getPostcode() == postcode)
					    		{
					    			throw new Exception("Supplier " + supplier.getName() +
					    					" already listed with postcode " + postcode.getName() + ".");
					    		}
					    	}
							
							supplier.setPostcode(postcode);
							ServerWindow.this.refreshAll();
						}
						catch (Exception ex)
						{
							JOptionPane.showMessageDialog(null,
									ex.getMessage(),
								    "Edit Error",
								    JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
			
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
						//check if supplier in use by ingredient
						for (Ingredient ingredient : server.getIngredients())
						{
							if (ingredient.getSupplier() == server.getSuppliers().get(r[index]))
							{
								throw new UnableToDeleteException("Supplier " + 
										server.getSuppliers().get(r[index]).getName() + " listed for ingredient " +
										ingredient.getName() + ".");
							}
						}
						
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
					JOptionPane.showMessageDialog(null,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(null,
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
			
			//ADD function
			this.buttons.get(0).addActionListener(e -> 
			{ 
				String name = (String) JOptionPane.showInputDialog(
                    ServerWindow.this, //frame
                    "Staff name:", //window title
                    "Add new staff:", //text
                    JOptionPane.PLAIN_MESSAGE,
                    null, //no custom icon
                    null,
                    "");
				
				if (name != null && name.length() > 0)
				{
					server.addStaff(name);
					ServerWindow.this.refreshAll();
				}
			});
			
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
					JOptionPane.showMessageDialog(null,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(null,
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
			
			//ADD function
			this.buttons.get(0).addActionListener(e -> 
			{ 
				try
				{
					String speed = (String) JOptionPane.showInputDialog(
	                    ServerWindow.this, //frame
	                    "Drone speed:", //window title
	                    "Add new drone:", //text
	                    JOptionPane.PLAIN_MESSAGE,
	                    null, //no custom icon
	                    null,
	                    "");
					
					if (speed != null && speed.length() > 0)
					{
						int intSpeed = Integer.parseInt(speed);
						
						if (intSpeed <= 0)
						{
							throw new Exception("Speed of drone cannot be 0 or less.");
						}
						
						server.addDrone(intSpeed);
						ServerWindow.this.refreshAll();
					}
				}
				catch (NumberFormatException ex)
				{
					JOptionPane.showMessageDialog(null,
						    "Enter a valid number.",
						    "Add Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(null,
						    ex.getMessage(),
						    "Add Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			});
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
					JOptionPane.showMessageDialog(null,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(null,
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
					JOptionPane.showMessageDialog(null,
							ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(null,
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
			
			//ADD function
			this.buttons.get(0).addActionListener(e -> 
			{ 
				try
				{
					String code = (String) JOptionPane.showInputDialog(
	                    ServerWindow.this, //frame
	                    "Postcode:", //window title
	                    "Add new postcode:", //text
	                    JOptionPane.PLAIN_MESSAGE,
	                    null, //no custom icon
	                    null,
	                    "");
					
					if (code != null && code.length() > 0)
					{
						code = code.toUpperCase();

						//check whether postcode already exists
						for (Postcode postcode : server.getPostcodes())
						{
							if (postcode.getName().replaceAll(" ", "").equals(code.replaceAll(" ", "")))
							{
								throw new Exception("Postcode already exists.");
							}
						}
						
						//check validity of postcode
						/*
						 * regex supplied by UK Govt (cabinetoffice.gov.uk, now archived):
						 * https://webarchive.nationalarchives.gov.uk/+/http://www.cabinetoffice.gov.uk/media/291370/bs7666-v2-0-xsd-PostCodeType.htm
						 */
						if (!Pattern.matches("([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y]"
								+ "[0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9][A-Za-z]?))))"
								+ "\\s?[0-9][A-Za-z]{2})", code))
						{
							throw new Exception("Invalid UK postcode.");
						}
						
						server.addPostcode(code);
						ServerWindow.this.refreshAll();
					}
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(null,
						    ex.getMessage(),
						    "Add Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			});
			
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
										server.getPostcodes().get(r[index]).getName() + " used by supplier " +
										supplier.getName() + ".");
							}
						}
						
						//check if postcode in use by drone
						for (Drone drone : server.getDrones())
						{
							if (server.getDroneDestination(drone) == server.getPostcodes().get(r[index]) ||
									server.getDroneSource(drone) == server.getPostcodes().get(r[index]))
							{
								throw new UnableToDeleteException("Postcode " + 
										server.getPostcodes().get(r[index]).getName() + " used by drone " +
										drone.getName() + ".");
							}
						}
						
						//check if postcode in use by user
						for (User user : server.getUsers())
						{
							if (user.getPostcode() == server.getPostcodes().get(r[index]))
							{
								throw new UnableToDeleteException("Postcode " + 
										server.getPostcodes().get(r[index]).getName() + " used by user " +
										user.getName() + ".");
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
					JOptionPane.showMessageDialog(null,
						    ex.getMessage(),
						    "Delete Error",
						    JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException ex)
				{
					JOptionPane.showMessageDialog(null,
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
