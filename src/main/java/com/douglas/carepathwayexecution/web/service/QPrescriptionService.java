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
import QueryMetamodel.Prescription;
import QueryMetamodel.QPrescription;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Version;

@Service
public class QPrescriptionService {
	@Autowired
	private QCarePathwayService service;

	private Map<String, Integer> prescriptionsMap;
	private Map<String, Map<String, Integer>> medicationsMap;
	private int qtdPrescriptions;
	
	public EQuery getRecurrentPrescription(EQuery eQuery, String prescription, int version) {
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					for (int i = 1; i < numVersion + 1; i++) {
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						List<Document> docs = new ArrayList<Document>(); //finding all the documents
						this.prescriptionsMap = new HashMap<>();
						this.qtdPrescriptions = 0;
						this.medicationsMap = new HashMap<>();
						for (int j = 0; j < 100; j++) {
							docs = service.filterDocuments(eQuery, j);
							QPrescription qPrescription = getData(docs, 
									prescription, 
									eQuery.getEAttribute().getRange(), 
									carePathway, 
									i,
									j);
							if (qPrescription.getPathway() != null) {
								eQuery.getEMethod().add(qPrescription);
							}
						}
					}
				}							
			}	
		}
		else if(version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setName(carePathway);
			int numVersion = Version.getByName(carePathway.getName()).getValue();
			this.prescriptionsMap = new HashMap<>();
			this.qtdPrescriptions = 0;
			this.medicationsMap = new HashMap<>();
			for (int i = 1; i < numVersion + 1; i++) {
				eQuery.getEAttribute().getCarePathway().setVersion(i);
				List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
				QPrescription qPrescription = getData(docs, 
						prescription, 
						eQuery.getEAttribute().getRange(), 
						carePathway, 
						i,
						99);
				docs.clear();
				if (qPrescription.getPathway() != null) {
					eQuery.getEMethod().add(qPrescription);
				}
			}		
		}
		else {
			this.prescriptionsMap = new HashMap<>();
			this.qtdPrescriptions = 0;
			this.medicationsMap = new HashMap<>();
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setName(carePathway);
			List<Document> docs = service.filterDocuments(eQuery);
			QPrescription qPrescription = getData(docs, 
					prescription, 
					eQuery.getEAttribute().getRange(), 
					carePathway, 
					version, 
					99);
			docs.clear();
			if (qPrescription.getPathway() != null) {
				eQuery.getEMethod().add(qPrescription);
			}									
		}		
		return eQuery;
	}

	private QPrescription getData(List<Document> docs, 
						String prescriptionStr, 
						ARange range, 
						CarePathway carePathway, 
						int version,
						int page) {
		QPrescription qPrescription = Query_metamodelFactory.eINSTANCE.createQPrescription();
		List<Entry<String, Double>> list = getPrescriptions(docs, prescriptionStr, version, range, 99);
		if (list.size() > 0) {			
			for (Entry<String, Double> entry : list) {
				Prescription prescription = Query_metamodelFactory.eINSTANCE.createPrescription();
				String key = entry.getKey();
				String[] prescriptionArr = key.split("%");
				prescription.setId(prescriptionArr[0]);
				prescription.setName(prescriptionArr[1]);
				prescription.setQuantity(prescriptionsMap.get(key));
				prescription.setPercentage(service.decimalFormat(entry.getValue()) + "%");
				qPrescription.getPrescription().add(prescription);
				Map<String, Integer> medications = medicationsMap.get(key);
				for (String	medicationStr : medications.keySet()) {
					String[] medicationArr = medicationStr.split("%");
					Medication medication = Query_metamodelFactory.eINSTANCE.createMedication();
					medication.setId(medicationArr[0]);
					medication.setName(medicationArr[1]);
					double percentage = service.rate(medications.get(medicationStr), prescriptionsMap.get(key));
					medication.setPercentage(service.decimalFormat(percentage) + "%");
					medication.setQuantity(medications.get(medicationStr));
					prescription.getMedication().add(medication);
				}
			}
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setId(carePathway.getValue() + "");
			pathway.setName(carePathway.getName());
			pathway.setQuantity(this.qtdPrescriptions);
			pathway.setVersion(version);
			qPrescription.setPathway(pathway);
		}		
		return qPrescription;
	}
	
	private List<Entry<String, Double>> getPrescriptions(List<Document> docs, 
															String prescriptionStr, 
															int number, 
															ARange range,
															int page) {
		for (Document doc : docs) {
			List<Document> eSteps = doc.get("executedSteps", new ArrayList<>());			
			getPrescriptionInStep(eSteps, prescriptionStr);
		}
		List<Entry<String, Double>> list = new LinkedList<>();		
		if (page == 99) {
			Map<String, Double> percentMap = new HashMap<>();
			for (String key : prescriptionsMap.keySet()) {
				double value = service.rate( prescriptionsMap.get(key), this.qtdPrescriptions);
				percentMap.put( key, value);
			}		
			list = new LinkedList<>( percentMap.entrySet());		
			service.sort(list, range.getOrder()); //sorting the list with a comparator		
			list = service.select( range.getQuantity(), list);
		}						
		return list;
	}
	
	private void getPrescriptionInStep(List<Document> eSteps, String name) {
		for (Document eStep : eSteps) { 
			Document step = eStep.get("step", new Document());
			String typeStr = step.getString("type");				
			if (typeStr.equals("Receita")) {
				List<Document> prescribedPrescription = eStep.get( "pprescription", new ArrayList<Document>());
				for (Document document : prescribedPrescription) {
					Document prescription = document.get( "prescription", new Document());
					String key = step.getInteger("_id") + "%" + 
								step.getString( "name");	
					String medication = prescription.getInteger("idMedication") + "%" + 
										prescription.getString( "medication");
					if (name == null) {
						add(key, medication);
					}			
					else {
						if (key.toLowerCase().matches(".*" + name.toLowerCase() + ".*")) {
							add(key, medication);
						}
					}
				}
			}
		}
	}

	private void add(String key, String medication) {
		if (prescriptionsMap.containsKey(key)) {
			int value = prescriptionsMap.get(key) + 1;
			prescriptionsMap.replace(key, value);
			this.qtdPrescriptions++;
		}
		else {
			prescriptionsMap.put(key, 1);
			this.qtdPrescriptions++;
		}
		if (medicationsMap.containsKey(key)) {
			if (medicationsMap.get(key).containsKey(medication)) {
				Map<String, Integer> map = medicationsMap.get(key);
				int value = map.get(medication) + 1;
				map.replace(medication, value);
				medicationsMap.replace(key, map);
			}
			else {
				Map<String, Integer> map = medicationsMap.get(key);
				map.put(medication, 1);
				medicationsMap.put(key, map);
			}
		}
		else {
			Map<String, Integer> map = new HashMap<>();
			map.put(medication, 1);
			medicationsMap.put(key, map);
		}
	}

	public EQuery getResults(JSONArray data) {
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < data.length(); i++) {
			JSONObject object = data.getJSONObject(i);
			JSONArray prescriptions = object.getJSONArray("prescription");
			for (int j = 0; j < prescriptions.length(); j++) {
				JSONObject prescription = prescriptions.getJSONObject(j);
				String name = prescription.getString("name");
				int quantity = prescription.getInt("quantity");
				if (map.containsKey(name)) {
					int value = quantity +
							map.get(name);
					map.replace( name, value);
				}
				else {
					map.put(name, quantity);
				}
			}
		}
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		for (String name : map.keySet()) {
			QPrescription qPrescription = Query_metamodelFactory.eINSTANCE.createQPrescription();
			Prescription prescription = Query_metamodelFactory.eINSTANCE.createPrescription();
			Integer quantity = map.get(name);
			prescription.setName(name);
			prescription.setQuantity(quantity);
			qPrescription.getPrescription().add(prescription);
			qPrescription.setPathway(null);
			eQuery.getEMethod().add(qPrescription);
		}
		return eQuery;
	}
}
