package com.douglas.carepathwayexecution.translator.model.pathway.step;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import MetamodelExecution.Answer;
import MetamodelExecution.Audit;
import MetamodelExecution.Complement;
import MetamodelExecution.EAuxiliaryConduct;
import MetamodelExecution.EDischarge;
import MetamodelExecution.EInformation;
import MetamodelExecution.EPrescription;
import MetamodelExecution.EReferral;
import MetamodelExecution.EStep;
import MetamodelExecution.ETreatment;
import MetamodelExecution.Examination;
import MetamodelExecution.Execution_metamodelFactory;
import MetamodelExecution.Justification;
import MetamodelExecution.Medication;
import MetamodelExecution.Numeric;
import MetamodelExecution.PExamination;
import MetamodelExecution.PInternment;
import MetamodelExecution.PMedication;
import MetamodelExecution.PPrescription;
import MetamodelExecution.PProcedure;
import MetamodelExecution.Prescription;
import MetamodelExecution.PrescriptionResult;
import MetamodelExecution.Question;
import MetamodelExecution.Result;
import MetamodelExecution.Step;
import MetamodelExecution.Variable;
import MetamodelExecution.YesOrNo;

public class ExecutedStep {
	public EStep createEElement(JSONObject json, EStep eElement) throws ParseException, JSONException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS", Locale.getDefault());
		
		//set step
		JSONObject stepJson = json.getJSONObject("passo");
		Step step = Execution_metamodelFactory.eINSTANCE.createStep();
		step.setId(stepJson.getInt("id"));
		step.setType(stepJson.getString("type"));
		step.setName(stepJson.getString("nome"));
		step.setDescription(stepJson.getString("descricao"));
		step.setIsInitial(stepJson.getBoolean("is_initial"));
		step.setIsTerminal(stepJson.getBoolean("is_terminal"));
		step.setMandatory(stepJson.getBoolean("obrigatoriedade"));
				
		//set audit
		if (!stepJson.isNull("ultima_auditoria")) {
			Audit audit = Execution_metamodelFactory.eINSTANCE.createAudit();	
			JSONObject auditJson = stepJson.getJSONObject("ultima_auditoria");
			String dateStr = auditJson.getString("data");
			Date date = dateFormat.parse(dateStr);	
			
			audit.setDate(date);
			
			step.setAudit(audit);
		}			
		
		//set dates
		String creationStr = json.getString("data_criacao");
		Date creationDate = dateFormat.parse(creationStr);
		String modificationStr = json.getString("data_modificacao");
		Date modificationDate = dateFormat.parse(modificationStr);
		
		//Set executed element/step
		eElement.setId(json.getInt("id"));
		eElement.setIsCurrent(json.getBoolean("is_current"));
		eElement.setReworked(json.getBoolean("reworked"));
		eElement.setExecuted(json.getBoolean("executado"));		
		eElement.setCreatedById(json.getInt("criado_por_id"));		
		eElement.setStep(step);
		eElement.setName(eElement.getStep().getName());		
		eElement.setCreationDate(creationDate);	
		eElement.setModificationDate(modificationDate);
		
		if(!json.isNull("previous")) {
			JSONObject previousJson = json.getJSONObject("previous");
			eElement.setPrevious(previousJson.getString("url"));
		}
		if(!json.isNull("next")) {
			JSONObject nextJson = json.getJSONObject("next");
			eElement.setNext(nextJson.getString("url"));
		}
		
		//set justification
		if (!json.isNull("justificativa")) {	
			Justification justification = Execution_metamodelFactory.eINSTANCE.createJustification();
			JSONObject justificationJson = json.getJSONObject("justificativa");
			justification.setId(justificationJson.getInt("id"));
			justification.setReason(justificationJson.getString("razao"));
			justification.setDescription(justificationJson.getString("descricao"));
			justification.setJustifiedById(justificationJson.getInt("justificado_por_id"));
		
			eElement.setJustification(justification);
		}		
		if (!json.isNull("executado_por_id")) {
			eElement.setExecutedById(json.getInt("executado_por_id"));
		}		
		if (!json.isNull("data_execucao")) {
			String executionStr = json.getString("data_execucao");
			Date executionDate = dateFormat.parse(executionStr);
			
			eElement.setExecutionDate(executionDate);
		}		
		
