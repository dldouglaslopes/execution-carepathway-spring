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
	
	private Map<String, Integer> medicationsMap;
	private int numVersion;
	private int qtdMedications;
	private int idPathway = 0; 
	
	public EQuery getMedications(EQuery eQuery, String name, int version) {
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					List<Document> docs = service.filterDocuments(eQuery);
					this.numVersion = 1;
					for (int i = 1; i < numVersion + 1; i++) {
						this.qtdMedications = 0;
						medicationsMap = new HashMap<>();
						QMedication qMedication = getData(	docs, 
								name,
								eQuery.getEAttribute().getRange(),
								i, 
								carePathway);
						if (qMedication.getPathway() != null) {
							eQuery.getEMethod().add(qMedication);
						}
					}
				}
			}
		}
		else if(version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery);
			this.numVersion = 1;
			for (int i = 1; i < numVersion + 1; i++) {
				this.qtdMedications = 0;
				medicationsMap = new HashMap<>();
				QMedication qMedication = getData(	docs, 
						name,
						eQuery.getEAttribute().getRange(),
						i, 
						carePathway);
				if (qMedication.getPathway() != null) {
					eQuery.getEMethod().add(qMedication);
				}
			}
		}
		else{
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery);
			this.qtdMedications = 0;
			medicationsMap = new HashMap<>();
			QMedication qMedication = getData(	docs, 
												name,
												eQuery.getEAttribute().getRange(),
												version, 
												carePathway);
			if (qMedication.getPathway() != null) {
				eQuery.getEMethod().add(qMedication);
			}
		}		
		return eQuery;
	}
	
	private QMedication getData(List<Document> docs, 
								String name, 
								ARange range, 
								int version, 
								CarePathway carePathway) {
		QMedication qMedication = Query_metamodelFactory.eINSTANCE.createQMedication();
		if (!docs.isEmpty()) {
			List<Entry<String, Double>> medications = 
					getMedicationInPathways(docs,
											name,
											range,
											version);				
			for (Entry<String, Double> entry : medications) {
				Medication medication = Query_metamodelFactory.eINSTANCE.createMedication();
				String[] medicationsArr = entry.getKey().split("-");
				if (medicationsArr.length > 1) {
					medication.setName(medicationsArr[1]);
				}
				else {
					medication.setName("");
				}
				medication.setId(medicationsArr[0]);				
				medication.setPercentage(service.decimalFormat(entry.getValue()) + "%");
				medication.setQuantity(medicationsMap.get(entry.getKey()));
				qMedication.getMedications().add(medication);
			}
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setQuantity(qMedication.getMedications().size());
			pathway.setVersion(version);
			pathway.setId(this.idPathway + "");
			qMedication.setPathway(pathway);
		}
		return qMedication; 
	}
	
	public List<Entry<String, Double>> getMedicationInPathways(List<Document> docs, 
																String name, 
																ARange range, 
																int number) { //the medication in executed step or conduct complementary
		for( Document doc : docs) {
			this.idPathway = doc.get("pathway", new Document()).getInteger("_id");
			int version = doc.get( "pathway", new Document()).getInteger("version");		
			if (version == number) {
				List<Document> complementaryConducts = doc.get( "complementaryConducts", new ArrayList<Document>());			
				if( !complementaryConducts.isEmpty()) {				
					getMedicationInComplementaryConducts(complementaryConducts, name);
				}	
				List<Document> executedSteps = doc.get( "executedSteps", new ArrayList<Document>());
				if (!executedSteps.isEmpty()) {
					getMedicationsInSteps(executedSteps, name);
				}
			}
			if (numVersion < version) {
				numVersion = version;
			}
		}
		Map<String, Double> percentMap = new HashMap<>();
		for (String key : medicationsMap.keySet()) {
			double value = service.rate( medicationsMap.get(key), qtdMedications);
			percentMap.put( key, value);
		}		
		List<Entry<String, Double>> list = new LinkedList<>( percentMap.entrySet());		
		service.sort(list, range.getOrder()); //sorting the list with a comparator		
		list = service.select( range.getQuantity(), list);						
		return list;
	}
	
	public void getMedicationsInSteps(List<Document> executedSteps, String name) { //the medication in executed step
		for( Document step : executedSteps) {				
			Document prescribedResource = step.get( "step", new Document());
			if ( prescribedResource.getString("type").equals("Tratamento") || 
				 prescribedResource.getString("type").equals("Receita")) {				
				List<Document> prescribedMedication = step.get( "pmedication", new ArrayList<Document>());
				for (Document document : prescribedMedication) {
					Document medication = document.get( "medication", new Document());
					String key = medication.getInteger("_id") + "-" + 
								medication.getString( "name");				
					if (name == null) {
						add(key);
						
					}			
					else {
						if (key.toLowerCase().matches(".*" + name.toLowerCase() + ".*")) {
							add(key);
						}
					}							
				}					
			}			
			if (prescribedResource.getString("type").equals("Receita")) {
				List<Document> prescribedPrescription = step.get( "pprescription", new ArrayList<Document>());				
				for (Document document : prescribedPrescription) {
					Document prescription = document.get( "prescription", new Document());
					String key = prescription.getInteger("_id") + "-" + 
								prescription.getString( "medication");				
					if (name == null) {
						add(key);
					}			
					else {
						if (key.toLowerCase().matches(".*" + name.toLowerCase() + ".*")) {
							add(key);
						}
					}
				}
			}
		}
	}
	
	public void getMedicationInComplementaryConducts(List<Document> complementaryConducts, String name) { //the medication in conduct complementary
		for( Document complementaryConduct : complementaryConducts) {
			Document prescribedResource = complementaryConduct.get( "prescribedresource", new Document());									
			if( complementaryConduct.getString( "type").equals( "MedicamentoComplementar")) {
				String key = prescribedResource.getInteger( "_id") + "-" +
								prescribedResource.getString( "name");				
				if (name == null) {
					add(key);
				}			
				else {
					if (key.toLowerCase().matches(".*" + name.toLowerCase() + ".*")) {
						add(key);
					}
				}
			}	
		}
	}
	
	private void add(String key) {
		if ( key != null && !key.isEmpty()) {
			if (medicationsMap.containsKey( key)) {
				int value = medicationsMap.get(key) + 1;
				medicationsMap.replace( key, value);
				qtdMedications++;
			}
			else {
				medicationsMap.put( key, 1);
				qtdMedications++;
			}
		}
	}
}