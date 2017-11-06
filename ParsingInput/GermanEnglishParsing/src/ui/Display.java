/**
* Display/user interface to dependency parse English and German sentences with the Stanford Parser
* @author: Dagmar Gromann
* @version: 1.0*/

package ui;

import java.io.*;

import resources.configuration.Configuration;
import resources.curator.CuratorInterface;
import resources.evaluation.CuratorPrepEval;
import resources.evaluation.ResultWriter;
import resources.lemmatizing.GermanLemmatizer;
import resources.parser.*;
import resources.writer.DatabaseWriter;

public class Display {

	
	/**
	 * This method serves as a basic user interface in text modus
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
	
	Configuration.loadPropertiesFile();
	
	System.out.println("Welcome to the Stanford Parser for English and German!"+ "\n");

	
	DependencyParsing parser = new DependencyParsing();
	parser.parseInput();

	}

}
