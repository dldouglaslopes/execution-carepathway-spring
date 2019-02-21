package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.EQuery;

@Service
public class QAnswerService {
	@Autowired
	private QCarePathwayService service;
	
	public EQuery occorrencesAnswer(EQuery eQuery, String questionStr) {
		//querying the average time
		List<Document> docs = service.getService(eQuery);
		
		Map<String, List<Document>> answersMap = new HashMap<>();
		
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
							if (answersMap.containsKey(text)) {
								List<Document> variables = answersMap.get(text);
								variables.add(variable);
								answersMap.replace( text, variables);
							}
							else {
								List<Document> variables = new ArrayList<>();
								variables.add(variable);
								answersMap.put(text, variables);
							}
						}						
						else {							
							if (questionStr.equals(text)) {
								if (answersMap.containsKey(text)) {
									List<Document> variables = answersMap.get(text);
									variables.add(variable);
									answersMap.replace( text, variables);
								}
								else {
									List<Document> variables = new ArrayList<>();
									variables.add(variable);
									answersMap.put(text, variables);
								}
							}	
						}
					}
				}
			}
		}

		return eQuery;
	}

}
