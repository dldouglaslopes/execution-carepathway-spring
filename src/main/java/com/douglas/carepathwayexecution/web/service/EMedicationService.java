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

import QueryMetamodel.EMedication;
import QueryMetamodel.EQuery;
import QueryMetamodel.Medication;
import QueryMetamodel.Pathway;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class EMedicationService {
	@Autowired
	private ECarePathwayService service;
	
	//the medication in executed step or conduct complementary
	public EQuery prescribedMedication(EQuery eQuery, String name) {
		//finding all the documents
		List<Document> medicationComps = service.getService(eQuery);	
		
		Map<String, Integer> medicationTimes = new HashMap<>();
		Map<String, Integer> medicationIds = new HashMap<>();
		Map<String, List<String>> medicationPathway = new HashMap<>();
		
		//counting how many medication occurrences in complementary conducts/executed steps
		for( Document doc : medicationComps) {
			List<Document> complementaryConducts = doc.get( "complementaryConducts", new ArrayList<Document>());
			Document pathway = doc.get("pathway", new Document());
			
			if( !complementaryConducts.isEmpty()) {				
				for( Document complementaryConduct : complementaryConducts) {
					Document prescribedResource = complementaryConduct.get( "prescribedresource", new Document());
											
					if( complementaryConduct.getString( "type").equals( "MedicamentoComplementar")) {
						
						String key = prescribedResource.getString( "name");
						
						if ( key != null && !key.isEmpty()) {
							if (medicationTimes.containsKey( prescribedResource.getString( "name"))) {
								int value = medicationTimes.get(key) + 1;
								medicationTimes.replace( key, value);
								List<String> pathways = medicationPathway.get(key);
								pathways.add(pathway.getString("name") + "/" + pathway.getInteger("_id"));
								medicationPathway.replace( key, pathways);
							}
							else {
								medicationIds.put( key, prescribedResource.getInteger("_id"));
								medicationTimes.put( key, 1);
								List<String> pathways = new ArrayList<>();
								pathways.add( pathway.getString("name") + "/" + pathway.getInteger("_id"));
								medicationPathway.put( key, pathways);
							}							
						}
					}	
				}
			}	

			List<Document> executedSteps = doc.get( "executedSteps", new ArrayList<Document>());
				
			if (!executedSteps.isEmpty()) {
				for( Document step : executedSteps) {				
					Document prescribedResource = step.get( "step", new Document());
					
					if ( prescribedResource.getString("type").equals("Tratamento") || 
						 prescribedResource.getString("type").equals("Receita")) {
						
						List<Document> prescribedMedication = step.get( "prescribedmedication", new ArrayList<Document>());
						
						for (Document document : prescribedMedication) {
							Document medication = document.get( "medication", new Document());
														
							String key = medication.getString( "name");
							
							if ( key != null && !key.isEmpty()) {
								if (medicationTimes.containsKey( key)) {
									int value = medicationTimes.get(key) + 1;
									medicationTimes.replace( key, value);
								}
								else {
									medicationIds.put( key, prescribedResource.getInteger("_id"));
									medicationTimes.put( key, 1);
								}
							}							
						}					
					}
				}
			}							
		}			
		
		Map<String, Double> percentMap = new HashMap<>();
		
		for (String key : medicationTimes.keySet()) {
			double value = service.rate( medicationTimes.get(key), medicationTimes.size());
			percentMap.put( key, value);
		}
		
		List<Entry<String, Double>> list = new LinkedList<>( percentMap.entrySet());

		//sorting the list with a comparator
		service.sort(list, eQuery.getEAttribute().getRange().getOrder());
		
		list = service.select( eQuery.getEAttribute().getRange().getQuantity(), list);				
	
		EMedication eMedication = Query_metamodelFactory.eINSTANCE.createEMedication();
		
		for (int i = 0; i < list.size(); i++) {
			String key = list.get(i).getKey();
			
			Medication medication = Query_metamodelFactory.eINSTANCE.createMedication();
			medication.setName( key);
			medication.setPercentage( list.get(i).getValue() + "%");
			medication.setQuantity( medicationTimes.get(key));							
			
			List<String> idPathwaysList = medicationPathway.get(key);
			Map<String, List<Integer>> idPathways = new HashMap<>();

			for (int k = 0; k < idPathwaysList.size(); k++) {
				String[] idPathwaysArr = service.splitBy( idPathwaysList.get(k), "/");

				if (idPathways.containsKey("name")) {
					List<Integer> idPathwaysMap = idPathways.get(idPathwaysArr[0]);
					idPathwaysMap.add( Integer.parseInt( idPathwaysArr[1]));
					
					idPathways.replace( idPathwaysArr[0], idPathwaysMap);
				}
				else {
					List<Integer> idPathwaysMap = new ArrayList<>();
					idPathwaysMap.add( Integer.parseInt( idPathwaysArr[1]));
					
					idPathways.put(idPathwaysArr[0], idPathwaysMap);
				}
			}	
			
			for (String idPathway : idPathways.keySet()) {
				Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
				double percentage = service.rate( idPathways.get(idPathway).size(), idPathwaysList.size());
				pathway.setPercentage( percentage + "%");
				pathway.setQuantity(idPathways.get(idPathway).size());
				pathway.getId().addAll(idPathways.get(idPathway));
				pathway.setName(idPathway);

				medication.getPathway().add(pathway);
			} 	
			
			eMedication.getMedications().add(medication);
		}
		
		eQuery.setEMethod(eMedication);
		
		return eQuery;
	}
	
	public EQuery prescribedMedication( EQuery eQuery) {
		return prescribedMedication(eQuery, null);
	}
}