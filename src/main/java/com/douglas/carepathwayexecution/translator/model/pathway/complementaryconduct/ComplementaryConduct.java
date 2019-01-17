package com.douglas.carepathwayexecution.translator.model.pathway.complementaryconduct;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import MetamodelExecution.ComplementaryConducts;
import MetamodelExecution.ComplementaryExamination;
import MetamodelExecution.ComplementaryMedication;
import MetamodelExecution.ComplementaryProcedure;
import MetamodelExecution.ExaminationPrescribedResource;
import MetamodelExecution.Execution_metamodelFactory;
import MetamodelExecution.MedicationPrescribedResource;
import MetamodelExecution.ProcedurePrescribedResource;
import MetamodelExecution.Standard;
import MetamodelExecution.Suspension;

public class ComplementaryConduct {
	public ComplementaryConducts createComplementaryConducts(JSONObject json, ComplementaryConducts complementaryConducts) throws ParseException, JSONException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS", Locale.getDefault());
		
		//set date
		String creationStr = json.getString("data_criacao");
		Date creationDate = dateFormat.parse(creationStr);		
		
		//set complementary conduct		
		complementaryConducts.setId(json.getInt("id"));
		complementaryConducts.setType(json.getString("type"));
		complementaryConducts.setPathway(json.getString("protocolo"));
		complementaryConducts.setCreationDate(creationDate);

		if (!json.isNull("recurso")) {
			complementaryConducts.setResource(json.getString("recurso"));
		}
		
		//set suspension
		if (!json.isNull("suspensao")) {
			Suspension suspension = Execution_metamodelFactory.eINSTANCE.createSuspension();
			JSONObject suspensionJson = json.getJSONObject("suspensao");
			
			//set date
			String suspensionStr = suspensionJson.getString("data_solicitacao");
			Date suspensionDate = dateFormat.parse(suspensionStr);
			
			suspension.setId(suspensionJson.getInt("id"));
			suspension.setMessage(suspensionJson.getString("mensagem"));
			suspension.setRequestDate(suspensionDate);
			suspension.setSuccess(suspensionJson.getBoolean("sucesso"));
			
			complementaryConducts.setSuspension(suspension);
		}
		
