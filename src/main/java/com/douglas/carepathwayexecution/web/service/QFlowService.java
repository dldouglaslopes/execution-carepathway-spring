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
import QueryMetamodel.Flow;
import QueryMetamodel.Pathway;
import QueryMetamodel.QFlow;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Sequence;

@Service
public class QFlowService {
	@Autowired
	private QCarePathwayService service;	
	
	private Map<String, Integer> flowMap;	

	public EQuery getRecurrentFlows(EQuery eQuery) {			
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {			
			for (CarePathway carePathway : CarePathway.VALUES) {
				QFlow recurrentFlow = Query_metamodelFactory.eINSTANCE.createQFlow();
				eQuery.getEAttribute().getCarePathway().setName(carePathway);
				List<Document> docs = service.filterDocuments(eQuery);	
				if (!docs.isEmpty()) {
					List<Entry<String, Double>> flowsList = getFlows(docs, 
																carePathway, 
																eQuery.getEAttribute().getRange());
					List<Flow> flows = getSequences(flowsList);
					for (Flow flow : flows) {
						recurrentFlow.getFlow().add(flow);
					}
					Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
					pathway.setName(carePathway.getName());
					pathway.setPercentage("");
					pathway.setQuantity(0);
					recurrentFlow.setPathway(pathway);
					eQuery.getEMethod().add(recurrentFlow);
				}
			}			
		}
		else {
			QFlow recurrentFlow = Query_metamodelFactory.eINSTANCE.createQFlow();
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery);
			if (!docs.isEmpty()) {
				List<Entry<String, Double>> flowsList = getFlows(docs, 
															carePathway, 
															eQuery.getEAttribute().getRange());
				List<Flow> flows = getSequences(flowsList);
				for (Flow flow : flows) {
					recurrentFlow.getFlow().add(flow);
				}
				Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
				pathway.setName(carePathway.getName());
				pathway.setPercentage("");
				pathway.setQuantity(0);
				recurrentFlow.setPathway(pathway);
				eQuery.getEMethod().add(recurrentFlow);
			}			
		}		
		return eQuery;
	}	
	
	private List<Flow> getSequences(List<Entry<String, Double>> list) {
		List<Flow> flows = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {			
			Flow flow = Query_metamodelFactory.eINSTANCE.createFlow();
			flow.setPercentage( service.decimalFormat(list.get(i).getValue()) + "%");
			flow.setQuantity( flowMap.get( list.get(i).getKey()));
			String flowStr = list.get(i).getKey();
			String[] flowArr = flowStr.split("#");
			for (int j = 0; j < flowArr.length; j++) {
				String[] oneFlow = flowArr[j].split("-");
				Sequence sequence = Query_metamodelFactory.eINSTANCE.createSequence();
				sequence.setType( oneFlow[0]);
				sequence.setId( oneFlow[1]);
				if (oneFlow.length > 2) {
					sequence.setName(oneFlow[2]);
				}
				else {
					sequence.setName("");
				}
				flow.getSequences().add(sequence);				
			}		
			flows.add(flow);
		}		
		return flows;
	}

	private List<Entry<String, Double>> getFlows(List<Document> docs, CarePathway carePathway, ARange range) {					
		flowMap = new HashMap<>();
		String field = "name";
		String literal = carePathway.getLiteral();
		int size = service.count( field, literal, docs);					
		for( Document carePathwayDoc : docs) { //querying the flows and counting how many flow occurrences			
			List<Document> executedStepDocs = carePathwayDoc.get( "executedSteps", new ArrayList<Document>());			
			String flow = "";			
			for (Document executedStepDoc : executedStepDocs) {
				Document stepDoc = executedStepDoc.get("step", new Document());
				flow += stepDoc.getString("type") + 
						"-" + stepDoc.getInteger("_id") +
						"-" + stepDoc.getString("name") + "#";
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
		for ( String key : flowMap.keySet()) { //calculating the percent of the flow
			int dividend = flowMap.get(key);			
			double percent = service.rate( dividend, size);
			percentMap.put( key, percent);	
		}		
		List<Entry<String, Double>> list = new LinkedList<>( percentMap.entrySet());
		service.sort( list, range.getOrder());	//sorting the list following the order		
		list = service.select( range.getQuantity(), list); //dividing the list	
		return list;
	}
}
