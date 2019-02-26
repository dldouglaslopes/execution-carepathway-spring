package com.douglas.carepathwayexecution.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.QAverageTime;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class QAverageTimeService {
	@Autowired
	private QCarePathwayService service;
	
	public EQuery getAverageByTime(EQuery eQuery) {	
		//querying the average time
		List<Document> docs = service.filterDocuments(eQuery);
		
		Map<String, Double> times = new HashMap<>();
		Map<String, Integer> quantity = new HashMap<>();
		
		for (Document document : docs) {
			Document pathway = document.get("pathway", new Document());
			String name = pathway.getString("name");
			
			if ( times.containsKey(name)) {
				int cont = quantity.get(name) + 1; 
				double sum = times.get(name) + document.getDouble("timeExecution");
				times.replace( name, sum);
				quantity.replace( name, cont);
			}	
			else {
				times.put(name, document.getDouble("timeExecution"));
				quantity.put(name, 1);
			}
		}
	
		for (String key : times.keySet()) { //getting the average time
			double avg = times.get(key) / quantity.get(key);			
			QAverageTime qAverageTime = Query_metamodelFactory.eINSTANCE.createQAverageTime();		
			qAverageTime.setAverage(avg / 60);
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(CarePathway.getByName(key).getName());
			pathway.setPercentage("");
			pathway.setQuantity(quantity.get(key));	
			eQuery.getEMethod().add(qAverageTime);
		}	
		return eQuery;
	}
}
