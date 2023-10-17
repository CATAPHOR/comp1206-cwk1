package comp1206.sushi.server;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

//class to read and parse configuration file
public class Configuration 
{
	//arraylist to store all lines of config file
	private ArrayList<String> configContent;
	//arraylist of arraylists to store all information parsed from configContent
	private ArrayList<ArrayList<String[]>> parsedContent;
	//array to store all patterns to search for when parsing configContent
	private String[] patterns;
	
	//constructor takes filepath; successful completion results in arraylist of parsed config information 
	public Configuration(String file)
	{
		this.parsedContent = new ArrayList<ArrayList<String[]>>();
		//initialise to all regex patterns to identify model data in config file
		this.patterns = new String[] { "RESTAURANT:[^:]*:[^:]*", "POSTCODE:[^:]*", "SUPPLIER:[^:]*:[^:]*",
				"INGREDIENT:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*", 
				"DISH:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:\\d+ \\* ((?!( \\* |,|:)).)+(,\\d+ \\* ((?!( \\* |,|:)).)+)*",
				"USER:[^:]*:[^:]*:[^:]*:[^:]*", "STAFF:[^:]*", "DRONE:[^:]*", 
				"ORDER:[^:]*:\\d+ \\* ((?!( \\* |,|:)).)+(,\\d+ \\* ((?!( \\* |,|:)).)+)*", "STOCK:[^:]*:[^:]*" };
		
		//attempt to read config file and place contents in configContent arraylist
		try
		{
			this.configContent = getFileContent(file);

			//interpret file contents and add the data for each model as an arraylist element
			for (int i = 0; i < patterns.length; i++)
			{
				this.parsedContent.add(this.getModels(patterns[i]));
			}
			
			//config file must define ONE restaurant
			if (this.parsedContent.get(0) == null || this.parsedContent.get(0).size() > 1)
			{
				throw(new Exception());
			}
		}
		//exceptions mean config file is invalid; set parsedContent to null
		catch (Exception ex)
		{
			this.parsedContent = null;
			System.err.println(new Exception("Invalid config file."));
		}
	}
	
	//returns arraylist of all textual data from file at input filepath
	private ArrayList<String> getFileContent(String file) throws Exception
	{
		//create input stream
		BufferedReader reader = new BufferedReader(new FileReader(file));
		//arraylist to store all lines of read file
		ArrayList<String> fileContent = new ArrayList<String>();
		
		//read through all lines, adding each to the arraylist
		while (reader.ready())
		{
			fileContent.add(reader.readLine());
		}
		
		//close stream
		reader.close();
		
		return fileContent;
	}
	
	//returns array of structured instance information for models, specified by input pattern
	private ArrayList<String[]> getModels(String pattern)
	{
		//array to contain all instances of specified model
		ArrayList<String[]> outputArray = new ArrayList<String[]>();
		
		//parse lines of configContent array
		for (String line : this.configContent)
		{
			//identify lines pertaining to the specified model with pattern string
			if (Pattern.matches(pattern, line))
			{
				//add instance entry to array, delimited with :, excluding the first element (model name) 
				outputArray.add(line.replaceFirst("[^:]*:", "").split(":"));
			}
		}
		
		//return outputArray if any valid info found; else return null
		if (outputArray.size() > 0)
		{
			return outputArray;
		}
		else
		{
			return null;
		}
	}
	
	//debugging method to print contents of parsedContent
	public void debugPrint()
	{
		if (this.parsedContent != null)
		{
			int count = 1;
			
			for (ArrayList<String[]> model : this.parsedContent)
			{
				System.out.println("model: " + count++);
				
				if (model != null)
				{
					for (String[] line : model)
					{
						for (String item : line)
						{
							System.out.print(item + "; ");
						}
						
						System.out.println();
					}
				}
			}
		}
	}
	
	//returns array of structured dish ingredients and their quantities
	public ArrayList<ArrayList<String[]>> getDishIngredientQuantities()
	{
		if (this.parsedContent != null && this.parsedContent.get(4) != null)
		{
			//array to output
			ArrayList<ArrayList<String[]>> output = new ArrayList<ArrayList<String[]>>();
			
			//iterate through all dishes held in parsedContent
			for (String[] dish : this.parsedContent.get(4))
			{
				//array to store a dish's ingredients/quantities
				ArrayList<String[]> dishIngredients = new ArrayList<String[]>();
				
				//split each ingredient/quantity instance
				String[] quantityIngredient = dish[5].split(",");
				//add each ing/q as a String array to dishIngredients
				for (String qi : quantityIngredient)
				{
					dishIngredients.add(new String[] { qi.split(" \\* ")[0], qi.split(" \\* ")[1] });
				}
				
				//add dish's ing/q's to total output
				output.add(dishIngredients);
			}
			
			return output;
		}
		else
		{
			return null;
		}
	}
	
	//returns array of structured order dishes and their quantities
	public ArrayList<ArrayList<String[]>> getOrderDishQuantities()
	{
		if (this.parsedContent != null && this.parsedContent.get(8) != null)
		{
			//array to output
			ArrayList<ArrayList<String[]>> output = new ArrayList<ArrayList<String[]>>();
			
			//iterate through all orders held in parsedContent
			for (String[] order : this.parsedContent.get(8))
			{
				//array to store a order's dishes/quantities
				ArrayList<String[]> orderDishes = new ArrayList<String[]>();
				
				//split each dish/quantity instance
				String[] quantityDish = order[1].split(",");
				//add each dish/q as a String array to dishIngredients
				for (String qd : quantityDish)
				{
					orderDishes.add(new String[] { qd.split(" \\* ")[0], qd.split(" \\* ")[1] });
				}
				
				//add dish's dish/q's to total output
				output.add(orderDishes);
			}
			
			return output;
		}
		else
		{
			return null;
		}
	}
	
	//returns parsedContent
	public ArrayList<ArrayList<String[]>> getConfig()
	{
		return this.parsedContent;
	}
}
