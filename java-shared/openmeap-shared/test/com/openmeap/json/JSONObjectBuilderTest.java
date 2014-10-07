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

package com.openmeap.json;

import java.util.Hashtable;
import java.util.Vector;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.openmeap.thirdparty.org.json.me.JSONObject;

public class JSONObjectBuilderTest extends TestCase {
	
	static public class Types implements Enum {

		static final public Types ONE = new Types("ONE");
		static final public Types TWO = new Types("TWO");
		static final private Types[] constants = new Types[] {
				ONE,
				TWO
			};
	    private final String value;
	    private Types(String v) {
	        value = v;
	    }
	    public Enum[] getStaticConstants() {
	    	return constants;
	    }
	    public String value() {
	        return value;
	    }
	    static public Types[] values() {
	    	return (Types[])ONE.getStaticConstants();
	    }
	    static public Types fromValue(String v) {
	    	return (Types)EnumUtils.fromValue(ONE, v);
	    }
		
	}
	static public class RootClass implements HasJSONProperties {
		
		private final static JSONProperty[] jsonProperties = new JSONProperty[] {
	    	new JSONProperty("child",BranchClass.class,
	    		new JSONGetterSetter(){
	    			public Object getValue(Object src) {
						return ((RootClass)src).getChild();
					}
					public void setValue(Object dest, Object value) {
						((RootClass)dest).setChild((BranchClass)value);
					}
	    		}),
	    	new JSONProperty("stringValue",String.class,
	    		new JSONGetterSetter(){
		    		public Object getValue(Object src) {
						return ((RootClass)src).getStringValue();
					}
					public void setValue(Object dest, Object value) {
						((RootClass)dest).setStringValue((String)value);
					}
	    		}),
	    	new JSONProperty("stringArrayValue",String[].class,
	    		new JSONGetterSetter(){
		    		public Object getValue(Object src) {
						return ((RootClass)src).getStringArrayValue();
					}
					public void setValue(Object dest, Object value) {
						((RootClass)dest).setStringArrayValue((String[])value);
					}
	    		}),
	    	new JSONProperty("longValue",Long.class,
		    	new JSONGetterSetter(){
		    		public Object getValue(Object src) {
						return ((RootClass)src).getLongValue();
					}
					public void setValue(Object dest, Object value) {
						((RootClass)dest).setLongValue((Long)value);
					}
	    		}),
	    	new JSONProperty("integerValue",Integer.class,
		    	new JSONGetterSetter(){
		    		public Object getValue(Object src) {
						return ((RootClass)src).getIntegerValue();
					}
					public void setValue(Object dest, Object value) {
						((RootClass)dest).setIntegerValue((Integer)value);
					}
	    		}),
	    	new JSONProperty("doubleValue",Double.class,
	    		new JSONGetterSetter(){
		    		public Object getValue(Object src) {
						return ((RootClass)src).getDoubleValue();
					}
					public void setValue(Object dest, Object value) {
						((RootClass)dest).setDoubleValue((Double)value);
					}
	    		}),
	    	new JSONProperty("hashTable",Hashtable.class,
	    		new JSONGetterSetter(){
		    		public Object getValue(Object src) {
						return ((RootClass)src).getHashTable();
					}
					public void setValue(Object dest, Object value) {
						((RootClass)dest).setHashTable((Hashtable)value);
					}
	    		}),
	    	new JSONProperty("vector",Vector.class,Long.class,
    	    		new JSONGetterSetter(){
    		    		public Object getValue(Object src) {
    						return ((RootClass)src).getVector();
    					}
    					public void setValue(Object dest, Object value) {
    						((RootClass)dest).setVector((Vector)value);
    					}
    	    		})
	    };
	    public JSONProperty[] getJSONProperties() {
			return jsonProperties;
		}
	    
	    private BranchClass child;
		private String stringValue;
		private String[] stringArrayValue;
		private Long longValue;
		private Integer integerValue;
		private Double doubleValue;
		private Hashtable hashTable;
		private Vector vector;
		
		public Hashtable getHashTable() {
			return hashTable;
		}
		public void setHashTable(Hashtable hashTable) {
			this.hashTable = hashTable;
		}
	    
