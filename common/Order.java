package comp1206.sushi.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;

import comp1206.sushi.common.Order;

public class Order extends Model {

	private String status;
	private Map<Dish, Number> basket;
	private User user;
	
	public Order() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();  
		this.name = dtf.format(now);
		this.status = "Pending";
	}
	
	public Order(User user, Map<Dish, Number> basket)
	{
		this();
		this.basket = basket;
		this.user = user;
	}

	public Number getDistance() {
		return 1;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public String getStatus() {
		return status;
	}
	
	public User getUser()
	{
		return user;
	}

	public Map<Dish, Number> getBasket()
	{
		return this.basket;
	}
	
	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}

}
