package com.openmeap.util;

import junit.framework.Assert;
import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {
	public void testJoin() {
		String[] parts = {"1","2","3","4"};
		Assert.assertEquals("1/2/3/4",StringUtils.join(parts, "/"));
		Assert.assertEquals("2/3/4",StringUtils.join(parts, "/",1,parts.length));
		Assert.assertEquals("1",StringUtils.join(parts, "/",0,1));
		Assert.assertEquals("4",StringUtils.join(parts, "/",parts.length-1,parts.length));
	}
}
