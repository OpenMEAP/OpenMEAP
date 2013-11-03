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

#include <openmeap-slic-core.h>

#include <Foundation/Foundation.h>
#include <UIKit/UIKit.h>


void om_ios_prefs_clear(om_prefs_ptr prefs) {
	
	[(NSMutableDictionary*)prefs->device_data release];
	NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
	
	NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithCapacity:10];
	[defaults setValue:dict forKey:[NSString stringWithUTF8String:prefs->name]];
	prefs->device_data = dict;
	[defaults synchronize];
}

om_bool om_ios_prefs_set(const om_prefs_ptr prefs, const char *key, const char *value) {
	NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
	NSMutableDictionary *dict = (NSMutableDictionary *)prefs->device_data;
	NSString *nsKey = [NSString stringWithUTF8String:key];
	NSString *nsValue = [NSString stringWithUTF8String:value];
	[dict setValue:nsValue forKey:nsKey];
	[defaults setValue:dict forKey:[NSString stringWithUTF8String:prefs->name]];
	[defaults synchronize];
	return OM_TRUE;
}

char * om_ios_prefs_get(const om_prefs_ptr prefs, const char *key) {
	NSDictionary *dict = (NSDictionary*)prefs->device_data;
	NSString *str = [dict objectForKey:[NSString stringWithUTF8String:key]];	
	if( str==nil ) {
		om_error_set_code(OM_ERR_NONE);
		return OM_NULL;
	}
	const char *cStr = [str cStringUsingEncoding:NSUTF8StringEncoding];
	int size = (strlen(cStr)*sizeof(char))+1;
	char *val = malloc(size);
	if( val==NULL ) {
		om_error_set_code(OM_ERR_MALLOC);
		return OM_NULL;
	}	
	memset(val,0,size);
	memcpy(val,cStr,size);
	return val;
}

void om_ios_prefs_remove(const om_prefs_ptr prefs, const char *key) {
	NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
	NSMutableDictionary *dict = (NSMutableDictionary*)prefs->device_data;
	[dict setValue:nil forKey:[NSString stringWithUTF8String:key]];
	[defaults setValue:dict forKey:[NSString stringWithUTF8String:prefs->name]];
	[defaults synchronize];
}

//////////////////////////

om_prefs_ptr om_prefs_acquire(const char *name) {
	
	if( strlen(name)>256 ) {
		om_error_set_code(OM_ERR_STRLEN);
		return OM_NULL;
	}
	
	// allocate our abstracted container
	// and copy the preference stores name to it
	om_prefs_ptr toret = om_malloc(sizeof(om_prefs));
	if( toret==NULL ) {
		om_error_set_code(OM_ERR_MALLOC);
		return OM_NULL;
	}
	memcpy(&toret->name,name,strlen(name)+1);
	
	// obtain the Application preferences domain
	// and create a dictionary for use under the "name"
	// otherwise use the existing one
	NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
	NSMutableDictionary *dict = [defaults objectForKey:[NSString stringWithUTF8String:name]];
	if( dict==nil ) {
		dict = [NSMutableDictionary dictionaryWithCapacity:10];
		[defaults setValue:dict forKey:[NSString stringWithUTF8String:name]];
		[defaults synchronize];
	}
	
	{
		// if the dict was already in the user defaults, 
		// then we now have an immutable dictionary...which is not what we need
		// The following line does not create a deep copy:
		//   dict = [NSMutableDictionary initWithDictionary:dict copyItems:TRUE];
		// So we are stuck with older functionallity:
		NSMutableDictionary *copiedDict = (NSMutableDictionary*) CFPropertyListCreateDeepCopy (
				kCFAllocatorDefault,
				dict,
				kCFPropertyListMutableContainersAndLeaves
			);
		//[dict release];
		dict = copiedDict;
		//[dict autorelease];		
	}
	//[dict retain];
	toret->device_data = dict;
	toret->clear = om_ios_prefs_clear;
	toret->get = om_ios_prefs_get;
	toret->remove = om_ios_prefs_remove;
	toret->set = om_ios_prefs_set;
	
	return toret;
}

void om_prefs_release(om_prefs_ptr prefs) {
	if( prefs->device_data!=OM_NULL ) {
		NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
		NSMutableDictionary *dict = prefs->device_data;
		[defaults setValue:dict forKey:[NSString stringWithUTF8String:prefs->name]];
		[defaults synchronize];
		//[dict release];
	}
	om_free(prefs);
}
