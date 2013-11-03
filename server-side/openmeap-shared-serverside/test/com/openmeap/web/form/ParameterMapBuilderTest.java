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

package com.openmeap.web.form;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class ParameterMapBuilderTest {
	
	@Test public void testParameterMapBuilder() throws Exception {
		
		TestObject obj = new TestObject();
		obj.setDoubleValue(3.141597);
		obj.setIntegerValue(123456);
		obj.setStringValue("AStringValue");
		
		ParameterMapBuilder builder = new ParameterMapBuilder();
		builder.setUseParameterMapUtilsFirstValue(false);
		builder.setMapValuesAsStrings(true);
		
		Map<String,Object> map = new HashMap<String,Object>();
		builder.toParameters(map,obj);
		TestObject roundTrip = new TestObject();
		builder.fromParameters(roundTrip, map);

		Assert.assertEquals("expected to be equal", obj, roundTrip);
	}
	
	static public class TestObject {
		
		String stringValue;
		Integer integerValue;
		Double doubleValue;
		
		@Parameter("string_value")
		public String getStringValue() {
			return stringValue;
		}
		public void setStringValue(String stringValue) {
			this.stringValue = stringValue;
		}
		
		@Parameter("integer_value")
		public Integer getIntegerValue() {
			return integerValue;
		}
		public void setIntegerValue(Integer integerValue) {
			this.integerValue = integerValue;
		}
		
		@Parameter("double_value")
		public Double getDoubleValue() {
			return doubleValue;
		}
		public void setDoubleValue(Double doubleValue) {
			this.doubleValue = doubleValue;
		}
		
		public int hashCode() {
			return (stringValue+"."+doubleValue+"."+integerValue).hashCode();
		}
		public boolean equals(Object obj) {
			TestObject to = (TestObject)obj;
			return this.hashCode()==obj.hashCode();
		}
	}
}
