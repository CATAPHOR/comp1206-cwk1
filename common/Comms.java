package comp1206.sushi.common;

import java.util.ArrayList;

import comp1206.sushi.client.Client;
import comp1206.sushi.server.Server;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class Comms 
{
	private ArrayList<Client> clients;
	private Server server;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	public Comms(Server server)
	{
		this.clients = new ArrayList<Client>();
		this.server = server;
		try
		{
			this.ois = new ObjectInputStream(new FileInputStream(""));
			this.oos = new ObjectOutputStream(new FileOutputStream(""));
		}
		catch (FileNotFoundException ex)
		{
			
		}
		catch (IOException ex)
		{
			
		}
	}
	
	//from client to server
	public void sendMessage(Message m, Client c) throws IOException
	{
		this.oos.writeObject(m);
	}
	
	//from server to clients
	public void sendMessage(Message m) throws IOException
	{
		this.oos.writeObject(m);
	}
	
	//from server by client
	public void receiveMessage(Message m, Client c)
	{
		
	}
	
	//from client by server
	public void receiveMessage(Message m)
	{
		
	}
}
