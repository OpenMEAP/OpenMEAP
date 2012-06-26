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

/** 
 * Provides Basic API Access to all clients, until
 * this file gets too large to manage, then specific clients
 * will get their own extension javascript.
 */

_openmeap_shallow_extend = function(obj,extendBy) {
	for( i in extendBy ) {
		obj[i] = extendBy[i];
	}
	return obj;
}

if( typeof OpenMEAP=='undefined' ) {
	OpenMEAP={data:{},config:{},persist:{cookie:{}}};
}

if( typeof OpenMEAP_Core!="undefined" ) {
	OpenMEAP.config['deviceType']=OpenMEAP_Core.getDeviceType();
	OpenMEAP = _openmeap_shallow_extend(OpenMEAP,{
		clearCache:function()
			{ return OpenMEAP_Core.clearCache(); },
		setTitle:function(text) 
			{ return OpenMEAP_Core.setTitle(text); },
		doToast:function(text,isLong)
			{ return OpenMEAP_Core.doToast(text,isLong); },
		getDeviceType:function()
			{ return OpenMEAP_Core.getDeviceType(); },
		getPreferences:function(name)
			{ return OpenMEAP_Core.getPreferences(name); },
		isTimeForUpdateCheck:function()
			{ return OpenMEAP_Core.isTimeForUpdateCheck(); },
		checkForUpdates:function()
			{ return OpenMEAP_Core.checkForUpdates(); },
        /**
         * Request that the container perform an update.
         * @param header The update header passed into OpenMEAP.updates.onUpdate(header)
         */
		performUpdate:function(header,stateChangeCallback)
			{ return OpenMEAP_Core.performUpdate(OpenMEAP.utils.toJSON(header),stateChangeCallback+' '); },
        /**
         * Force a reload of the application.  This would be done,
         * generally, after a javascript mediated REQUIRED or OPTIONAL
         * update.
         */
		reload:function() 
			{ OpenMEAP_Core.reload(); },
        /**
         * Notify the container that callback overrides are available
         * and to proceed with the update check, if it's time.
         * There is a time-out for this, so if an IMMEDIATE update is
         * pushed, then the update will proceed after the configured 
         * time-out, should this method not have been called.
         */
        notifyReadyForUpdateCheck:function()
            { OpenMEAP_Core.notifyReadyForUpdateCheck(); }
	});
} else {
	/**
	 * OpenMEAP JS API Web-Browser Implementation that is intended to take simplify development
	 * by providing a functional api interface to web browsers.
	 */
	OpenMEAP = _openmeap_shallow_extend(OpenMEAP,{
		config:{ 
			deviceType:"Browser" 
		},
		clearCache:function() {	
		},
		setTitle:function(text) { 
			document.title=text; 
		},
		doToast:function(text,isLong) { 
			alert(text); 
		},
		getDeviceType:function() { 
			return OpenMEAP.config.deviceType; 
		},
		getPreferences:function(name) {
			var c_name = "OpenMEAP_keyValuePairs_"+name;
			var values = unescape(OpenMEAP.persist.cookie.get(c_name));
			eval("var values = "+values);
			var toRet = {
				cookieName:c_name,
				cookieValues:(typeof values == "object" ? values : {}),
				get:function(key){
					return this.cookieValues[key];
				},
				put:function(key,value) {
					this.cookieValues[key]=value;
					var values = escape(OpenMEAP.utils.toJSON(this.cookieValues));
					this.setCookie();
				},
				remove:function(key) {
					var newValues = {}
					for( i in this.cookieValues ) {
						if( i != key ) {
							newValues[i]=this.cookieValues[key];
						}
					}
					this.cookieValues = newValues;
					this.setCookie();
				},
				clear:function() {
					this.cookieValues = {};
					this.setCookie();
				},
				setCookie:function(){
					OpenMEAP.persist.cookie.set(this.cookieName,escape(OpenMEAP.utils.toJSON(this.cookieValues)));
				}
			};
			return toRet;
		},
		isTimeForUpdateCheck:function()
			{ return false; },
		checkForUpdates:function()
			{ return; },
		performUpdate:function(header,stateChangeCallback)
			{ return; },
		reload:function() 
			{ ; },
        notifyReadyForUpdateCheck:function()
            { ; }
	});
}

OpenMEAP.utils = {
	toJSON:function(obj) {
		if( typeof obj == "object" ) {
			if( typeof obj.length != "number" ) {
				var str = "{";
				for( key in obj ) {
					var val = '"'+key+"\":"+this.toJSON(obj[key]);
					str += ( str.length>1 ? ","+val : val ); 
				}
				str += "}";
				return str;
			} else {
				var str = "[";
				for( var i=0; i<obj.length; i++ ) {
					var val = this.toJSON(obj[i]);
					str += ( i>0 ? ","+val : val );
				}
				str += "]";
				return str;
			}
		} else if( typeof obj == "string" )
			return "\""+obj+"\"";
		else if( typeof obj == "number" )
			return ""+obj;
	},
	/**
	 * Converts an '&' separated set of key=value pairs into an object
	 */
	paramsToObj:function(str) {
		var vars = str.split("&");
		var retObj = {};
		for( var i=0; i<vars.length; i++ ) {
			var parts = vars[i].split("=");
			if( parts.length > 1 )
				retObj[parts[0]]=parts[1];
			else retObj[parts[0]]=true;
		}
		return retObj;
	}
};

