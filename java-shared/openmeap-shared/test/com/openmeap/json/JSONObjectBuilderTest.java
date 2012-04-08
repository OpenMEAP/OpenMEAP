package com.openmeap.json;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONObject;

public class JSONObjectBuilderTest extends TestCase {
	
	static public class Types implements Enum {

		static final public Types ONE = new Types("ONE");
		static final public Types TWO = new Types("TWO");
	    private final String value;
	    private Types(String v) {
	        value = v;
	    }
	    public String value() {
	        return value;
	    }
	    static public Types[] values() {
	    	return (Types[])EnumUtils.values(Types.class);
	    }
	    static public Types fromValue(String v) {
	    	return (Types)EnumUtils.fromValue(Types.class, v);
	    }
		
	}
	static public class RootClass implements HasJSONProperties {
		private BranchClass child;
		private String stringValue;
		private String[] stringArrayValue;
		private Long longValue;
		private Integer integerValue;
		private Double doubleValue;
		private static JSONProperty[] jsonProperties = new JSONProperty[] {
	    	new JSONProperty("getChild"),
	    	new JSONProperty("getStringValue"),
	    	new JSONProperty("getStringArrayValue"),
	    	new JSONProperty("getLongValue"),
	    	new JSONProperty("getIntegerValue"),
	    	new JSONProperty("getDoubleValue")
	    };
	    public JSONProperty[] getJSONProperties() {
			return jsonProperties;
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
		private static JSONProperty[] jsonProperties = new JSONProperty[] {
	    	new JSONProperty("getString"),
	    	new JSONProperty("getTypeOne"),
	    	new JSONProperty("getTypeTwo")
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
		root.setDoubleValue(Double.valueOf(3.14));
		root.setLongValue(Long.valueOf(1000L));
		root.setIntegerValue(Integer.valueOf(2000));
		root.setStringValue("value");
		root.setStringArrayValue(new String[]{"value1","value2"});
		root.setChild(new BranchClass());
		root.getChild().setTypeOne(Types.TWO);
		root.getChild().setTypeTwo(Types.ONE);
		root.getChild().setString("child_string");
		
		JSONObjectBuilder builder = new JSONObjectBuilder();
		JSONObject jsonObj = builder.toJSON(root);
		Assert.assertEquals(
					"{\"child\":{\"typeTwo\":\"ONE\",\"typeOne\":\"TWO\",\"string\":\"child_string\"}"
					+",\"longValue\":1000,\"integerValue\":2000,\"stringArrayValue\":[\"value1\",\"value2\"],"
					+"\"stringValue\":\"value\",\"doubleValue\":3.14}",
					jsonObj.toString()
				);
		RootClass afterRoundTrip = (RootClass)builder.fromJSON(jsonObj,new RootClass());
		Assert.assertEquals( builder.toJSON(afterRoundTrip).toString(), jsonObj.toString() );
	}
}
