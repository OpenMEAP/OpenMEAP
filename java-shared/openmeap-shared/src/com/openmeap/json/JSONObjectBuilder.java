package com.openmeap.json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
		
		Method[] methods = rootObject.getClass().getMethods();
		for( Method getterMethod : methods ) {
			if( getterMethod.getAnnotation(JSONProperty.class)==null ) {
				continue;
			}
			Class<?> returnType = getterMethod.getReturnType();
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
					if( returnType.isEnum() ) {
						
						Class<? extends Enum> e = (Class<? extends Enum>)returnType; 
						setterMethod.invoke(rootObject,Enum.valueOf(e,(String)value));
					} else if( value instanceof JSONArray ) {
						
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
						setterMethod.invoke(rootObject, fromJSON((JSONObject)value,obj));
					} else if( PropertyUtils.isSimpleType(returnType) ) {
						
						setterMethod.invoke(rootObject, PropertyUtils.correctCasting(returnType,value));
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
		
		JSONObject jsonObj = new JSONObject();
		// iterate over each JSONProperty annotated method
		Method[] methods = obj.getClass().getMethods();
		for( Method method : methods ) {
			
			// if the method is not annotated, then skip it
			if( method.getAnnotation(JSONProperty.class)==null ) {
				continue;
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
				if( returnType.isEnum() ) {
					Enum ret = (Enum)method.invoke(obj);
					jsonObj.put(propertyName, ret.toString());
				} else if( PropertyUtils.isSimpleType(returnType) ) {
					if( returnType.isArray() ) {
						Object[] returnValues = (Object[])method.invoke(obj);
						JSONArray jsonArray = new JSONArray();
						for( Object value : returnValues ) {
							jsonArray.put(value);
						}
						jsonObj.put(propertyName, jsonArray);
					} else {
						jsonObj.put(propertyName, method.invoke(obj));
					}
				} else {
					if( returnType.isArray() ) {
						Object[] returnValue = (Object[])method.invoke(obj);
						JSONArray jsonArray = new JSONArray();
						for( Object value : returnValue ) {
							jsonArray.put(toJSON(value));
						}
						jsonObj.put(propertyName, jsonArray);
					} else {
						jsonObj.put(propertyName, toJSON(method.invoke(obj)));
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
	
	private Object[] toTypedArray(List<?> list) {
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
