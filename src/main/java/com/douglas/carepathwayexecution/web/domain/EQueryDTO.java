package com.douglas.carepathwayexecution.web.domain;

import org.springframework.data.mongodb.core.mapping.Document;

import QueryMetamodel.EAttribute;
import QueryMetamodel.EMethod;

@Document
public class EQueryDTO {
	private EMethod method;
	private EAttribute attribute;
	
	public EQueryDTO() {}
	
	public EQueryDTO(EMethod method, EAttribute attribute) {
		super();
		this.attribute = attribute;
		this.method = method;
	}	

	public EMethod getMethod() {
		return method;
	}

	public void setMethod(EMethod method) {
		this.method = method;
	}
	
	public EAttribute getAttribute() {
		return attribute;
	}

	public void setAttribute(EAttribute attribute) {
		this.attribute = attribute;
	}
}
