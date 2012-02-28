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

package com.openmeap.samples.banking.web;

import java.io.*;
import java.util.Date;

import javax.servlet.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.openmeap.samples.banking.web.model.*;
import com.openmeap.samples.banking.web.model.Error;

@SuppressWarnings("serial")
public class BankingServlet extends GenericServlet {

	@Override
	public void service(ServletRequest req, ServletResponse resp) 
			throws ServletException, IOException {
		
		String action = req.getParameter("action");
		String authToken = req.getParameter("auth");
		String userName = req.getParameter("username");
		String password = req.getParameter("password");
		String acctNumber = req.getParameter("acct");
		String destAcctNumber = req.getParameter("destAcct");
		String amount = req.getParameter("amount");
		
		Result result = new Result();
		if( action==null ) {
			Error err = new Error();
			err.setCode(ErrorType.PARAM_MISSING);
			err.setMessage("The action parameter is missing");
			result.setError(err);
		} else if( action.equals("login") ) {
			LoginResult loginResult = BankingService.getInstance().login(userName, password);
			if( loginResult!=null )
				result.setLogin(loginResult);
			else {
				Error err = new Error();
				err.setCode(ErrorType.LOGIN_FAILED);
				err.setMessage("Login failed for "+userName);
				result.setError(err);
			}
		} else if( authToken==null ) {
			Error err = new Error();
			err.setCode(ErrorType.PARAM_MISSING);
			err.setMessage("The auth token is missing");
			result.setError(err);
		} else if( action.equals("pending") ) {
			Transactions trans = BankingService.getInstance().getTransactions(TransactionStatus.PENDING, userName, acctNumber, authToken);
			result.setTransactions(trans);
		} else if( action.equals("completed") ) {
			Transactions trans = BankingService.getInstance().getTransactions(TransactionStatus.COMPLETED, userName, acctNumber, authToken);
			result.setTransactions(trans);
		} else if( action.equals("accounts") ) {
			Accounts accts = BankingService.getInstance().getAccounts(userName, authToken);
			result.setAccounts(accts);
		} else if( action.equals("transfer") && userName!=null && amount!=null && destAcctNumber!=null && acctNumber!=null ) {
			BankingService svc = BankingService.getInstance();
			Error err = svc.submitTransfer(userName, acctNumber, destAcctNumber, new Date(), Double.valueOf(amount));
			if( err!=null ) 
				result.setError(err);
		}

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance("com.openmeap.samples.banking.web.model");
			Marshaller m = jaxbContext.createMarshaller();
			resp.setContentType("text/xml");
			m.marshal(result, resp.getOutputStream());
		} catch( Exception e ) {
			throw new RuntimeException(e);
		}
	}
}
