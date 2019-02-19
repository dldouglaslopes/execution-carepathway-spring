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
import com.douglas.carepathwayexecution.web.service.EAverageTimeService;
import com.douglas.carepathwayexecution.web.service.ECarePathwayService;

import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "AverageTime", description = "Show the average time of the care pathway execution")
@Controller
public class EAverageTimeResource {
	@Autowired
	private ECarePathwayService service;
	@Autowired
	private EAverageTimeService timeService;
	
	@ApiOperation(value = "Calculate the average time of a specified care pathway id")
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/time" }, 
			method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> averageTimeToOnePathway(
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
		eQuery = timeService.averageByTime(eQuery);
		queryDTO.setMethod(eQuery.getEMethod());
		
		return ResponseEntity.ok().body(queryDTO);
	}

	@ApiOperation(value = "Calculate the average time of each care pathway")
	@RequestMapping(value = { "/medcare/execution/pathways/time" }, 
			method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> averageTimeToAllPathways(
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
		eQuery = timeService.averageByTime(eQuery);
		queryDTO.setMethod(eQuery.getEMethod());
		
		return ResponseEntity.ok().body(queryDTO);
	}
}
