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
import com.douglas.carepathwayexecution.web.service.QAnswerService;
import com.douglas.carepathwayexecution.web.service.QCarePathwayService;

import QueryMetamodel.EQuery;
import QueryMetamodel.Query_metamodelFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Answer", 
	description = "Show the answer occorrences of the questions in the care pathway execution",
	produces ="application/json")
@Controller
public class QAnswerResource {
	@Autowired
	private QCarePathwayService service;
	@Autowired
	private QAnswerService answerService;
	
	@ApiOperation(value = "Calculate the answer occorrences of a specified care pathway id and the all questions")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/answers/{type}" }, 
					method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllAnswersToOnePathwayAndAllQuestions(
		@PathVariable( value = "id", required=true) String idPathway,
		@PathVariable( value = "type", required=false) String type,
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
		eQuery = answerService.getOccorrencesAnswer(eQuery, null, type, 0);
		queryDTO.setMethod( eQuery.getEMethod());

		return ResponseEntity.ok().body(queryDTO);
	}

	@ApiOperation(value = "Calculate the answer occorrences of a specified care pathway id and the all questions")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/version/{version}/answers/{type}" }, 
					method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllAnswersToOnePathwayAndAllQuestionsByVersion(
		@PathVariable( value = "id", required=true) String idPathway,
		@PathVariable( value = "type", required=false) String type,
		@PathVariable( value = "version", required=false) int version,
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
		eQuery = answerService.getOccorrencesAnswer(eQuery, null, type, version);
		queryDTO.setMethod( eQuery.getEMethod());

		return ResponseEntity.ok().body(queryDTO);
	}

	
	@ApiOperation(value = "Calculate the answer occurrences of all care pathway and one question")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/answers/{type}/questions/{name}" }, 
					method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllAnswersToAllPathwaysAndOneQuestion(
		@PathVariable( value = "name", required=true) String question,
		@PathVariable( value = "type", required=false) String type,
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
		eQuery = answerService.getOccorrencesAnswer(eQuery, question, type, 0);
		queryDTO.setMethod( eQuery.getEMethod());		
		return ResponseEntity.ok().body(queryDTO);
	}

	@ApiOperation(value = "Calculate the answer occurrences of all care pathway and all questions")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/answers/{type}" }, 
					method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllAnswersToAllPathwaysAndAllQuestions(
		@PathVariable( value = "type", required=false) String type,
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
		eQuery = answerService.getOccorrencesAnswer(eQuery, null, type, 0);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}

	@ApiOperation(value = "Calculate the answer occorrences of each care pathway and one question")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/answers/{type}/questions/{name}" }, 
					method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllAnswersToOnePathwayAndOneQuestion(
		@PathVariable( value = "id", required=true) String idPathway,
		@PathVariable( value = "type", required=false) String type,
		@PathVariable( value = "name", required=true) String question,
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
		eQuery = answerService.getOccorrencesAnswer(eQuery, question, type, 0);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
	
	@ApiOperation(value = "Calculate the answer occorrences of each care pathway and one question")
	@ApiResponses(value= @ApiResponse(code=200, 
										response= EQueryDTO.class, 
										message = ""))
	@RequestMapping(value = { "/medcare/execution/pathways/{id}/version/{version}/answers/{type}/questions/{name}" }, 
					method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EQueryDTO> getAllAnswersToOnePathwayAndOneQuestionByVersion(
		@PathVariable( value = "id", required=true) String idPathway,
		@PathVariable( value = "type", required=false) String type,
		@PathVariable( value = "name", required=true) String question,
		@PathVariable( value = "version", required=true) int version,
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
		eQuery = answerService.getOccorrencesAnswer(eQuery, question, type, version);
		queryDTO.setMethod( eQuery.getEMethod());
		return ResponseEntity.ok().body(queryDTO);
	}
}
