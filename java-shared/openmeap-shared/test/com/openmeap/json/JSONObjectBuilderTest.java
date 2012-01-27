package com.openmeap.json;

import org.json.JSONObject;

import org.junit.Test;
import org.junit.Assert;


public class JSONObjectBuilderTest {
	
	public enum Types {
		ONE,TWO
	}
	static public class RootClass {
		private BranchClass child;
		private String stringValue;
		private String[] stringArrayValue;
		private Long longValue;
		private Integer integerValue;
		private Double doubleValue;
		@JSONProperty public BranchClass getChild() {
			return child;
		}
		public void setChild(BranchClass child) {
			this.child = child;
		}
		@JSONProperty public String getStringValue() {
			return stringValue;
		}
		public void setStringValue(String stringValue) {
			this.stringValue = stringValue;
		}
		@JSONProperty public String[] getStringArrayValue() {
			return stringArrayValue;
		}
		public void setStringArrayValue(String[] stringArrayValue) {
			this.stringArrayValue = stringArrayValue;
		}
		@JSONProperty public Long getLongValue() {
			return longValue;
		}
		public void setLongValue(Long longValue) {
			this.longValue = longValue;
		}
		@JSONProperty public Integer getIntegerValue() {
			return integerValue;
		}
		public void setIntegerValue(Integer integerValue) {
			this.integerValue = integerValue;
		}
		@JSONProperty public Double getDoubleValue() {
			return doubleValue;
		}
		public void setDoubleValue(Double doubleValue) {
			this.doubleValue = doubleValue;
		}
	}
	static public class BranchClass {
		private String string;
		private Types typeOne;
		private Types typeTwo;
		@JSONProperty public String getString() {
			return string;
		}
		public void setString(String string) {
			this.string = string;
		}
		@JSONProperty public Types getTypeOne() {
			return typeOne;
		}
		public void setTypeOne(Types typeOne) {
			this.typeOne = typeOne;
		}
		@JSONProperty public Types getTypeTwo() {
			return typeTwo;
		}
		public void setTypeTwo(Types typeTwo) {
			this.typeTwo = typeTwo;
		}
	}
	
	@Test public void testToJSON() throws Exception {
		RootClass root = new RootClass();
		root.setDoubleValue(3.14);
		root.setLongValue(1000L);
		root.setIntegerValue(2000);
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