{
	try {
		var useSessionStorage = false;
		if( sessionStorage != null )
			var useSessionStorage = true;
	} catch(e) {
		;
	}
	
	if( ! useSessionStorage ) {
		OpenMEAP.persist.keyPairs = {
			setup:function()             { this.prefs = OpenMEAP.getPreferences("keyPairs"); },
			getItem:function(key)        { if(typeof this.prefs=="undefined") this.setup(); if(this.prefs.get(key)) return unescape(this.prefs.get(key)); },
			setItem:function(key, value) { if(typeof this.prefs=="undefined") this.setup(); this.prefs.put(key,escape(value)); },
			removeItem:function(key)     { if(typeof this.prefs=="undefined") this.setup(); this.prefs.remove(key); },
			clear:function()             { if(typeof this.prefs=="undefined") this.setup(); this.prefs.clear(); }
		};
	} else {
		OpenMEAP.persist.keyPairs = {
			getItem:function(key)        { return sessionStorage.getItem(key); },
			setItem:function(key, value) { sessionStorage.setItem(key,value); },
			removeItem:function(key)     { sessionStorage.removeItem(key); },
			clear:function()             { sessionStorage.clear(); }
		};
	}
}

if( OpenMEAP.config.deviceType=='iOS' ) {
	window.cookies={};
	OpenMEAP.persist.cookie.set = function(name,value,expireDays) {
		window.cookies[name]=value;
	}
	OpenMEAP.persist.cookie.get = function(name) {
		return window.cookies[name];
	}
} else {
	OpenMEAP.persist.cookie.set = function(name,value,expireDays) {
		var expireDate = new Date();
		expireDate.setDate(expireDate.getDate() + expireDays);
		var c_value = escape(value) + ((expireDays==null) ? "" : "; expires="+expireDate.toUTCString());
		document.cookie = name + "=" + c_value;
	}
	OpenMEAP.persist.cookie.get = function(name) {
		var i,key,value,ARRcookies = document.cookie.split(";");
		for (i=0; i<ARRcookies.length; i++) {
			key   = ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
			value = ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
			key   = key.replace(/^\s+|\s+$/g,"");
			if (key == name) {
				return unescape(value);
			}
		}
	}
}

/**
 * Application lifecycle call-backs to be overridden
 */
OpenMEAP.lifeCycleCallBacks={
	onPause:function(data){}	
};
if( OpenMEAP.config.deviceType=='Browser' ) {
	// TODO: provide logic calling each of the lifeCycleCallBacks
}

/**
 * Provides the default update engine logic
 * to be overridden
 */
OpenMEAP.updates={

    // The following three are called on every check

    onUpdate:function(update) {
		OpenMEAP.performUpdate(update,function(data) {
            OpenMEAP.updates.onStateChange(data);
        });
	},
    onNoUpdate:function() {
        //OpenMEAP.doToast('no update');
    },
	onCheckError:function(error) {
		OpenMEAP.doToast("An error occurred checking for an update\n\n"+error.type+':'+error.message);
	},
    
	/**
	 * A default on-state-change callback used by OpenMEAP.updates.onUpdate(update)
	 * when calling OpenMEAP.performUpdate(updateData,onStateChangeCallBack)
	 */
	onStateChange:function(data) {
		if( data.complete == true ) {
			this.onUpdateComplete(data);
		} else if( ! data.error ) {
			this.onUpdateIncrement(data);
		} else {
			this.onUpdateError(data);
		}
	},
	/**
	 * Called by OpenMEAP.updates.onStatusChange(),
	 * when the archive download has completed.
	 */
	onUpdateComplete:function(data) {
		var callbackLoc = document.getElementById("update-callback");
		if( callbackLoc ) {
			var total = data.update.installNeeds - data.update.storageNeeds;
			callbackLoc.innerHTML = callbackLoc.innerText = "done";
		}
		OpenMEAP.doToast("Update complete");
		OpenMEAP.reload();
	},
	/**
	 * Called by OpenMEAP.updates.onStateChange(), 
	 * each percent the application archive download completes.
	 */
	onUpdateIncrement:function(data) {
		var callbackLoc = document.getElementById("update-callback");
		if( callbackLoc ) {
			var total = data.update.installNeeds - data.update.storageNeeds;
			callbackLoc.innerHTML = callbackLoc.innerText = data.bytesDownloaded+"/"+total;
		}
	},
	/**
	 * Called by OpenMEAP.updates.onStateChange(), 
	 * when an error has occurred.
	 */
	onUpdateError:function(data) {
		OpenMEAP.doToast(data.error.type+":"+data.error.message);
	}
};
