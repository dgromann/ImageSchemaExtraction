/**
* Simplistic UI extendable to proper UI to start of parser/labeler
* @author: Dagmar Gromann
* @version: 1.0*/

package ui;

import java.io.*;

import resources.configuration.Configuration;
import resources.parser.*;

public class Display {

	
	/**
	 * This method serves as a basic user interface in text modus, which allows you to 
	 * select a data source (at the moment this is only a text file)
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
	
	Configuration.loadPropertiesFile();
	
	System.out.println("Welcome to the Swedish Parser and Labeler!"+ "\n");
	
	StaggerParsing tagger = new StaggerParsing();
	tagger.process();

	}

}
