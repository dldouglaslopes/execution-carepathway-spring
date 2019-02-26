package com.douglas.carepathwayexecution.web.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.douglas.carepathwayexecution.web.service.QStatusService;

import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Status", 
	description = "Show the status rating of the care pathway execution",
	produces ="application/json")
@Controller
public class QStatusResource {
	@Autowired
	private QCarePathwayService service;
	@Autowired
	private QStatusService statusService;
	
	@ApiOperation(value = "Calculate the status rating of a specified care pathway id")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/status" }, 
			method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getCountStatusToOnePathway(
		@PathVariable( value = "id", required=true) String idPathway,
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( Integer.parseInt(idPathway),
										conductStr,						
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										null);
		
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = statusService.countStatus(eQuery);
		queryDTO.setMethod( eQuery.getEMethod());
		
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the status rating of a specified care pathway id")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/status" }, 
			method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getStatusToAllPathway(
		@RequestParam( value = "conduct", required=false) String conductStr,
		@RequestParam( value = "status", required=false) String statusStr,
		@RequestParam( value = "age", required=false) String ageStr,
		@RequestParam( value = "sex", required=false) String sexStr,
		@RequestParam( value = "date", required=false) String dateStr,
		Model model) throws ParseException{			
	
		EQuery eQuery = Query_metamodelFactory.eINSTANCE.createEQuery();
		eQuery = service.setAtribbutte( 0,
										conductStr,
										service.splitBy( statusStr, ","),
										service.splitBy( ageStr, ","),
										sexStr, 
										service.splitBy( dateStr, ","),
										null);
		
		EQueryDTO queryDTO = new EQueryDTO();
		queryDTO.setAttribute(eQuery.getEAttribute());
		eQuery = statusService.countStatus(eQuery);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}

}
