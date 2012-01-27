package com.openmeap.model.dto;

import org.junit.*;

public class ApplicationTest {
	
	private Application createValidApplication() {
		Application app = new Application();
		return app;
	}
	
	@Test public void testValidate() {
		createValidApplication();
	}
}
