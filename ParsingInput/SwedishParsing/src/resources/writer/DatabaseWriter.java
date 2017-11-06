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
	
	String DBname = Configuration.dataSource.getProperty("DBname");
	String DBUser = Configuration.dataSource.getProperty("DBUser");
	String DBPassword = Configuration.dataSource.getProperty("DBPassword");
	String DBTable = Configuration.dataSource.getProperty("DBTableName");
	Connection conn;
	Statement stmt; 
	
	public DatabaseWriter(){
		openDatabase(DBname);
	}
	
	/**
	 * This class opens the database containing the termbase and 
	 * parses the natural language definition sentences contained in there 
	 */
	public void openDatabase(String DBname) {
		String dbConnection = "jdbc:mysql://localhost:3306/"+DBname+"?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";
		try {
			conn = DriverManager.getConnection(dbConnection, DBUser, DBPassword);
			stmt = conn.createStatement();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void savePrepOutput(String verb, String prep, String noun, String verbLemma, String nounLemma, String rel, String sentence){
		String query = "INSERT INTO "+DBTable+" (verb, prep, noun, rel, verbLemma, nounLemma, sentence) VALUES(\""+verb+"\", \""+prep+"\", \""+noun+"\", \""+rel+"\", \""+verbLemma+"\", \""+nounLemma+"\", \""+sentence+"\")";
		try{
			stmt.execute(query);
		}catch (SQLException e) {
			System.out.println("Some problem with writing the parsed output to the relational database! "+e);
			System.out.println(sentence.length()+" "+sentence);
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
