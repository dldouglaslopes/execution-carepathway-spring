package com.douglas.carepathwayexecution.translator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.douglas.carepathwayexecution.query.DBConfig;
import com.douglas.carepathwayexecution.query.DBOperations;
import com.douglas.carepathwayexecution.translator.config.FileConfig;
import com.douglas.carepathwayexecution.translator.model.EPathwayTranslator;

import MetamodelExecution.EPathway;

public class Translator {
	private List<EPathway> pathways = new ArrayList<EPathway>();
	
	public void translate() {		
		jsonToXmi();		
		//saveEPathway(new DBConfig(), new DBOperations());	
	}
	
	private void jsonToXmi() {
		try {
			String folderStr = "C:/Users/dldou/OneDrive/Documentos/Protocolos/";	//path of JSON folder
			File folder = new File(folderStr);	
			String[] namesStr = folder.list(); // names of folders that contain in JSON folder
			
			for (int i = 0; i < namesStr.length; i++) {
				System.out.println(namesStr[i] + "---> TRANSLATE...");
				String pathStr = folderStr + namesStr[i]; //create the path of each folder in JSON folder
				
				int pos = namesStr[i].lastIndexOf(".");
				if (pos > 0 && pos < (namesStr[i].length() - 1)) {
					namesStr[i] = namesStr[i].substring(0, pos);
				}
				
				FileConfig fileConfig = new FileConfig(); //configure files
				fileConfig.setResource("C:/Users/dldou/OneDrive/Documentos/ProtocolosXMI/" +  namesStr[i] + ".xmi");
				
				EPathwayTranslator translator = new EPathwayTranslator();				
				
				JSONObject json = fileConfig.toJSONObject(pathStr); //convert JSON file to JSON object
				translator.toXMI(json); //convert JSON object to XMI files				
				
				fileConfig.saveContents(translator.getePathway()); //save the generated contents			
				pathways.add(translator.getePathway());
				System.err.println(namesStr[i] + ".xmi ---> OK!");
			}		
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void saveEPathway(DBConfig dbConfig, DBOperations dbOperations) {
		for (int i = 0; i < pathways.size(); i++) {
			if (!dbOperations.hasEPathway(pathways.get(i).getId())) {
				dbOperations.saveEPathway(pathways.get(i).getName(), pathways.get(i));	//adding executed pathway in MongoDB
				System.out.println("-->XMI(s) added in MongoDB.");
			}
		}
		
		dbConfig.close(); //closing MongoDB client	
		System.out.println("*Closed to the database --> OK");
	}
}