	    public Vector getVector() {
			return vector;
		}
		public void setVector(Vector vector) {
			this.vector = vector;
		}
		public BranchClass getChild() {
			return child;
		}
		public void setChild(BranchClass child) {
			this.child = child;
		}
		public String getStringValue() {
			return stringValue;
		}
		public void setStringValue(String stringValue) {
			this.stringValue = stringValue;
		}
		public String[] getStringArrayValue() {
			return stringArrayValue;
		}
		public void setStringArrayValue(String[] stringArrayValue) {
			this.stringArrayValue = stringArrayValue;
		}
		public Long getLongValue() {
			return longValue;
		}
		public void setLongValue(Long longValue) {
			this.longValue = longValue;
		}
		public Integer getIntegerValue() {
			return integerValue;
		}
		public void setIntegerValue(Integer integerValue) {
			this.integerValue = integerValue;
		}
		public Double getDoubleValue() {
			return doubleValue;
		}
		public void setDoubleValue(Double doubleValue) {
			this.doubleValue = doubleValue;
		}
	}
	static public class BranchClass implements HasJSONProperties {
		private String string;
		private Types typeOne;
		private Types typeTwo;
		
		private final static JSONProperty[] jsonProperties = new JSONProperty[] {
	    	new JSONProperty("string",String.class,
	    		new JSONGetterSetter(){
	    			public Object getValue(Object src) {
						return ((BranchClass)src).getString();
					}
					public void setValue(Object dest, Object value) {
						((BranchClass)dest).setString((String)value);
					}
	    		}),
	    	new JSONProperty("typeOne",Types.class,
	    		new JSONGetterSetter(){
		    		public Object getValue(Object src) {
						return ((BranchClass)src).getTypeOne();
					}
					public void setValue(Object dest, Object value) {
						((BranchClass)dest).setTypeOne(Types.fromValue((String)value));
					}
	    		}),
	    	new JSONProperty("typeTwo",Types.class,
	    		new JSONGetterSetter(){
		    		public Object getValue(Object src) {
						return ((BranchClass)src).getTypeTwo();
					}
					public void setValue(Object dest, Object value) {
						((BranchClass)dest).setTypeTwo(Types.fromValue((String)value));
					}
	    		})
	    };
	    public JSONProperty[] getJSONProperties() {
			return jsonProperties;
		}

		public String getString() {
			return string;
		}
		public void setString(String string) {
			this.string = string;
		}
		public Types getTypeOne() {
			return typeOne;
		}
		public void setTypeOne(Types typeOne) {
			this.typeOne = typeOne;
		}
		public Types getTypeTwo() {
			return typeTwo;
		}
		public void setTypeTwo(Types typeTwo) {
			this.typeTwo = typeTwo;
		}
	}
	
	public void testToJSON() throws Exception {
		RootClass root = new RootClass();
		root.setDoubleValue(Double.valueOf("3.14"));
		root.setLongValue(new Long(Long.parseLong("1000")));
		root.setIntegerValue(new Integer(Integer.parseInt("2000")));
		root.setStringValue("value");
		root.setStringArrayValue(new String[]{"value1","value2"});
		root.setChild(new BranchClass());
		root.getChild().setTypeOne(Types.TWO);
		root.getChild().setTypeTwo(Types.ONE);
		root.getChild().setString("child_string");
		
		Hashtable table = new Hashtable();
		table.put("key1","value1");
		table.put("key2",new Long(1000));
		table.put("key3",new Integer(1000));
		root.setHashTable(table);
		
		Vector vector = new Vector();
		vector.add(Long.valueOf(1));
		vector.add(Long.valueOf(2));
		vector.add(Long.valueOf(3));
		root.setVector(vector);
		
		JSONObjectBuilder builder = new JSONObjectBuilder();
		JSONObject jsonObj = builder.toJSON(root);
		System.out.println(jsonObj.toString());
		Assert.assertEquals(
				 "{\"stringValue\":\"value\",\"vector\":[1,2,3],\"integerValue\":2000,"
				+"\"stringArrayValue\":[\"value1\",\"value2\"],"
				+"\"hashTable\":{\"key3\":1000,\"key2\":1000,\"key1\":\"value1\"},"
				+"\"doubleValue\":\"3.14\",\"longValue\":1000,"
				+"\"child\":{\"string\":\"child_string\",\"typeTwo\":\"ONE\",\"typeOne\":\"TWO\"}}",
				jsonObj.toString()
			);
		RootClass afterRoundTrip = (RootClass)builder.fromJSON(jsonObj,new RootClass());
		Assert.assertEquals( builder.toJSON(afterRoundTrip).toString(), jsonObj.toString() );
	}
}
