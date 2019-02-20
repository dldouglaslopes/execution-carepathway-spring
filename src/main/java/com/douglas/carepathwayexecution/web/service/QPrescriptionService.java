package com.douglas.carepathwayexecution.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QueryMetamodel.EQuery;

@Service
public class QPrescriptionService {
	@Autowired
	private QCarePathwayService service;

	public EQuery recurrentPrescription(EQuery eQuery, String prescription) {
		// TODO Auto-generated method stub
		return null;
	}

}
