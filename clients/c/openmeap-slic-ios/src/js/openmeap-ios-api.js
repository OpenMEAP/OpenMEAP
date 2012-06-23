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

OpenMEAP_Core = {
	config:{
		deviceType:"iOS"
	},
	getPreferences:function(name) {
		var toRet = {
			name:name,
			get:function(key){
				var val = OpenMEAP_Core.getApiUrl("jsapi://preferences/get/?name="+encodeURIComponent(name)+"&key="+encodeURIComponent(key));
                if(val!=null) {
                   return decodeURIComponent(val);
                }
                return null
			},
			put:function(key,value) {
				return eval(OpenMEAP_Core.getApiUrl("jsapi://preferences/put/?name="+encodeURIComponent(name)+"&key="+encodeURIComponent(key)+"&value="+encodeURIComponent(value)));
			},
			remove:function(key) {
				return OpenMEAP_Core.getApiUrl("jsapi://preferences/remove/?name="+encodeURIComponent(name)+"&key="+encodeURIComponent(key));
			},
			clear:function() {
				return OpenMEAP_Core.getApiUrl("jsapi://preferences/clear/?name="+encodeURIComponent(name));
			}
		};
		return toRet;
	},
	clearCache:function() {
		return OpenMEAP_Core.getApiUrl("jsapi://application/clearCache/");
	},
	setTitle:function(text) { 
		return OpenMEAP_Core.getApiUrl("jsapi://application/setTitle/?value="+encodeURIComponent(key)); 
	},
	doToast:function(text,duration) { 
		return alert(text); 
	},
	getDeviceType:function() {
		return OpenMEAP_Core.config.deviceType; 
	},
	isTimeForUpdateCheck:function() {
		return OpenMEAP_Core.getApiUrl("jsapi://updates/isTimeForCheck/");
	},
	checkForUpdates:function(callback) { 
		return OpenMEAP_Core.getApiUrl("jsapi://updates/checkForUpdates/?callback="+encodeURIComponent(callback+' ')); 
	},
	performUpdate:function(header,callback) { 
		return OpenMEAP_Core.getApiUrl("jsapi://updates/performUpdate/?header="+encodeURIComponent(header)+"&callback="+encodeURIComponent(callback+' ')); 
	},
	reload:function() { 
		OpenMEAP_Core.getApiUrl("jsapi://application/reload/");
	},
    notifyReadyForUpdateCheck:function() { 
		OpenMEAP_Core.getApiUrl("jsapi://application/notifyReadyForUpdateCheck/");
	},
	
	/*
	 * iOS specific methods
	 */
	
	getApiUrl:function(url) {
		var request = new XMLHttpRequest();
		request.open('GET', url, false);
		request.send(null);
		if (request.status == 0) {
			return eval(request.responseText);
		} else {
			return null;
		}
	}
};
