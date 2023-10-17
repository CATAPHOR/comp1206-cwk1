package comp1206.sushi.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import comp1206.sushi.common.Staff;
import comp1206.sushi.server.Server;

public class Staff extends Model implements Runnable {

	private String name;
	private String status;
	private Number fatigue;
	
	private Server server;
	private Map<Ingredient, Number> ingredientLevels;
	private Map<Dish, Number> dishLevels;
	
	public Staff(String name) {
		this.setName(name);
		this.setFatigue(0);
		this.setStatus("Idle");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getFatigue() {
		return fatigue;
	}

	public void setFatigue(Number fatigue) {
		this.fatigue = fatigue;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}
	
	public void setIngDishMonitor(Map<Ingredient, Number> ingredientLevels, Map<Dish, Number> dishLevels)
	{
		this.ingredientLevels = ingredientLevels;
		this.dishLevels = dishLevels;
	}
	
	public void setServer(Server server)
	{
		this.server = server;
		setIngDishMonitor(server.getIngredientStockLevels(), server.getDishStockLevels());
	}
	
	public void run()
	{	
		//thread constantly checks dishes
		while (true)
		{
			for (Dish dish : server.getDishes())
			{
				//lock dish use; synchronisation obv doesn't work but feeling too faint to think
				synchronized (this)
				{
					//prepare new meals if dishes in stock are below their restock threshold
					if (dishLevels.get(dish).intValue() < dish.getRestockThreshold().intValue())
					{
						//ensure sufficient ingredients to produce batch of restock amount
						boolean sufficientIngredients = true;
						for (Map.Entry<Ingredient, Number> entry : dish.getRecipe().entrySet())
						{
							sufficientIngredients &= ingredientLevels.get(entry.getKey()).intValue() >= 
								entry.getValue().intValue() * dish.getRestockAmount().intValue();
						}
						
						if (sufficientIngredients)
						{
							//remove all ingredients to be used in preparing the batch (mitigate concurrency issue)
							for (Map.Entry<Ingredient, Number> entry : dish.getRecipe().entrySet())
							{
//								System.out.println(this.getName() + ": ING: " + entry.getKey().getName() + " OR: "
//										+ ingredientLevels.get(entry.getKey()).intValue() + " SUB: " +
//										entry.getValue().intValue());
								server.setStock(entry.getKey(), ingredientLevels.get(entry.getKey()).intValue() - 
										entry.getValue().intValue());
							}
							ingredientLevels = server.getIngredientStockLevels();
							
							//prepare each dish
							for (int i = 0; i < dish.getRestockAmount().intValue(); i++)
							{
								//set status
//								System.out.println(("Staff: " + this.getName() + " preparing: " + dish.getName() +
//										" * " + dish.getRestockAmount() + "."));
								this.setStatus("Staff: " + this.getName() + " preparing: " + dish.getName() +
										" * " + dish.getRestockAmount() + ".");
								//sleep for 20 - 60 secs (preparing dish)
								try
								{
									Thread.sleep(new Random().nextInt(40000) + 20000);
								}
								catch (InterruptedException ex)
								{
									
								}
								//increment server's dish stocks
								server.setStock(dish, dish.getStock().intValue() + 1);
							}
							//reset staff's status to idle
							this.setStatus("Idle");
						}
					}
				}
			}
		}
	}
}
