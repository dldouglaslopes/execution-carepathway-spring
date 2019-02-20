package com.douglas.carepathwayexecution.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.EQuery;

@Service
public class QStepService {
	@Autowired
	private QCarePathwayService service;
	
	public EQuery recurrentStep(EQuery eQuery, String step) {
		// TODO Auto-generated method stub
		return null;
	}

}
