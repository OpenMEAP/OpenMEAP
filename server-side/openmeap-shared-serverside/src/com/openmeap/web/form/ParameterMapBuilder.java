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

package com.openmeap.web.form;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.openmeap.util.ParameterMapUtils;
import com.openmeap.util.PropertyUtils;

public class ParameterMapBuilder {
	
	private Boolean mapValuesAsStrings = false;
	private Boolean useParameterMapUtilsFirstValue = true;
	private Boolean noNulls = true;

	public void toParameters(Map<String,Object> map, Object obj) throws ParameterMapBuilderException {
		toParameters(map, obj, "");
	}
		
	public void toParameters(Map<String,Object> map, Object obj, String prefix) throws ParameterMapBuilderException {
		
		for( Method method : obj.getClass().getMethods() ) {
			Parameter param = method.getAnnotation(Parameter.class);
			if(param!=null) {
				String paramName = prefix+formNameForMethod(param,method);
				Validation validation = param.validation();
				try {
					Object value = method.invoke(obj);
					if( value!=null && ! PropertyUtils.isSimpleType(value.getClass()) ) {
						throw new ParameterMapBuilderException("Expecting a simple type");
					}
					if( value==null && noNulls ) {
						continue;
					}
					map.put(paramName, mapValuesAsStrings?value.toString():value);
					if( validation.verify() ) {
						map.put(paramName+validation.verifySuffix(), mapValuesAsStrings?value.toString():value);
					}
				} catch(Exception e) {
					throw new ParameterMapBuilderException(e);
				}
			}
		}
	}
	
	public void fromParameters(Object obj, Map<String,Object> parameters) throws ParameterMapBuilderException {
		fromParameters(obj,parameters,"");
	}
	
	public void fromParameters(Object obj, Map<String,Object> parameters, String prefix) throws ParameterMapBuilderException {
		
		Class clazz = obj.getClass();
		for( Map.Entry<String,Object> entry : parameters.entrySet() ) {
			
			String formName = entry.getKey().replaceFirst(prefix, "");
			if( entry.getValue()==null || (String.class.isAssignableFrom(entry.getValue().getClass()) && StringUtils.isBlank((String) entry.getValue())) ) {
				continue;
			}
			
			Method getterMethod = methodForFormName(clazz,formName);
			if( getterMethod==null ) {
				continue;
			}
			Method setterMethod = PropertyUtils.setterForGetterMethod(getterMethod);
			Constructor constructor = null;
			
			Class<?> valueClass = null;
			try {
				valueClass = getterMethod.getReturnType();
				constructor = valueClass.getConstructor(valueClass);
			} catch(Exception e) {
				;//throw new ParameterMapBuilderException(e);
			}
			
			Object value = null;
			try {
				if(this.useParameterMapUtilsFirstValue) {
					value = ParameterMapUtils.firstValue(formName, (Map)parameters);
				} else {
					value = parameters.get(formName);
				}
				if( value!=null ) {
					if( constructor!=null ) {
						value = constructor.newInstance(value);
					} else {
						value = PropertyUtils.correctCasting(valueClass, value);
					}
				}
			} catch (Exception e) {
				throw new ParameterMapBuilderException(e);
			}
			
			try {
				setterMethod.invoke(obj, value);
			} catch (Exception e) {
				throw new ParameterMapBuilderException(e);
			}
		}
	}
	
	private String formNameForMethod(Parameter param, Method method) {
		
		String paramName = null; 
		if( StringUtils.isNotBlank(param.value()) ) {
			paramName = param.value();
		} else {
			paramName = PropertyUtils.propertyForGetterMethodName(method.getName());
		}
		return paramName;
	}
	
	private Method methodForFormName(Class<?> clazz, String formName) {
		
		for( Method method : clazz.getMethods() ) {
			Parameter param = method.getAnnotation(Parameter.class);
			if(param!=null) {
				if( formNameForMethod(param,method).equals(formName) ) {
					return method;
				}
			}
		}
		return null;
	}

	public Boolean getMapValuesAsStrings() {
		return mapValuesAsStrings;
	}
	public void setMapValuesAsStrings(Boolean mapValuesAsStrings) {
		this.mapValuesAsStrings = mapValuesAsStrings;
	}
	
	public Boolean getUseParameterMapUtilsFirstValue() {
		return useParameterMapUtilsFirstValue;
	}
	public void setUseParameterMapUtilsFirstValue(
			Boolean useParameterMapUtilsFirstValue) {
		this.useParameterMapUtilsFirstValue = useParameterMapUtilsFirstValue;
	}

	public Boolean getNoNulls() {
		return noNulls;
	}
	public void setNoNulls(Boolean noNulls) {
		this.noNulls = noNulls;
	}
}
