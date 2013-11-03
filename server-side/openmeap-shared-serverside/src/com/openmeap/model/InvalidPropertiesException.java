/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

package com.openmeap.model;

import java.util.Map;
import java.util.Map.Entry;
import java.lang.reflect.Method;

public class InvalidPropertiesException extends ModelException {
	
	private static final long serialVersionUID = 5513847215741525425L;
	
	Map<Method,String> methodMap;
	private ModelEntity entity;
	
	public <T extends ModelEntity> InvalidPropertiesException(T entity, Map<Method,String> methodMap) {
		this.methodMap = methodMap;
		this.entity = entity;
	}
	
	public ModelEntity getEntity() {
		return entity;
	}
	
	public Map<Method,String> getMethodMap() {
		return methodMap;
	}
	
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("The following methods returned invalid data: ");
		Boolean first = true;
		for( Entry<Method,String> e : methodMap.entrySet() ) {
			String method = e.getKey().getDeclaringClass().getSimpleName()+"."+e.getKey().getName()+"()";
			if( !first ) 
				sb.append(", ");
			sb.append(method+" - "+e.getValue());
			first=false;
		}
		return sb.toString();
	}
}
