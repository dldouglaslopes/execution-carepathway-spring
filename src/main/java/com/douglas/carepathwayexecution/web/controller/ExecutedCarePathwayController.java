package com.douglas.carepathwayexecution.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import QueryMetamodel.EMethod;
import QueryMetamodel.Method;
import QueryMetamodel.Query_metamodelFactory;

@Controller
@RequestMapping("/")
public class ExecutedCarePathwayController {
	
	@RequestMapping(value = { "/execution" }, method = RequestMethod.GET)
	public String selectOptions(Model model) {		
	    Form form = new Form();
	    model.addAttribute("form", form);
	
	    List<EMethod> methodNames = new ArrayList<>();
	    List<Method> methodEnums = Method.VALUES;
	    
	    for (Method method : methodEnums) {
			EMethod eMethod = Query_metamodelFactory.eINSTANCE.createEMethod();
			eMethod.setName(method);
			
			methodNames.add(eMethod);
		}
	    
	    model.addAttribute("methodNames", methodNames);
	    
	    return "executionCarePathway";
	}
}

class Form {
	 
	private Long methodId;
 
    public Long getMethodId() {
        return methodId;
    }
 
    public void setMethodId(Long methodId) {
        this.methodId = methodId;
    }
}