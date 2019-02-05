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

import com.mongodb.client.FindIterable;

import QueryMetamodel.EQuery;
import QueryMetamodel.ERecurrentFlow;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class ERecurrentFlowService {
	@Autowired
	private ECarePathwayService service;

	public EQuery recurrentFlow(EQuery eQuery) {
		
		//finding all the documents belonging to the same care pathway
		FindIterable<Document> carePathwayDocs = service.getService(eQuery);
				
		//count how many occurrences of same care pathway name 
		String field = "name";
		String literal = eQuery.getEAttribute().getCarePathway().getName().getLiteral();
		int size = service.count( field, literal, carePathwayDocs);		
		
		Map<String, Integer> flowMap = new HashMap<>();
		
		//querying the flows and counting how many flow occurrences
		for( Document carePathwayDoc : carePathwayDocs) {
			
			List<Document> executedStepDocs = carePathwayDoc.get( "executedSteps", new ArrayList<Document>());
			
			String flow = "";
			
			for (Document executedStepDoc : executedStepDocs) {
				Document stepDoc = executedStepDoc.get("step", new Document());
				
				flow += stepDoc.getString("type") + 
						" - " +
						" id: " + stepDoc.getInteger("_id") + " / ";
			}
								
			if (flowMap.containsKey(flow)) {
				int value = flowMap.get(flow) + 1;
				flowMap.replace(flow, value);
			}
			else {
				flowMap.put(flow, 1);
			}					
		}								
	
		Map<String, Double> percentMap = new HashMap<>();
		
		//calculating the percent of the flow
		for ( String key : flowMap.keySet()) {
			int dividend = flowMap.get(key);
			
			double percent = service.rate( dividend, size);
			percentMap.put( key, percent);	
		}
		
		List<Entry<String, Double>> list = new LinkedList<>( percentMap.entrySet());
		
		//sorting the list following the order
		service.sort( list, eQuery.getEAttribute().getRange().getOrder());
		
		//dividing the list
		list = service.select( eQuery.getEAttribute().getRange().getQuantity(), list);
	
		ERecurrentFlow recurrentFlow = Query_metamodelFactory.eINSTANCE.createERecurrentFlow();		
		
		for (int i = 0; i < list.size(); i++) {
			String percentage = service.decimalFormat(list.get(i).getValue()) + "%";
			recurrentFlow.getFlows().add(percentage + ": " + list.get(i).getKey());
		}
		
		eQuery.setEMethod(recurrentFlow);
		
		return eQuery;
	}
	
}
