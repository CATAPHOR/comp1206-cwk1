package comp1206.sushi.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.JOptionPane;

import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 
public class Server implements ServerInterface {

    private static final Logger logger = LogManager.getLogger("Server");
	
	public Restaurant restaurant;
	public ArrayList<Dish> dishes = new ArrayList<Dish>();
	public ArrayList<Drone> drones = new ArrayList<Drone>();
	public ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
	public ArrayList<Order> orders = new ArrayList<Order>();
	public ArrayList<Staff> staff = new ArrayList<Staff>();
	public ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
	public ArrayList<User> users = new ArrayList<User>();
	public ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
	public Comms comms;
	private ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();
	
	public boolean restockingDishesEnabled;
	public boolean restockingIngredientsEnabled;
	
	public int test;
	
	public Server() {
        logger.info("Starting up server...");
		
        test = 0;
        
		Postcode restaurantPostcode = new Postcode("SO17 1BJ");
		restaurant = new Restaurant("Mock Restaurant",restaurantPostcode);
		
		Postcode postcode1 = addPostcode("SO17 1TJ");
		Postcode postcode2 = addPostcode("SO17 1BX");
		Postcode postcode3 = addPostcode("SO17 2NJ");
		Postcode postcode4 = addPostcode("SO17 1TW");
		Postcode postcode5 = addPostcode("SO17 2LB");
		
		Supplier supplier1 = addSupplier("Supplier 1",postcode1);
		Supplier supplier2 = addSupplier("Supplier 2",postcode2);
		Supplier supplier3 = addSupplier("Supplier 3",postcode3);
		
		Ingredient ingredient1 = addIngredient("Ingredient 1","grams",supplier1,1,5,1);
		Ingredient ingredient2 = addIngredient("Ingredient 2","grams",supplier2,1,5,1);
		Ingredient ingredient3 = addIngredient("Ingredient 3","grams",supplier3,1,5,1);
		
		Dish dish1 = addDish("Dish 1","Dish 1",1,1,10);
		Dish dish2 = addDish("Dish 2","Dish 2",2,1,10);
		Dish dish3 = addDish("Dish 3","Dish 3",3,1,10);
		
		orders.add(new Order());

		addIngredientToDish(dish1,ingredient1,1);
		addIngredientToDish(dish1,ingredient2,2);
		addIngredientToDish(dish2,ingredient2,3);
		addIngredientToDish(dish2,ingredient3,1);
		addIngredientToDish(dish3,ingredient1,2);
		addIngredientToDish(dish3,ingredient3,1);
		
		addStaff("Staff 1");
		addStaff("Staff 2");
		addStaff("Staff 3");
		
		addDrone(1);
		addDrone(2);
		addDrone(3);
		
		Random random = new Random();
//		for (Dish dish : getDishes())
//		{
//			dish.setStock(random.nextInt(50) + dish.getRestockThreshold().intValue());
//		}
		
		for (Ingredient ingredient : getIngredients())
		{
			ingredient.setStock(random.nextInt(100) + ingredient.getRestockThreshold().intValue());
		}
		
		for (Staff staff : getStaff())
		{
			staff.setServer(this);
			new Thread(staff).start();
		}
		
		this.setRestockingDishesEnabled(true);
		this.setRestockingIngredientsEnabled(true);
		
		this.comms = new Comms(this);
	}
	
	@Override
	public List<Dish> getDishes() {
		return this.dishes;
	}

