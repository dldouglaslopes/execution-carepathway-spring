package com.douglas.carepathwayexecution.web.controller;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.douglas.carepathwayexecution.translator.config.FileConfig;
import com.douglas.carepathwayexecution.web.domain.EQueryDTO;
import com.douglas.carepathwayexecution.web.service.QAbortedStepService;
import com.douglas.carepathwayexecution.web.service.QAnswerService;
import com.douglas.carepathwayexecution.web.service.QAverageTimeService;
import com.douglas.carepathwayexecution.web.service.QCarePathwayService;
import com.douglas.carepathwayexecution.web.service.QConductsService;
import com.douglas.carepathwayexecution.web.service.QExamService;
import com.douglas.carepathwayexecution.web.service.QFlowService;
import com.douglas.carepathwayexecution.web.service.QMedicationService;
import com.douglas.carepathwayexecution.web.service.QOccurrenceService;
import com.douglas.carepathwayexecution.web.service.QPatientReturnService;
import com.douglas.carepathwayexecution.web.service.QPrescriptionService;
import com.douglas.carepathwayexecution.web.service.QStatusService;
import com.douglas.carepathwayexecution.web.service.QStepService;

import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;
import ch.qos.logback.core.joran.action.NewRuleAction;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Controller
public class QCarePathwayResource {
	@Autowired
	private QStatusService qStatusService;
	@Autowired
	private QAbortedStepService qAbortedStepService;
	@Autowired
	private QPatientReturnService qPatientReturnService;
	@Autowired
	private QAnswerService qAnswerService;
	@Autowired
	private QAverageTimeService qAverageTimeService;
	@Autowired
	private QStepService qStepService;
	@Autowired
	private QPrescriptionService qPrescriptionService;
	@Autowired
	private QOccurrenceService qOccurrenceService;
	@Autowired
	private QConductsService qConductsService;
	@Autowired
	private QMedicationService qMedicationService;
	@Autowired
	private QFlowService qFlowService;
	@Autowired
	private QExamService qExamService;
	@Autowired
	private QCarePathwayService qCarePathwayService;
	
	@ApiOperation(value = "Calculate the results by each care pathway")
	
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{method}" }, 
					method = RequestMethod.POST,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getResults(
			@PathVariable(value = "method") String method,
			@RequestParam(value = "path") String path) throws IOException, JSONException {

		FileConfig config = new FileConfig();
		JSONObject jsonObject = config.toJSONObject(path);
		JSONObject jsonResult = new JSONObject();
		
		switch (method) {
		case "status":
			
			break;
		case "occurrence":
			
			break;
		case "conduct":
			
			break;
		case "time":
			
			break;
		case "prescription":
			
			break;
		case "exam":
			
			break;
		case "answer":
			
			break;
		case "abort":
			
			break;
		case "step":
			
			break;
		case "flow":
			
			break;
		case "medication":
			
			break;
		case "return":
			
			break;
		default:
			break;
		}
		
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(null);
		eQuery = qCarePathwayService.getResult(jsonResult);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
}
