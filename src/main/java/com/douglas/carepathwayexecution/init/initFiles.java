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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class initFiles {
	public static void main(String[] args) throws JSONException, Exception {
		String url = "";
		for (int j = 0; j < 5; j++) {			
			if (url != null) {
				System.out.println(url);
				JSONObject json = new JSONObject(getText(url)); 
				JSONArray results = json.getJSONArray("results");
				for (int i = 0; i < results.length(); i++) {
					JSONObject result = results.getJSONObject(i);
					String name = "" + result.get("id");
					System.out.println(name);
					if (!name.equals("3998") && !name.equals("3939")
							&& !name.equals("4000")
							&& !name.equals("4019")) {
						String urlResult = result.getString("url") + "resumo/?format=json";
						JSONObject jsonResult = new JSONObject(getText(urlResult)); 
						createFile( name, jsonResult.toString());
					}
				}
				url = json.getString("next");
			}	
		}			
	}
	
	private static void createFile(String name, String text) throws IOException {
		List<String> lines = Arrays.asList(text);
		Path file = Paths.get("C:/Users/dldou/OneDrive/Documentos/Protocolos/" + name + ".json");
		Files.write(file, lines, Charset.forName("UTF-8"));
	}
	
	private static String getText(String url) throws Exception {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader( new InputStreamReader(connection.getInputStream(),"UTF8"));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) 
            response.append(inputLine);

        in.close();

        return response.toString();
    }
}
