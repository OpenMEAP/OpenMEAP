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

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * The aim here is a simple service for a sample application
 * where the focus is to demonstrate possible server-client interaction and client functionality,
 * NOT to provide a complete banking service application.
 *    That being said, there are probably a billion-and-one design, integrity and security considerations
 * that have not been made here that should be made for a "real" banking service app.
 * @author schang
 */
public class BankingService {
	static private BankingService instance = null;
	static private Map<String,Map<Account,Transactions>> accounts = null;
	static private String AUTH_SALT=UUID.randomUUID().toString();
	
	synchronized static public BankingService getInstance() {
		if( instance == null )
			instance = new BankingService();
		return instance;
	}
	
	synchronized public LoginResult login(String userName, String password) {
		UUID authId = UUID.randomUUID();
		String authToken = authId.toString()+"."+getSha1(authId.toString()+"."+AUTH_SALT);
		Accounts accts = getAccounts(userName,authToken);
		if( accts==null )
			return null;
		LoginResult toRet = new LoginResult();
		toRet.setAccounts(accts);
		toRet.setAuthToken(authToken);
		toRet.setOwner(userName);
		return toRet;
	}
	
	synchronized public Accounts getAccounts(String userName, String authToken) {
		Accounts accts = new Accounts();
		accts.setOwner(userName);
		List<Account> acctList = accts.getAccount();
		for( Map.Entry<String, Map<Account,Transactions>> ent : accounts.entrySet() ) {
			if( ent.getKey().equals(userName) ) {
				for( Map.Entry<Account,Transactions> acctEnt : ent.getValue().entrySet() ) {
					acctList.add(acctEnt.getKey());
				}
			}
		}
		if( accts.getAccount().size()>0 )
			return accts;
		else return null;
	}
	
	synchronized public Transactions getTransactions(TransactionStatus status, String userName, String acctNumber, String authToken) {
		Account acct = findAccount(userName,acctNumber);
		Transactions trans = findAccountTransactions(userName,acctNumber);
		Transactions toRet = createTransactions(acct);
		for( Transaction tran : trans.getTrans() )
			if( tran.getStatus().equals(status) )
				toRet.getTrans().add(tran);
		Collections.sort(toRet.getTrans(),new TransactionDateComparator());
		return toRet;
	}
	
	synchronized public void completeAllTransactions() {
		for( Map.Entry<String, Map<Account,Transactions>> ent : accounts.entrySet() ) {
			for( Map.Entry<Account,Transactions> acctEnt : ent.getValue().entrySet() ) {
				for( Transaction trans : acctEnt.getValue().getTrans() ) {
					if( trans.getStatus().equals(TransactionStatus.PENDING) ) {
						Account acct = acctEnt.getKey();
						completeAccountTransaction(trans,acct);
					}
				}
			}
		}
	}
	
	synchronized public void submitAndCompleteAccountTransaction(Transaction trans, Account acct) {
		submitAccountTransaction(trans,acct);
		completeAccountTransaction(trans,acct);
	}
	
	synchronized public Error submitTransfer(String userName, String srcAcct, String destAcct, Date date, double amount) {
		DatatypeFactory df = null;
		try {
			df = DatatypeFactory.newInstance();
		} catch( DatatypeConfigurationException dce ) {
			throw new RuntimeException(dce);
		}
		Account src = null, dest = null;
		src = findAccount(userName, srcAcct);
		dest = findAccount(userName, destAcct);
		if( src==null || dest==null ) {
			Error err = new Error();
			err.setCode(ErrorType.PARAM_BAD);
			err.setMessage("Could not find either the source or destination account.");
			return err;
		}
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(date.getTime());
		submitTransfer(src,dest,df.newXMLGregorianCalendar(cal),amount);
		return null;
	}
	
	private void submitTransfer(Account source, Account destination, XMLGregorianCalendar date, double amount) {
		submitAccountTransaction(
				createTransaction(
						TransactionType.DEPOSIT,
						date,
						"xfr "+source.getType()+"("+source.getNumber()+")",
						amount
						),destination);
		submitAccountTransaction(
				createTransaction(
						TransactionType.WITHDRAW,
						date,
						"xto "+destination.getType()+"("+destination.getNumber()+")",
						amount
						),source);
	}
	
	synchronized public void submitAccountTransaction(Transaction trans, Account acct) {
		trans.setAcctNumber(acct.getNumber());
		trans.setStatus(TransactionStatus.PENDING);
		findAccountTransactions(acct.getOwner(),acct.getNumber()).getTrans().add(trans);
		if( trans.getType()==TransactionType.WITHDRAW )
			acct.setAvailable( acct.getAvailable().subtract(trans.getAmount()) );
		else if( trans.getType()==TransactionType.DEPOSIT )
			acct.setAvailable( acct.getAvailable().add(trans.getAmount()) ); 
	}
	
