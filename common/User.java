package comp1206.sushi.common;

import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comp1206.sushi.common.Dish;

public class User extends Model {
	
	private String name;
	private String password;
	private String address;
	private Postcode postcode;
	private Map<Dish, Number> basket;
	private ArrayList<Order> orders;

	public User(String username, String password, String address, Postcode postcode) {
		this.name = username;
		this.password = password;
		this.address = address;
		this.postcode = postcode;
		this.basket = new HashMap<Dish, Number>();
		this.orders = new ArrayList<Order>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getDistance() {
		return postcode.getDistance();
	}

	public Postcode getPostcode() {
		return this.postcode;
	}
	
	public void setPostcode(Postcode postcode) {
		this.postcode = postcode;
	}
	
	public Map<Dish, Number> getBasket()
	{
		return this.basket;
	}
	
	public void addDishToBasket(Dish dish, Number quantity)
	{
		this.basket.put(dish, quantity);
	}
	
	public void removeDishFromBasket(Dish dish)
	{
		this.basket.remove(dish);
	}
	
	public Order addOrder(Order order)
	{
		this.orders.add(order);
		return order;
	}
	
	public List<Order> getOrders()
	{
		return this.orders;
	}
	
	public void clearBasket()
	{
		this.basket = new HashMap<Dish, Number>();
	}
	
	public void removeOrder(Order order)
	{
		this.orders.remove(order);
	}
	
	//checks whether attempt at password is correct
	public boolean checkPassword(char[] attempt)
	{
		char[] password = this.password.toCharArray();
		
		boolean output = true;
		if (password.length == attempt.length)
		{
			for (int i = 0; i < password.length; i++)
			{
				output &= password[i] == attempt[i];
			}
		}
		else
		{
			output = false;
		}
		
		password = attempt = null;
		return output;
	}
}
