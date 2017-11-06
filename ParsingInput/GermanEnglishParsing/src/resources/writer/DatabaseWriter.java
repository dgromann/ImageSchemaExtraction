/**
* Utility class to write parsed output to DB
* @author: Dagmar Gromann
* @version: 1.0*/

package resources.writer;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import resources.configuration.Configuration;

import java.sql.Connection;

public class DatabaseWriter extends Writer{
	
	String DBName = Configuration.dataSource.getProperty("DBName");
	String DBUser = Configuration.dataSource.getProperty("DBUser");
	String DBPassword = Configuration.dataSource.getProperty("DBPassword");
	
	String DBEnglish = Configuration.dataSource.getProperty("DBTable_en");
	String DBGerman = Configuration.dataSource.getProperty("DBTable_de");
	
	Connection conn;
	Statement stmt; 
	
	public DatabaseWriter(String DBname){
		openDatabase(DBname);
	}
	
	/**
	 * This class opens the database containing the termbase and 
	 * parses the natural language definition sentences contained in there 
	 */
	public void openDatabase() {
		String dbConnection = "jdbc:mysql://localhost:3306/"+DBName+"?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";
		try {
			conn = DriverManager.getConnection(dbConnection, DBUser , DBPassword);
			stmt = conn.createStatement();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveEnglishOutput(Integer rowID, String rel, String frontNoun, String frontVerb, String  prep, String backNoun, String sentence){
		String query = "INSERT INTO "+DBEnglish+" (rowID, rel, frontNoun, frontVerb, prep, backNoun, sentence) VALUES("+rowID+", \""+rel+"\", \""+frontNoun+"\", \""+frontVerb+"\", \""+prep+"\", \""+backNoun+"\", \""+sentence+"\")";
		try{
			stmt.execute(query);
		}catch (SQLException e) {
			System.out.println("Some problem with writing the parsed output into the relational database! "+e);
			e.printStackTrace();
		}
		
	}
	
	public void saveGermanOutput(Integer rowID, String rel, String verb, String verbDependency, String prep, String noun, String sentence){
		String query = "INSERT INTO "+DBGerman+" (rowID, rel, verb, verbDependency, prep, noun, sentence) VALUES("+rowID+", \""+rel+"\", \""+verb+"\", \""+verbDependency+"\", \""+prep+"\", \""+noun+"\", \""+sentence+"\")";
		try{
			stmt.execute(query);
		}catch (SQLException e) {
			System.out.println("Some problem with writing the parsed output into the relational database! "+e);
			e.printStackTrace();
		}
	}


	@Override
	public void closeDatabase(Connection conn) {
		if(conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				System.out.println("Connection could not be closed: "+e);
				e.printStackTrace();
			}
		}
	}

}
