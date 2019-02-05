package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.EPrescribedMedication;
import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class EPrescribedMedicationService {
	@Autowired
	private ECarePathwayService service;
	
	//the medication in executed step or conduct complementary
	public EQuery prescribedMedication( EQuery eQuery) {
				
		//finding all the documents
		List<Document> medicationComps = service.getService(eQuery);	
		
		Map<String, Double> medicationTimes = new HashMap<>();
		
		//counting how many medication occurrences in complementary conducts/executed steps
		for( Document doc : medicationComps) {
			List<Document> complementaryConducts = doc.get( "complementaryConducts", new ArrayList<Document>());

			if( !complementaryConducts.isEmpty()) {				
				for( Document complementaryConduct : complementaryConducts) {
					Document doc2 = complementaryConduct.get( "prescribedresource", new Document());
											
					if( complementaryConduct.getString( "type").equals( "MedicamentoComplementar")) {
						
						String key = doc2.getString( "name");
						
						if ( key != null && !key.isEmpty()) {
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
			}	

			List<Document> executedSteps = doc.get( "executedSteps", new ArrayList<Document>());
				
			if (!executedSteps.isEmpty()) {
				for( Document step : executedSteps) {				
					Document doc2 = step.get( "step", new Document());
					
					if ( doc2.getString("type").equals("Tratamento") || 
						 doc2.getString("type").equals("Receita")) {
						
						List<Document> prescribed = step.get( "prescribedmedication", new ArrayList<Document>());
						
						for (Document document : prescribed) {
							Document medication = document.get( "medication", new Document());
														
							String key = medication.getString( "name");
							
							if ( key != null && !key.isEmpty()) {
								if (medicationTimes.containsKey( key)) {
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
			}							
		}			
		
		for (String key : medicationTimes.keySet()) {
			double value = service.rate( medicationTimes.get(key), medicationTimes.size());
			medicationTimes.replace( key, value);
		}
		
		List<Entry<String, Double>> list = new LinkedList<>( medicationTimes.entrySet());

		//sorting the list with a comparator
		service.sort(list, eQuery.getEAttribute().getRange().getOrder());
		
		list = service.select( eQuery.getEAttribute().getRange().getQuantity(), list);				
	
		EPrescribedMedication prescribedMedication = Query_metamodelFactory.eINSTANCE.createEPrescribedMedication();
		
		for (int i = 0; i < list.size(); i++) {
			prescribedMedication.getMedications().add(list.get(i).getValue() + "%: " + list.get(i).getKey());
		}
		
		eQuery.setEMethod(prescribedMedication);
		
		return eQuery;
	}

}
