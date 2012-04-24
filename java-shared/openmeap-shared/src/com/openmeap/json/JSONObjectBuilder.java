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

package com.openmeap.json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.Enum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openmeap.util.PropertyUtils;

/**
 * Converts an object hierarchy into a JSON representation.
 * Only looks at the JSONProperty annotated getter-methods.
 * Does not support collections, only statically-typed arrays.
 * Assumes camel-case is desired.
 * Assumes the objects are instantiable without constructor arguments.
 * 
 * @author schang
 */
public class JSONObjectBuilder {
	
	public Object fromJSON(JSONObject jsonObj, Object rootObject) throws JSONException {
		
		if( jsonObj==null ) {
			return null;
		}
		if( ! HasJSONProperties.class.isAssignableFrom(rootObject.getClass()) ) {
			throw new RuntimeException("The rootObject being converted to JSON must implement the HashJSONProperties interface.");
		}		
		JSONProperty[] properties = ((HasJSONProperties)rootObject).getJSONProperties();
		for( int jsonPropertyIdx=0; jsonPropertyIdx<properties.length; jsonPropertyIdx++ ) {
			JSONProperty property = properties[jsonPropertyIdx];
			Method getterMethod;
			try {
				getterMethod = rootObject.getClass().getMethod(property.getGetterName(),null);
			} catch (Exception e1) {
				throw new JSONException(e1);
			} 
			Class returnType = getterMethod.getReturnType();
			Method setterMethod = PropertyUtils.setterForGetterMethod(getterMethod);
			String propertyName = PropertyUtils.propertyForGetterMethodName(getterMethod.getName());
			try {
				if( setterMethod!=null ) {
					
					Object value = null;
					try {
						
						value = jsonObj.get(propertyName);
					} catch( JSONException e ) {
						
						continue;
					}
					if( value == JSONObject.NULL ) {
						
						continue;
					}
					if( Enum.class.isAssignableFrom(returnType) ) {
						
						Class e = (Class)returnType; 
						try {
							setterMethod.invoke(rootObject,new Object[]{returnType.getDeclaredField((String)value).get(null)});
						} catch (Exception p) {
							throw new JSONException(p);
						}
					} else if( value instanceof JSONArray && List.class.isAssignableFrom(returnType) ) {
						
						JSONArray array = (JSONArray)value;
						List list = new ArrayList();
						Class containedType = property.getContainedType();
						for( int i=0; i<array.length(); i++ ) {
							Object obj = array.get(i);
							if( obj instanceof JSONObject ) {
								Object newObj = (Object)containedType.newInstance();
								list.add(fromJSON((JSONObject)obj,newObj));
							} else {
								list.add(obj);
							}
						}
						setterMethod.invoke(rootObject,new Object[] {list});
					} else if( value instanceof JSONArray && returnType.isArray() ) {
						
						JSONArray array = (JSONArray)value;
						List list = new ArrayList();
						for( int i=0; i<array.length(); i++ ) {
							Object obj = array.get(i);
							if( obj instanceof JSONObject ) {
								Object newObj = (Object)returnType.newInstance();
								list.add(fromJSON((JSONObject)obj,newObj));
							} else {
								list.add(obj);
							}
						}
						setterMethod.invoke(rootObject,new Object[] {toTypedArray(list)});
					} else if( value instanceof JSONObject ) {
						
						// instantiate a new object, of the type correct for the
						Object obj = (Object)returnType.newInstance();
						setterMethod.invoke(rootObject, new Object[]{fromJSON((JSONObject)value,obj)});
					} else if( PropertyUtils.isSimpleType(returnType) ) {
						
						setterMethod.invoke(rootObject, new Object[]{PropertyUtils.correctCasting(returnType,value)});
					} 
				}
			} catch( InstantiationException e ) {
				throw new JSONException(e);
			} catch (InvocationTargetException e) {
				throw new JSONException(e);
			} catch( IllegalAccessException ite ) {
				throw new JSONException(ite);
			} catch (JSONException e) {
				throw new JSONException(e);
			}
		}
		return rootObject;
	}
	
