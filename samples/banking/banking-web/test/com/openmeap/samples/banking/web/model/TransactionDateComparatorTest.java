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

package com.openmeap.samples.banking.web.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jasper.compiler.TldLocationsCache;
import org.junit.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

public class TransactionDateComparatorTest {
	
	@Test public void testDateComparator() {
		
		DatatypeFactory df = null;
		try {
			df = DatatypeFactory.newInstance();
		} catch( DatatypeConfigurationException dce ) {
			throw new RuntimeException(dce);
		}
		
		List<Transaction> tl = new ArrayList<Transaction>();
		
		Transaction t = new Transaction();
		t.setAcctNumber("1");
		t.setDate(df.newXMLGregorianCalendar("2011-03-03T12:00:00+03:00"));
		tl.add(t);
		
		t = new Transaction();
		t.setAcctNumber("2");
		t.setDate(df.newXMLGregorianCalendar("2011-03-03T12:00:00+03:00"));
		tl.add(t);
		
		t = new Transaction();
		t.setAcctNumber("3");
		t.setDate(df.newXMLGregorianCalendar("2011-03-04T12:00:00+03:00"));
		tl.add(t);
		
		t = new Transaction();
		t.setAcctNumber("4");
		t.setDate(df.newXMLGregorianCalendar("2011-03-01T12:00:00+03:00"));
		tl.add(t);
		
		Collections.sort(tl,new TransactionDateComparator());
		Assert.assertTrue(tl.get(0).getAcctNumber().equals("4"));
		Assert.assertTrue(tl.get(1).getAcctNumber().equals("1"));
		Assert.assertTrue(tl.get(2).getAcctNumber().equals("2"));
		Assert.assertTrue(tl.get(3).getAcctNumber().equals("3"));
	}
}
