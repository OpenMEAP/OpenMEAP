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

#include <openmeap-slic-core.h>

char * om_ios_props_get(om_props_ptr props, const char *key) {
	NSDictionary *dict = (NSDictionary *)props->device_data;
	NSString *val = [dict objectForKey:[NSString stringWithUTF8String:key]];
	if( val==nil ) {
		return OM_NULL;
	}
	char * utf8Str = (char *)[val UTF8String];
	char * toret = malloc(strlen(utf8Str)+1);
	if( toret == NULL ) {
		om_error_set_code(OM_ERR_MALLOC);
		return OM_NULL;
	} 
	memcpy(toret,utf8Str,strlen(utf8Str)+1);
	return toret;
}

/////////////////////////////

om_props_ptr om_props_acquire(const char *name) {
	
	NSString * plistPath = [[NSBundle mainBundle] pathForResource:[NSString stringWithUTF8String:name] ofType:@"plist"];
	//NSString * plistPath = [[NSBundle mainBundle] pathForResource:@"props-test.plist" ofType:@"plist"];
	void * dict = (void*)[NSDictionary dictionaryWithContentsOfFile:plistPath];
	//void * dict = (void*)[NSDictionary dictionaryWithContentsOfFile:[NSString stringWithUTF8String:name]];
	
	if( dict == nil ) {
		om_error_set_code(OM_ERR_NOTFOUND);
		return OM_NULL;
	} else [(NSDictionary*)dict retain];
	
	om_props_ptr props = om_malloc(sizeof(om_props));
	if( props == NULL ) {
		om_error_set_code(OM_ERR_MALLOC);
		return OM_NULL;
	}
	props->device_data = dict;
	props->get = om_ios_props_get;
	
	return props;
}

void om_props_release(om_props_ptr props) {
	if( props->device_data!=OM_NULL )
		[(NSDictionary*)props->device_data release];
	om_free(props);
}