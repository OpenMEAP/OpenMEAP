package com.openmeap.blackberry;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;

public class AssetsInputConnection implements InputConnection {

	private static String ASSETS_PREFIX = "assets://";
	private static String FILES_PREFIX = "file://";
	
	private String path;
	
	public AssetsInputConnection(String path) {
		this.path = path;
	}
	
	public void close() throws IOException {
		
	}

	public DataInputStream openDataInputStream() throws IOException {
		return new DataInputStream(openInputStream());
	}

	public InputStream openInputStream() throws IOException {
		if(path.startsWith(ASSETS_PREFIX)) {
			return System.class.getResourceAsStream(path.substring(ASSETS_PREFIX.length()));
		} else if(path.startsWith(FILES_PREFIX)) {
			return Connector.openInputStream(path);
		}
		throw new IOException(path+" is of a schema not handled");
	}

}
