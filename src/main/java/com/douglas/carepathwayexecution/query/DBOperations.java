package com.douglas.carepathwayexecution.query;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import MetamodelExecution.EPathway;

public class DBOperations {
	private MongoCollection<Document> collection;
	
	public DBOperations() {
		this.collection = new DBConfig().getCollection();
	}	
	
	public void saveEPathway(String name, EPathway ePathway) {		
		//Document ePathwayDoc = new Document("name", ePathway.getName()).append("xmi", ePathway); //send epathway to document
		//Send epathway to document
		Document ePathwayDoc = new Document("name", ePathway.getName())
				.append("idCP", ePathway.getId())
				.append("cid", ePathway.getCid())
				.append("creation", ePathway.getCreationDate())
				.append("conclusion", ePathway.getConclusionDate())
				.append("completed", ePathway.isCompleted())
				.append("aborted", ePathway.isAborted())
				.append("executedSteps", ePathway.getEStep())
				.append("justification", ePathway.getJustification())
				.append("timeExecution", ePathway.getTimeExecution())
				.append("attendance", ePathway.getMedicalcare())
				.append("complementaryConducts", ePathway.getComplementaryconducts())
				.append("idsEStep", ePathway.getIdsExecutedStep())
				.append("pathway", ePathway.getPathway());
		
		collection.insertOne(ePathwayDoc);	//insert a document	in a collection
	}
		
	public boolean hasEPathway(int id) {
		return collection.count(Filters.eq("idCP", id)) > 0;
	}
}
