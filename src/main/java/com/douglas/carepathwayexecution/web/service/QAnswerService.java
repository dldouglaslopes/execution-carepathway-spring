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
import QueryMetamodel.Question;

@Service
public class QAnswerService {
	@Autowired
	private QCarePathwayService service;

	private Map<String, List<Document>> answersMap;

	//service.sort( list, range.getOrder());	//sorting the list following the order		
	//list = service.select( range.getQuantity(), list); //dividing the list
	
	public EQuery occorrencesAnswer(EQuery eQuery, String questionStr) { //querying the average time		
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				eQuery.getEAttribute().getCarePathway().setName(carePathway);
				List<Document> docs = service.filterDocuments(eQuery);
				List<Entry<String, List<Document>>> list = getAnswers(docs, 
																		questionStr, 
																		eQuery.getEAttribute().getRange());	
				List<Question> questions = getQuestion(list);
			}	
		}
		else {
			List<Document> docs = service.filterDocuments(eQuery);	
		}
		return eQuery;
	}
	
	private List<Question> getQuestion(List<Entry<String, List<Document>>> list) {
		return null;
	}
	
	private List<Entry<String, List<Document>>> getAnswers(List<Document> docs, String questionStr, ARange range) {
		answersMap = new HashMap<>();
		for (Document document : docs) {
			List<Document> eSteps = document.get("executedSteps", new ArrayList<>());			
			for (Document eStep : eSteps) {
				Document step = eStep.get("step", new Document());
				String type = step.getString("type");				
				if (type.equals("AuxilioConduta")) {
					List<Document> answersList = eStep.get("answer", new ArrayList<>());				
					for (Document answer : answersList) {
						Document question = answer.get("question", new Document());
						Document variable = question.get("variable", new Document());
						String text = question.getString("text");
						
						if (questionStr == null) {
							add(text, variable);
						}						
						else {							
							if (questionStr.equals(text)) {
								add(text, variable);
							}	
						}
					}
				}
			}
		}		
		List<Entry<String, List<Document>>> list = new LinkedList<>( answersMap.entrySet());
		return list;
	}
	
	private void add(String key, Document value) {
		if (answersMap.containsKey(key)) {
			List<Document> variables = answersMap.get(key);
			variables.add(value);
			answersMap.replace( key, variables);
		}
		else {
			List<Document> variables = new ArrayList<>();
			variables.add(value);
			answersMap.put(key, variables);
		}
	}
}
