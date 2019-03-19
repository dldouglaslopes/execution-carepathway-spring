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
import QueryMetamodel.Prescription;
import QueryMetamodel.QPrescription;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class QPrescriptionService {
	@Autowired
	private QCarePathwayService service;

	private int numVersion;
	private int idPathway = 0;
	private Map<String, Integer> prescriptionsMap;
	private Map<String, Map<String, Integer>> medicationsMap;
	private int qtdPrescriptions;
	
	public EQuery getRecurrentPrescription(EQuery eQuery, String prescription, int version) {
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					List<Document> docs = service.filterDocuments(eQuery);
					this.numVersion = 1;
					for (int i = 1; i < numVersion + 1; i++) {
						QPrescription qPrescription = getData(docs, 
								prescription, 
								eQuery.getEAttribute().getRange(), 
								carePathway, 
								i);
						if (qPrescription.getPathway() != null) {
							eQuery.getEMethod().add(qPrescription);
						}
					}
				}							
			}	
		}
		else if(version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setName(carePathway);
			List<Document> docs = service.filterDocuments(eQuery);
			numVersion = 1;
			for (int i = 1; i < numVersion + 1; i++) {
				QPrescription qPrescription = getData(docs, 
						prescription, 
						eQuery.getEAttribute().getRange(), 
						carePathway, 
						i);
				if (qPrescription.getPathway() != null) {
					eQuery.getEMethod().add(qPrescription);
				}
			}		
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setName(carePathway);
			List<Document> docs = service.filterDocuments(eQuery);
			QPrescription qPrescription = getData(docs, 
					prescription, 
					eQuery.getEAttribute().getRange(), 
					carePathway, 
					version);
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
						int version) {
		QPrescription qPrescription = Query_metamodelFactory.eINSTANCE.createQPrescription();
		this.prescriptionsMap = new HashMap<>();
		this.qtdPrescriptions = 0;
		this.medicationsMap = new HashMap<>();
		if (!docs.isEmpty()) {			
			List<Entry<String, Double>> list = getPrescriptions(docs, prescriptionStr, version, range);
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
			pathway.setId(this.idPathway + "");
			pathway.setName(carePathway.getName());
			pathway.setQuantity(this.qtdPrescriptions);
			pathway.setVersion(version);
			qPrescription.setPathway(pathway);
		}		
		return qPrescription;
	}
	
	private List<Entry<String, Double>> getPrescriptions(List<Document> docs, String prescriptionStr, int number, ARange range) {
		for (Document doc : docs) {
			int version = doc.get("pathway", new Document()).getInteger("version");
			this.idPathway = doc.get("pathway", new Document()).getInteger("_id");
			if (version == number) {
				List<Document> eSteps = doc.get("executedSteps", new ArrayList<>());			
				getPrescriptionInStep(eSteps, prescriptionStr);
			}
			if (this.numVersion < version) {
				this.numVersion = version;
			}
		}
		Map<String, Double> percentMap = new HashMap<>();
		for (String key : prescriptionsMap.keySet()) {
			double value = service.rate( prescriptionsMap.get(key), this.qtdPrescriptions);
			percentMap.put( key, value);
		}		
		List<Entry<String, Double>> list = new LinkedList<>( percentMap.entrySet());		
		service.sort(list, range.getOrder()); //sorting the list with a comparator		
		list = service.select( range.getQuantity(), list);						
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
}