	public JSONObject toJSON(Object obj) throws JSONException {
		
		if( obj==null ) {
			return null;
		}
		if( ! HasJSONProperties.class.isAssignableFrom(obj.getClass()) ) {
			throw new RuntimeException("The rootObject being converted to JSON must implement the HashJSONProperties interface.");
		}		
		JSONProperty[] properties = ((HasJSONProperties)obj).getJSONProperties();
		JSONObject jsonObj = new JSONObject();
		
		// iterate over each JSONProperty annotated method
		for( int jsonPropertyIdx=0; jsonPropertyIdx<properties.length; jsonPropertyIdx++ ) {
			JSONProperty property = properties[jsonPropertyIdx];
			
			Method method;
			try {
				method = obj.getClass().getMethod(property.getGetterName(),null);
			} catch (Exception e1) {
				throw new JSONException(e1);
			}
			
			// determine the method return type
			Class returnType = method.getReturnType();
			
			if( returnType==null ) {
				throw new JSONException(
						obj.getClass().getSimpleName()+"::"+method.getName()
						+" is annotated with JSONProperty, but has no return type."
						+"  I can't work with this.");
			}
			
			// strip "get" off the front
			String propertyName = PropertyUtils.propertyForGetterMethodName(method.getName());
			
			try {
				if( Enum.class.isAssignableFrom(returnType) ) {
					Enum ret = (Enum)method.invoke(obj,null);
					jsonObj.put(propertyName, ret.value());
				} else if( PropertyUtils.isSimpleType(returnType) ) {
					jsonObj.put(propertyName, handleSimpleType(returnType,method.invoke(obj,null)) );
				} else {
					if( returnType.isArray() ) {
						
						Object[] returnValues = (Object[])method.invoke(obj,null);
						JSONArray jsonArray = new JSONArray();
						for( int returnValueIdx=0; returnValueIdx<returnValues.length; returnValueIdx++ ) {
							Object value = returnValues[returnValueIdx];
							if(PropertyUtils.isSimpleType(returnType) ) {
								jsonArray.put(value);
							} else {
								jsonArray.put(toJSON(value));
							}
						}
						jsonObj.put(propertyName, jsonArray);
					} else if( Map.class.isAssignableFrom(returnType) ) {
						
						Map map = (Map)method.invoke(obj,null);
						JSONObject jsonMap = new JSONObject();
						Iterator iterator = map.entrySet().iterator();
						while( iterator.hasNext() ) {
							Object o = iterator.next();
							Map.Entry entry = (Map.Entry)o; 
							if(PropertyUtils.isSimpleType(entry.getValue().getClass())) {
								jsonMap.put(entry.getKey().toString(), handleSimpleType(returnType,entry.getValue()));
							} else {
								jsonMap.put(entry.getKey().toString(), toJSON(entry.getValue()));
							}
						}
						jsonObj.put(propertyName, jsonMap);
					} else if( List.class.isAssignableFrom(returnType) ) {
						
						List returnValues = (List)method.invoke(obj,null);
						JSONArray jsonArray = new JSONArray();
						int size = returnValues.size();
						for( int returnValueIdx=0; returnValueIdx<size; returnValueIdx++ ) {
							Object value = returnValues.get(returnValueIdx);
							if(PropertyUtils.isSimpleType(property.getContainedType()) ) {
								jsonArray.put(value);
							} else {
								jsonArray.put(toJSON(value));
							}
						}
						jsonObj.put(propertyName, jsonArray);
					} else {
						jsonObj.put(propertyName, toJSON(method.invoke(obj,null)));
					}
				}
			} catch( InvocationTargetException ite ) {
				throw new JSONException(ite);
			} catch( IllegalAccessException ite ) {
				throw new JSONException(ite);
			} catch( JSONException ite ) {
				throw new JSONException(ite);
			}
			
		}
		return jsonObj;
	}
	
	private Object handleSimpleType(Class returnType, Object value) {
		if( returnType.isArray() ) {
			Object[] returnValues = (Object[])value;
			JSONArray jsonArray = new JSONArray();
			for( int returnValuesIdx=0; returnValuesIdx<returnValues.length; returnValuesIdx++ ) {
				Object thisValue = returnValues[returnValuesIdx];
				jsonArray.put(thisValue);
			}
			return jsonArray;
		} else {
			return value;
		}
	}
	
	private Object[] toTypedArray(List list) {
		if( list.isEmpty() ) {
			return null;
		}
		Object first = list.get(0);
		Object[] ret = null;
		if( first instanceof String ) {
			ret = new String[list.size()];
		} else if( first instanceof Double ) {
			ret = new Double[list.size()];
		} else if( first instanceof Integer ) {
			ret = new Integer[list.size()];
		} else if( first instanceof Long ) {
			ret = new Long[list.size()];
		} else if( first instanceof Boolean ) {
			ret = new Boolean[list.size()];
		}
		return list.toArray(ret);
	}
}
