/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

final public class CharUtils {
	private CharUtils() {}
	
	final private static char[] chars = new char[] {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	
	final public static boolean isHexDigit(char thisChar) {
		if( Character.isDigit(thisChar) 
				|| (thisChar<='f' && thisChar>='a')
				|| (thisChar<='F' && thisChar>='A') ) {
			return true;
		} else {
			return false;
		}
	}
	final static public char toChar(String chars) {
		String charsLower = chars.toLowerCase();
		char highChar = charsLower.length()>1?charsLower.charAt(0):'0';
		char lowChar = charsLower.length()>1?charsLower.charAt(1):charsLower.charAt(0);
		int byteHigh = Character.isDigit(highChar) ? highChar-'0' : highChar-'a'+10;
		int byteLow = Character.isDigit(lowChar) ? lowChar-'0' : lowChar-'a'+10;
		return (char) (((byteHigh<<4) | byteLow) & 0xff); 
	}
	
	final static public byte[] bytesValue(char thisChar) {
		StringBuffer sb = new StringBuffer();
		sb.append(thisChar);
		return sb.toString().getBytes();
	}
}
