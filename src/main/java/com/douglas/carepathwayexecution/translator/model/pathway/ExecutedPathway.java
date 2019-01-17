package com.douglas.carepathwayexecution.translator.model.pathway;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import MetamodelExecution.Audit;
import MetamodelExecution.EPathway;
import MetamodelExecution.Execution_metamodelFactory;
import MetamodelExecution.Justification;
import MetamodelExecution.MedicalCare;
import MetamodelExecution.Pathway;


public class ExecutedPathway {	
	public EPathway addEPathway(JSONObject json, EPathway ePathway) throws ParseException, JSONException{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS", Locale.ROOT);
		
		//Set pathway
		JSONObject pathwayJson = json.getJSONObject("protocolo");
		Pathway pathway = Execution_metamodelFactory.eINSTANCE.createPathway();
		pathway.setId(pathwayJson.getInt("id"));
		pathway.setCode(pathwayJson.getString("codigo"));		
		pathway.setName(pathwayJson.getString("nome"));				
		pathway.setVersion(pathwayJson.getInt("versao"));
		pathway.setCompleted(pathwayJson.getBoolean("finalizado"));
		
		//set audit
		Audit audit = Execution_metamodelFactory.eINSTANCE.createAudit();
		if (!pathwayJson.isNull("ultima_auditoria")) {
			JSONObject auditJson = pathwayJson.getJSONObject("ultima_auditoria");
			String dateStr = auditJson.getString("data");
			Date date = dateFormat.parse(dateStr);	
			audit.setDate(date);
		}
		pathway.setAudit(audit);	
		
		//set justification			
		Justification justification = Execution_metamodelFactory.eINSTANCE.createJustification();
		if (!json.isNull("justificativa")) {
			JSONObject justificationJson = json.getJSONObject("justificativa");			
			justification.setId(justificationJson.getInt("id"));
			justification.setReason(justificationJson.getString("razao"));
			justification.setDescription(justificationJson.getString("descricao"));
			justification.setJustifiedById(justificationJson.getInt("justificado_por_id"));	
		}		
		ePathway.setJustification(justification);		
		
		//set dates
		String creationStr = json.getString("data_criacao");
		Date creationDate = dateFormat.parse(creationStr);		
		
		Date conclusionDate= null;
		if (!json.isNull("data_conclusao")) {
			String conclusionStr = json.getString("data_conclusao");
			conclusionDate = dateFormat.parse(conclusionStr);
		}
		
		
		//set attendance
		JSONObject attendanceJson = json.getJSONObject("atendimento");
		MedicalCare medicalCare = Execution_metamodelFactory.eINSTANCE.createMedicalCare();
		medicalCare.setCodeMedicalCare(attendanceJson.getInt("codigo_atendimento"));
		medicalCare.setHospitalUnit(attendanceJson.getString("unidade"));
		medicalCare.setIdProfessional(attendanceJson.getInt("profissional_id"));
		medicalCare.setPatientRecord(attendanceJson.getString("prontuario"));
				
		//Set executed pathway
		ePathway.setId(json.getInt("id"));		
		ePathway.setName(pathwayJson.getString("nome"));
		ePathway.setCompleted(json.getBoolean("finalizado"));
		ePathway.setAborted(json.getBoolean("abortado"));
		ePathway.setPathway(pathway);	
		ePathway.setConclusionDate(conclusionDate);
		ePathway.setCreationDate(creationDate);
		ePathway.setMedicalcare(medicalCare);
		ePathway.setCid(json.getString("cid"));
		ePathway.setTimeExecution(json.getDouble("tempo_execucao"));
		
		JSONArray idsExecutionStepJson = json.getJSONArray("passos_executados_ids");		
		for (int i = 0; i < idsExecutionStepJson.length(); i++) {
			ePathway.getIdsExecutedStep().add(idsExecutionStepJson.optInt(i));
		}
		
		return ePathway;
	}	
}