	@Override
	public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		Dish newDish = new Dish(name,description,price,restockThreshold,restockAmount);
		this.dishes.add(newDish);
		this.notifyUpdate();
		return newDish;
	}
	
	@Override
	public void removeDish(Dish dish) {
		this.dishes.remove(dish);
		this.notifyUpdate();
	}

	@Override
	public Map<Dish, Number> getDishStockLevels() 
	{
		List<Dish> dishes = getDishes();
		HashMap<Dish, Number> levels = new HashMap<Dish, Number>();
		
		for(Dish dish : dishes) 
		{
			levels.put(dish, dish.getStock());
		}
		return levels;
	}
	
	@Override
	public void setRestockingIngredientsEnabled(boolean enabled) 
	{
		this.restockingIngredientsEnabled = enabled;
	}

	@Override
	public void setRestockingDishesEnabled(boolean enabled) 
	{
		this.restockingDishesEnabled = enabled;
	}
	
	@Override
	public void setStock(Dish dish, Number stock) 
	{
		dish.setStock(stock);
		this.notifyUpdate();
	}

	@Override
	public void setStock(Ingredient ingredient, Number stock) 
	{
		ingredient.setStock(stock);
		this.notifyUpdate();
	}

	@Override
	public List<Ingredient> getIngredients() {
		return this.ingredients;
	}

	@Override
	public Ingredient addIngredient(String name, String unit, Supplier supplier,
			Number restockThreshold, Number restockAmount, Number weight) {
		Ingredient mockIngredient = new Ingredient(name,unit,supplier,restockThreshold,restockAmount,weight);
		this.ingredients.add(mockIngredient);
		this.notifyUpdate();
		return mockIngredient;
	}

	@Override
	public void removeIngredient(Ingredient ingredient) {
		int index = this.ingredients.indexOf(ingredient);
		this.ingredients.remove(index);
		this.notifyUpdate();
	}

	@Override
	public List<Supplier> getSuppliers() {
		return this.suppliers;
	}

	@Override
	public Supplier addSupplier(String name, Postcode postcode) {
		Supplier mock = new Supplier(name,postcode);
		this.suppliers.add(mock);
		return mock;
	}


	@Override
	public void removeSupplier(Supplier supplier) {
		int index = this.suppliers.indexOf(supplier);
		this.suppliers.remove(index);
		this.notifyUpdate();
	}

	@Override
	public List<Drone> getDrones() {
		return this.drones;
	}

	@Override
	public Drone addDrone(Number speed) {
		Drone mock = new Drone(speed);
		this.drones.add(mock);
		return mock;
	}

	@Override
	public void removeDrone(Drone drone) {
		int index = this.drones.indexOf(drone);
		this.drones.remove(index);
		this.notifyUpdate();
	}

	@Override
	public List<Staff> getStaff() {
		return this.staff;
	}

	@Override
	public Staff addStaff(String name) {
		Staff mock = new Staff(name);
		this.staff.add(mock);
		return mock;
	}

	@Override
	public void removeStaff(Staff staff) {
		this.staff.remove(staff);
		this.notifyUpdate();
	}

	@Override
	public List<Order> getOrders() {
		return this.orders;
	}

	@Override
	public void removeOrder(Order order) {
		int index = this.orders.indexOf(order);
		this.orders.remove(index);
		this.notifyUpdate();
	}
	
	@Override
	public Number getOrderCost(Order order) 
	{
		if (order.getBasket() != null)
		{
			double output = 0;
			for (Map.Entry<Dish, Number> entry : order.getBasket().entrySet())
			{
				output += entry.getKey().getPrice().doubleValue() * entry.getValue().intValue();
			}
			return output;
		}
		else
		{
			Random random = new Random();
			return random.nextInt(100);
		}
	}

	@Override
	public Map<Ingredient, Number> getIngredientStockLevels() 
	{
		HashMap<Ingredient, Number> levels = new HashMap<Ingredient, Number>();
		
		for(Ingredient ingredient : getIngredients()) 
		{
			levels.put(ingredient, ingredient.getStock());
		}
		return levels;
	}

	@Override
	public Number getSupplierDistance(Supplier supplier) {
		return supplier.getDistance();
	}

	@Override
	public Number getDroneSpeed(Drone drone) {
		return drone.getSpeed();
	}

	@Override
	public Number getOrderDistance(Order order) {
		Order mock = (Order)order;
		return mock.getDistance();
	}

	@Override
	public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
		if(quantity == Integer.valueOf(0)) {
			removeIngredientFromDish(dish,ingredient);
		} else {
			dish.getRecipe().put(ingredient,quantity);
		}
	}

	@Override
	public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
		dish.getRecipe().remove(ingredient);
		this.notifyUpdate();
	}

	@Override
	public Map<Ingredient, Number> getRecipe(Dish dish) {
		return dish.getRecipe();
	}

	@Override
	public List<Postcode> getPostcodes() {
		return this.postcodes;
	}

	@Override
	public Postcode addPostcode(String code) {
		Postcode mock = new Postcode(code);
		this.postcodes.add(mock);
		this.notifyUpdate();
		return mock;
	}

	@Override
	public void removePostcode(Postcode postcode) throws UnableToDeleteException {
		this.postcodes.remove(postcode);
		this.notifyUpdate();
	}

	@Override
	public List<User> getUsers() {
		return this.users;
	}
	
	@Override
	public void removeUser(User user) {
		this.users.remove(user);
		this.notifyUpdate();
	}

	@Override
	public void loadConfiguration(String filename) throws FileNotFoundException
	{
		if (!new File(filename).exists())
		{
			throw new FileNotFoundException();
		}
		
		//create Configuration instance with file; constructor will parse file content
		Configuration config = new Configuration(filename);
		
		if (config.getConfig() != null)
		{
			//get arraylist of parsed config content
			ArrayList<ArrayList<String[]>> parsedContent = config.getConfig();
			//get arraylist of parsed ingredients and their quantities for each dish
			ArrayList<ArrayList<String[]>> dishIngredientQuantities = config.getDishIngredientQuantities();
			//get arraylist of parsed dishes and their quantities for each order
			ArrayList<ArrayList<String[]>> orderDishQuantities = config.getOrderDishQuantities();
			
			//disable restocking
			setRestockingIngredientsEnabled(false);
			setRestockingDishesEnabled(false);
			
			/*
			 * CLEAR all pre-existing data
			 */
			this.dishes = new ArrayList<Dish>();
			this.drones = new ArrayList<Drone>();
			this.ingredients = new ArrayList<Ingredient>();
			this.orders = new ArrayList<Order>();
			this.staff = new ArrayList<Staff>();
			this.suppliers = new ArrayList<Supplier>();
			this.users = new ArrayList<User>();
			this.postcodes = new ArrayList<Postcode>();
			
			/*
			 * REPOPULATE with data from config
			 */
			//SET RESTAURANT (0)
			this.restaurant = new Restaurant(parsedContent.get(0).get(0)[0], 
					new Postcode(parsedContent.get(0).get(0)[1]));
			
			//SET POSTCODES (1)
			if (parsedContent.get(1) != null)
			{
				for (String[] postcode : parsedContent.get(1))
				{
					addPostcode(postcode[0]);
				}
			}
			
			//SET SUPPLIERS (2)
			if (parsedContent.get(2) != null)
			{
				//create array of names of available postcodes
				String[] availablePostcodes = new String[getPostcodes().size()];
				for (int i = 0; i < availablePostcodes.length; i++)
				{
					availablePostcodes[i] = getPostcodes().get(i).getName();
				}
				//add suppliers
				for (String[] supplier : parsedContent.get(2))
				{
					try
					{
						//use index of server's corresponding postcode to get the addSupplier Postcode argument
						addSupplier(supplier[0], getPostcodes().get(posInArray(availablePostcodes, supplier[1])));
					}
					catch (ArrayIndexOutOfBoundsException ex)
					{
						//skip over invalid supplier
					}
				}
			}
			
			//SET INGREDIENTS (3)
			if (parsedContent.get(3) != null)
			{
				//create array of names of available suppliers
				String[] availableSuppliers = new String[getSuppliers().size()];
				for (int i = 0; i < availableSuppliers.length; i++)
				{
					availableSuppliers[i] = getSuppliers().get(i).getName();
				}
				//add ingredients
				for (String[] ingredient : parsedContent.get(3))
				{
					try
					{
						//use index of server's corresponding supplier to get the addIngredient Supplier argument
						addIngredient(ingredient[0], ingredient[1], 
								getSuppliers().get(posInArray(availableSuppliers, ingredient[2])), 
								Integer.parseInt(ingredient[3]), Integer.parseInt(ingredient[4]), 
								Integer.parseInt(ingredient[5]));
					}
					catch (ArrayIndexOutOfBoundsException ex)
					{
						//skip over invalid ingredient
					}
					catch (NumberFormatException ex)
					{
						//skip over invalid ingredient
					}
				}
			}
			
			//SET DISHES (4)
			if (parsedContent.get(4) != null && dishIngredientQuantities != null)
			{
				//count variable for use in for loop
				int c = -1;
				//create array of names of available ingredients
				String[] availableIngredients = new String[getIngredients().size()];
				for (int i = 0; i < availableIngredients.length; i++)
				{
					availableIngredients[i] = getIngredients().get(i).getName();
				}
				
				for (String[] dish : parsedContent.get(4))
				{
					//increment counter
					c++;
					
					try
					{
						//add dish
						Dish currentDish = addDish(dish[0], dish[1], Integer.parseInt(dish[2]), 
								Integer.parseInt(dish[3]), Integer.parseInt(dish[4]));
						//add ingredients to dish; c used as index to identify current dish
						for (String[] ingredientQuantity : dishIngredientQuantities.get(c))
						{
							try
							{
								addIngredientToDish(currentDish, 
										getIngredients().get(posInArray(availableIngredients, ingredientQuantity[1])),
										Integer.parseInt(ingredientQuantity[0]));
							}
							catch (ArrayIndexOutOfBoundsException ex)
							{
								//skip over invalid ingredient
							}
						}
					}
					catch (NumberFormatException ex)
					{
						//skip over invalid dish
					}
				}
			}
			
			//SET USERS (5)
			if (parsedContent.get(5) != null)
			{
				//create array of names of available postcodes
				String[] availablePostcodes = new String[getPostcodes().size()];
				for (int i = 0; i < availablePostcodes.length; i++)
				{
					availablePostcodes[i] = getPostcodes().get(i).getName();
				}
				
				for (String[] user : parsedContent.get(5))
				{
					//get index of user's postcode in the server's postcodes (if it exists)
					int pos = posInArray(availablePostcodes, user[3]);
					//add postcode to server's postcodes if not already present
					if (pos == -1)
					{
						getUsers().add(new User(user[0], user[1], user[2], addPostcode(user[3])));
					}
					//else pass the correct postcode object as an argument
					else
					{
						getUsers().add(new User(user[0], user[1], user[2], getPostcodes().get(pos)));
					}
				}
			}
			
			//SET STAFF (6)
			if (parsedContent.get(6) != null)
			{
				for (String[] staff : parsedContent.get(6))
				{
					addStaff(staff[0]);
				}
				//set server for ingredient/dish monitoring; start threads
				for (Staff staff : getStaff())
				{
					staff.setServer(this);
					new Thread(staff).start();
				}
			}
			
			//SET DRONES (7)
			if (parsedContent.get(7) != null)
			{
				for (String[] drone : parsedContent.get(7))
				{
					try
					{
						addDrone(Integer.parseInt(drone[0]));
					}
					catch (NumberFormatException ex)
					{
						//skip over invalid drone
					}
				}
			}
			
			//SET ORDERS (8)
			if (parsedContent.get(8) != null)
			{
				//create array of names of available dishes
				String[] availableDishes = new String[getDishes().size()];
				for (int i = 0; i < availableDishes.length; i++)
				{
					availableDishes[i] = getDishes().get(i).getName();
				}
				
				//create array of names of available users
				String[] availableUsers = new String[getUsers().size()];
				for (int i = 0; i < availableUsers.length; i++)
				{
					availableUsers[i] = getUsers().get(i).getName();
				}
				
				//count variable for use in for loop
				int c = -1;
				
				for (String[] order : parsedContent.get(8))
				{
					try
					{
						//increment counter
						c++;
						//get index of user
						int userPos = posInArray(availableUsers, order[0]);
						
						//construct hashmap of dishes in order
						HashMap<Dish, Number> orderDishNumMap = new HashMap<Dish, Number>();
						
						for (String[] dishQuantity : orderDishQuantities.get(c))
						{
							try
							{
								int dishPos = posInArray(availableDishes, dishQuantity[1]);
								orderDishNumMap.put(getDishes().get(dishPos), Integer.valueOf(dishQuantity[0]));
							}
							catch (NumberFormatException ex)
							{
								//skip over invalid dishes
							}
							catch (ArrayIndexOutOfBoundsException ex)
							{
								//skip over invalid dishes
							}
						}
						
						//add hashmap to server's orders
						this.orders.add(new Order(getUsers().get(userPos), orderDishNumMap));
					}
					catch (ArrayIndexOutOfBoundsException ex)
					{
						//skip over invalid order
					}
				}
			}
			
			//STOCK (9)
			//set default values for all dishes and ingredients to 0
			for (Dish dish : getDishes())
			{
				dish.setStock(0);
			}
			for (Ingredient ingredient : getIngredients())
			{
				ingredient.setStock(0);
			}
			
			if (parsedContent.get(9) != null)
			{
				//create array of names of available dishes
				String[] availableDishes = new String[getDishes().size()];
				for (int i = 0; i < availableDishes.length; i++)
				{
					availableDishes[i] = getDishes().get(i).getName();
				}
				
				//create array of names of available ingredients
				String[] availableIngredients = new String[getIngredients().size()];
				for (int i = 0; i < availableIngredients.length; i++)
				{
					availableIngredients[i] = getIngredients().get(i).getName();
				}
				
				for (String[] stock : parsedContent.get(9))
				{
					try
					{
						//determine whether the name is of a dish or ingredient
						int dishPos = posInArray(availableDishes, stock[0]);
						int ingPos = posInArray(availableIngredients, stock[0]);
						
						//set stock of identified ingredient/dish (ingredient takes priority)
						if (ingPos > -1)
						{
							getIngredients().get(ingPos).setStock(Integer.parseInt(stock[1]));
						}
						else if (dishPos > -1)
						{
							getDishes().get(dishPos).setStock(Integer.parseInt(stock[1]));
						}
					}
					catch (NumberFormatException ex)
					{
						//skip over invalid stock
					}
					catch (ArrayIndexOutOfBoundsException ex)
					{
						//skip over invalid stock
					}
				}
			}
			
			//re-enable restocking
			setRestockingIngredientsEnabled(true);
			setRestockingDishesEnabled(true);
		}
	}
	
	//method to help determine the position of an object in an array; used to get models from server
	public int posInArray(Object[] array, Object match)
	{
		//default -1 if not found; will trigger ArrayIndexOutOfBoundsException if output used elsewhere
		int output = -1;
		
		for (int i = 0; i < array.length; i++)
		{
			if (array[i].equals(match)) 
			{
				output = i;
			}
		}
		
		return output;
	}

	@Override
	public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
		for(Entry<Ingredient, Number> recipeItem : recipe.entrySet()) {
			addIngredientToDish(dish,recipeItem.getKey(),recipeItem.getValue());
		}
		this.notifyUpdate();
	}

	@Override
	public boolean isOrderComplete(Order order) {
		return true;
	}

	@Override
	public String getOrderStatus(Order order) 
	{
		return order.getName();
	}
	
	@Override
	public String getDroneStatus(Drone drone) {
		Random rand = new Random();
		if(rand.nextBoolean()) {
			return "Idle";
		} else {
			return "Flying";
		}
	}
	
	@Override
	public String getStaffStatus(Staff staff) {
		Random rand = new Random();
		if(rand.nextBoolean()) {
			return "Idle";
		} else {
			return "Working";
		}
	}

	@Override
	public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
		dish.setRestockThreshold(restockThreshold);
		dish.setRestockAmount(restockAmount);
		this.notifyUpdate();
	}

	@Override
	public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
		ingredient.setRestockThreshold(restockThreshold);
		ingredient.setRestockAmount(restockAmount);
		this.notifyUpdate();
	}

	@Override
	public Number getRestockThreshold(Dish dish) {
		return dish.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Dish dish) {
		return dish.getRestockAmount();
	}

	@Override
	public Number getRestockThreshold(Ingredient ingredient) {
		return ingredient.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Ingredient ingredient) {
		return ingredient.getRestockAmount();
	}
	
	@Override
	public void addUpdateListener(UpdateListener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public void notifyUpdate() {
		this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
	}

	@Override
	public Postcode getDroneSource(Drone drone) {
		return drone.getSource();
	}

	@Override
	public Postcode getDroneDestination(Drone drone) {
		return drone.getDestination();
	}

	@Override
	public Number getDroneProgress(Drone drone) {
		return drone.getProgress();
	}

	@Override
	public String getRestaurantName() {
		return restaurant.getName();
	}

	@Override
	public Postcode getRestaurantPostcode() {
		return restaurant.getLocation();
	}
	
	@Override
	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void sendMessage(Message m)
	{
		try
		{
			this.comms.sendMessage(m);
		}
		catch (IOException ex)
		{
			
		}
	}
}
