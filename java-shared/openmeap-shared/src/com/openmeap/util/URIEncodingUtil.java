/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Rob Thacher                                 #
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

import java.io.UnsupportedEncodingException;

import com.openmeap.constants.FormConstants;

/**
 * Utility class for JavaScript compatible UTF-8 encoding and decoding.
 * 
 * @author schang 
 */
final public class URIEncodingUtil
{
	private URIEncodingUtil() {}
	
	private final static String PERCENT = "%";
	
	/**
	 * Decodes the passed UTF-8 String using an algorithm that's compatible with
	 * JavaScript's <code>decodeURIComponent</code> function. Returns
	 * <code>null</code> if the String is <code>null</code>.
	 *
	 * @param s The UTF-8 encoded String to be decoded
	 * @return the decoded String
	 */
	public static String decodeURIComponent(String s)
	{
		if (s == null) {
			return null;
		}
		
		String result = s;
		
		result = StringUtils.replaceAll(result, "+", "%20");
		result = StringUtils.replaceAll(result, "!", "%21");
		result = StringUtils.replaceAll(result, "'", "%27");
		result = StringUtils.replaceAll(result, "(", "%28");
		result = StringUtils.replaceAll(result, ")", "%29");
		result = StringUtils.replaceAll(result, "~", "%7E");
		
		StringBuffer sb = new StringBuffer();
		int d = result.length();
		for(int i=0; i<d; i++ ) {
			char thisChar = result.charAt(i);
			if(thisChar=='%') {
				if(i+2>=d) {
					throw new URIEncodingException("Expecting an encoded char at index "+i);
				}
				char[] chars = new char[]{result.charAt(i+1),result.charAt(i+2)};
				i+=2;
				char value = CharUtils.toChar(new String(chars));
				sb.append(value);
			} else {
				sb.append(thisChar);
			}
		}
		return sb.toString();
	}

	/**
	 * Encodes the passed String as UTF-8 using an algorithm that's compatible
	 * with JavaScript's <code>encodeURIComponent</code> function. Returns
	 * <code>null</code> if the String is <code>null</code>.
	 * 
	 * @param s The String to be encoded
	 * @return the encoded String
	 */
	public static String encodeURIComponent(String s)
	{
		StringBuffer sb = new StringBuffer();
		int d = s.length();
		for(int i=0; i<d; i++ ) {
			char thisChar = s.charAt(i);
			if(Character.isDigit(thisChar)
					||Character.isLowerCase(thisChar)
					||Character.isUpperCase(thisChar)) {
				sb.append(thisChar);
			} else {
				byte[] thisByte = CharUtils.bytesValue(thisChar);
				sb.append(PERCENT);
				sb.append(Utils.byteArray2Hex(thisByte));
			}
		}
		
		String result = sb.toString();
		result = StringUtils.replaceAll(result, "%20", "+");
		result = StringUtils.replaceAll(result, "%21", "!");
		result = StringUtils.replaceAll(result, "%27", "'");
		result = StringUtils.replaceAll(result, "%28", "(");
		result = StringUtils.replaceAll(result, "%29", ")");
		result = StringUtils.replaceAll(result, "%7E", "~");

		return result;
	}
}
