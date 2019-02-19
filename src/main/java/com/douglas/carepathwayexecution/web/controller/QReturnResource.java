package com.douglas.carepathwayexecution.web.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.douglas.carepathwayexecution.web.domain.EQueryDTO;
import com.douglas.carepathwayexecution.web.service.QAverageTimeService;
import com.douglas.carepathwayexecution.web.service.QCarePathwayService;
import com.douglas.carepathwayexecution.web.service.QReturnService;

import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Return", 
	description = "Show the return patient of the care pathway execution",
	produces ="application/json")
public class QReturnResource {
	@Autowired
	private QCarePathwayService service;
	@Autowired
	private QReturnService returnService;
	
	@ApiOperation(value = "Calculate the patients that return of a specified care pathway id")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/return/patients" }, 
					method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllPatientsToOnePathway(
		@PathVariable( value = "id", required=true) String idPathway,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( Integer.parseInt(idPathway),
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		//eQuery = flowService.recurrentFlow(eQuery);
		queryDTO.setMethod(eQuery.getEMethod());
		
		return ResponseEntity.ok().body(queryDTO);
	}

	@ApiOperation(value = "Calculate the patients that return of each care pathway")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/return/patients" }, 
					method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllPatientsToAllPathways(
		@PathVariable( value = "id", required=true) String idPathway,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( Integer.parseInt(idPathway),
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		//eQuery = flowService.recurrentFlow(eQuery);
		queryDTO.setMethod(eQuery.getEMethod());
		
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patient that return of a specified care pathway id")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/return/patients/{code}" }, 
					method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getOnePatientToOnePathway(
		@PathVariable( value = "id", required=true) String idPathway,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( Integer.parseInt(idPathway),
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		//eQuery = flowService.recurrentFlow(eQuery);
		queryDTO.setMethod(eQuery.getEMethod());
		
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the patient that return of each care pathway")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/return/patients/{code}" }, 
					method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getOnePatientToAllPathways(
		@PathVariable( value = "id", required=true) String idPathway,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( Integer.parseInt(idPathway),
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		//eQuery = flowService.recurrentFlow(eQuery);
		queryDTO.setMethod(eQuery.getEMethod());
		
		return ResponseEntity.ok().body(queryDTO);
	}

}
