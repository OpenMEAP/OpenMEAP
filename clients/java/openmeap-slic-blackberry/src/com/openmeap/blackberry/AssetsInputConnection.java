package com.openmeap.blackberry;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.InputConnection;

public class AssetsInputConnection implements InputConnection {

	
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
		return System.class.getResourceAsStream(path);
	}

}
