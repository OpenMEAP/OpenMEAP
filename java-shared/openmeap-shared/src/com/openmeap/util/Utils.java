/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Formatter;
import java.util.Scanner;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

abstract public class Utils {
	
//	private static Logger logger = LoggerFactory.getLogger(Utils.class);
	
	public static void consumeInputStream(InputStream is) throws IOException {
		byte[] b = new byte[1024];
		while(is.read(b)!=(-1));
	}
	
	/**
	 * A convenience method provided for reading a given input stream into a string.
	 * Replaces line separators with the default for the system.
	 * Always ends the string with a new line.
	 * 
	 * @param inputStream
	 * @param fEncoding
	 * @return
	 * @throws IOException
	 */
    public static String readInputStream(InputStream inputStream, String fEncoding) throws IOException {
      StringBuilder text = new StringBuilder();
      String NL = System.getProperty("line.separator");
      Scanner scanner = new Scanner(inputStream, fEncoding);
      try {
        while (scanner.hasNextLine()){
          text.append(scanner.nextLine() + NL);
        }
      }
      finally{
        scanner.close();
      }
      return text.toString();
    }
    
    public static void pipeInputStreamIntoOutputStream(InputStream inputStream, OutputStream outputStream) throws IOException {
    	byte[] bytes = new byte[1024];
        int count = inputStream.read(bytes);
        while( count!=(-1) ) {
        	outputStream.write(bytes,0,count);
        	count = inputStream.read(bytes);
        }
    }
    
    /**
     * Convenience method to parse an input stream into a document
     * @param inputStream
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document getDocument(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(inputStream);
    }
    
    /**
     * Takes a simple Map<String,String> and replaces all occurrences 
     * of each "${entryKey}" in the template with the corresponding 
     * "entryValue".
     * 
     * @param variables The map of field variable/value pairs
     * @param content The template content to replace fields in
     * @return
     */
    public static String replaceFields(Map<String,String> variables, String content) {
		for( Map.Entry<String,String> variable : variables.entrySet() ) {
			content = content.replaceAll("\\$\\{"+variable.getKey()+"\\}", variable.getValue());
		}
		return content;
	}
    
    public static String hashInputStream(String hashName, InputStream is) throws NoSuchAlgorithmException, IOException {
    	MessageDigest md = MessageDigest.getInstance(hashName);
    	try {
    	  is = new DigestInputStream(is, md);
    	  byte[] bytes = new byte[1024];
    	  while( is.read(bytes)==1024 );
    	}
    	finally {
    	  is.close();
    	}
    	return byteArray2Hex(md.digest());
    }
    
    public static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
