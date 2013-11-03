/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

package com.openmeap.samples.banking.web.model;

import javax.xml.bind.*;

import org.junit.*;

public class BankingServiceTest {
	@Test public void testGetInstance() {
		BankingService instance = BankingService.getInstance();
		Assert.assertTrue(instance!=null);
		Assert.assertTrue(instance==BankingService.getInstance());
	}
	@Test public void testGetAccounts() {
		BankingService instance = BankingService.getInstance();
		LoginResult res = instance.login("Jon Doe", "password");
		Assert.assertTrue(res!=null);
		Assert.assertTrue(res.getAccounts()!=null && res.getAccounts().getAccount().size()==2 );
		Account checking = res.getAccounts().getAccount().get(0);
		Account savings = res.getAccounts().getAccount().get(1);
		Assert.assertTrue(checking.getPosted().equals(checking.getAvailable()));
		Assert.assertTrue(savings.getPosted().equals(savings.getAvailable()));
		res = instance.login("Not Existing", "password");
		Assert.assertTrue(res==null);

	}
	/*@Test public void test() throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance("com.openmeap.samples.banking.web.model");
		Marshaller m = jaxbContext.createMarshaller();
		Result result = new Result();
		result.setLoginResult(BankingService.getInstance().login("Jon Doe","password"));
		m.marshal(result, System.out);
	}*/
}
