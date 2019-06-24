package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.CarePathway;
import QueryMetamodel.EQuery;
import QueryMetamodel.Pathway;
import QueryMetamodel.QConduct;
import QueryMetamodel.Query_metamodelFactory;
import QueryMetamodel.Version;

@Service
public class QConductsService {
	@Autowired
	private QCarePathwayService service;
	
	private int withConduct;
	private int noConduct;
	
	public EQuery getConducts(EQuery eQuery, int version) {
		long start = System.currentTimeMillis();
		//System.out.println(start);
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					for (int i = 1; i < numVersion + 1; i++) {
						this.noConduct = 0;
						this.withConduct = 0;
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						List<Document> docs = new ArrayList<Document>();
						for (int j = 0; j < 100; j++) {
							docs = service.filterDocuments(eQuery, j); //finding all the documents
							QConduct qConduct = getData(docs, carePathway, i, j);
							if (qConduct.getPathway() != null) {
								eQuery.getEMethod().add(qConduct);
							}
						}
					}
				}
			}
		}
		else if (version == 0) {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			if (!carePathway.equals(CarePathway.NONE)) {
				int numVersion = Version.getByName(carePathway.getName()).getValue();
				for (int i = 1; i < numVersion + 1; i++) {
					this.noConduct = 0;
					this.withConduct = 0;
					eQuery.getEAttribute().getCarePathway().setVersion(i);
					List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
					QConduct qConduct = getData(docs, carePathway, i, 99);
					if (qConduct.getPathway() != null) {
						eQuery.getEMethod().add(qConduct);
					}
				}
			}
		}
		else {
			CarePathway carePathway = eQuery.getEAttribute().getCarePathway().getName();
			eQuery.getEAttribute().getCarePathway().setVersion(version);
			List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
			this.noConduct = 0;
			this.withConduct = 0;
			QConduct qConduct = getData(docs, carePathway, version, 99);
			if (qConduct.getPathway() != null) {
				eQuery.getEMethod().add(qConduct);
			}
		}	
		System.out.print((System.currentTimeMillis() - start ) + " ");
		return eQuery;
	}
	
	private QConduct getData(List<Document> docs, CarePathway carePathway, int number, int page) {
		QConduct qConduct = Query_metamodelFactory.eINSTANCE.createQConduct();
		int id = 0;
		for (Document document : docs) {
			List<Document> conducts = document.get("complementaryConducts", new ArrayList<Document>());
			Document pathway = document.get("pathway", new Document());
			int version = pathway.getInteger("version");
			id = pathway.getInteger("_id");
			if (version == number) {
				if (!conducts.isEmpty()) {
					this.noConduct++;
				}
				else {
					this.withConduct++;
				}
			}
		}	
		if (this.noConduct != 0 || this.withConduct != 0) {
			if (page == 99) {
				qConduct.setNoConduct(this.noConduct);
				qConduct.setWithConduct(this.withConduct);
				Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
				pathway.setName(carePathway.getName());
				pathway.setQuantity(this.noConduct + this.withConduct);
				pathway.setVersion(number);
				pathway.setId(id + "");
				qConduct.setPathway(pathway);
			}
		}
		return qConduct;
	}

	public EQuery getResults(JSONArray data) {
		Map<String, Map<String, Integer>> map = new HashMap<>();
		for (int i = 0; i < data.length(); i++) {
			JSONObject object = data.getJSONObject(i);
			String pathway = object.getJSONObject("pathway").getString("name");
			if (map.containsKey(pathway)) {
				int withConduct = object.getInt("withConduct") +
						map.get(pathway).get("withConduct");
				int noConduct = object.getInt("noConduct") + 
						map.get(pathway).get("noConduct");
				Map<String, Integer> map2 = map.get(pathway);
				map2.replace("withConduct", withConduct);
				map2.replace("noConduct", noConduct);
				map.replace(pathway, map2);
			}
			else {
				Map<String, Integer> map2 = new HashMap<String, Integer>();
				map2.put("withConduct", object.getInt("withConduct"));
				map2.put("noConduct", object.getInt("noConduct"));
				map.put(pathway, map2);
			}
		}
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		for (String name : map.keySet()) {
			QConduct qConduct = Query_metamodelFactory.eINSTANCE.createQConduct();
			qConduct.setNoConduct(map.get(name).get("noConduct"));
			qConduct.setWithConduct(map.get(name).get("withConduct"));
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(name);
			pathway.setQuantity(map.get(name).get("noConduct") +
								map.get(name).get("withConduct"));
			pathway.setId("");
			pathway.setVersion(0);
			qConduct.setPathway(pathway);
			eQuery.getEMethod().add(qConduct);
		}
		return eQuery;
	}
}
