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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.openmeap.constants.FormConstants;

/**
 * Utility class for JavaScript compatible UTF-8 encoding and decoding.
 * 
 * @see http://stackoverflow.com/questions/607176/java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-output
 * @author John Topley 
 */
public class URIEncodingUtil
{
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
		if (s == null)
		{
			return null;
		}

		String result = null;

		try
		{
			result = URLDecoder.decode(s, FormConstants.CHAR_ENC_DEFAULT);
		}

		// This exception should never occur.
		catch (UnsupportedEncodingException e)
		{
			result = s;  
		}

		return result;
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
		String result = null;

		try
		{
			result = URLEncoder.encode(s, FormConstants.CHAR_ENC_DEFAULT)
			.replaceAll("\\+", "%20")
			.replaceAll("\\%21", "!")
			.replaceAll("\\%27", "'")
			.replaceAll("\\%28", "(")
			.replaceAll("\\%29", ")")
			.replaceAll("\\%7E", "~");
		}

		// This exception should never occur.
		catch (UnsupportedEncodingException e)
		{
			result = s;
		}

		return result;
	}  

	/**
	 * Private constructor to prevent this class from being instantiated.
	 */
	private URIEncodingUtil()
	{
		super();
	}
}
