package uk.org.taverna.databundle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility functions for dealing with data bundles.
 * <p>
 * The style of using this class is similar to that of {@link Files}. In fact, a
 * data bundle is implemented as a set of {@link Path}s.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class DataBundles {

	private static final String INPUTS = "inputs";
	private static final String OUTPUTS = "outputs";

	public static Path createDataBundle() throws IOException {

		// Create ZIP file as http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
		
		Path dataBundle = Files.createTempFile("databundle", ".robundle.zip");
		
		FileSystem fs = createFSfromZip(dataBundle);
//		FileSystem fs = createFSfromJar(dataBundle);		
		return fs.getRootDirectories().iterator().next();
		//return Files.createTempDirectory("databundle");
	}

	protected static FileSystem createFSfromZip(Path dataBundle)
			throws FileNotFoundException, IOException {
		ZipOutputStream out = new ZipOutputStream(
			    new FileOutputStream(dataBundle.toFile()));
		ZipEntry mimeTypeEntry = new ZipEntry("mimetype");
		out.putNextEntry(mimeTypeEntry);
		out.closeEntry();
		out.close();
		return FileSystems.newFileSystem(dataBundle,  null);
	}

	protected static FileSystem createFSfromJar(Path path)
			throws IOException {
		Files.deleteIfExists(path);
		URI uri;
		try {
			uri = new URI("jar", path.toUri().toASCIIString(), null);
		} catch (URISyntaxException e) {
			throw new IOException("Can't make jar: URI using " + path.toUri());
		}		
		Map<String, String> env = new HashMap<>();
		env.put("create", "true");
		return FileSystems.newFileSystem(uri, env);
	}

	public static Path getInputs(Path dataBundle) throws IOException {
		Path inputs = dataBundle.resolve(INPUTS);
		Files.createDirectories(inputs);
		return inputs;
	}

	public static Path getOutputs(Path dataBundle) throws IOException {
		Path inputs = dataBundle.resolve(OUTPUTS);
		Files.createDirectories(inputs);
		return inputs;
	}

	public static boolean hasInputs(Path dataBundle) {
		Path inputs = dataBundle.resolve(INPUTS);
		return Files.isDirectory(inputs);
	}

	public static boolean hasOutputs(Path dataBundle) {
		Path outputs = dataBundle.resolve(OUTPUTS);
		return Files.isDirectory(outputs);
	}
}
