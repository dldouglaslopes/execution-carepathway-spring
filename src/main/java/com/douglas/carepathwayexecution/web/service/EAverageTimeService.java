package com.douglas.carepathwayexecution.web.service;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.EAverageTime;
import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class EAverageTimeService {
	@Autowired
	private ECarePathwayService service;
	
	public EQuery averageByTime(EQuery eQuery) {	
		//quering the average time
		List<Document> docs = service.getService(eQuery);
				
		int cont = 0;
		double sum = 0; 
		
		for (Document document : docs) {
			cont += 1;
			sum += document.getDouble("timeExecution");
		}
		
		//getting the average time
		double avg = sum / cont;
		
		EAverageTime averageTime = Query_metamodelFactory.eINSTANCE.createEAverageTime();
		averageTime.setAverage(avg / 60);		
		eQuery.setEMethod(averageTime);
		
		return eQuery;
	}
	
}
