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

import QueryMetamodel.ABoolean;
import QueryMetamodel.ANumeric;
import QueryMetamodel.ARange;
import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.QAnswer;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Question;
import QueryMetamodel.Step;
import QueryMetamodel.Version;

@Service
public class QAnswerService {
	@Autowired
	private QCarePathwayService service;

	private Map<String, List<String>> variablesMap;
	private Map<String, Integer> yesMap;
	private Map<String, Integer> noMap;
	private Map<String, Map<String,Double>> numericMap;
	private Map<String, Map<String, Integer>> stepsMap;
	
	public EQuery getOccorrencesAnswer(EQuery eQuery, 
										String questionStr, 
										String type, 
										int version) { //querying the average time		
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					for (int i = 1; i < numVersion + 1; i++) {
						this.yesMap = new HashMap<>();
						this.noMap = new HashMap<>();	
						this.numericMap = new HashMap<>();
						this.variablesMap = new HashMap<>();
						this.stepsMap = new HashMap<>();
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						List<Document> docs = new ArrayList<Document>();
						for (int j = 0; j < 100; j++) {
							docs = service.filterDocuments(eQuery, j); //finding all the documents
							QAnswer qAnswer = getData(docs, 
									questionStr, 
									type, 
									eQuery.getEAttribute().getRange(), 
									carePathway, 
									i,
									j);
							if (qAnswer.getPathway() != null) {
								eQuery.getEMethod().add(qAnswer);
							}
						}
					}
				}							
			}	
		}
		else if(version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			if (!carePathway.equals(CarePathway.NONE)) {
				int numVersion = Version.getByName(carePathway.getName()).getValue();
				for (int i = 1; i < numVersion + 1; i++) {
					this.yesMap = new HashMap<>();
					this.noMap = new HashMap<>();	
					this.numericMap = new HashMap<>();
					this.variablesMap = new HashMap<>();
					this.stepsMap = new HashMap<>();
					eQuery.getEAttribute().getCarePathway().setVersion(i);
					List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
					QAnswer qAnswer = getData(docs, 
							questionStr, 
							type, 
							eQuery.getEAttribute().getRange(), 
							carePathway, 
							i, 
							99);
					if (qAnswer.getPathway() != null) {
						eQuery.getEMethod().add(qAnswer);
					}
				}
			}		
		}
		else {
			this.yesMap = new HashMap<>();
			this.noMap = new HashMap<>();	
			this.numericMap = new HashMap<>();
			this.variablesMap = new HashMap<>();
			this.stepsMap = new HashMap<>();
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setVersion(version);
			List<Document> docs = service.filterDocuments(eQuery);
			QAnswer qAnswer = getData(docs, 
										questionStr, 
										type, 
										eQuery.getEAttribute().getRange(), 
										carePathway, 
										version,
										99);
			if (qAnswer.getPathway() != null) {
				eQuery.getEMethod().add(qAnswer);
			}										
		}
		return eQuery;
	}
	
	private QAnswer getData(List<Document> docs, 
								String name,
								String type,
								ARange range, 
								CarePathway carePathway, 
								int version,
								int page) {
		QAnswer qAnswer = Query_metamodelFactory.eINSTANCE.createQAnswer();
		getVariables(docs, name, type, version);	
		getAnswers();
		if (page == 99) {
			List<Question> questions = getQuestions(range);				
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setQuantity(questions.size());
			pathway.setVersion(version);
			pathway.setId(carePathway.getValue() + "");
			qAnswer.setPathway(pathway);					
			for (Question question : questions) {
				qAnswer.getQuestion().add(question);
			}
		}
		return qAnswer;
	}
	
	private List<Question> getQuestions( ARange range) {
		List<Question> questions = new ArrayList<>();
		for ( String key : yesMap.keySet()) {
			String[] questionArr = key.split("-");
			ABoolean aBoolean = Query_metamodelFactory.eINSTANCE.createABoolean();
			aBoolean.setFalseQuantity(noMap.get(key));
			aBoolean.setTrueQuantity(yesMap.get(key));
			Question question = Query_metamodelFactory.eINSTANCE.createQuestion();
			question.setName(questionArr[1]);
			question.setQuantity(yesMap.get(key) + noMap.get(key));
			question.setType("BooleanAnswer");
			question.setPercentage("");
			question.setId(questionArr[0]);
			question.getAnswer().add(aBoolean);
			for (String stepStr : stepsMap.get(key).keySet()) {
				Step step = Query_metamodelFactory.eINSTANCE.createStep();
				String[] stepArr = stepStr.split("%");
				step.setId(stepArr[0]);
				step.setType(stepArr[2]);
				step.setName(stepArr[1]);
				step.setPercentage("");
				if (stepArr.length > 3) {
					step.setDescription(stepArr[3]);
				}
				step.setQuantity(stepsMap.get(key).get(stepStr));
				question.getStep().add(step);
			}
			questions.add(question);
		}
		for ( String key : numericMap.keySet()) {
			String[] questionArr = key.split("-");
			Question question = Query_metamodelFactory.eINSTANCE.createQuestion();
			question.setName(questionArr[1]);
			question.setQuantity(numericMap.get(key).size());
			question.setId(questionArr[0]);
			question.setType("NumericAnswer");
			question.setPercentage("");
			List<Entry<String, Double>> listKey = new LinkedList<>( numericMap.get(key).entrySet());
			service.sort( listKey, range.getOrder());	//sorting the list following the order		
			listKey = service.select( range.getQuantity(), listKey); //dividing the list
			for (int i = 0; i < listKey.size(); i++) {
				ANumeric aNumeric = Query_metamodelFactory.eINSTANCE.createANumeric();
				aNumeric.setQuantity( listKey.get(i).getValue().intValue());
				aNumeric.setValue(Double.parseDouble(listKey.get(i).getKey()));
				question.getAnswer().add(aNumeric);
			}
			for (String stepStr : stepsMap.get(key).keySet()) {
				Step step = Query_metamodelFactory.eINSTANCE.createStep();
				String[] stepArr = stepStr.split("%");
				step.setId(stepArr[0]);
				step.setType(stepArr[2]);
				step.setName(stepArr[1]);
				//if (stepArr[2].equals("ExameComplementar")) {
					//medication.setName(medicationArr[1].split(":")[1]);
				//}
				//double percentage = service.rate(stepsMap.get(key).get(stepStr), yesMap.get(key));
				step.setPercentage("");
				if (stepArr.length > 3) {
					step.setDescription(stepArr[3]);
				}
				step.setQuantity(stepsMap.get(key).get(stepStr));
				question.getStep().add(step);
			}
			questions.add(question);
		}
		return questions;
	}
	
	private void getVariables(List<Document> docs, String questionStr, String type, int number) {
		for (Document document : docs) {
			int version = document.get("pathway", new Document()).getInteger("version");
			List<Document> eSteps = document.get("executedSteps", new ArrayList<>());			
			for (Document eStep : eSteps) { 
				Document step = eStep.get("step", new Document());
				String typeStr = step.getString("type");				
				if (typeStr.equals("AuxilioConduta")) {
					List<Document> answersList = eStep.get("answer", new ArrayList<>());				
					for (Document answer : answersList) {
						Document question = answer.get("question", new Document());
						int idQuestion = question.getInteger("_id");
						Document value = answer.get("value", new Document());
						String text = idQuestion + "-" +
										question.getString("text");
						String stepStr = step.getInteger("_id") + "%" +
								step.getString("name") + "%" + 
								step.getString("type") + "%" +
								step.getString("description");
						String data = "";
						if (answer.getString("type").equals("RespostaNumerica")) {
							data = answer.getString("type") + "-" +
									value.getDouble("value");
						}
						else if (answer.getString("type").equals("RespostaSimOuNao")) {
							data = answer.getString("type") + "-" +
									value.getBoolean("value");
						}
						if (version == number) {
							if (questionStr == null) {
								if (type == null) {
									add(text, data, stepStr);
								}
								else {
									if (type.equals("boolean") && 
											answer.getString("type").equals("RespostaSimOuNao")) {
										add(text, data, stepStr);
									}
									else if(type.equals("numeric") && 
											answer.getString("type").equals("RespostaNumerica")) {
										add(text, data, stepStr);
									}
								}
							}
							else {		
								if (text.toLowerCase().matches(".*" + questionStr.toLowerCase() + ".*")) {
									add(text, data, stepStr);
								}
							}
						}
					}
				}
			}
		}
	}
		
	private void getAnswers() {
		for (String key : variablesMap.keySet()) {
			List<String> variablesList = variablesMap.get(key);
			for (String data : variablesList) {
				String[] dataArr = data.split("-");
				String type = dataArr[0];
				String value = "";
				if (dataArr.length > 1) {
					value = dataArr[1];	
					if (type.equals("RespostaNumerica")) {
						getNumericAnswers(key, value);
					}
					else if(type.equals("RespostaSimOuNao")) {
						getBooleanAnswers(key, Boolean.parseBoolean(value));
					}
				}				
			}
		}
	}
	
	private void getNumericAnswers(String key, String num) {
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
		
	private void add(String key, String value, String step) {
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
		if (stepsMap.containsKey(key)) {
			if (stepsMap.get(key).containsKey(step)) {
				Map<String, Integer> valueMap = stepsMap.get(key);
				int sum = valueMap.get(step) + 1;
				valueMap.replace(step, sum);
				stepsMap.put(key, valueMap);
			}
			else {
				Map<String, Integer> valueMap = stepsMap.get(key);
				valueMap.put(step, 1);
				stepsMap.put(key, valueMap);
			}
		}
		else {
			Map<String, Integer> valueMap = new HashMap<>();
			valueMap.put(step, 1);
			stepsMap.put(key, valueMap);
		}
	}

	public EQuery getResults(JSONArray data) {
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < data.length(); i++) {
			JSONObject object = data.getJSONObject(i);
			JSONArray questions = object.getJSONArray("question");
			for (int j = 0; j < questions.length(); j++) {
				JSONObject question = questions.getJSONObject(j);
				int quantity = question.getInt("quantity");
				String name = question.getString("name");
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
			QAnswer qAnswer = Query_metamodelFactory.eINSTANCE.createQAnswer();
			Question question = Query_metamodelFactory.eINSTANCE.createQuestion();
			question.setName(name);
			question.setQuantity(map.get(name));
			qAnswer.getQuestion().add(question);
			eQuery.getEMethod().add(qAnswer);
		}
		return eQuery;
	}
}
