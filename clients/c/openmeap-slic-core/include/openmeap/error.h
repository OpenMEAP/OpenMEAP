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

#ifndef __OPENMEAP_SLIC_CORE_ERROR__
#define __OPENMEAP_SLIC_CORE_ERROR__

typedef unsigned om_error_code;

enum om_error_codes {
	OM_ERR_NONE,         // no error has transpired
	OM_ERR_APP_SVC_CONN, // unable to connect to the service
	OM_ERR_MALLOC,       // unable to allocate memory
	OM_ERR_STRLEN,       // the length of a string was out of specification
	OM_ERR_NOTFOUND,     // a resource could not be found
	OM_ERR_NET_CONN,     // some data connection failed
	OM_ERR_NET_DOWNLOAD,
	OM_ERR_XML_PARSE,
	OM_ERR_FILE_READ,
	OM_ERR_FILE_WRITE,
	OM_ERR_FILE_OPEN,
	OM_ERR_FILE_CLOSE,
	OM_ERR_FILE_DELETE,
	OM_ERR_FILE_SPACE,
	OM_ERR_FILESYS_SPACE_AVAIL, // not enough storage space to perform operation
	OM_ERR_DIR_DEL_RECURSE,
	OM_ERR_ZIP_GLOBALINFO,
    OM_ERR_PLATFORM,
    OM_ERR_HASH_MISMATCH,
    OM_ERR_IO_EXCEPTION,
    OM_ERR_INTERRUPTED,
    OM_ERR_ZIP_FILE
};

/**
 * Clears out the last error
 */
OM_EXPORT void om_error_clear();

/**
 * Returns the code of the last error
 */
OM_EXPORT om_error_code om_error_get_code();

/**
 * Returns a human readable error message for the code passed in
 */
OM_EXPORT const char * om_error_get_message_for_code(om_error_code code);

/**
 * Gets the message associated to the last set error code.
 *
 * @retur
 */
OM_EXPORT const char * om_error_get_message(void);

/**
 * Sets the error code using the canned message.
 *
 * If a custom message was set using om_error_set,
 * then memory for that message is freed.
 *
 * @param code One of the om_error_codes enum error codes.
 */
OM_EXPORT void om_error_set_code(om_error_code code);

/**
 * Sets the error code and a custom error message.
 *
 * The message is copied and the next call to set error
 * will free the memory associated with it.
 *
 * The next call to om_error_get_message()
 *
 * @param code One of the members of the om_error_codes enum
 * @param message The message to set
 */
OM_EXPORT void om_error_set(om_error_code code, const char *message);


OM_EXPORT void om_error_set_format(om_error_code,const char *format, ...);

#endif
