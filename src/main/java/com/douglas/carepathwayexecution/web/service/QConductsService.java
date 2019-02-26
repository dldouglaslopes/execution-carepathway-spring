package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.QConduct;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class QConductsService {
	@Autowired
	private QCarePathwayService service;
	
	private Map<String, Integer> withConduct = new HashMap<>();
	private Map<String, Integer> noConduct = new HashMap<>();
	
	public EQuery countConducts(EQuery eQuery) {
		//finding all the documents
		List<Document> conductsDoc = service.filterDocuments(eQuery);	
		
		for (Document document : conductsDoc) {
			
			List<Document> conducts = document.get("complementaryConducts", new ArrayList<Document>());
			Document pathway = document.get("pathway", new Document());
			String key = pathway.getString("name");
			
			if (!conducts.isEmpty()) {
				if (withConduct.containsKey(key)) {
					int value = withConduct.get(key) + 1;
					withConduct.replace(key, value);
				}
				else {
					withConduct.put(key, 1);
					noConduct.put(key, 0);
				}
			}
			else {
				if (noConduct.containsKey(key)) {
					int value = noConduct.get(key) + 1;
					noConduct.replace(key, value);
				}
				else {
					noConduct.put(key, 1);
					withConduct.put(key, 0);
				}
			}
		}
	
		for (String key : withConduct.keySet()) {
			QConduct qConduct = Query_metamodelFactory.eINSTANCE.createQConduct();
			qConduct.setNoConduct(noConduct.get(key));
			qConduct.setWithConduct(withConduct.get(key));
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(CarePathway.getByName(key).getName());
			pathway.setPercentage("");
			pathway.setQuantity(0);
			qConduct.setPathway(pathway);
			eQuery.getEMethod().add(qConduct);
		}		
		return eQuery;
	}
}
