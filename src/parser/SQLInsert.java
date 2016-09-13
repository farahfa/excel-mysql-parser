package parser;

import java.sql.*;
import org.apache.log4j.Logger;

public class SQLInsert {
	
	static final Logger logger2 = Logger.getLogger(SQLInsert.class);
	
	//JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/testDB";

	//Database credentials
	static final String USER = "root";
	static final String PASS = "";
	
	public Connection getConnection() {
		Connection conn = null;

	    try {
	        // Register JDBC driver
	        Class.forName("com.mysql.jdbc.Driver");

	        // Open a connection
	        //System.out.print("Connecting to database...");
	        conn = DriverManager.getConnection(DB_URL, USER, PASS);
	        //System.out.println(" SUCCESS!\n");
	        logger2.debug("Connecting to database... SUCCESS!");
	        
	        }catch(Exception e) {
	        logger2.error("Unable to register JDBC driver.", e);
	    }
	    return conn;
	}

}
