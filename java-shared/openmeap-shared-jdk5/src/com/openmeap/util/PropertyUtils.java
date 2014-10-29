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

import java.lang.reflect.Method;

/**
 * Utility methods I pulled from JSONObjectBuilder for ParameterMapBuilder
 * 
 * @author schang
 */
public class PropertyUtils {
	
	static public Method setterForGetterMethod(Method getterMethod) {
		
		String setterName = getterMethod.getName();
		if( setterName.startsWith("g") ) {
			setterName = setterName.replaceFirst("g", "s");
		}
		Method setterMethod = null;
		try {
			Class clazz = getterMethod.getDeclaringClass();
			Class returnType = getterMethod.getReturnType();
			setterMethod = clazz.getMethod(setterName,new Class[]{returnType});
		} catch( NoSuchMethodException ite ) {
			// we don't care, here
		}
		return setterMethod;
	}
	
	static public String propertyForGetterMethodName(String methodName) {
		
		String propertyName = methodName;
		if( propertyName.startsWith("get") ) {
			propertyName = propertyName.substring(3);
		}
		String firstLetter = propertyName.substring(0,1);
		if( firstLetter.matches("[A-Z]") ) {
			propertyName = firstLetter.toLowerCase()+propertyName.substring(1);
		}
		return propertyName;
	}
	
	static public boolean isSimpleType(Class returnType) {
		if(returnType.isArray()) {
			return Boolean[].class.isAssignableFrom(returnType) 
				|| Long[].class.isAssignableFrom(returnType) 
				|| Double[].class.isAssignableFrom(returnType)
				|| Integer[].class.isAssignableFrom(returnType)
				|| String[].class.isAssignableFrom(returnType);
		} else {
			return Boolean.class.isAssignableFrom(returnType) 
				|| Long.class.isAssignableFrom(returnType) 
				|| Double.class.isAssignableFrom(returnType)
				|| Integer.class.isAssignableFrom(returnType)
				|| String.class.isAssignableFrom(returnType);
		}
	}
	
	static public Object correctCasting(Class type, Object obj) {
		
		if( type.equals(Long.class) ) {
			return new Long(Long.parseLong(obj.toString()));
		} else if( type.equals(Double.class) ) {
			return Double.valueOf(obj.toString());
		} else if( type.equals(Integer.class) ) {
			return Integer.valueOf(obj.toString());
		} else return obj;
	}
}
