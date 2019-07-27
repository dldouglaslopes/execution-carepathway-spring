package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.ARange;
import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Medication;
import QueryMetamodel.Pathway;
import QueryMetamodel.QMedication;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Step;
import QueryMetamodel.Version;

@Service
public class QMedicationService {
	@Autowired
	private QCarePathwayService service;
	
	private Map<String, Integer> medicationsMap;
	private Map<String, Map<String, Integer>> stepsMap;
	private int qtdMedications;
	
	public EQuery getMedications(EQuery eQuery, String name, int version) {
		long start = System.currentTimeMillis();
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					for (int i = 1; i < numVersion + 1; i++) {
						this.qtdMedications = 0;
						medicationsMap = new HashMap<>();
						stepsMap = new HashMap<>();
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						List<Document> docs = new ArrayList<Document>();
						for (int j = 0; j < 100; j++) {
							docs = service.filterDocuments(eQuery, j);
							QMedication qMedication = getData(	docs, 
									name,
									eQuery.getEAttribute().getRange(),
									i, 
									carePathway,
									j);
							if (qMedication.getPathway() != null) {
								eQuery.getEMethod().add(qMedication);
							}	
						}
					}
				}
			}
		}
		else if(version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			int numVersion = Version.getByName(carePathway.getName()).getValue();
			for (int i = 1; i < numVersion + 1; i++) {
				this.qtdMedications = 0;
				medicationsMap = new HashMap<>();
				stepsMap = new HashMap<>();
				eQuery.getEAttribute().getCarePathway().setVersion(i);
				List<Document> docs = service.filterDocuments(eQuery);
				QMedication qMedication = getData(	docs, 
						name,
						eQuery.getEAttribute().getRange(),
						i, 
						carePathway,
						99);
				docs.clear();
				if (qMedication.getPathway() != null) {
					eQuery.getEMethod().add(qMedication);
				}
			}
		}
		else{
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setVersion(version);
			List<Document> docs = service.filterDocuments(eQuery);
			this.qtdMedications = 0;
			medicationsMap = new HashMap<>();
			stepsMap = new HashMap<>();
			QMedication qMedication = getData(	docs, 
												name,
												eQuery.getEAttribute().getRange(),
												version, 
												carePathway,
												99);
			docs.clear();
			if (qMedication.getPathway() != null) {
				eQuery.getEMethod().add(qMedication);
			}
		}
		System.out.println("Total: "+(System.currentTimeMillis() - start));
		return eQuery;
	}
	
	private QMedication getData(List<Document> docs, 
								String name, 
								ARange range, 
								int version, 
								CarePathway carePathway,
								int page) {
		QMedication qMedication = Query_metamodelFactory.eINSTANCE.createQMedication();
		List<Entry<String, Double>> medications = 
				getMedicationInPathways(docs,
										name,
										range,
										page);
		if (medications.size() > 0) {
			for (Entry<String, Double> entry : medications) {
				Medication medication = Query_metamodelFactory.eINSTANCE.createMedication();
				String key = entry.getKey();
				String[] medicationsArr = entry.getKey().split("-");
				int size = medicationsArr.length;
				if (size > 2) {
					medication.setName(medicationsArr[1]);
					medication.setBrand(medicationsArr[2]);					
				}
				else if (size > 1) {
					medication.setName(medicationsArr[1]);
					medication.setBrand("");
				}
				medication.setId(medicationsArr[0]);						
				medication.setPercentage(service.decimalFormat(entry.getValue()) + "%");
				medication.setQuantity(medicationsMap.get(entry.getKey()));
				for (String stepStr : stepsMap.get(key).keySet()) {
					Step step = Query_metamodelFactory.eINSTANCE.createStep();
					String[] stepArr = stepStr.split("%");
					step.setId(stepArr[0]);
					step.setType(stepArr[2]);
					step.setName(stepArr[1]);
					double percentage = service.rate(stepsMap.get(key).get(stepStr), medicationsMap.get(key));
					step.setPercentage(service.decimalFormat(percentage) + "%");
					if (stepArr.length > 3) {
						step.setDescription(stepArr[3]);
					}
					step.setQuantity(stepsMap.get(key).get(stepStr));
					medication.getStep().add(step);
				}
				qMedication.getMedications().add(medication);
			}
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setQuantity(qMedication.getMedications().size());
			pathway.setVersion(version);
			pathway.setId(carePathway.getValue() + "");
			qMedication.setPathway(pathway);
		}
		return qMedication; 
	}
	
	public List<Entry<String, Double>> getMedicationInPathways(List<Document> docs, 
																String name, 
																ARange range,
																int page) { //the medication in executed step or conduct complementary
		for( Document doc : docs) {
			List<Document> complementaryConducts = doc.get( "complementaryConducts", new ArrayList<Document>());			
			if( !complementaryConducts.isEmpty()) {				
				getMedicationInComplementaryConducts(complementaryConducts, name);
			}	
			List<Document> executedSteps = doc.get( "executedSteps", new ArrayList<Document>());
			if (!executedSteps.isEmpty()) {
				getMedicationsInSteps(executedSteps, name);
			}
		}
		List<Entry<String, Double>> list = new ArrayList<Map.Entry<String,Double>>();
		if (page == 99) {
			Map<String, Double> percentMap = new HashMap<>();
			for (String key : medicationsMap.keySet()) {
				double value = service.rate( medicationsMap.get(key), qtdMedications);
				percentMap.put( key, value);
			}		
			list = new LinkedList<>( percentMap.entrySet());		
			service.sort(list, range.getOrder()); //sorting the list with a comparator		
			list = service.select( range.getQuantity(), list);
		}						
		return list;
	}
	
	public void getMedicationsInSteps(List<Document> executedSteps, String name) { //the medication in executed step
		for( Document step : executedSteps) {				
			Document prescribedResource = step.get( "step", new Document());
			if ( prescribedResource.getString("type").equals("Tratamento") //|| 
				 //prescribedResource.getString("type").equals("Receita")
					) {				
				List<Document> prescribedMedication = step.get( "pmedication", new ArrayList<Document>());
				for (Document document : prescribedMedication) {
					Document medication = document.get( "medication", new Document());
					String key = medication.getInteger("idMedication") + "-" + 
								medication.getString( "name") + "-" +
								medication.getString( "brand");				
					String stepStr = step.get("step", new Document()).getInteger("_id") + "%" +
							step.get("step", new Document()).getString("name") + "%" + 
							step.get("step", new Document()).getString("type") + "%" +
							step.get("step", new Document()).getString("description");
					if (name == null) {						
						add(key, stepStr);						
					}			
					else {
						if (key.toLowerCase().matches(".*" + name.toLowerCase() + ".*")) {
							add(key, stepStr);
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
				String key = prescribedResource.getInteger( "idMedication") + "-" +
								prescribedResource.getString( "name") + "-" +
								prescribedResource.getString( "brand");				
				String step = complementaryConduct.getInteger("_id") + "%" +
					 	complementaryConduct.getString("resource") + "%" + 
					 	complementaryConduct.getString( "type") + "%" +
						complementaryConduct.getString("justification");
				if (name == null) {
					add(key, step);
				}			
				else {
					if (key.toLowerCase().matches(".*" + name.toLowerCase() + ".*")) {
						add(key, step);
					}
				}
			}	
		}
	}
	
	private void add(String key, String step) {
		if ( key != null && !key.isEmpty()) {
			if (medicationsMap.containsKey( key)) {
				int value = medicationsMap.get(key) + 1;
				medicationsMap.replace( key, value);
				this.qtdMedications++;
			}
			else {
				medicationsMap.put( key, 1);
				this.qtdMedications++;
			}
		}
		if (stepsMap.containsKey(key)) {
			if (stepsMap.get(key).containsKey(step)) {
				Map<String, Integer> value = stepsMap.get(key);
				int sum = value.get(step) + 1;
				value.replace(step, sum);
				stepsMap.put(key, value);
			}
			else {
				Map<String, Integer> value = stepsMap.get(key);
				value.put(step, 1);
				stepsMap.put(key, value);
			}
		}
		else {
			Map<String, Integer> value = new HashMap<>();
			value.put(step, 1);
			stepsMap.put(key, value);
		}
	}

	public EQuery getResults(JSONArray data) {
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < data.length(); i++) {
			JSONObject object = data.getJSONObject(i);
			JSONArray medications = object.getJSONArray("medications");
			for (int j = 0; j < medications.length(); j++) {
				JSONObject medication = medications.getJSONObject(j);
				int quantity = medication.getInt("quantity");
				Object name = medication.get("name");
				if (map.containsKey(name.toString())) {
					int value = quantity +
							map.get(name.toString());
					map.replace( name.toString(), value);
				}
				else {
					map.put(name.toString(), quantity);
				}
			}
			 
		}
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		for (String name : map.keySet()) {
			QMedication qMedication = Query_metamodelFactory.eINSTANCE.createQMedication();
			Medication medication = Query_metamodelFactory.eINSTANCE.createMedication();
			medication.setName(name);
			medication.setQuantity(map.get(name));
			qMedication.getMedications().add(medication);
			eQuery.getEMethod().add(qMedication);
		}
		return eQuery;
	}
}