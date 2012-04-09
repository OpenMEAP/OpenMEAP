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
