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

@Service
public class QAnswerService {
	@Autowired
	private QCarePathwayService service;

	private Map<String, List<String>> variablesMap;
	private Map<String, Integer> yesMap;
	private Map<String, Integer> noMap;
	private Map<String, Map<String,Double>> numericMap;
	
	public EQuery occorrencesAnswer(EQuery eQuery, String questionStr) { //querying the average time		
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				eQuery.getEAttribute().getCarePathway().setName(carePathway);
				List<Document> docs = service.filterDocuments(eQuery);
				getVariables(docs, questionStr);	
				getAnswers(eQuery.getEAttribute().getRange());
			}	
		}
		else {
			List<Document> docs = service.filterDocuments(eQuery);
			getVariables(docs, questionStr);
			getAnswers(eQuery.getEAttribute().getRange());	
		}
		return eQuery;
	}
	
	private void getAnswers(ARange range) {
		for (String key : variablesMap.keySet()) {
			List<String> variablesList = variablesMap.get(key);
			for (String data : variablesList) {
				String[] dataArr = data.split("-");
				String type = dataArr[0];
				String value = dataArr[1];
				if (type == "RespostaNumerica") {
					getNumericAnswers(key, range, value);
				}
				else if(type == "RespostaSimOuNao") {
					getBooleanAnswers(key, Boolean.parseBoolean(value));
				}
			}
		}
	}
	
	private void getNumericAnswers(String key, ARange range, String num) {
		if (numericMap.containsKey(key)) {
			if (numericMap.get(key).containsKey(num)) {
				double sum = numericMap.get(key).get(num) + 1;
				numericMap.get(key).replace(num, sum);
			}
			else {
				numericMap.get(key).put(num, 1.0);
			}
		}				
		else {
			numericMap.put(key, new HashMap<>());
			numericMap.get(key).put(num, 1.0);
		}		
//		for (String key : numericMap.keySet()) {
//			List<Entry<String, Double>> listKey = new LinkedList<>( numericMap.get(key).entrySet());
//			service.sort( listKey, range.getOrder());	//sorting the list following the order		
//			listKey = service.select( range.getQuantity(), listKey); //dividing the list
//		}
	}

	private void getBooleanAnswers(String key, boolean bool) {
		if (bool) {
			if (yesMap.containsKey(key)) {
				int sum = yesMap.get(key) + 1;
				yesMap.replace(key, sum);
			}
			else {
				yesMap.put(key, 1);
				noMap.put(key, 0);
			}
		}
		else {
			if (noMap.containsKey(key)) {
				int sum = noMap.get(key) + 1;
				noMap.replace(key, sum);
			}
			else {
				yesMap.put(key, 0);
				noMap.put(key, 1);
			}
		}
	}
	
	private void getVariables(List<Document> docs, String questionStr) {
		variablesMap = new HashMap<>();
		for (Document document : docs) {
			List<Document> eSteps = document.get("executedSteps", new ArrayList<>());			
			for (Document eStep : eSteps) {
				Document step = eStep.get("step", new Document());
				String type = step.getString("type");				
				if (type.equals("AuxilioConduta")) {
					List<Document> answersList = eStep.get("answer", new ArrayList<>());				
					for (Document answer : answersList) {
						Document question = answer.get("question", new Document());
						Document value = answer.get("value", new Document());
						String text = question.getString("text");
						String data = "";						
						if (answer.getString("type").equals("RespostaNumerica")) {
							data = answer.getString("type") + "-" +
									value.getDouble("value");
						}
						else if (answer.getString("type").equals("RespostaSimOuNao")) {
							data = answer.getString("type") + "-" +
									value.getBoolean("value");
						}						
						if (questionStr == null) {
							add(text, data);
						}						
						else {							
							if (questionStr.equals(text)) {
								add(text, data);
							}	
						}
					}
				}
			}
		}
	}
	
	private void add(String key, String value) {
		if (variablesMap.containsKey(key)) {
			List<String> variables = variablesMap.get(key);
			variables.add(value);
			variablesMap.replace( key, variables);
		}
		else {
			List<String> variables = new ArrayList<>();
			variables.add(value);
			variablesMap.put(key, variables);
		}
	}
}
