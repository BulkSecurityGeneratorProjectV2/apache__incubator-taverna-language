package uk.org.taverna.scufl2.translator.t2flow.defaultdispatchstack;

import static uk.org.taverna.scufl2.translator.t2flow.T2FlowParser.ravenURI;

import java.math.BigInteger;
import java.net.URI;

import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.io.ReaderException;
import uk.org.taverna.scufl2.translator.t2flow.ParserState;
import uk.org.taverna.scufl2.translator.t2flow.T2FlowParser;
import uk.org.taverna.scufl2.translator.t2flow.defaultactivities.AbstractActivityParser;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.ConfigBean;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.ParallelizeConfig;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ParallelizeParser extends AbstractActivityParser {

	private static URI modelRavenURI = ravenURI
			.resolve("net.sf.taverna.t2.core/workflowmodel-impl/");

	private static String className = "net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize";

	public static URI scufl2Uri = URI
			.create("http://ns.taverna.org.uk/2010/scufl2/taverna/dispatchlayer/Parallelize");

	public static class Defaults {
		public static int maxJobs = 1;
	}

	@Override
	public boolean canHandlePlugin(URI pluginURI) {
		String uriStr = pluginURI.toASCIIString();
		return uriStr.startsWith(modelRavenURI.toASCIIString())
				&& uriStr.endsWith(className);
	}

	@Override
	public URI mapT2flowRavenIdToScufl2URI(URI t2flowActivity) {
		return scufl2Uri;
	}

	@Override
	public Configuration parseConfiguration(T2FlowParser t2FlowParser,
			ConfigBean configBean, ParserState parserState)
			throws ReaderException {
		ParallelizeConfig parallelConfig = unmarshallConfig(t2FlowParser,
				configBean, "xstream", ParallelizeConfig.class);
		Configuration c = new Configuration();
		c.setType(scufl2Uri.resolve("#Config"));

		BigInteger maxJobs = parallelConfig.getMaxJobs();
		if (maxJobs != null && maxJobs.intValue() > 0
				&& maxJobs.intValue() != Defaults.maxJobs) {
			ObjectNode json = (ObjectNode) c.getJson();
			json.put("maxJobs", maxJobs.intValue());
		}
		return c;
	}
}