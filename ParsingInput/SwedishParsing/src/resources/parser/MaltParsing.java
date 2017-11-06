/**
* Swedish dependency parser trained on Talbanken subset and semantic role labeling
* Output is send to DB writer 
* @author: Dagmar Gromann
* @version: 1.0*/


package resources.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;

import resources.configuration.Configuration;
import resources.writer.DatabaseWriter;

public class MaltParsing {

	 String dataPath = Configuration.dataSource.getProperty("dataPath");
	 String filename = Configuration.dataSource.getProperty("fileName");
	 String workingDir = Configuration.dataSource.getProperty("workingDir");
	 DatabaseWriter db;
	 MaltParserService service;
	 ArrayList<String> prepTags = new ArrayList<String>();
	 
	 /*
	  * General concstructor of MaltParser setting basic parameters
	  */
	 public MaltParsing(){
		db = new DatabaseWriter();
		try {
			service =  new MaltParserService();
			// Inititalize the parser model 'model0' and sets the working directory to '.' and sets the logging file to 'parser.log'
			service.initializeParserModel("-c germanTreebank -m parse -w "+workingDir+" -lfi parser.log");
		} catch (MaltChainedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	 
	 /*
	  * Main method to parse Swedish sentences to store verb-preposition-noun triples, their semantic roles and their sentences of origin
	  * without deduplicating to allow for frequency counts
	  */
	 public void parsingPreposition(String[] tokens, String[] lemmas, String sentence) throws IOException{
		 try {	  
			 	String verb = "";
			 	String verbLemma = "";
			 	String noun = "";
			 	String nounLemma = "";
			 	String preposition = "";
			 	String rel = "";
			 	
			 	// Parses the Swedish sentence above
			    DependencyStructure graph = service.parse(tokens);
			    
			    for (Edge edge: graph.getEdges()){ 
			    	String posSource = "";
			    	List<String> mainDependencies = Arrays.asList("+A","AA","CA","KA","MA","NA","OA","RA","TA","VA");
			    	String relationType = edge.toString().substring(edge.toString().indexOf("DEPREL:")+7, edge.toString().length()).trim();
			    	String tags = edge.getTarget().toString().substring(edge.getTarget().toString().indexOf(" POSTAG:")+8, edge.getTarget().toString().indexOf("FEATS")-1).trim();
			    	
			    	if (mainDependencies.contains(relationType) && tags.trim().equals("PP")){
			    		int sourceIndex = edge.getSource().getIndex();
				    	if (sourceIndex > 0){
				    		String source = edge.getSource().toString();
				    		posSource = source.substring(source.indexOf(" POSTAG:")+8, source.indexOf("FEATS:")-1).trim();
				    	}
				    	
				    	if (posSource.equals("VB")){
				    		preposition = edge.getTarget().toString().substring(edge.getTarget().toString().indexOf("FORM:")+5, edge.getTarget().toString().indexOf(" LEMMA")).trim();
				    		verbLemma = lemmas[sourceIndex-1];
				    		verb = edge.getSource().toString().substring(edge.getSource().toString().indexOf("FORM:")+5, edge.getSource().toString().indexOf(" LEMMA")).trim();
				    		rel = relationType;
				    	}
			    	}
			    	
			    	if(relationType.trim().equals("PA") || relationType.trim().equals("HD")){
			    		if(tags.trim().equals("NN") && !preposition.isEmpty()){
				    		int targetIndex = edge.getTarget().getIndex();
					    	if (targetIndex > 0){
					    		String target = edge.getTarget().toString();
					    		noun = target.substring(target.indexOf("FORM:")+5, target.indexOf(" LEMMA")).trim();
					    		nounLemma = lemmas[targetIndex-1];
					    	}
			    		}
			    		if (!verb.isEmpty() && !noun.isEmpty() && !preposition.isEmpty()){
			    			//db.savePrepOutput(verb.toLowerCase(), preposition.toLowerCase(), noun.toLowerCase(), verbLemma, nounLemma, rel, sentence.replace("\"", "\'"));
			    			System.out.println(verb+" "+preposition+" "+noun+" "+rel+" "+verbLemma+" "+nounLemma);
			    			verb = "";
			    			preposition = ""; 
			    			noun = ""; 
			    		}
			    	}
			    }
			} catch (MaltChainedException e) {
				// TODO Auto-generated catch block					
				e.printStackTrace();
			}
	 }

	 /*
	  * Method to terminate and close the parser model
	  */
	 public void closeMaltParsing(){
		    try {
				service.terminateParserModel();
			} catch (MaltChainedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
}
