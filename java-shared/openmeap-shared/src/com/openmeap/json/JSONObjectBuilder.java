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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.openmeap.json.HasJSONProperties;
import com.openmeap.json.Enum;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 * Converts an object hierarchy into a JSON representation.
 * Only looks at the JSONProperty annotated getter-methods.
 * Limited Hashtable support, but statically-typed arrays are good.
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
			
			Class returnType = property.getReturnType();
			
			String propertyName = property.getPropertyName();
			
			try {
					
					// get the unparsed value from the JSONObject
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
						
						property.getGetterSetter().setValue(rootObject,value);
						
					} else if( value instanceof JSONArray ) {
						
						JSONArray array = (JSONArray)value;
						Vector list = new Vector();
						for( int i=0; i<array.length(); i++ ) {
							Object obj = array.get(i);
							if( obj instanceof JSONObject ) {
								Object newObj = (Object)returnType.newInstance();
								list.addElement(fromJSON((JSONObject)obj,newObj));
							} else {
								list.addElement(obj);
							}
						}
						
						if(property.getContainedType()!=null) {
							property.getGetterSetter().setValue(rootObject,list);
						} else {
							property.getGetterSetter().setValue(rootObject,toTypedArray(list));
						}
						
					} else if( value instanceof JSONObject ) {
						
						Object obj = (Object)returnType.newInstance();
						if( Hashtable.class.isAssignableFrom(returnType) ) {
							Hashtable table = (Hashtable)obj;
							JSONObject jsonMap = (JSONObject)value;
							Enumeration keysEnum = jsonMap.keys();
							while(keysEnum.hasMoreElements()){
								String key = (String)keysEnum.nextElement();
								Object thisValue = jsonMap.get(key);
								if( thisValue instanceof JSONObject ) {
									Object newObj = (Object)returnType.newInstance();
									table.put(key,fromJSON((JSONObject)thisValue,newObj));
								} else {
									table.put(key,thisValue);
								}
							}
							property.getGetterSetter().setValue(rootObject, table);
						} else {
							// of the type correct for the
							property.getGetterSetter().setValue(rootObject, fromJSON((JSONObject)value,obj));
						}
					} else if( isSimpleType(returnType) ) {
						
						property.getGetterSetter().setValue(rootObject, correctCasting(returnType,value));
					} 

			} catch( Exception e ) {
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
			throw new RuntimeException("The rootObject being converted to JSON must implement the HasJSONProperties interface.");
		}		
		JSONProperty[] properties = ((HasJSONProperties)obj).getJSONProperties();
		JSONObject jsonObj = new JSONObject();
		
		// iterate over each JSONProperty annotated method
		for( int jsonPropertyIdx=0; jsonPropertyIdx<properties.length; jsonPropertyIdx++ ) {
			
			JSONProperty property = properties[jsonPropertyIdx];
			
			// determine the method return type
			Class returnType = property.getReturnType();
			
			Object value = property.getGetterSetter().getValue(obj);
			if(value==null) {
				continue;
			}
			
			if( returnType==null ) {
				throw new JSONException(
						obj.getClass().getName()+"."+property.getPropertyName()
						+" is annotated with JSONProperty, but has no return type."
						+"  I can't work with this.");
			}
			
			// strip "get" off the front
			String propertyName = property.getPropertyName();

			try {
				if( Enum.class.isAssignableFrom(returnType) ) {
					Enum ret = (Enum)value;
					jsonObj.put(propertyName, ret.value());
				} else if( isSimpleType(returnType) ) {
					jsonObj.put(propertyName, handleSimpleType(returnType,property.getGetterSetter().getValue(obj)) );
				} else {
					if( returnType.isArray() ) {
						Object[] returnValues = (Object[])value;
						JSONArray jsonArray = new JSONArray();
						for( int returnValueIdx=0; returnValueIdx<returnValues.length; returnValueIdx++ ) {
							Object thisValue = returnValues[returnValueIdx];
							jsonArray.put(toJSON(thisValue));
						}
						jsonObj.put(propertyName, jsonArray);
					} else if( Hashtable.class.isAssignableFrom(returnType) ) {
						Hashtable map = (Hashtable)value;
						JSONObject jsonMap = new JSONObject();
						Enumeration enumer = map.keys();
						while( enumer.hasMoreElements() ) {
							Object key = (String)enumer.nextElement();
							Object thisValue = (Object)map.get(key); 
							if(isSimpleType(thisValue.getClass())) {
								jsonMap.put(key.toString(), handleSimpleType(returnType,thisValue));
							} else {
								jsonMap.put(key.toString(), toJSON(thisValue));
							}
						}
						jsonObj.put(propertyName, jsonMap);
					} else if( Vector.class.isAssignableFrom(returnType) ) {
						
						Vector returnValues = (Vector)property.getGetterSetter().getValue(obj);
						JSONArray jsonArray = new JSONArray();
						int size = returnValues.size();
						for( int returnValueIdx=0; returnValueIdx<size; returnValueIdx++ ) {
							Object thisValue = returnValues.elementAt(returnValueIdx);
							if(isSimpleType(property.getContainedType()) ) {
								jsonArray.put(thisValue);
							} else {
								jsonArray.put(toJSON(thisValue));
							}
						}
						jsonObj.put(propertyName, jsonArray);
					} else {
						jsonObj.put(propertyName, toJSON(value));
					}
				}
			} catch( Exception ite ) {
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
	
	private Object[] toTypedArray(Vector list) {
		if( list.isEmpty() ) {
			return null;
		}
		Object first = list.elementAt(0);
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
		list.copyInto(ret);
		return (Object[])ret;
	}
	
	private Object correctCasting(Class type, Object obj) {
		
		if( type.equals(Long.class) ) {
			return new Long(Long.parseLong(obj.toString()));
		} else if( type.equals(Double.class) ) {
			return Double.valueOf(obj.toString());
		} else if( type.equals(Integer.class) ) {
			return Integer.valueOf(obj.toString());
		} else return obj;
	}
	
	private boolean isSimpleType(Class returnType) {
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
}
