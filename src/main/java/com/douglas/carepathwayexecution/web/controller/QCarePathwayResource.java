package com.douglas.carepathwayexecution.web.controller;

import java.io.IOException;

import org.json.JSONArray;
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "CarePathway", 
	description = "",
	produces ="application/json")
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
	@RequestMapping(value = { "/medcare/execution/pathways/compress/{method}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getResults(
			@PathVariable(value = "method") String method,
			@RequestParam(value = "path", required = true) String path) throws IOException, JSONException {

		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		FileConfig config = new FileConfig();
		JSONArray data = config.toJSONObject(path).getJSONArray("method");
		
		switch (method) {
		case "status":
			eQuery = qStatusService.getResults(data);
			break;
		case "occurrence":
			eQuery = qOccurrenceService.getResults(data);
			break;
		case "conduct":
			eQuery = qConductsService.getResults(data);
			break;
		case "time":
			eQuery = qAverageTimeService.getResults(data);
			break;
		case "prescription":
			//
			eQuery = qPrescriptionService.getResults(data);
			break;
		case "exam":
			eQuery = qExamService.getResults(data);
			break;
		case "answer":
			eQuery = qAnswerService.getResults(data);
			break;
		case "abort":
			eQuery = qAbortedStepService.getResults(data);
			break;
		case "step":
			eQuery = qStepService.getResults(data);
			break;
		case "flow":
			
			break;
		case "medication":
			eQuery = qMedicationService.getResults(data);
			break;
		case "return":
			
			break;
		default:
			break;
		}
		
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(null);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
}
