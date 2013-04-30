package uk.org.taverna.databundle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
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

	private static final String APPLICATION_VND_WF4EVER_ROBUNDLE_ZIP = "application/vnd.wf4ever.robundle+zip";
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final String INPUTS = "inputs";
	private static final String OUTPUTS = "outputs";

	public static Path createDataBundle() throws IOException {

		// Create ZIP file as http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
		
		Path dataBundle = Files.createTempFile("databundle", ".zip");
		
		FileSystem fs = createFSfromZip(dataBundle);
//		FileSystem fs = createFSfromJar(dataBundle);
		return fs.getRootDirectories().iterator().next();
		//return Files.createTempDirectory("databundle");
	}
	
	public static Path openDataBundle(Path zip) throws IOException {
		FileSystem fs = FileSystems.newFileSystem(zip, null);
		return fs.getRootDirectories().iterator().next();
	}

	protected static FileSystem createFSfromZip(Path dataBundle)
			throws FileNotFoundException, IOException {
		ZipOutputStream out = new ZipOutputStream(
			    new FileOutputStream(dataBundle.toFile()));
		addMimeTypeToZip(out);
		out.close();
		return FileSystems.newFileSystem(dataBundle, null);
	}

	private static void addMimeTypeToZip(ZipOutputStream out) throws IOException {
		// FIXME: Make the mediatype a parameter
		byte[] bytes = APPLICATION_VND_WF4EVER_ROBUNDLE_ZIP.getBytes(UTF8);
		
		// We'll have to do the mimetype file quite low-level 
		// in order to ensure it is STORED and not COMPRESSED
		
		ZipEntry entry = new ZipEntry("mimetype");
		entry.setMethod(ZipEntry.STORED);
		entry.setSize(bytes.length);
		CRC32 crc = new CRC32();
		crc.update(bytes);
		entry.setCrc(crc.getValue());
		
		out.putNextEntry(entry);
		out.write(bytes);
		out.closeEntry();
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

	public static Path getPort(Path map, String portName) throws IOException {
		Files.createDirectories(map);
		return map.resolve(portName);
	}

	public static void setStringValue(Path path, String string) throws IOException {		
		Files.write(path, string.getBytes(UTF8));
	}

	public static String getStringValue(Path path) throws IOException {
		return new String(Files.readAllBytes(path), UTF8);
	}

	public static void createList(Path path) throws IOException {
		Files.createDirectories(path);
	}

	public static Path newListItem(Path list) throws IOException {
		long max = -1L;
		createList(list);
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(list)) {
			for (Path entry : ds) {
				String name = filenameWithoutExtension(entry);
				//System.out.println(name);
				try {
					long entryNum = Long.parseLong(name);
					if (entryNum > max) {
						max = entryNum;
					}
				} catch (NumberFormatException ex) {
				}
			}
		} catch (DirectoryIteratorException ex) {
			throw ex.getCause();
		}
		return list.resolve(Long.toString(max+1));
	}

	protected static String filenameWithoutExtension(Path entry) {
		String fileName = entry.getFileName().toString();
		int lastDot = fileName.lastIndexOf(".");
		if (lastDot < 0) {
			return fileName;
		}
		return fileName.substring(0, lastDot);
	}

	public static boolean isList(Path list) {
		return Files.isDirectory(list);
	}

	public static List<Path> getList(Path list) throws IOException {
		List<Path> paths = new ArrayList<>();
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(list)) {
			for (Path entry : ds) {
				String name = filenameWithoutExtension(entry);
				//System.out.println(name);
				try {
					int entryNum = Integer.parseInt(name);
					while (paths.size() <= entryNum) {
						// Fill any gaps
						paths.add(null);
					}
					// NOTE: Don't use add() as these could come in any order!
					paths.set(entryNum, entry);					
				} catch (NumberFormatException ex) {
				}
			}
		} catch (DirectoryIteratorException ex) {
			throw ex.getCause();
		}
		return paths;		
	}

	public static void closeAndSaveDataBundle(Path dataBundle, Path destination) throws IOException {
		Path zipPath = closeDataBundle(dataBundle);
		Files.copy(zipPath, destination);		
	}

	public static Path closeDataBundle(Path dataBundle) throws IOException {
		URI uri = dataBundle.getRoot().toUri();
		dataBundle.getFileSystem().close();
		String s = uri.getSchemeSpecificPart();
		if (! s.endsWith("!/")) { // sanity check
			throw new IllegalStateException("Can't parse JAR URI: " + uri);
		}
		URI zip = URI.create(s.substring(0, s.length()-2));
		return Paths.get(zip); // Look up our path
	}

	
}
