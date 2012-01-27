package com.openmeap.json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
			Method setterMethod = setterForGetterMethod(getterMethod);
			String propertyName = propertyForMethodName(getterMethod.getName());
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
					} else if( isSimpleType(returnType) ) {
						
						setterMethod.invoke(rootObject, correctCasting(returnType,value));
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
			String propertyName = propertyForMethodName(method.getName());
			
			try {
				if( returnType.isEnum() ) {
					Enum ret = (Enum)method.invoke(obj);
					jsonObj.put(propertyName, ret.toString());
				} else if( isSimpleType(returnType) ) {
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
	
	private Object correctCasting(Class<?> type, Object obj) {
		if( type.equals(Long.class) ) {
			return Long.valueOf(obj.toString());
		} else if( type.equals(Double.class) ) {
			return Double.valueOf(obj.toString());
		} else if( type.equals(Integer.class) ) {
			return Integer.valueOf(obj.toString());
		} else return obj;
	}
	
	private Boolean isSimpleType(Class<?> returnType) {
		Class<?> testAgainst = returnType.isArray() ? returnType.getComponentType() : returnType;
		return Boolean.class.isAssignableFrom(testAgainst) 
			|| Long.class.isAssignableFrom(testAgainst) 
			|| Double.class.isAssignableFrom(testAgainst)
			|| Integer.class.isAssignableFrom(testAgainst)
			|| String.class.isAssignableFrom(testAgainst);
	}
	
	private Method setterForGetterMethod(Method getterMethod) {
		String setterName = getterMethod.getName();
		if( setterName.startsWith("g") ) {
			setterName = setterName.replaceFirst("g", "s");
		}
		Method setterMethod = null;
		try {
			Class clazz = getterMethod.getDeclaringClass();
			Class returnType = getterMethod.getReturnType();
			setterMethod = clazz.getMethod(setterName,returnType);
		} catch( NoSuchMethodException ite ) {
			// we don't care, here
		}
		return setterMethod;
	}
	
	private String propertyForMethodName(String methodName) {
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
