/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
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

import java.util.Map;

/**
 * Helper class to help deal with the HttpServletRequest.getParameterMap() and clones
 *  
 * @author schang
 */
final public class ParameterMapUtils {
	private ParameterMapUtils() {}
	
	/**
	 * A little helper method to work with the parameter map
	 * 
	 * @param name
	 * @param parameterMap
	 * @return null if the string is empty or doesn't exist in the parameter map
	 */
	static public String firstValue(String name, Map<Object,Object> parameterMap) {
		if( notEmpty(name,parameterMap) )
			return ((String[])parameterMap.get(name))[0];
		return null;
	}
	
	static public void setValue(String name, Object value, Map<Object,Object> parameterMap) {
		parameterMap.put(name,new String[]{(String)value});
	}
	
	static public Boolean notEmpty(String name, Map<Object,Object> parameterMap) {
		return parameterMap.get(name)!=null && ((String[])parameterMap.get(name)).length>0 && ((String[])parameterMap.get(name))[0].length()>0;
	}
	
	static public Boolean empty(String name, Map<Object,Object> parameterMap) {
		return parameterMap.get(name)==null || ((String[])parameterMap.get(name)).length==0 || ((String[])parameterMap.get(name))[0].length()==0;
	}
	
	static public Boolean equalsEachOther(String param1, String param2, Map<Object,Object> map ) {
		if( ( notEmpty(param1,map) && empty(param2,map) ) || ( empty(param1,map) && notEmpty(param2,map) ) )
			return Boolean.FALSE;
		return firstValue(param1,map).equals(firstValue(param2,map));
	}
	
	static public String toString(Map<String,String[]> map) {
		StringBuilder sb = new StringBuilder();
		
		boolean first=true;
		for( Map.Entry<String,String[]> entry : map.entrySet() ) {
			
			if( first==false ) {
				sb.append(",");
			} else {
				first=false;
			}
			
			sb.append(entry.getKey()+"={");
			boolean innerFirst = true;
			for( String value : entry.getValue() ) {
				
				if( innerFirst==false ) {
					sb.append(",");
				} else {
					innerFirst=false;
				}
				sb.append(value);
			}
			
			sb.append("}");
		}
		return sb.toString();
	}
}
