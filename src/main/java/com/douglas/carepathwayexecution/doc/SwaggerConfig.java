package com.douglas.carepathwayexecution.doc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurationSupport {

	  @Bean
	  public Docket api() {
	    return new Docket(DocumentationType.SWAGGER_2)
	        .apiInfo(metaData())
	        .select()
	        .apis(RequestHandlerSelectors.basePackage("com.douglas.carepathwayexecution.web.controller"))
	        .paths(paths())
	        .build();	
	  }
	
	  private ApiInfo metaData() {
	    return new ApiInfoBuilder()
	        .title("Spring Boot REST API")
	        .description("\"Spring Boot REST API for Data Extraction of Care Pathway Attendance\"")
	        .version("1.0.0")
	        .license("Apache License Version 2.0")
	        .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0\"")
	        .build();
	  }
	
//	  @Override
//	  protected void addResourceHandlers(ResourceHandlerRegistry registry) {
//	    registry.addResourceHandler("swagger-ui.html")
//	        .addResourceLocations("classpath:/META-INF/resources/");
//	
//	    registry.addResourceHandler("/webjars/**")
//	        .addResourceLocations("classpath:/META-INF/resources/webjars/");
//	  }
//	  

	private Predicate<String> paths() {
	    return Predicates.and( PathSelectors.regex("/customer.*"),
	    						Predicates.not(PathSelectors.regex("/error.*")));
	}
	
}