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

import QueryMetamodel.ARange;
import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Medication;
import QueryMetamodel.Pathway;
import QueryMetamodel.QMedication;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class QMedicationService {
	@Autowired
	private QCarePathwayService service;
	
	private Map<String, Integer> medicationsMap = new HashMap<>();
	private Map<String, Double> percentMap = new HashMap<>();
	
	public EQuery getMedications(EQuery eQuery, String name) {
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				eQuery.getEAttribute().getCarePathway().setName(carePathway);
				List<Document> docs = service.filterDocuments(eQuery);
				if (!docs.isEmpty()) {
					List<Entry<String, Double>> medications = getMedicationInPathways(docs,
																					eQuery.getEAttribute().getRange());
					QMedication qMedication =  Query_metamodelFactory.eINSTANCE.createQMedication();
					for (Entry<String, Double> entry : medications) {
						Medication medication = Query_metamodelFactory.eINSTANCE.createMedication();
						medication.setName(entry.getKey());
						medication.setPercentage(percentMap.get(entry.getKey()) + "%");
						medication.setQuantity(medicationsMap.get(entry.getKey()));;
						qMedication.getMedications().add(medication);
					}
					Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
					pathway.setName(carePathway.getName());
					pathway.setPercentage("");
					pathway.setQuantity(medications.size());
					qMedication.setPathway(pathway);
					eQuery.getEMethod().add(qMedication);
				}
			}
		}
		else{
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery);
			if (!docs.isEmpty()) {
				List<Entry<String, Double>> medications = getMedicationInPathways(docs,
						eQuery.getEAttribute().getRange());
				QMedication qMedication =  Query_metamodelFactory.eINSTANCE.createQMedication();
				for (Entry<String, Double> entry : medications) {
					Medication medication = Query_metamodelFactory.eINSTANCE.createMedication();
					medication.setName(entry.getKey());
					medication.setPercentage(percentMap.get(entry.getKey()) + "%");
					medication.setQuantity(medicationsMap.get(entry.getKey()));;
					qMedication.getMedications().add(medication);
				}
				Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
				pathway.setName(carePathway.getName());
				pathway.setPercentage("");
				pathway.setQuantity(medications.size());
				qMedication.setPathway(pathway);
				eQuery.getEMethod().add(qMedication);
			}
		}
		
		return eQuery;
	}
	
	public List<Entry<String, Double>> getMedicationInPathways(List<Document> docs, ARange range) { //the medication in executed step or conduct complementary
		for( Document doc : docs) {
			List<Document> complementaryConducts = doc.get( "complementaryConducts", new ArrayList<Document>());			
			if( !complementaryConducts.isEmpty()) {				
				getMedicationInComplementaryConducts(complementaryConducts);
			}	
			List<Document> executedSteps = doc.get( "executedSteps", new ArrayList<Document>());
			if (!executedSteps.isEmpty()) {
				getMedicationsInSteps(executedSteps);
			}							
		}		
		percentMap = new HashMap<>();
		for (String key : medicationsMap.keySet()) {
			double value = service.rate( medicationsMap.get(key), medicationsMap.size());
			percentMap.put( key, value);
		}		
		List<Entry<String, Double>> list = new LinkedList<>( percentMap.entrySet());		
		service.sort(list, range.getOrder()); //sorting the list with a comparator		
		list = service.select( range.getQuantity(), list);						
		return list;
	}
	
	public void getMedicationsInSteps(List<Document> executedSteps) { //the medication in executed step
		for( Document step : executedSteps) {				
			Document prescribedResource = step.get( "step", new Document());
			
			if ( prescribedResource.getString("type").equals("Tratamento") || 
				 prescribedResource.getString("type").equals("Receita")) {
				
				List<Document> prescribedMedication = step.get( "pmedication", new ArrayList<Document>());
				
				for (Document document : prescribedMedication) {
					Document medication = document.get( "medication", new Document());
					String key = medication.getString( "name");							
					add(key);							
				}					
			}
			
			if (prescribedResource.getString("type").equals("Receita")) {
				List<Document> prescribedPrescription = step.get( "pprescription", new ArrayList<Document>());
				
				for (Document document : prescribedPrescription) {
					Document prescription = document.get( "prescription", new Document());
					String key = prescription.getString( "medication");							
					add(key);						
				}
			}
		}
	}
	
	public void getMedicationInComplementaryConducts(List<Document> complementaryConducts) { //the medication in conduct complementary
		for( Document complementaryConduct : complementaryConducts) {
			Document prescribedResource = complementaryConduct.get( "prescribedresource", new Document());
									
			if( complementaryConduct.getString( "type").equals( "MedicamentoComplementar")) {
				String key = prescribedResource.getString( "name");						
				add(key);
			}	
		}
	}
	
	private void add(String key) {
		if ( key != null && !key.isEmpty()) {
			if (medicationsMap.containsKey( key)) {
				int value = medicationsMap.get(key) + 1;
				medicationsMap.replace( key, value);
			}
			else {
				medicationsMap.put( key, 1);
			}
		}
	}
}