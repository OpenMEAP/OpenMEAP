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

#include <openmeap-slic-core.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

char * last_message = OM_NULL;

const char *om_error_messages[] = {
    //OM_ERR_NONE
	"No error has occurred",
    //OM_ERR_APP_SVC_CONN,
	"An error occurred trying to connect to the application management service",
    //OM_ERR_MALLOC,      
	"The application was unable to allocate memory for an operation",
    //OM_ERR_STRLEN,
	"A c-string parameter's length was out of specification",
    //OM_ERR_NOTFOUND, 
	"A required resource could not be located",
    //OM_ERR_NET_CONN,  
	"A network connection failed",
    //OM_ERR_NET_DOWNLOAD,
    "Download failed",
    //OM_ERR_XML_PARSE,
    "Failed to parse xml",
    //OM_ERR_FILE_READ,
    "Failed to read from a file",
    //OM_ERR_FILE_WRITE,
    "Failed to write to a file",
    //OM_ERR_FILE_OPEN,
    "Failed to open a file",
    //OM_ERR_FILE_CLOSE,
    "Failed to close a file",
    //OM_ERR_FILE_DELETE,
    "Failed to delete a file",
    //OM_ERR_FILE_SPACE,
    "File system lacks space for the operation",
    //OM_ERR_FILESYS_SPACE_AVAIL,
    "File system lacks space for the operation",
    //OM_ERR_DIR_DEL_RECURSE,
    "Failed to recursively delete a directory tree",
    //OM_ERR_ZIP_GLOBALINFO
    "Zip file global info is corrupted",
    //OM_ERR_PLATFORM
    "An undefined platform error has occurred",
    //OM_ERR_HASH_MISMATCH,
    "The archive integrity check failed",
    //OM_ERR_IO_EXCEPTION,
    "An issue occurred downloading the archive",
    //OM_ERR_INTERRUPTED,
    "The process was interrupted",
    //OM_ERR_ZIP_FILE
    "Unable to process zip archive"
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

