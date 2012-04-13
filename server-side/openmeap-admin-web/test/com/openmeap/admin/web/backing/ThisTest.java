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

package com.openmeap.admin.web.backing;

import org.junit.Test;

public class ThisTest {
	
	interface I {
		<T extends Object> T v(T o);
	}
	class T implements I {
		public String v(Integer i) {
			System.out.println("In override integer method");
			return i.toString();
		}
		public String v(String i) {
			System.out.println("In override string method");
			return i.toString();
		}
		public Object v(Object o) {
			System.out.println("In interface method");
			return o;
		}
	}
	
	@Test public void testing() {
		I t = new T();
		t.v(100);
		t.v("alkhasdf");
	}
}
