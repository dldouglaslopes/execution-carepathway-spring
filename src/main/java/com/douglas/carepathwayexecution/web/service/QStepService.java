package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.QStep;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Step;

@Service
public class QStepService {
	@Autowired
	private QCarePathwayService service;
	
	private Map<String, Integer> stepsMap;
	private int count;
	private int numVersion = 1;
	
	public EQuery getRecurrentSteps(EQuery eQuery, String stepStr, int version) {
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				stepsMap = new HashMap<>();
				count = 0; 
				eQuery.getEAttribute().getCarePathway().setName(carePathway);
				List<Document> docs = service.filterDocuments(eQuery);
				if (!docs.isEmpty()) {		
					QStep qStep = Query_metamodelFactory.eINSTANCE.createQStep();
					getSteps(docs, stepStr, 0);
					for (String key : stepsMap.keySet()) {
						Step step = Query_metamodelFactory.eINSTANCE.createStep();
						String[] stepArr = key.split("-");
						step.setId(stepArr[1]);
						step.setType(stepArr[2]);
						step.setName(stepArr[3]);
						step.setQuantity(stepsMap.get(key));
						double percentage = service.rate(stepsMap.get(key), count);
						step.setPercentage(service.decimalFormat(percentage) + "%");
						if (stepArr.length > 4) {
							step.setDescription(stepArr[4]);
						}
						else {
							step.setDescription("");
						}
						qStep.getStep().add(step);
					}
					Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
					pathway.setName(carePathway.getName());
					pathway.setPercentage("");
					pathway.setQuantity(0);
					pathway.setVersion(0);
					qStep.setPathway(pathway);
					eQuery.getEMethod().add(qStep);
				}			
			}	
		}
		else if (version == 0){
			stepsMap = new HashMap<>();
			count = 0; 
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery);
			if (!docs.isEmpty()) {
				for (int i = 1; i < numVersion + 1; i++) {
					QStep qStep = Query_metamodelFactory.eINSTANCE.createQStep();
					getSteps(docs, stepStr, i);
					for (String key : stepsMap.keySet()) {
						Step step = Query_metamodelFactory.eINSTANCE.createStep();
						String[] stepArr = key.split("-");
						step.setId(stepArr[1]);
						step.setType(stepArr[2]);
						step.setName(stepArr[3]);
						step.setQuantity(stepsMap.get(key));
						double percentage = service.rate(stepsMap.get(key), count);
						step.setPercentage(service.decimalFormat(percentage) + "%");
						if (stepArr.length > 4) {
							step.setDescription(stepArr[4]);
						}
						else {
							step.setDescription("");
						}
						qStep.getStep().add(step);
					}
					Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
					pathway.setName(carePathway.getName());
					pathway.setPercentage("");
					pathway.setQuantity(0);
					pathway.setVersion(i);
					qStep.setPathway(pathway);
					eQuery.getEMethod().add(qStep);
				}
			}		
		}
		else {
			stepsMap = new HashMap<>();
			count = 0; 
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery);
			QStep qStep = Query_metamodelFactory.eINSTANCE.createQStep();
			getSteps(docs, stepStr, version);
			for (String key : stepsMap.keySet()) {
				Step step = Query_metamodelFactory.eINSTANCE.createStep();
				String[] stepArr = key.split("-");
				step.setId(stepArr[1]);
				step.setType(stepArr[2]);
				step.setName(stepArr[3]);
				step.setQuantity(stepsMap.get(key));
				double percentage = service.rate(stepsMap.get(key), count);
				step.setPercentage(service.decimalFormat(percentage) + "%");
				if (stepArr.length > 4) {
					step.setDescription(stepArr[4]);
				}
				else {
					step.setDescription("");
				}
				qStep.getStep().add(step);
			}
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setPercentage("");
			pathway.setQuantity(0);
			pathway.setVersion(version);
			qStep.setPathway(pathway);
			eQuery.getEMethod().add(qStep);			
		}
		return eQuery;
	}
	
	private void getSteps(List<Document> docs, String stepStr, int number) {
		for (Document doc : docs) {
			int version = doc.get("pathway", new Document()).getInteger("version");
			if (number == 0) {
				List<Document> stepsDoc = doc.get( "executedSteps", new ArrayList<Document>());
				if (!stepsDoc.isEmpty()) {
					addSteps(stepsDoc, stepStr, version);
				}
			}
			else {
				if (version == number) {
					List<Document> stepsDoc = doc.get( "executedSteps", new ArrayList<Document>());
					if (!stepsDoc.isEmpty()) {
						addSteps(stepsDoc, stepStr, version);
					}
				}
				if (numVersion < version) {
					numVersion = version;
				}
			}
		}		
	}

	private void addSteps(List<Document> stepsDoc, String stepStr, int version) {
		for (Document document : stepsDoc) {
			Document step = document.get( "step", new Document());
			String key = version + "-" +
						step.getInteger("_id") + "-" +
						step.getString("type") + "-" +
						step.getString("name") + "-" +
						step.getString("description");
			String type = step.getString("type");
			if (stepsMap.containsKey(key)) {
				int value = stepsMap.get(key) + 1; 
				stepsMap.replace(key, value);
				count++;
			}
			else {
				if (stepStr == null) {
					stepsMap.put(key, 1);
					count++;
				}
				else{
					if (type.toLowerCase().matches(".*" + stepStr.toLowerCase() + ".*")) {
						stepsMap.put(key, 1);
						count++;
					}
				}
			}
		}
	}
}
