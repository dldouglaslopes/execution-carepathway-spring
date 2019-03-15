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
import QueryMetamodel.Step;
@Service
public class QFlowService {
	@Autowired
	private QCarePathwayService service;	
	
	private Map<String, Integer> flowMap;	
	private int numVersion;
	private Integer idPathway;
	private int numFlows;

	public EQuery getRecurrentFlows(EQuery eQuery, int version) {			
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {			
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					this.numVersion = 1;
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					List<Document> docs = service.filterDocuments(eQuery);	
					for (int i = 1; i < numVersion + 1; i++) {
						flowMap = new HashMap<>();
						numFlows = 0;
						QFlow qFlow = getData(docs, 
											carePathway, 
											i, 
											eQuery.getEAttribute().getRange());
						if (qFlow.getPathway() != null) {
							eQuery.getEMethod().add(qFlow);
						}
					}
				}
			}			
		}
		else if(version == 0) {
			this.numVersion = 1;
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery);	
			for (int i = 1; i < numVersion + 1; i++) {
				flowMap = new HashMap<>();
				numFlows = 0;
				QFlow qFlow = getData(docs, 
									carePathway, 
									i, 
									eQuery.getEAttribute().getRange());
				if (qFlow.getPathway() != null) {
					eQuery.getEMethod().add(qFlow);
				}
			}
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			List<Document> docs = service.filterDocuments(eQuery);	
			flowMap = new HashMap<>();
			numFlows = 0;
			QFlow qFlow = getData(docs, 
								carePathway, 
								version, 
								eQuery.getEAttribute().getRange());
			if (qFlow.getPathway() != null) {
				eQuery.getEMethod().add(qFlow);
			}			
		}		
		return eQuery;
	}	
	
	private QFlow getData(List<Document> docs,
								CarePathway carePathway, 
								int version,
								ARange range){
		QFlow qFlow = Query_metamodelFactory.eINSTANCE.createQFlow();
		if (!docs.isEmpty()) {
			List<Entry<String, Double>> flowsList = getFlows(docs, 
														carePathway, 
														range,
														version);
			List<Flow> flows = getSequences(flowsList);
			for (Flow flow : flows) {
				qFlow.getFlow().add(flow);
			}
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setQuantity(flows.size());
			pathway.setVersion(version);
			pathway.setId(idPathway + "");
			qFlow.setPathway(pathway);
		}		
		return qFlow;
	}
	
	private List<Flow> getSequences(List<Entry<String, Double>> list) {
		List<Flow> flows = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {			
			Flow flow = Query_metamodelFactory.eINSTANCE.createFlow();
			double percentage = service.rate(flowMap.get( list.get(i).getKey()), this.numFlows);
			flow.setPercentage( service.decimalFormat(percentage) + "%");
			String flowStr = list.get(i).getKey();
			String[] flowArr = flowStr.split("#");
			flow.setQuantity( flowMap.get( list.get(i).getKey()));
			for (int j = 0; j < flowArr.length; j++) {
				String[] oneFlow = flowArr[j].split("-");
				if (oneFlow.length > 1) {
					Step step = Query_metamodelFactory.eINSTANCE.createStep();
					step.setType( oneFlow[0]);
					step.setId( oneFlow[1]);
					if (oneFlow.length > 2) {
						step.setName(oneFlow[2]);
						if (oneFlow.length > 3) {
							step.setDescription(oneFlow[3]);
						}
						else {
							step.setDescription("");
						}
					}
					else {
						step.setName("");
						step.setDescription("");
					}
					step.setPercentage("");
					step.setQuantity(0);
					flow.getStep().add(step);
				}				
			}		
			flows.add(flow);
		}		
		return flows;
	}

	private List<Entry<String, Double>> getFlows(List<Document> docs, 
													CarePathway carePathway, 
													ARange range, 
													int number) {					
		String field = "name";
		String literal = carePathway.getLiteral();
		int size = service.count( field, literal, docs);					
		for( Document doc : docs) { //querying the flows and counting how many flow occurrences			
			int version = doc.get("pathway", new Document()).getInteger("version");
			this.idPathway = doc.get("pathway", new Document()).getInteger("_id");
			List<Document> executedStepDocs = doc.get( "executedSteps", new ArrayList<Document>());			
			String flow = "";			
			for (Document executedStepDoc : executedStepDocs) {
				Document stepDoc = executedStepDoc.get("step", new Document());
				flow += stepDoc.getString("type") + 
						"-" + stepDoc.getInteger("_id") +
						"-" + stepDoc.getString("name") + 
						"-" + stepDoc.getString("description") + "#";
			}
			if (number == version) {
				add(flow); 
			}
			if (this.numVersion < version) {
				this.numVersion = version;
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
	
	private void add(String flow) {
		if (flowMap.containsKey(flow)) {
			int value = flowMap.get(flow) + 1;
			flowMap.replace(flow, value);
			this.numFlows++;
		}
		else {
			flowMap.put(flow, 1);
			this.numFlows++;
		}
	}
}
