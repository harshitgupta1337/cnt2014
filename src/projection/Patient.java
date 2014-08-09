package projection;

import java.util.ArrayList;
import java.util.List;

public class Patient 
{
	int id;
	List<Integer> attributes;
	public Patient(int _id)
	{
		attributes = new ArrayList<Integer>();
		id = _id;
	}
	public List<Integer> getAttributeList()
	{
		return attributes;
	}
	public int getId()
	{
		return id;
	}
	public void addAttribute(int att)
	{
		attributes.add(att);
	}

}