		return eElement;
	}
	
	public EAuxiliaryConduct createEAuxiliaryConduct(JSONObject json) throws ParseException, JSONException{
		//Set executed auxiliary conduct
		EAuxiliaryConduct eAuxiliaryConduct = Execution_metamodelFactory.eINSTANCE.createEAuxiliaryConduct();
		eAuxiliaryConduct = (EAuxiliaryConduct) createEElement(json, eAuxiliaryConduct);
		
		//Set answers
		JSONArray answers = json.getJSONArray("respostas");
		
		for (int i = 0; i < answers.length(); i++) {
			//set answer
			JSONObject answerJson = answers.getJSONObject(i);
			Answer answer = Execution_metamodelFactory.eINSTANCE.createAnswer();			
			answer.setId(answerJson.getInt("id"));
			answer.setType(answerJson.getString("type"));
			
			String type = answerJson.getString("type");		
			
			if (type.equals("RespostaSimOuNao")) {
				//set yes or no
				YesOrNo yesOrNo = Execution_metamodelFactory.eINSTANCE.createYesOrNo();
				yesOrNo.setValue(answerJson.getBoolean("valor"));				
				//save value
				//answer.setValue(yesOrNo);
			}
			else if (type.equals("RespostaNumerica")) {
				//set numeric
				Numeric numeric = Execution_metamodelFactory.eINSTANCE.createNumeric();
				numeric.setValue(answerJson.getDouble("valor"));				
				//save value
				//answer.setValue(numeric);
			}	
			
			//set question
			JSONObject questionJson = answerJson.getJSONObject("pergunta");
			Question question = Execution_metamodelFactory.eINSTANCE.createQuestion();			
			question.setId(questionJson.getInt("id"));
			question.setText(questionJson.getString("texto"));
			
			if (!questionJson.isNull("categoria")) {
				question.setCategory(questionJson.getString("categoria"));
			}  
			
			JSONObject variableJson = questionJson.getJSONObject("variavel");
			Variable variable = Execution_metamodelFactory.eINSTANCE.createVariable();
			variable.setId(variableJson.getInt("id"));
			variable.setType(variableJson.getString("type"));
			variable.setName(variableJson.getString("nome"));
			variable.setWeight(variableJson.getInt("peso"));			
			
			if (type.equals("RespostaSimOuNao")) {			
				//set yes or no
				YesOrNo yesOrNo = Execution_metamodelFactory.eINSTANCE.createYesOrNo();
				yesOrNo.setValue(variableJson.getBoolean("valor"));					
				//save yes or no
				//variable.setValue(yesOrNo);			
			}else if (type.equals("RespostaNumerica")) {
				//set numeric
				Numeric numeric = Execution_metamodelFactory.eINSTANCE.createNumeric();
				numeric.setValue(variableJson.getDouble("valor"));				
				//save numeric
				//variable.setValue(numeric);
			}				
			
			question.setVariable(variable);			
			//save question
			answer.setQuestion(question);			
			//save answer
			eAuxiliaryConduct.getAnswer().add(answer);
		}		
		
		return eAuxiliaryConduct;
	}
	
	public ETreatment createETreatment(JSONObject json) throws ParseException, JSONException{
		//set executed treatment
		ETreatment eTreatment = Execution_metamodelFactory.eINSTANCE.createETreatment();		
		eTreatment = (ETreatment) createEElement(json, eTreatment);	
		
		//set prescribed examination
		JSONArray idsPExaminationJson = json.getJSONArray("exames_prescritos_ids");		
		for (int i = 0; i < idsPExaminationJson.length(); i++) {
			//save prescribed examination
			eTreatment.getIdsPExamination().add(idsPExaminationJson.optInt(i));
		}
		
		//set prescribed  procedure
		JSONArray idsPProceduresJson = json.getJSONArray("procedimentos_prescritos_ids");		
		for (int i = 0; i < idsPProceduresJson.length(); i++) {
			//save prescribed  procedure
			eTreatment.getIdsPProcedure().add(idsPProceduresJson.optInt(i));
		}
		
		//set prescribed internment
		JSONArray idsPInternmentJson = json.getJSONArray("internamentos_prescritos_ids");		
		for (int i = 0; i < idsPInternmentJson.length(); i++) {
			//save prescribed internment
			eTreatment.getIdsPInternment().add(idsPInternmentJson.optInt(i));
		}
		
		//set prescribed medication 
		JSONArray idsPMedicationJson = json.getJSONArray("medicamentos_prescritos_ids");		
		for (int i = 0; i < idsPMedicationJson.length(); i++) {
			//save prescribed medication 
			eTreatment.getIdsPMedication().add(idsPMedicationJson.optInt(i));
		}	
		
		//save prescribed medication
		eTreatment.getPmedication().addAll(createPMedication(json));
		
		//set prescribed examination
		JSONArray pExaminations = json.getJSONArray("exames_prescritos");			
		for (int i = 0; i < pExaminations.length(); i++) {			
			PExamination pExamination = Execution_metamodelFactory.eINSTANCE.createPExamination();
			JSONObject pExaminationJson = pExaminations.getJSONObject(i);
			pExamination.setId(pExaminationJson.getInt("id"));
			pExamination.setReport(pExaminationJson.getString("laudo"));
			
			if (!pExaminationJson.isNull("numero_guia")) {
				pExamination.setNumberGuide(pExaminationJson.getInt("numero_guia"));
			}			
			
			if (!pExaminationJson.isNull("resultado")) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS", Locale.getDefault());
				JSONObject resultJson = pExaminationJson.getJSONObject("resultado");			
				
				Result result = Execution_metamodelFactory.eINSTANCE.createResult();
				
				result.setId(resultJson.getInt("id"));
				result.setSuccess(resultJson.getBoolean("sucesso"));
				result.setMessage(resultJson.getString("mensagem"));			
				
				String resultStr = resultJson.getString("data_solicitacao");
				Date resultDate = dateFormat.parse(resultStr);			
				
				result.setRequestDate(resultDate);
			
				pExamination.setResult(result);			
			}						
			
			//save prescription 
			pExamination.setPrescriptionResult(createPrescriptionResult(pExaminationJson));
			
			//set complement
			Complement complement = Execution_metamodelFactory.eINSTANCE.createComplement();			
			if (!pExaminationJson.isNull("complemento")) {				
				JSONObject complementJson = pExaminationJson.getJSONObject("complemento");
				complement.setId(complementJson.getInt("id"));
				complement.setSideLimb(complementJson.getString("lado_membro"));
				complement.setJustification(complementJson.getString("justificativa"));
				complement.setClinicalIndication(complementJson.getString("indicacao_clinica"));
				
				if (!complementJson.isNull("quantidade")) {
					complement.setQuantity(complementJson.getInt("quantidade"));
				}			
			}
			
			//save complement	
			pExamination.setComplement(complement);
			
			//set examination
			Examination examination = Execution_metamodelFactory.eINSTANCE.createExamination();
			JSONObject examinationJson = pExaminationJson.getJSONObject("exame");
			examination.setId(examinationJson.getInt("id"));
			examination.setSideLimb(examinationJson.getString("lado_membro"));
			examination.setQuantity(examinationJson.getInt("quantidade"));
			examination.setJustification(examinationJson.getString("justificativa"));
			examination.setClinicalIndication(examinationJson.getString("indicacao_clinica"));
			
			JSONObject examJson = examinationJson.getJSONObject("exame");
			examination.setIdExamination(examJson.getInt("id"));
			examination.setCode(examJson.getInt("codigo"));
			examination.setName(examJson.getString("nome"));
			examination.setDescription(examJson.getString("descricao"));
			examination.setOnlyEmergency(examJson.getBoolean("somente_emergencia"));
			examination.setMemberPeers(examJson.getBoolean("membros_pares"));
			
			//save examination
			pExamination.setExamination(examination);
			
			//save prescribed examination
			eTreatment.getPexamination().add(pExamination);
		}	
		
		//set prescribed procedure
		JSONArray pProcedures = json.getJSONArray("procedimentos_prescritos");
		for (int i = 0; i < pProcedures.length(); i++) {			
			PProcedure pProcedure = Execution_metamodelFactory.eINSTANCE.createPProcedure();
			//save prescribed procedure
			eTreatment.getPprocedure().add(pProcedure);
		}
		
		//set prescribed internment
		JSONArray pInternments = json.getJSONArray("internamentos_prescritos");
		for (int i = 0; i < pInternments.length(); i++) {			
			PInternment pInternment = Execution_metamodelFactory.eINSTANCE.createPInternment();
			//save prescribed internment
			eTreatment.getPinternment().add(pInternment);
		}		
		
		return eTreatment;
	}

	//set executed precription
	public EPrescription createEPrescription(JSONObject json) throws ParseException, JSONException{		
		EPrescription ePrescription = Execution_metamodelFactory.eINSTANCE.createEPrescription();
		ePrescription = (EPrescription) createEElement(json, ePrescription);
		
		ePrescription.setText(json.getString("texto"));
		
		//set ids prescribed medication 
		JSONArray idsPMedicationJson = json.getJSONArray("medicamentos_prescritos_ids");		
		for (int i = 0; i < idsPMedicationJson.length(); i++) {
			//save prescribed medication 
			ePrescription.getIdsPMedication().add(idsPMedicationJson.optInt(i));
		}
		
		//set ids prescribed prescription item
		JSONArray idsPPrescriptionJson = json.getJSONArray("medicamentos_prescritos_ids");		
		for (int i = 0; i < idsPPrescriptionJson.length(); i++) {
			//save prescribed Prescription 
			ePrescription.getIdsPPrescription().add(idsPPrescriptionJson.optInt(i));
		}
		
		//save prescription info
		//ePrescription.getPrescriptionResult().add(createPrescriptionResult(json));
		
		//set prescription prescription item
		JSONArray pPrescriptionJson = json.getJSONArray("itens_receita_prescritos");

		for (int i = 0; i < pPrescriptionJson.length(); i++) {			
			PPrescription pPrescription = Execution_metamodelFactory.eINSTANCE.createPPrescription();	
			Prescription prescription = Execution_metamodelFactory.eINSTANCE.createPrescription();
			
			JSONObject prescriptionJson = pPrescriptionJson.getJSONObject(i);
			//prescription.setAccess( prescriptionJson.getString("via_acesso"));
			//prescription.setFrequency(prescriptionJson.getInt("quantidade_frequencia_uso"));
			prescription.setId(prescriptionJson.getInt("id"));
			//prescription.setIdMedication(prescriptionJson.getInt("medicamento_id"));
			//prescription.setMedication(prescriptionJson.getString("medicamento"));
			//prescription.setName(prescriptionJson.getString("nome"));
//			prescription.setOrder(prescriptionJson.getInt("ordem"));
//			prescription.setPresentation(prescriptionJson.getString("apresentacao"));
//			prescription.setQtdDuration(prescriptionJson.getInt("quantidade_duracao"));
//			prescription.setQtdPrescription(prescriptionJson.getInt("quantidade_receita"));
//			prescription.setUnitDuration(prescriptionJson.getString("unidade_duracao_display"));
//			prescription.setUnitFrequency(prescriptionJson.getString("unidade_frequencia_display"));
//s			prescription.setComplement(prescriptionJson.getString("complemento"));
			
			pPrescription.setLastPrescriptionExecuted(prescriptionJson.getBoolean("prescrito_ultima_receita_executado"));
			//save prescription
			pPrescription.setPrescription(prescription);
			
			//save prescription prescription
			ePrescription.getPprescription().add(pPrescription);
		}
		
		//save prescription medication
		List<PMedication> pMedications = createPMedication(json);		
		for (int i = 0; i < pMedications.size(); i++) {
			ePrescription.getPmedication().add(pMedications.get(i));
		}		
			
		return ePrescription;
	}

	//set executed information
	public EInformation createEInformation(JSONObject json) throws ParseException, JSONException{		
		EInformation eInformation = Execution_metamodelFactory.eINSTANCE.createEInformation();
		eInformation = (EInformation) createEElement(json, eInformation);
		
		return eInformation;
	}
	
	//set executed referral
	public EReferral createEReferral(JSONObject json) throws ParseException, JSONException{		
		EReferral eReferral = Execution_metamodelFactory.eINSTANCE.createEReferral();
		eReferral = (EReferral) createEElement(json, eReferral);
		
		return eReferral;
	}
	
	//set executed discharge
	public EDischarge createEDischarge(JSONObject json) throws ParseException, JSONException{
		EDischarge eDischarge = Execution_metamodelFactory.eINSTANCE.createEDischarge();
		eDischarge = (EDischarge) createEElement(json, eDischarge);
		
		return eDischarge;
	}
	
	//set prescribed medication
	private List<PMedication> createPMedication(JSONObject json) throws ParseException, JSONException {
		JSONArray pMedicationsJson = json.getJSONArray("medicamentos_prescritos");
		List<PMedication> pMedications = new ArrayList<PMedication>();
		
		for (int i = 0; i < pMedicationsJson.length(); i++) {
			PMedication pMedication = Execution_metamodelFactory.eINSTANCE.createPMedication();
			JSONObject pMedicationJson = pMedicationsJson.getJSONObject(i);
			
			pMedication.setId(pMedicationJson.getInt("id"));
			if (!pMedicationJson.isNull("resultado")) {
				pMedication.setResult(pMedicationJson.getString("resultado"));
			}
			
			//save prescription
			pMedication.setPrescriptionResult(createPrescriptionResult(pMedicationJson));
			
			//set medicament
			Medication medicament = Execution_metamodelFactory.eINSTANCE.createMedication();
			JSONObject medicamentJson = pMedicationJson.getJSONObject("medicamento");
			medicament.setId(medicamentJson.getInt("id"));
			medicament.setName(medicamentJson.getString("nome"));
			medicament.setCode(medicamentJson.getString("codigo"));
			medicament.setDescription(medicamentJson.getString("descricao"));
			medicament.setBrand(medicamentJson.getString("marca"));
			medicament.setDailyDosage(medicamentJson.getInt("dose_diaria"));
			medicament.setCycles(medicamentJson.getInt("ciclos"));
			medicament.setFrequency(medicamentJson.getInt("frequencia"));
			medicament.setTimeInterval(medicamentJson.getInt("dias_intervalo"));
			medicament.setTimeTreatement(medicamentJson.getInt("dias_tratamento"));
			
			if (!medicamentJson.isNull("ambulatorial")) {
				medicament.setOutpatient(medicamentJson.getBoolean("ambulatorial"));
			}
			
			if (!medicamentJson.isNull("padrao")) {
				medicament.setStandard(medicamentJson.getString("padrao"));
			}	
			
			JSONObject unitJson = medicamentJson.getJSONObject("unidade");
			medicament.setIdUnit(unitJson.getInt("id"));
			medicament.setName(unitJson.getString("nome"));
			medicament.setCode(unitJson.getString("codigo"));
			medicament.setUnit(unitJson.getString("unidade"));

			JSONObject accessJson = medicamentJson.getJSONObject("via_acesso");
			medicament.setIdAccess(accessJson.getInt("id"));
			medicament.setNameAcess(accessJson.getString("nome"));
			medicament.setCodeAccess(accessJson.getInt("codigo"));
			
			//add prescribedMedication
			pMedications.add(pMedication);
		}		
		
		return pMedications;
	}
	
	//set prescription result exam
	private PrescriptionResult createPrescriptionResult(JSONObject json) throws ParseException, JSONException {
		PrescriptionResult prescription = Execution_metamodelFactory.eINSTANCE.createPrescriptionResult();
		
		if (!json.isNull("prescricao")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS", Locale.getDefault());
			JSONObject prescriptionJson = json.getJSONObject("prescricao");			
			
			prescription.setId(prescriptionJson.getInt("id"));
			prescription.setSuccess(prescriptionJson.getBoolean("sucesso"));
			prescription.setMessage(prescriptionJson.getString("mensagem"));			
			
			String requestStr = prescriptionJson.getString("data_solicitacao");
			Date requestDate = dateFormat.parse(requestStr);			
			
			prescription.setRequestDate(requestDate);
		}		
		
		return prescription;
	}
}
