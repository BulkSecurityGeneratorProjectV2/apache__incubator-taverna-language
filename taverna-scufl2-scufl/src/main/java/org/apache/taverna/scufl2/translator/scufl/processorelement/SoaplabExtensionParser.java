/**
 * 
 */
package org.apache.taverna.scufl2.translator.scufl.processorelement;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author alanrw
 */
public class SoaplabExtensionParser extends AbstractExtensionParser {
	private static final String SOAPLAB_XSD = "/uk/org/taverna/scufl2/translator/scufl/xsd/scufl-soaplab.xsd";

	@Override
	public boolean canHandle(Class<?> c) {
		return c.equals(org.apache.taverna.scufl2.xml.scufl.jaxb.SoaplabwsdlType.class);
	}

	@Override
	public List<URI> getAdditionalSchemas() {
		URL soaplabXsd = getClass().getResource(SOAPLAB_XSD);
		try {
			return Arrays.asList(soaplabXsd.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Can't find Soaplab schema "
					+ soaplabXsd);
		}
	}

	@Override
	public void parseScuflObject(Object o) {
		// TODO write to log?
		System.err.println(this.getClass() + " is not yet implemented");
	}
}
