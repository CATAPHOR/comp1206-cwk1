package comp1206.sushi.common;

import java.io.Serializable;

public class Message implements Serializable
{
	private static final long serialVersionUID = 439808854500750694L;
	private Object[] content;
	public Message(Object[] content)
	{
		this.content = content;
	}
	
	public Object[] getContent()
	{
		return this.content;
	}
}