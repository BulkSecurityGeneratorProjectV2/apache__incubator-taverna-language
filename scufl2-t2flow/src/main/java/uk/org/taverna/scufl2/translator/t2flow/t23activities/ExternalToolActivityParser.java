package uk.org.taverna.scufl2.translator.t2flow.t23activities;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import uk.org.taverna.scufl2.api.common.URITools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.io.ReaderException;
import uk.org.taverna.scufl2.api.property.PropertyLiteral;
import uk.org.taverna.scufl2.api.property.PropertyObject;
import uk.org.taverna.scufl2.api.property.PropertyResource;
import uk.org.taverna.scufl2.translator.t2flow.T2FlowParser;
import uk.org.taverna.scufl2.translator.t2flow.defaultactivities.AbstractActivityParser;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.BeanshellConfig;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.ConfigBean;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.ExternalToolConfig;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.Group;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.ScriptInputStatic;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.UsecaseConfig;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.UsecaseDescription;

public class ExternalToolActivityParser extends AbstractActivityParser {

	private static final String EXTERNALTOOLACTIVITY_XSD = "../xsd/externaltoolactivity.xsd";

	private static URI usecaseActivityRavenUri = T2FlowParser.ravenURI
			.resolve("net.sf.taverna.t2.activities/usecase-activity/");

	private static String usecaseActivityClass = "net.sf.taverna.t2.activities.usecase.UseCaseActivity";

	private static URI externalToolRavenUri = T2FlowParser.ravenURI
			.resolve("net.sf.taverna.t2.activities/external-tool-activity/");

	private static String externalToolClass = "net.sf.taverna.t2.activities.externaltool.ExternalToolActivity";

	public static URI ACTIVITY_URI = URI
			.create("http://ns.taverna.org.uk/2010/activity/tool");
	
	public static URI DC = URI
			.create("http://purl.org/dc/elements/1.1/");

	@Override
	public boolean canHandlePlugin(URI activityURI) {
		String activityUriStr = activityURI.toASCIIString();
		if (activityUriStr.startsWith(usecaseActivityRavenUri.toASCIIString())
				&& activityUriStr.endsWith(usecaseActivityClass)) {
			return true;
		}
		return activityUriStr.startsWith(externalToolRavenUri.toASCIIString())
				&& activityUriStr.endsWith(externalToolClass);
	}

