/**
* Abstract class of utility class to write parsed output to DB
* @author: Dagmar Gromann
* @version: 1.0*/

package resources.writer;

import java.sql.Connection;

public abstract class Writer {

	public abstract void openDatabase(String DBname);
		
	public abstract void closeDatabase(Connection conn);

	
}
