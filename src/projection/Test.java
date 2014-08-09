package projection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Test {

	public static boolean isInitialized = false;
	public static Connection con = null;
	public static Statement stmt = null;
	public static String sDriver = "com.mysql.jdbc.Driver";
	public static String sURL = "jdbc:mysql://10.14.99.97/test" ;
	public static String sUsername = "root";
	public static String sPassword = "123456";
	public static int main(String[] args) 
	{
		try    
	 	{     
       	 	Class.forName( sDriver ).newInstance();
			System.out.println("Driver properly loaded") ;
	 	}
	 	catch( Exception e )  
	 	{     		
	 		return 1;
	 	} 

		try
		{
			con = DriverManager.getConnection( sURL, sUsername ,sPassword);
			stmt = con.createStatement();     				
		}
		catch ( Exception e)
		{     		 
			System.err.println( e.getMessage() );     		 
		}
		if (stmt != null)
		{
			System.out.println("Connected to the database!!!");
			return 0;
		}
		else
		{
			System.out.println("Could not connect to the database!!!");
			return 1;
		}

	}

}
