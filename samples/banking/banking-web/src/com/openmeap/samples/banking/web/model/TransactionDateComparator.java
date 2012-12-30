/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2013 OpenMEAP, Inc.                                   #
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

import java.util.Comparator;
import java.util.GregorianCalendar;

public class TransactionDateComparator implements Comparator<Transaction> {

	@Override
	public int compare(Transaction o1, Transaction o2) {
		GregorianCalendar o1Cal = o1.getDate().toGregorianCalendar();
		GregorianCalendar o2Cal = o2.getDate().toGregorianCalendar();
		return o1Cal.equals(o2Cal) ? 0 : ( o1Cal.before(o2Cal) ? (-1) : 1 );
	}

}
