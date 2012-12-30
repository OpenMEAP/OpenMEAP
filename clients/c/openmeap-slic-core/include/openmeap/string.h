/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2013 OpenMEAP, Inc.                                   #
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

#ifndef __OPENMEAP_STRING_H__
#define __OPENMEAP_STRING_H__

#define OM_STRING_FORMAT_LIMIT (size_t)4096

typedef struct om_substring_type {
	char *str;
	int len;
} om_substring, *om_substring_ptr;

OM_EXPORT om_substring_ptr om_substring_new(const char *str, int len);
OM_EXPORT void om_substring_release(om_substring_ptr str);

OM_EXPORT om_bool om_substring_equals(om_substring_ptr ptr, char *match);
OM_EXPORT char * om_substring_copy(om_substring_ptr ptr);

OM_EXPORT char * om_string_copy(const char *str);
OM_EXPORT char * om_string_format(const char *formatting, ...);

OM_EXPORT char * om_string_append(const char *str, const char *app);
OM_EXPORT char * om_string_substr(const char *str, int start, int length);
OM_EXPORT char * om_string_tolower(const char *str);
OM_EXPORT char * om_string_toupper(const char *str);

#define om_char_toupper(c) ((c>='a'&&c<='z')?(c-('a'-'A')):c)

#define om_string_is_alpha(c)        ( om_string_is_alpha_lower(c) || om_string_is_alpha_upper(c) )
#define om_string_is_alpha_lower(c)  ( c>='a' && c<='z' )
#define om_string_is_alpha_upper(c)  ( c>='A' && c<='Z' )
#define om_string_is_numeric(c)      ( c>='0' && c<='9' )
#define om_string_is_whitespace(c)   ( c==' ' || c=='\n' || c=='\t' )
#define om_string_is_hex_char(c)     ( (c>='A' && c<='F') || (c>='a' && c<='f') || (c>='0' && c<='9'))
#define om_string_is_alphanumeric(c) ( om_string_is_alpha_lower(c) || om_string_is_alpha_upper(c) || om_string_is_numeric(c) )

#include <openmeap/list.h>

OM_EXPORT om_list_ptr om_string_explode(const char *str, const char sep);
OM_EXPORT char * om_string_implode(om_list_ptr list, const char sep);

OM_EXPORT char * om_string_encodeURI(const char *str);
OM_EXPORT char * om_string_decodeURI(const char *str);

#endif
