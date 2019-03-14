package com.douglas.carepathwayexecution.init;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class initPathways {
	public static void main(String[] args) throws JSONException, Exception {
		String url = "http://app-desenv.hapvida.com.br/api/protocolo/hospitalar/protmed/autoria/repositorios/?format=json";
		
		JSONObject json = new JSONObject(getText(url)); 
		createFile( "names", json.toString());	
	}
	
	private static void createFile(String name, String text) throws IOException {
		List<String> lines = Arrays.asList(text);
		Path file = Paths.get("C:/Users/dldou/OneDrive/Documentos/ProtocolosArquivos/" + name + ".json");
		Files.write(file, lines, Charset.forName("UTF-8"));
	}
	
	private static String getText(String url) throws IOException {        
        try {
        	URL api = new URL(url);
			URLConnection connection = api.openConnection();
			
			BufferedReader in = new BufferedReader( new InputStreamReader(connection.getInputStream(),"UTF8"));

	        StringBuilder response = new StringBuilder();
	        String inputLine;

	        while ((inputLine = in.readLine()) != null) 
	            response.append(inputLine);

	        in.close();
	        
	        return response.toString();
		} catch (IOException e) {
			return null;
		}	
    }
}
