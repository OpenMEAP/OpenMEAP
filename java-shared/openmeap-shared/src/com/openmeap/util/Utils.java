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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

import com.openmeap.digest.DigestException;
import com.openmeap.digest.DigestInputStreamFactory;
import com.openmeap.digest.DigestInputStream;

final public class Utils {
	
	private static String TRUE = "true";
	
	private Utils() {}
	
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
		StringBuffer text = new StringBuffer();
		InputStreamReader scanner = new InputStreamReader(inputStream, fEncoding);
		try {
			char[] chars = new char[1024];
			int numRead;
			while((numRead=scanner.read(chars))!=(-1)) {
				text.append(Utils.arraySlice(chars, new char[numRead], 0));
			} 
		}
		finally{
			scanner.close();
		}
		return text.toString();
    }
	
	public static String readLine(InputStream is, String encoding) throws IOException {
		StringBuffer text = new StringBuffer();
		int read, lastRead;
		while( (read=is.read())!=(-1) ) {
			if(read=='\r') {
				lastRead = read;
				read=is.read();
				if(read==(-1)) {
					text.append((char)lastRead);
				} else if(read=='\n') {
					break;
				} else {
					text.append((char)lastRead);
					text.append((char)read);
				}
			} else {
				text.append((char)read);
			}
			lastRead = read;
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
    
    public static String createParamsString(Hashtable params) throws UnsupportedEncodingException {
	    if( params==null ) {
			return null;
		}
	    StringBuffer data = new StringBuffer();
		boolean firstPass = true;
		Enumeration entriesIter = params.keys();
		while( entriesIter.hasMoreElements() ) {
			String key = (String)entriesIter.nextElement();
			String value = params.get(key).toString();
			if( !firstPass ) {
				data.append("&");
			} else firstPass=false;
			data.append(URIEncodingUtil.encodeURIComponent(key));
			data.append("=");
			data.append(URIEncodingUtil.encodeURIComponent(value));
		}
		return data.toString();
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
    public static String replaceFields(Hashtable variables, String content) {
    	Enumeration variablesIter = variables.keys();
		while( variablesIter.hasMoreElements() ) {
			String key = (String)variablesIter.nextElement();
			Object variable = (String)variables.get(key);
			content = StringUtils.replaceAll(content,"${"+key+"}", (String) variable);
		}
		return content;
	}
    
    public static Object[] arraySlice(Object[] source, Object[] dest, int startIdx) {
    	for(int i=startIdx; i<dest.length; i++) {
    		dest[i]=source[i];
    	}
    	return dest;
    }
    public static char[] arraySlice(char[] source, char[] dest, int startIdx) {
    	for(int i=startIdx; i<dest.length; i++) {
    		dest[i]=source[i];
    	}
    	return dest;
    }
    
    public static String hashInputStream(String hashName, InputStream is) throws DigestException {
    	DigestInputStream md = DigestInputStreamFactory.getDigestInputStream(hashName);
    	md.setInputStream(is);
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
	
	final static public boolean parseBoolean(String string) {
		return string.equalsIgnoreCase(TRUE);
	}
}