	@Override
	public List<URI> getAdditionalSchemas() {
		URL externalToolXsd = getClass().getResource(EXTERNALTOOLACTIVITY_XSD);
		try {
			return Arrays.asList(externalToolXsd.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Can't find external tool schema " + externalToolXsd);
		}
	}
	
	@Override
	public URI mapT2flowRavenIdToScufl2URI(URI t2flowActivity) {
		return ACTIVITY_URI;
	}
	
	private static URITools uriTools = new URITools();

	@Override
	public Configuration parseConfiguration(T2FlowParser t2FlowParser,
			ConfigBean configBean) throws ReaderException {
		
		ExternalToolConfig externalToolConfig = null;
		UsecaseConfig usecaseConfig = null;
		
		try { 
			externalToolConfig = unmarshallConfig(t2FlowParser,
					configBean, "xstream", ExternalToolConfig.class);
		} catch (ReaderException ex) {
			usecaseConfig = unmarshallConfig(t2FlowParser,
					configBean, "xstream", UsecaseConfig.class);
		}
		
		
		Configuration configuration = new Configuration();
		configuration.setParent(getParserState().getCurrentProfile());
		PropertyResource configResource = configuration.getPropertyResource();
		configResource.setTypeURI(ACTIVITY_URI.resolve("#Config"));
		
		if (usecaseConfig != null && usecaseConfig.getRepositoryUrl() != null) {
			URI repositoryUri = URI.create(usecaseConfig.getRepositoryUrl());
			URI usecase = repositoryUri.resolve("#" + uriTools.validFilename(usecaseConfig.getUsecaseid()));
			configResource.addPropertyReference(ACTIVITY_URI.resolve("#toolId"), usecase);
		} else if (externalToolConfig  != null && externalToolConfig.getRepositoryUrl() != null) {
			URI repositoryUri = URI.create(externalToolConfig.getRepositoryUrl());
			URI usecase = repositoryUri.resolve("#" + uriTools.validFilename(externalToolConfig.getExternaltoolid()));
			configResource.addPropertyReference(ACTIVITY_URI.resolve("#toolId"), usecase);
		}
		
		if (externalToolConfig  != null) {
			configResource.addProperty(ACTIVITY_URI.resolve("#edited"), 
					new PropertyLiteral(externalToolConfig.isEdited()));
			
			configResource.addPropertyReference(ACTIVITY_URI.resolve("#mechanismType"), 
					ACTIVITY_URI.resolve("#" + uriTools.validFilename(externalToolConfig.getMechanismType())));
			
			configResource.addPropertyAsString(ACTIVITY_URI.resolve("#mechanismName"), 
					externalToolConfig.getMechanismName());
			configResource.addProperty(ACTIVITY_URI.resolve("#mechanismXml"),
					new PropertyLiteral(externalToolConfig.getMechanismName(), PropertyLiteral.XML_LITERAL));

			configResource.addProperty(ACTIVITY_URI.resolve("#toolDescription"), 
					parseToolDescription(externalToolConfig.getUseCaseDescription()));

			configResource.addProperty(ACTIVITY_URI.resolve("#invocationGroup"), 
					parseGroup(externalToolConfig.getGroup()));
			
			
		}
		
		return configuration;
	}

	protected PropertyObject parseToolDescription(
			UsecaseDescription toolDesc) {
		PropertyResource propertyResource = new PropertyResource();
		propertyResource.setTypeURI(ACTIVITY_URI.resolve("#ToolDescription"));
		
		propertyResource.addPropertyAsString(ACTIVITY_URI.resolve("#usecaseid"), 
				toolDesc.getUsecaseid());
		
		propertyResource.addPropertyAsString(ACTIVITY_URI.resolve("#group"), 
				toolDesc.getGroup());
		
		propertyResource.addPropertyAsString(DC.resolve("#description"), 
				toolDesc.getDescription());
		
		propertyResource.addPropertyAsString(ACTIVITY_URI.resolve("#command"), 
				toolDesc.getCommand());
		propertyResource.addProperty(ACTIVITY_URI.resolve("#preparingTimeoutInSeconds"), 
				new PropertyLiteral(toolDesc.getPreparingTimeoutInSeconds()));
		propertyResource.addProperty(ACTIVITY_URI.resolve("executionTimeoutInSeconds"), 
				new PropertyLiteral(toolDesc.getExecutionTimeoutInSeconds()));
		
		
		// Ignoring tags, REs, queue__preferred, queue__deny

		
		// static inputs
		for (ScriptInputStatic inputStatic : toolDesc.getStaticInputs().getDeUniLuebeckInbKnowarcUsecasesScriptInputStatic()) {
			//
		}
		// Inputs
		// Outputs
		
		propertyResource.addProperty(ACTIVITY_URI.resolve("#includeStdIn"), 
				new PropertyLiteral(toolDesc.isIncludeStdIn()));
		propertyResource.addProperty(ACTIVITY_URI.resolve("#includeStdOut"), 
				new PropertyLiteral(toolDesc.isIncludeStdOut()));
		propertyResource.addProperty(ACTIVITY_URI.resolve("#includeStdErr"), 
				new PropertyLiteral(toolDesc.isIncludeStdErr()));
		
		
		return propertyResource;
	}

	protected PropertyObject parseGroup(Group group) {
		PropertyResource propertyResource = new PropertyResource();
		propertyResource.setTypeURI(ACTIVITY_URI.resolve("#InvocationGroup"));
		return propertyResource;
	}
}