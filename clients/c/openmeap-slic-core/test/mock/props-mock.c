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

#include <openmeap-slic-core.h>

char * om_mock_props_get(om_props_ptr props, const char *key) {
	char * ptr = om_dict_get(props->device_data,key);
	if( ptr==OM_NULL )
		return ptr;
	return om_string_copy(ptr);
}

///////////////////

om_props_ptr om_props_acquire(const char *name) {
	om_props_ptr props = om_malloc(sizeof(om_props));
	props->device_data = om_dict_new(15);
	//((om_dict_ptr)props->device_data))->copy_value_func
	props->get=om_mock_props_get;
	return props;	
}

void om_props_release(om_props_ptr props) {
	om_dict_release(props->device_data);
	om_free(props);
}
