/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2012 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the LGPLv3                                                #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU Lesser General Public License as published #
 #    by the Free Software Foundation, either version 3 of the License, or     #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU Lesser General Public License for more details.                      #
 #                                                                             #
 #    You should have received a copy of the GNU Lesser General Public License #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.openmeap.constants.FormConstants;

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
    
    public static long pipeInputStreamIntoOutputStream(InputStream inputStream, OutputStream outputStream) throws IOException {
    	byte[] bytes = new byte[1024];
        int count = inputStream.read(bytes);
        long totalCount = 0;
        while( count!=(-1) ) {
        	outputStream.write(bytes,0,count);
        	totalCount += count = inputStream.read(bytes);
        }
        return totalCount;
    }
    
    public static String createParamsString(Map params) throws UnsupportedEncodingException {
	    if( params==null ) {
			return null;
		}
		StringBuilder data = new StringBuilder();
		boolean firstPass = true;
		Iterator entriesIter = params.entrySet().iterator();
		while( entriesIter.hasNext() ) { 
			Map.Entry ent = (Entry) entriesIter.next();
			if( !firstPass ) {
				data.append("&");
			} else firstPass=false;
			data.append(URLEncoder.encode((String)ent.getKey(), FormConstants.CHAR_ENC_DEFAULT));
			data.append("=");
			data.append(URLEncoder.encode(ent.getValue().toString(), FormConstants.CHAR_ENC_DEFAULT));
		}
		return data.toString();
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
    public static String replaceFields(Map variables, String content) {
    	Iterator variablesIter = variables.entrySet().iterator();
		while( variablesIter.hasNext() ) {
			Map.Entry variable = (Entry) variablesIter.next();
			content = content.replaceAll("\\$\\{"+variable.getKey()+"\\}", (String) variable.getValue());
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
    
    private static char[] hexChars = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	public static String byteArray2Hex(byte[] bytes){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++){
			int highBits = (bytes[i]>>4) & 0xf;
			int lowBits = bytes[i] & 0xf;
			sb.append(""+hexChars[highBits]+hexChars[lowBits]);
		}
		return sb.toString();
	}
}
