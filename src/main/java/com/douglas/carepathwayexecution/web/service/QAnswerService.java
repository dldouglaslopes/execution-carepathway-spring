package com.douglas.carepathwayexecution.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.EQuery;

@Service
public class QAnswerService {
	@Autowired
	private QCarePathwayService service;
	
	public EQuery occorrencesAnswer(EQuery eQuery, String question) {
		// TODO Auto-generated method stub
		return null;
	}

}
