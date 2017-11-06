// This is a pos tagger and lemmatizer for Dutch, English
package resources.lemmatizing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.hfst.NoTokenizationException;
import postaggersalanguage.Lemmatizer;
import postaggersalanguage.POSTaggersALanguage;
import resources.configuration.Configuration;

public class GermanLemmatizer {

	String resourceFolder = Configuration.dataSource.getProperty("resourceFolder");
	String language = "de";
			
	POSTaggersALanguage postagger = new POSTaggersALanguage();
	Lemmatizer lemmatizer = new Lemmatizer();
	
	
	public String lemmatizeGerman(String word, String pos){
		String lemma = null;
		try {
			System.out.println(word);
			lemma = Lemmatizer.getLemma(resourceFolder, word, language, pos);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoTokenizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (StringIndexOutOfBoundsException e){
			e.printStackTrace();
		}catch (ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		return lemma;
	}
}
