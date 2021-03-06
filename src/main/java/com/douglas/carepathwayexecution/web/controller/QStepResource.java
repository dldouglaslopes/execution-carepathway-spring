package com.douglas.carepathwayexecution.web.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.douglas.carepathwayexecution.web.domain.EQueryDTO;
import com.douglas.carepathwayexecution.web.service.QCarePathwayService;
import com.douglas.carepathwayexecution.web.service.QStepService;

import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Step", 
	description = "Show the recurrent step of the care pathway execution",
	produces ="application/json")
@Controller
public class QStepResource {
	@Autowired
	private QCarePathwayService service;
	@Autowired
	private QStepService stepService;
	
	@ApiOperation(value = "Calculate the reccurrent step of a specified care pathway id")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/steps" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getRecurrentStepToOnePathway(
		@PathVariable( value = "id", required=true) int idPathway,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( idPathway,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = stepService.getRecurrentSteps(eQuery, null, 0);
		queryDTO.setMethod( eQuery.getEMethod());
		
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the reccurrent step of a specified care pathway id by pathway version")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/version/{version}/steps" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getRecurrentStepToOnePathwayByVersion(
		@PathVariable( value = "id", required=true) int idPathway,
		@PathVariable( value = "version", required=true) int version,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( idPathway,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = stepService.getRecurrentSteps(eQuery, null, version);
		queryDTO.setMethod( eQuery.getEMethod());
		
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the reccurrent step of each care pathway")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/steps" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getRecurrentStepToAllPathways(
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( 0,
										conductStr,					
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = stepService.getRecurrentSteps(eQuery, null, 0);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the reccurrent step of each care pathway")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/steps/{name}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getRecurrentStepToAllPathwaysAndOneStep(
		@PathVariable( value = "name", required=true) String step,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( 0,
										conductStr,					
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = stepService.getRecurrentSteps(eQuery, step, 0);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the reccurrent step of each care pathway")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/steps/{name}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getRecurrentStepToOnePathwayAndOneStep(
		@PathVariable( value = "id", required=true) int idPathway,
		@PathVariable( value = "name", required=true) String step,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( idPathway,
										conductStr,					
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = stepService.getRecurrentSteps(eQuery, step, 0);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the reccurrent step of each care pathway by pathway version")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/version/{version}/steps/{name}" }, 
					method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getRecurrentStepToOnePathwayByVersionAndOneStep(
		@PathVariable( value = "id", required=true) int idPathway,
		@PathVariable( value = "version", required=true) int version,
		@PathVariable( value = "name", required=true) String step,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		@RequestParam( value = "range", required=false) String rangeStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( idPathway,
										conductStr,					
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										service.splitBy( rangeStr, ","));
			
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = stepService.getRecurrentSteps(eQuery, step, version);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
}
