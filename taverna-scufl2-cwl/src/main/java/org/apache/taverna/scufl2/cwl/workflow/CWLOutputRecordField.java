package org.apache.taverna.scufl2.cwl.workflow;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CWLOutputRecordField {
	@JsonProperty(required=true)
	String name;
	@JsonProperty(required=true)
	List<String> type; 
	String doc;
	CWLCommandOutputBinding outputBinding;
	
}
