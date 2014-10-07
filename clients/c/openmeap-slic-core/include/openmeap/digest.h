/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
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

#ifndef __OPENMEAP_DIGEST_CONFIG__
#define __OPENMEAP_DIGEST_CONFIG__

typedef enum om_hash_enum_type {
	OM_HASH_MD5=1001,
	OM_HASH_SHA1
} om_hash_enum;

OM_EXPORT const char * OmHashTypeMd5;// = "MD5";
OM_EXPORT const char * OmHashTypeSha1;// = "SHA1";

struct om_file_input_stream_type;
OM_EXPORT const char * om_hash_enum_to_string(om_hash_enum hash);
OM_EXPORT om_hash_enum om_hash_string_to_enum(const char * algorithm);
OM_EXPORT char * om_digest_hash_file(struct om_file_input_stream_type *file, om_hash_enum algorithm);
OM_EXPORT char * om_digest_hash_string(char *file, om_hash_enum algorithm);

#endif
