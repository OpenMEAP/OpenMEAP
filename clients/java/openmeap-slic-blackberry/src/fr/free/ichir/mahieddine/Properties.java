package fr.free.ichir.mahieddine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.io.LineReader;

/**
 * Properties loader from input stream. Properties are of the form "key=value".
 * One can use its own field separator via the {@link #Properties(String)} constructor, overriding the default "=" separator.
 * 
 * <h2>Usage</h2>
 * 
 * <pre>
 * Properties properties = new Properties();
 * try {
 * 		Class classs = Class.forName(this.getClass().getName());
 * 		InputStream stream = classs.getResourceAsStream("/file.properties");
 * 		properties.load(stream);
 * } catch (ClassNotFoundException e) {
 * 		e.printStackTrace();
 * } catch (IOException e) {
 * 		e.printStackTrace();
 * }
 * 
 * properties.getProperties();
 * 
 * </pre>
 * 
 * @author mahieddine.ichir
 * @version 1.0.0
 */
public class Properties {

	/**
	 * Table of loaded properties.
	 */
	private Hashtable hashtable;

	/**
	 * Fields separator.
	 */
	private String separator;

	/**
	 * Default fields separator.
	 */
	private static final String DEFAULT_SEPARATOR = "=";

	/**
	 * Default separator "=" constructor.
	 */
	public Properties() {
		this(DEFAULT_SEPARATOR);
	}

	/**
	 * Constructor with input fields separator.
	 * @param separator input separator
	 */
	public Properties(String separator) {
		hashtable = new Hashtable();
		this.separator = separator;
	}

	/**
	 * Parse input stream retrieving data.
	 * @param stream input stream
	 * @throws IOException on stream read error or on stream end
	 */
	public final void load(InputStream stream) throws IOException {
		String line;
		LineReader reader = new LineReader(stream);
		byte[] data = null;
		
		// first line
		data = reader.readLine();
		line = new String(data).trim();
		
		int index;

		// remaining lines
		while (reader.lengthUnreadData() > 0) {
			data = reader.readLine();
			line = new String(data).trim();
			if ( line.length()==0 ) {
				continue;
			}
			if (line.startsWith("#")) {
				// a comment
				continue;
			}
			index = line.indexOf(separator);
			hashtable.put(line.substring(0, index), line.substring(index+1));
		}
	}

	/**
	 * @return loaded properties
	 */
	public final Hashtable getProperties() {
		return hashtable;
	}

	/**
	 * {@inheritDoc}}
	 */
	public String toString() {
		Enumeration keys = hashtable.keys();
		String res = "";
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			res += key + separator + hashtable.get(key) + "\n";
		}
		return res;
	}
}

