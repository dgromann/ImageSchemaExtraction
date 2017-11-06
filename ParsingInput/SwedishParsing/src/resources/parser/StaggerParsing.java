/**
* Class to POS tag Swedish input sentences and send them to the parser
* @author: Dagmar Gromann
* @version: 1.0*/

package resources.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import resources.configuration.Configuration;
import se.su.ling.stagger.SwedishTokenizer;
import se.su.ling.stagger.TagNameException;
import se.su.ling.stagger.TaggedToken;
import se.su.ling.stagger.Tagger;
import se.su.ling.stagger.Token;
import se.su.ling.stagger.Tokenizer;

public class StaggerParsing {
	
	 String dataPath = Configuration.dataSource.getProperty("dataPath");
	 String filename = Configuration.dataSource.getProperty("fileName");
	 String model = Configuration.dataSource.getProperty("stModel");
	 Tagger tagger;
	 MaltParsing depParsing;
	
	 /*
	  * Main empty constructor to Stagger
	  */
	public StaggerParsing() {
		try {
			ObjectInputStream modelReader = new ObjectInputStream(new FileInputStream(model));
			tagger = (Tagger) modelReader.readObject();
			modelReader.close();
			tagger.setExtendLexicon(true);
			depParsing = new MaltParsing();
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	}
	
	/*
	 * Method for POS tagging that loads the main data file and sends the tagged and pre-parsed content 
	 * to the Maltparser, which in turn writes the parsed contents to the DB
	 */
	public void process() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(dataPath,filename)));
			} catch (FileNotFoundException e1) {
			System.out.println("File not found "+e1);
			e1.printStackTrace();
		}
		
		Tokenizer tokenizer = new SwedishTokenizer(reader);
		
		ArrayList<Token> sentences;
		int sentIdx = 1;
		
		try {
			while((sentences=tokenizer.readSentence())!=null) {
				StringBuilder sb = new StringBuilder();
				TaggedToken[] sent = new TaggedToken[sentences.size()];
				
				for (int i = 0; i < sentences.size(); i++){
					Token token = sentences.get(i);
					sb.append(sentences.get(i).value+" ");
					sent[i] = new TaggedToken(token, (sentIdx+":"+token.offset));
				}
				
				TaggedToken[] taggedSent = tagger.tagSentence(sent, true, false);
				
				int tokenIdx = 1;
				String[] tokens = new String[taggedSent.length];
				String[] lemmas = new String[taggedSent.length];
				for(TaggedToken token:taggedSent) {
					String form = token.token.value;
					String pos = tagger.getTaggedData().getPosTagSet().getTagName(token.posTag);
					String posFirst = "";
					String posRest = "\t_";
					if (pos.contains("|")){
						int index = pos.indexOf("|");
						posFirst = pos.substring(0, index);
						posRest = "\t"+pos.substring(index+1, pos.length());
					}
					else{
						posFirst = pos;
					}
					tokens[tokenIdx-1] = tokenIdx+"\t"+form+"\t_\t"+posFirst+"\t"+posFirst+posRest;
					lemmas[tokenIdx-1] = form;
					
					tokenIdx++;
				}
				System.out.println(tokens.toString());
				System.out.println(lemmas);
				System.out.println(sb.toString().trim());
				depParsing.parsingPreposition(tokens, lemmas, sb.toString().trim());
				
				sentIdx++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TagNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			tokenizer.yyclose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		depParsing.closeMaltParsing();
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		

}
