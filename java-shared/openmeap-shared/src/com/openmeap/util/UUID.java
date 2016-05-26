/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
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

package com.openmeap.util;

import java.util.Random;

final public class UUID {
	private UUID() {
	}
	static private int[] eighth = new int[]{8, 9, 11, 12};
	
	static final public String randomUUID() {
		byte[] uuid = new byte[16];
		Random rand = new Random();
		for(int i=0; i<16; i++) {
			if( i==6 ) {
				int highBits = 4 << 4;
				int lowBits = rand.nextInt(16);
				uuid[i] = new Integer(highBits | lowBits).byteValue();
			} else if (i == 8) {
				uuid[i] = new Integer(((rand.nextInt(4)+8) << 4) | rand.nextInt(16)).byteValue();
			} else {
				uuid[i] = new Integer(rand.nextInt(255)).byteValue();
			}
		}
		String returnVal = Utils.byteArray2Hex(uuid);
		StringBuffer sb = new StringBuffer();
		sb.append(returnVal.substring(0, 8));
		sb.append('-');
		sb.append(returnVal.substring(8, 12));
		sb.append('-');
		sb.append(returnVal.substring(12, 16));
		sb.append('-');
		sb.append(returnVal.substring(16, 20));
		sb.append('-');
		sb.append(returnVal.substring(16, 32));
		return sb.toString();
	}
}
