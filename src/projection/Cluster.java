package projection;

import java.util.ArrayList;
import java.util.List;

public class Cluster 
{
	List<Integer> patients;
	public Cluster()
	{
		patients = new ArrayList<>();
	}
	public void addPatient(int p)
	{
		patients.add(p);
	}
	public List<Integer> getPatients()
	{
		return patients;
	}
}
