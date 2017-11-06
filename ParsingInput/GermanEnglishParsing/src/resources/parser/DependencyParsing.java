package resources.parser;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasTag;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoNLLOutputter;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.Redwood;
import resources.configuration.Configuration;
import resources.lemmatizing.GermanLemmatizer;
import resources.lemmatizing.StanfordLemmatizerEnglish;
import resources.maltparser.MaltParsing;
import resources.writer.DatabaseWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Demonstrates how to first use the tagger, then use the NN dependency
 * parser. Note that the parser will not work on untagged text.
 *
 * @author Dagmar Gromann
 */
public class DependencyParsing {

	 String dataPath = Configuration.dataSource.getProperty("dataPath");
	 String filename = Configuration.dataSource.getProperty("filename");
	 String dbName = Configuration.dataSource.getProperty("DBname");
	
	 //Change property between English and German 
	 String modelPath = Configuration.dataSource.getProperty("modelPath");
	 String taggerPath = Configuration.dataSource.getProperty("taggerPath");	
	 
	 
	 //If you want to do lemmatization
	 //StanfordLemmatizerEnglish lemmatizer = new StanfordLemmatizerEnglish();
	//GermanLemmatizer lemmatizer = new GermanLemmatizer();
	 
	 MaxentTagger tagger = new MaxentTagger(taggerPath);
	 DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);
	 
	 	 
	 public void parseInput(){		 		    
		 DatabaseWriter db = new DatabaseWriter(dbName);
		 MaxentTagger tagger = new MaxentTagger(taggerPath);
		 DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);
		 
		List<String> backNounDependencies = Arrays.asList("cop", "nmod");
		List<String> verbDependencies = Arrays.asList("acl:relcl", "nsubj", "nsubjpass");
		
		BufferedReader in = null;
		 
		try {
			in = new BufferedReader(new FileReader(new File(dataPath,filename)));
		} catch (IOException e1) {
			System.out.println("File not found "+e1);
			e1.printStackTrace();
		}
		 String s = "";
		 String originalSentence = "";
		 int rowID = 1;
		 
		 try {
			while((s = in.readLine()) != null){

				//Replace symbols and punctuation that might represent a problem for parser
				originalSentence = s.replaceAll("\"", "").replaceAll("\'", "");
				s = s.replaceAll("\\d+.", "").replaceAll("%", "percent").replaceAll("[-(){};!?<>%\"+]", "").replaceAll("\\s+", " ");
				s = s.replaceAll(",", " ").replaceAll("\\d+", "");
	
				 DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(s));
				 for (List<HasWord> sentence : tokenizer) {
					 List<TaggedWord> tagged = tagger.tagSentence(sentence);
					 //Tree parse = lp.apply(sentence);
					 
					 GrammaticalStructure gs = parser.predict(tagged);
					 Collection<TypedDependency> tdl = gs.typedDependencies();
				     
					 for (TypedDependency test : tdl){
						 String wordPrep = null;
						 String frontVerb = null;
						 String rel = null;
						 String verbDependency = null;
						 String backNoun = null;
						 String backNounCompound = null;
						 int positionFrontVerb = 0;
						 int positionBackNoun = 0;

						 //German:
						 if(test.dep().tag().equals("ADP")){
						 
							//English: 
						 //if(test.dep().tag().equals("IN")){
							 
							 int positionPrep = test.dep().index();
							 wordPrep = test.dep().value();
							 
							 if(test.gov().tag() != null && test.gov().tag().startsWith("NOUN")){
								 backNoun = test.gov().value();
								 positionBackNoun = test.gov().index();
								 rel = test.reln().getLongName();
							 }

							 for (TypedDependency t1 : tdl){
								 if((backNoun != null) && t1.toString().contains(backNoun) && t1.reln().toString().contains("compound")){
									 backNounCompound = t1.dep().value()+" "+t1.gov().value();
								 }
								 if((backNoun != null) && t1.toString().contains(backNoun+"-"+positionBackNoun) && backNounDependencies.contains(t1.reln().toString()) && t1.gov().tag().startsWith("VERB")){
									 frontVerb = t1.gov().value();
									 positionFrontVerb = t1.gov().index();
								 }
								 for (TypedDependency t2 : tdl){
									 if((frontVerb != null) && t2.toString().contains(frontVerb) ){
										 verbDependency = t2.dep().value();
									 }
								 }
							 }
							if (frontVerb != null && wordPrep != null && backNoun != null){
								if (backNounCompound != null){
									db.saveGermanOutput(rowID, rel, frontVerb.toLowerCase(), verbDependency, wordPrep.toLowerCase(), backNounCompound.toLowerCase(), originalSentence);
								}
								else{
									db.saveGermanOutput(rowID, rel, frontVerb.toLowerCase(), verbDependency, wordPrep.toLowerCase(), backNoun.toLowerCase(), originalSentence);
								}
							 }
					 	}
				 	}
				 }
				 rowID += 1;
			}			 
		} catch (IOException e) {
			System.out.println("Problem with reading input "+e);
			e.printStackTrace();
		}
	 }
}