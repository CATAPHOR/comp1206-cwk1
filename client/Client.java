package comp1206.sushi.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Order;
import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.Restaurant;
import comp1206.sushi.common.UpdateEvent;
import comp1206.sushi.common.UpdateListener;
import comp1206.sushi.common.User;

public class Client implements ClientInterface 
{

    private static final Logger logger = LogManager.getLogger("Client");
	
    public Restaurant restaurant;
    public ArrayList<User> users;
    public ArrayList<Postcode> postcodes;
    public ArrayList<Dish> dishes;
    private ArrayList<UpdateListener> listeners;
    
	public Client() 
	{
        logger.info("Starting up client...");
        
        this.users = new ArrayList<User>();
        this.postcodes = new ArrayList<Postcode>();
        this.dishes = new ArrayList<Dish>();
        this.listeners = new ArrayList<UpdateListener>();
        
        this.restaurant = new Restaurant("Mock Restaurant", new Postcode("SO17 1BJ"));
        
        this.postcodes.add(new Postcode("SO17 1TJ"));
        this.postcodes.add(new Postcode("SO17 1BX"));
        this.postcodes.add(new Postcode("SO17 2NJ"));
        this.postcodes.add(new Postcode("SO17 1TW"));
        Postcode postcode = new Postcode("SO17 2LB");
        this.postcodes.add(postcode);
        
        this.users.add(new User("username", "password", "nowhere", postcode));
        
        this.dishes.add(new Dish("Dish 1","Dish 1",1,1,10));
        this.dishes.add(new Dish("Dish 2","Dish 2",2,1,10));
        this.dishes.add(new Dish("Dish 3","Dish 3",3,1,10));
	}
	
	//method to help determine the position of an object in an array; used to get models from client
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
	public Restaurant getRestaurant() 
	{
		return this.restaurant;
	}
	
	@Override
	public String getRestaurantName() 
	{
		return this.restaurant.getName();
	}

	@Override
	public Postcode getRestaurantPostcode() 
	{
		return this.restaurant.getLocation();
	}
	
	@Override
	public User register(String username, String password, String address, Postcode postcode) 
	{
		//populate array of registered usernames
		String[] registeredUsernames = new String[this.users.size()];
		for (int i = 0; i < registeredUsernames.length; i++)
		{
			registeredUsernames[i] = this.users.get(i).getName();
		}
		//only proceed if username is unique
		int pos = posInArray(registeredUsernames, username);
		if (pos == -1)
		{
			User user = new User(username, password, address, postcode);
			this.users.add(user);
			return user;
		}
		//fails if username not unique
		else
		{
			return null;
		}
	}

	@Override
	public User login(String username, String password) 
	{
		//populate array of registered usernames
		String[] registeredUsernames = new String[this.users.size()];
		for (int i = 0; i < registeredUsernames.length; i++)
		{
			registeredUsernames[i] = this.users.get(i).getName();
		}
		
		int pos = posInArray(registeredUsernames, username);
		//verify if username is registered
		if (pos > -1)
		{
			//return User with specified username if checkPassword() returns true (valid password)
			if (this.users.get(pos).checkPassword(password.toCharArray()))
			{
				return this.users.get(pos);
			}
		}
		return null;
	}

	@Override
	public List<Postcode> getPostcodes() 
	{
		return this.postcodes;
	}

	@Override
	public List<Dish> getDishes() 
	{
		return this.dishes;
	}

	@Override
	public String getDishDescription(Dish dish) 
	{
		return dish.getDescription();
	}

	@Override
	public Number getDishPrice(Dish dish) 
	{
		return dish.getPrice();
	}

	@Override
	public Map<Dish, Number> getBasket(User user) 
	{
		return user.getBasket();
	}

	@Override
	public Number getBasketCost(User user) 
	{
		double output = 0;
		for(Map.Entry<Dish, Number> entry : user.getBasket().entrySet())
		{
			output += entry.getKey().getPrice().doubleValue() * entry.getValue().intValue();
		}
		return output;
	}

	@Override
	public void addDishToBasket(User user, Dish dish, Number quantity)
	{
		if (quantity.intValue() > 0)
		{
			user.addDishToBasket(dish, quantity);
		}
	}

	@Override
	public void updateDishInBasket(User user, Dish dish, Number quantity) 
	{
		if (quantity.intValue() == 0)
		{
			user.removeDishFromBasket(dish);
		}
		else if (quantity.intValue() > 0)
		{
			user.addDishToBasket(dish, quantity);
		}
	}

	@Override
	public Order checkoutBasket(User user) 
	{
		Order order = user.addOrder(new Order(user, user.getBasket()));
		user.clearBasket();
		return order;
	}

	@Override
	public void clearBasket(User user) 
	{
		user.clearBasket();
	}

	@Override
	public List<Order> getOrders(User user) 
	{
		return user.getOrders();
	}

	@Override
	public boolean isOrderComplete(Order order) 
	{
		return getOrderStatus(order).toLowerCase().equals("complete");
	}

	@Override
	public String getOrderStatus(Order order) 
	{
		return order.getStatus();
	}

	@Override
	public Number getOrderCost(Order order) 
	{
		double output = 0;
		for (Map.Entry<Dish, Number> entry : order.getBasket().entrySet())
		{
			output += entry.getKey().getPrice().doubleValue() * entry.getValue().intValue();
		}
		return output;
	}

	@Override
	public void cancelOrder(Order order) 
	{
		order.getUser().removeOrder(order);
	}

	@Override
	public void addUpdateListener(UpdateListener listener) 
	{
		this.listeners.add(listener);
	}

	@Override
	public void notifyUpdate() 
	{
		this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
	}

}
