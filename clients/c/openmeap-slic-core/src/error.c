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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

char * last_message = OM_NULL;

const char *om_error_messages[] = {
	"No error has occurred",
	"An error occurred trying to connect to the application management service.",
	"The application was unable to allocate memory for an operation.",
	"A c-string parameter's length was out of specification.",
	"A required resource could not be located",
	"A network connection failed"
};

om_error_code om_last_error_code = OM_ERR_NONE;

void om_error_clear() {
	om_last_error_code = OM_ERR_NONE;
	if( last_message!=OM_NULL ) {
		om_free(last_message);
		last_message=OM_NULL;
	}
}

om_error_code om_error_get_code() {
	return om_last_error_code;
}

void om_error_set_code(om_error_code code) {
	if( last_message!=OM_NULL ) {
		om_free(last_message);
		last_message=OM_NULL;
	}
	om_last_error_code = code;
}

void om_error_set(om_error_code code, const char * message) {
	om_error_set_code(code);
	if( last_message!=OM_NULL ) {
		om_free(last_message);
		last_message=OM_NULL;
	}
	if( om_last_error_code != OM_ERR_MALLOC )
		last_message = om_string_copy(message);
}

void om_error_set_format(om_error_code code, const char *format, ...) {
	char *str = om_malloc(OM_STRING_FORMAT_LIMIT+1);
	va_list ap;
	va_start(ap,format);
	vsnprintf(str, OM_STRING_FORMAT_LIMIT, format, ap);
	va_end(ap);
	om_error_set(code,str);
	om_free(str);
}

const char * om_error_get_message_for_code(om_error_code code) {
	return om_error_messages[code];
}

const char * om_error_get_message(void) {
	if( last_message!=OM_NULL )
		return last_message;
	return om_error_get_message_for_code(om_error_get_code());
}

