package projection;

import java.util.ArrayList;
import java.util.List;

public class Entry 
{
	private int id;
	private List<Integer> attributes;
	public Entry(int _id)
	{
		id = _id;
		attributes = new ArrayList<>();
	}
	public Entry()
	{
		id = 0;
		attributes = new ArrayList<>();
	}
	public int getId()
	{
		return id;
	}
	public List<Integer> getAttributes()
	{
		return attributes;
	}
	public void addAttribute(int i)
	{
		attributes.add(i);
	}

}
