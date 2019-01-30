package com.douglas.carepathwayexecution.web.service;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.FindIterable;

import QueryMetamodel.EConduct;
import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;

@Service
public class EConductsService {
	@Autowired
	private ECarePathwayService service;
	
	public EQuery countConducts(EQuery eQuery) {
		//finding all the documents
		FindIterable<Document> conductsDoc = service.getService(eQuery);	
		
		EConduct conduct = Query_metamodelFactory.eINSTANCE.createEConduct();
		int withConduct = 0;
		int noConduct = 0;
		
		//counting the occurrences when the care pathway has conducts or not
		for (Document document : conductsDoc) {
			List<Document> conducts = (List<Document>) document.get("complementaryConducts");
			
			if (!conducts.isEmpty()) {
				withConduct++;
			}
			else {
				noConduct++;
			}
		}
		
		conduct.setNoConduct(noConduct);
		conduct.setWithConduct(withConduct);
		eQuery.setEMethod(conduct);
		
		return eQuery;
	}
}
