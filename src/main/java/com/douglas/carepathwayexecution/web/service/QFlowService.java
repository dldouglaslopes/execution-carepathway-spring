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

import com.douglas.carepathwayexecution.query.DBConfig;
import com.mongodb.client.FindIterable;

import QueryMetamodel.ARange;
import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Flow;
import QueryMetamodel.Pathway;
import QueryMetamodel.QFlow;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Step;
import QueryMetamodel.Version;
@Service
public class QFlowService {
	@Autowired
	private QCarePathwayService service;	
	
	private Map<String, Integer> flowMap;	
	private int numFlows;

	public EQuery getRecurrentFlows(EQuery eQuery, int version) {			
		long start = System.currentTimeMillis();
		//System.out.println(start);
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {			
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					for (int i = 1; i < numVersion + 1; i++) {
						flowMap = new HashMap<>();
						numFlows = 0;
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						List<Document> docs = new ArrayList<Document>();
						for (int j = 0; j < 100; j++) {
							docs = service.filterDocuments(eQuery, j);	
							QFlow qFlow = getData(docs, 
												carePathway, 
												i, 
												eQuery.getEAttribute().getRange(),
												j);
							docs.clear();
							if (qFlow.getPathway() != null) {
								eQuery.getEMethod().add(qFlow);
							}
						}
					}
				}
			}			
		}
		else if(version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			int numVersion = Version.getByName(carePathway.getName()).getValue();
			for (int i = 1; i < numVersion + 1; i++) {
				flowMap = new HashMap<>();
				numFlows = 0;
				eQuery.getEAttribute().getCarePathway().setVersion(i);
				List<Document> docs = service.filterDocuments(eQuery);	
				QFlow qFlow = getData(docs, 
									carePathway, 
									i, 
									eQuery.getEAttribute().getRange(),
									99);
				docs.clear();
				if (qFlow.getPathway() != null) {
					eQuery.getEMethod().add(qFlow);
				}
			}
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setVersion(version);
			List<Document> docs = service.filterDocuments(eQuery);	
			flowMap = new HashMap<>();
			numFlows = 0;
			QFlow qFlow = getData(docs, 
								carePathway, 
								version, 
								eQuery.getEAttribute().getRange(),
								99);
			docs.clear();
			if (qFlow.getPathway() != null) {
				eQuery.getEMethod().add(qFlow);
			}			
		}		
		System.out.println("Total: "+(System.currentTimeMillis() - start));
		return eQuery;
	}	
	
	private QFlow getData(List<Document> docs,
								CarePathway carePathway, 
								int version,
								ARange range,
								int page){
		QFlow qFlow = Query_metamodelFactory.eINSTANCE.createQFlow();
		List<Entry<String, Double>> flowsList = getFlows(docs, 
				carePathway, 
				range,
				version,
				page);
		if (flowsList.size() > 0) {
			List<Flow> flows = getSequences(flowsList);
			for (Flow flow : flows) {
				qFlow.getFlow().add(flow);
			}
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setQuantity(flows.size());
			pathway.setVersion(version);
			pathway.setId(carePathway.getValue() + "");
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
													int number,
													int page) {					
		String field = "name";
		String literal = carePathway.getLiteral();
		int size = service.count( field, literal, docs);					
		for( Document doc : docs) { //querying the flows and counting how many flow occurrences			
			List<Document> executedStepDocs = doc.get( "executedSteps", new ArrayList<Document>());			
			String flow = "";			
			for (Document executedStepDoc : executedStepDocs) {
				Document stepDoc = executedStepDoc.get("step", new Document());
				flow += stepDoc.getString("type") + 
						"-" + stepDoc.getInteger("_id") +
						"-" + stepDoc.getString("name") + 
						"-" + stepDoc.getString("description") + "#";
			}
			add(flow); 
		}	
		List<Entry<String, Double>> list = new LinkedList<>();
		if (page == 99) {
			Map<String, Double> percentMap = new HashMap<>();
			for ( String key : flowMap.keySet()) { //calculating the percent of the flow
				int dividend = flowMap.get(key);			
				double percent = service.rate( dividend, size);
				percentMap.put( key, percent);	
			}		
			list = new LinkedList<>( percentMap.entrySet());
			service.sort( list, range.getOrder());	//sorting the list following the order		
			list = service.select( range.getQuantity(), list); //dividing the list	
		}
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

	public EQuery getResults() {
		FindIterable<Document> docs = new DBConfig().getFlowCollection().find();
		Map<String, Integer> map = new HashMap<>();
		for (Document document : docs) {
			ArrayList<Document> docList = document.get("flow", new ArrayList<>());
			for (Document doc : docList) {
				ArrayList<Document> steps = doc.get("step", new ArrayList<>());
				int quantity = doc.getInteger("quantity");
				String str = "";
				for (Document step : steps) {
					str += step.getString("name") + "$";
				}
				if (map.containsKey(str)) {
					quantity += map.get(str);
					map.replace(str, quantity);
				}
				else {
					map.put(str, quantity);
				}
			}
		}
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		int[] top = {0,0,0,0,0};
		for (String str : map.keySet()) {
			QFlow qFlow = Query_metamodelFactory.eINSTANCE.createQFlow();
			Flow flow = Query_metamodelFactory.eINSTANCE.createFlow();
			Step step = Query_metamodelFactory.eINSTANCE.createStep();
			step.setName(str);
			step.setQuantity(map.get(str));
			flow.getStep().add(step);
			qFlow.getFlow().add(flow);
			eQuery.getEMethod().add(qFlow);
			int quantity = map.get(str);
			for (int j = 0; j < top.length; j++) {
				if (quantity > top[j]) {
					for (int i = top.length - 1; i > j; i--) {
						top[i] = top[i - 1];
					}
					top[j] = quantity;
					break;
				}
			}
		}
		for (int i : top) {
			System.out.println(i);
		}
		
		return eQuery;
	}
}