	synchronized public void completeAccountTransaction(Transaction trans, Account acct) {
		trans.setStatus(TransactionStatus.COMPLETED);
		if( trans.getType()==TransactionType.WITHDRAW )
			acct.setPosted( acct.getPosted().subtract(trans.getAmount()) );
		else if( trans.getType()==TransactionType.DEPOSIT )
			acct.setPosted( acct.getPosted().add(trans.getAmount()) ); 
		trans.setBalance(acct.getPosted());
	}
	
	private Account findAccount(String userName, String number) {
		for( Map.Entry<String, Map<Account,Transactions>> ent : accounts.entrySet() ) {
			if( ent.getKey().equals(userName) )
				for( Map.Entry<Account,Transactions> acctEnt : ent.getValue().entrySet() ) {
					if( acctEnt.getKey().getNumber().equals(number) )
						return acctEnt.getKey();
				}
		}
		return null;
	}
	
	private Transactions findAccountTransactions(String userName, String number) {
		for( Map.Entry<String, Map<Account,Transactions>> ent : accounts.entrySet() ) {
			if( ent.getKey().equals(userName) )
				for( Map.Entry<Account,Transactions> acctEnt : ent.getValue().entrySet() ) {
					if( acctEnt.getKey().getNumber().equals(number) )
						return acctEnt.getValue();
				}
		}
		return null;
	}
	
	private Transaction createTransaction(TransactionType type, XMLGregorianCalendar date, String desc, double amount) {
		Transaction trans = new Transaction();
		trans.setDate(date);
		trans.setDesc(desc);
		trans.setType(type);
		trans.setAmount(BigDecimal.valueOf(amount));
		return trans;
	}
	
	private Transactions createTransactions(Account acct) {
		Transactions trans = new Transactions();
		trans.setOwner(acct.getOwner());
		trans.setAccountNumber(acct.getNumber());
		return trans;
	}
	
	private Account createAccount(AccountType type, String number, String owner, double posted, double available) {
		Account acct = new Account();
		acct.setType(type);
		acct.setNumber(number);
		acct.setOwner(owner);
		acct.setAvailable(BigDecimal.valueOf(available));
		acct.setPosted(BigDecimal.valueOf(posted));
		return acct;
	}
	
	private static String getSha1(String value) {
		MessageDigest sha1;
		try {
			sha1 = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		Formatter formatter = new Formatter();
        for (byte b : sha1.digest(value.getBytes())) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
	}
	
	/**
	 * We'll just go ahead and manually create a bunch of sample data here in the service class.
	 */
	private BankingService() {
		
		accounts = new HashMap<String,Map<Account,Transactions>>();
		
		Account accountChecking = null;
		Account accountSavings = null;
		Transactions transactions = null;
		String currentOwner = null;
		Map<Account,Transactions> thisSet = null;
		
		// create the first user and batch of accounts and transactions
		{
			currentOwner = "Jon Doe";
			thisSet = new HashMap<Account,Transactions>();
			accounts.put(currentOwner, thisSet);
			
			DatatypeFactory df = null;
			try {
				df = DatatypeFactory.newInstance();
			} catch( DatatypeConfigurationException dce ) {
				throw new RuntimeException(dce);
			}
			
			accountChecking = createAccount(AccountType.CHECKING,"*6303",currentOwner,2533.00,2533.00);
			transactions = createTransactions(accountChecking);
			thisSet.put(accountChecking,transactions);
			submitAccountTransaction(createTransaction(
					TransactionType.DEPOSIT,
					df.newXMLGregorianCalendar("2011-03-01T12:00:00+03:00"),
					"Celerity",
					1300.00),accountChecking );
			submitAccountTransaction(createTransaction(
					TransactionType.WITHDRAW,
					df.newXMLGregorianCalendar("2011-03-02T12:00:00+03:00"),
					"Mega Groceries, Inc.",
					240.00),accountChecking);
			submitAccountTransaction(createTransaction(
					TransactionType.WITHDRAW,
					df.newXMLGregorianCalendar("2011-03-02T12:00:00+03:00"),
					"Mega Convenience Chain, Inc.",
					12.00),accountChecking);
			
			accountSavings = createAccount(AccountType.SAVINGS,"*6323",currentOwner,15533.00,15533.00);
			transactions = createTransactions(accountSavings);
			thisSet.put(accountSavings,transactions);
			submitTransfer(accountChecking,accountSavings,df.newXMLGregorianCalendar("2011-03-01T12:00:00+03:00"),2000.00);
			submitTransfer(accountSavings,accountChecking,df.newXMLGregorianCalendar("2011-03-01T12:00:00+03:00"),150.00);
			submitTransfer(accountSavings,accountChecking,df.newXMLGregorianCalendar("2011-03-02T12:00:00+03:00"),150.00);
			submitTransfer(accountSavings,accountChecking,df.newXMLGregorianCalendar("2011-03-03T12:00:00+03:00"),150.00);
			submitTransfer(accountChecking,accountSavings,df.newXMLGregorianCalendar("2011-03-04T12:00:00+03:00"),1400.00);
			
			completeAllTransactions();
		}
	}
}
