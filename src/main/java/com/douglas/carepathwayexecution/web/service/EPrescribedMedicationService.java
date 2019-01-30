package com.douglas.carepathwayexecution.web.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.FindIterable;

import QueryMetamodel.EQuery;

@Service
public class EPrescribedMedicationService {
	@Autowired
	private ECarePathwayService service;
	
	//medication in executed step or conduct complementary
	public EQuery prescribedMedication( EQuery eQuery) {
				
		//finding all the documents
		FindIterable<Document> medicationComps = service.getService(eQuery);	
		
		Map<String, Double> medicationTimes = new HashMap<>();
		
		//counting how many medication occurences in complementary conducts/executed steps
		for( Document doc : medicationComps) {
			List<Document> complementaryConducts = ( List<Document>) doc.get( "complementaryConducts");

			if( !complementaryConducts.isEmpty()) {				
				for( Document complementaryConduct : complementaryConducts) {
					Document doc2 = ( Document) complementaryConduct.get( "prescribedresource");
											
					if( complementaryConduct.getString( "type").equals( "MedicamentoComplementar") &&
							!doc2.getString( "name").isEmpty()) {
						
						String key = doc2.getString( "name");

						if (medicationTimes.containsKey( doc2.getString( "name"))) {
							double value = medicationTimes.get(key) + 1;
							medicationTimes.replace( key, value);
						}
						else {
							medicationTimes.put( key, 1.0);
						}
					}	
				}
			}	

			List<Document> executedSteps = ( List<Document>) doc.get( "executedSteps");
				
			for( Document step : executedSteps) {						
				if (doc.get("step.type").equals("Tratamento") || 
					doc.get("step.type").equals("Receita")) {
					
					List<Document> prescribed = ( List<Document>) doc.get( "prescribedmedication");
					
					for (Document document : prescribed) {
						Document medication = ( Document) document.get( "medication");
													
						String key = medication.getString( "name");
						
						if (medicationTimes.containsKey( medication.getString( "name"))) {
							double value = medicationTimes.get(key) + 1;
							medicationTimes.replace( key, value);
						}
						else {
							medicationTimes.put( key, 1.0);
						}
					}					
				}
			}				
		}			
		
		List<Entry<String, Double>> list = new LinkedList<>( medicationTimes.entrySet());

		//sorting the list with a comparator
		service.sort(list, eQuery.getEAttribute().getRange().getOrder());
		
		list = service.select( eQuery.getEAttribute().getRange().getQuantity(), list);				
	
		return eQuery;
	}

}
