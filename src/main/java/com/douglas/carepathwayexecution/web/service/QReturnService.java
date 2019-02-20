package com.douglas.carepathwayexecution.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.EQuery;

@Service
public class QReturnService {
	@Autowired
	private QCarePathwayService service;

	public EQuery returnPatient(EQuery eQuery, String codePatient) {
		// TODO Auto-generated method stub
		return null;
	}

}
