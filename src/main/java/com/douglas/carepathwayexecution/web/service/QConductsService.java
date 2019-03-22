package com.douglas.carepathwayexecution.web.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
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
		if (eQuery.getEAttribute().getCarePathway().getName().equals(CarePathway.NONE)) {
			for (CarePathway carePathway : CarePathway.VALUES) {
				if (!carePathway.equals(CarePathway.NONE)) {
					eQuery.getEAttribute().getCarePathway().setName(carePathway);
					int numVersion = Version.getByName(carePathway.getName()).getValue();
					for (int i = 1; i < numVersion + 1; i++) {
						this.noConduct = 0;
						this.withConduct = 0;
						eQuery.getEAttribute().getCarePathway().setVersion(i);
						List<Document> docs = service.filterDocuments(eQuery); //finding all the documents
						QConduct qConduct = getData(docs, carePathway, i);
						if (qConduct.getPathway() != null) {
							eQuery.getEMethod().add(qConduct);
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
					QConduct qConduct = getData(docs, carePathway, i);
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
			QConduct qConduct = getData(docs, carePathway, version);
			if (qConduct.getPathway() != null) {
				eQuery.getEMethod().add(qConduct);
			}
		}	
		return eQuery;
	}
	
	private QConduct getData(List<Document> docs, CarePathway carePathway, int number) {
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
			qConduct.setNoConduct(this.noConduct);
			qConduct.setWithConduct(this.withConduct);
			Pathway pathway = Query_metamodelFactory.eINSTANCE.createPathway();
			pathway.setName(carePathway.getName());
			pathway.setQuantity(this.noConduct + this.withConduct);
			pathway.setVersion(number);
			pathway.setId(id + "");
			qConduct.setPathway(pathway);
		}
		return qConduct;
	}
}
