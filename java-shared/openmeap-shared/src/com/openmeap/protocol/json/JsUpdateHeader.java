/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

package com.openmeap.protocol.json;



import com.openmeap.json.JSONObjectBuilder;
import com.openmeap.protocol.dto.UpdateHeader;
import com.openmeap.thirdparty.org.json.me.JSONException;
import com.openmeap.thirdparty.org.json.me.JSONObject;
import com.openmeap.util.GenericRuntimeException;

public class JsUpdateHeader {
	
	private UpdateHeader updateHeader;
	private Long spaceAvailable;
	
	public JsUpdateHeader(UpdateHeader updateHeader, Long spaceAvailable) {
		this.updateHeader=updateHeader;
		this.spaceAvailable = spaceAvailable;
		if( updateHeader!=null ) {
			this.updateHeader.setSpaceAvailable(spaceAvailable);
		}
	}
	
	public JsUpdateHeader(String json) throws JSONException {
		JSONObject obj = new JSONObject(json);
		JSONObjectBuilder builder = new JSONObjectBuilder();
		UpdateHeader update = (UpdateHeader)builder.fromJSON(obj, new UpdateHeader());
		updateHeader = update;
	}
	
	public UpdateHeader getWrappedObject() {
		return updateHeader;
	}
	
	public String toString() {
		return toJSON().toString();
	}
	
	public String toJSON() {
		if( updateHeader!=null ) {
			try {
				JSONObjectBuilder builder = new JSONObjectBuilder();
				return builder.toJSON(updateHeader).toString();
			} catch( JSONException e ) {
				throw new GenericRuntimeException(e);
			}
		}
		return "null";
	}
}