		return complementaryConducts;
	}
	
	public ComplementaryMedication createComplementaryMedicamention(JSONObject json) throws ParseException, JSONException {
		ComplementaryMedication complementaryMedication = Execution_metamodelFactory.eINSTANCE.createComplementaryMedication();
		complementaryMedication = (ComplementaryMedication) createComplementaryConducts(json, complementaryMedication);
		
		if (json.has("recurso_prescrito")) {
			MedicationPrescribedResource medicationPrescribedResource = Execution_metamodelFactory.eINSTANCE.createMedicationPrescribedResource();
			JSONObject resourceJson = json.getJSONObject("recurso_prescrito");
			
			medicationPrescribedResource.setId(resourceJson.getInt("id"));
			medicationPrescribedResource.setIdMedication(resourceJson.getInt("medicamento_id"));
			medicationPrescribedResource.setName(resourceJson.getString("nome"));
			medicationPrescribedResource.setBrand(resourceJson.getString("marca"));
			medicationPrescribedResource.setCode(resourceJson.getString("codigo"));
			medicationPrescribedResource.setCycles(resourceJson.getInt("ciclos"));
			medicationPrescribedResource.setDescription(resourceJson.getString("descricao"));
			medicationPrescribedResource.setTimeInterval(resourceJson.getInt("dias_intervalo"));
			medicationPrescribedResource.setDailyDosage(resourceJson.getInt("dose_diaria"));
			medicationPrescribedResource.setFrequency(resourceJson.getInt("frequencia"));
			medicationPrescribedResource.setTimeTreatement(resourceJson.getInt("dias_tratamento"));
			medicationPrescribedResource.setMedication(resourceJson.getString("medicamento"));
			medicationPrescribedResource.setUnit(resourceJson.getString("unidade"));
			medicationPrescribedResource.setAccess(resourceJson.getString("via_acesso"));
		
			if (!resourceJson.isNull("categoria")) {
				medicationPrescribedResource.setCategory(resourceJson.getString("categoria"));
			}
			if (!resourceJson.isNull("padrao")) {
				Standard standard = Execution_metamodelFactory.eINSTANCE.createStandard();
				JSONObject standardJson = resourceJson.getJSONObject("padrao");
				
				standard.setId(standardJson.getInt("id"));
				standard.setNameDiluent(standardJson.getString("nome_diluente"));
				standard.setCodeActiveAgent(standardJson.getInt("codigo_principio_ativo"));
				standard.setCodeApresDiluent(standardJson.getString("codigo_apresentacao_diluente"));
				standard.setCodeDiluent(standardJson.getInt("codigo_diluente"));
				standard.setCodeEventsDiluent(standardJson.getInt("codigo_ocorrencia_diluicao"));
				standard.setCodeOrderAdmin(standardJson.getInt("codigo_ordem_administracao"));
				standard.setCodeUnitDosage(standardJson.getString("codigo_unidade_dosagem"));
				standard.setAdminDiluent(standardJson.getString("administracao_diluicao"));
				standard.setQtyDiluent(standardJson.getInt("quantidade_diluente"));
				standard.setQtyDosage(standardJson.getInt("quantidade_dosagem"));
				standard.setQtyVolume(standardJson.getInt("quantidade_volume"));
				standard.setTypeAccess(standardJson.getString("tipo_acesso"));
				standard.setTypeAdmin(standardJson.getString("tipo_administracao"));
				standard.setMnemonic(standardJson.getString("mnemonico"));
			}			
			if (!resourceJson.isNull("ambulatorial")) {
				medicationPrescribedResource.setOutpatient(resourceJson.getBoolean("ambulatorial"));
			}
			
			complementaryMedication.setPrescribedresource(medicationPrescribedResource);
		}
		
		return complementaryMedication;
	}
	
	public ComplementaryProcedure createComplementaryProcedure(JSONObject json) throws ParseException, JSONException {
		ComplementaryProcedure complementaryProcedure = Execution_metamodelFactory.eINSTANCE.createComplementaryProcedure();
		complementaryProcedure = (ComplementaryProcedure) createComplementaryConducts(json, complementaryProcedure);
		
		if (json.has("recurso_prescrito")) {
			ProcedurePrescribedResource procedurePrescribedResource = Execution_metamodelFactory.eINSTANCE.createProcedurePrescribedResource();
			JSONObject resourceJson = json.getJSONObject("recurso_prescrito");
			
			procedurePrescribedResource.setId(resourceJson.getInt("id"));
			procedurePrescribedResource.setIdProcedure(resourceJson.getInt("procedimento_id"));
			procedurePrescribedResource.setQuantity(resourceJson.getInt("quantidade"));
			procedurePrescribedResource.setProcedure(resourceJson.getString("procedimento"));
			procedurePrescribedResource.setFrequency(resourceJson.getInt("frequencia"));
			
			if (!resourceJson.isNull("categoria")) {
				procedurePrescribedResource.setCategory(resourceJson.getString("categoria"));	
			}
			
			complementaryProcedure.setProcedureprescribedresource(procedurePrescribedResource);
		}
		
		return complementaryProcedure;
	}
	
	public ComplementaryExamination createComplementaryExamination(JSONObject json) throws ParseException, JSONException {
		ComplementaryExamination complementaryExamination = Execution_metamodelFactory.eINSTANCE.createComplementaryExamination();
		complementaryExamination = (ComplementaryExamination) createComplementaryConducts(json, complementaryExamination);
		
		if (json.has("recurso_prescrito")) {
			ExaminationPrescribedResource examinationPrescribedResource = Execution_metamodelFactory.eINSTANCE.createExaminationPrescribedResource();
			JSONObject resourceJson = json.getJSONObject("recurso_prescrito");
			
			examinationPrescribedResource.setId(resourceJson.getInt("id"));
			examinationPrescribedResource.setIdExam(resourceJson.getInt("exame_id"));
			examinationPrescribedResource.setExam(resourceJson.getString("exame"));
			examinationPrescribedResource.setJustification(resourceJson.getString("justificativa"));
			examinationPrescribedResource.setQuantity(resourceJson.getInt("quantidade"));
			examinationPrescribedResource.setSideLimb(resourceJson.getString("lado_membro"));
			examinationPrescribedResource.setClinicalIndication(resourceJson.getString("indicacao_clinica"));
			
			if (!resourceJson.isNull("categoria")) {
				examinationPrescribedResource.setCategory(resourceJson.getString("categoria"));
			}
			
			complementaryExamination.setExaminationprescribedresource(examinationPrescribedResource);
		}
		
		return complementaryExamination;
	}
}
