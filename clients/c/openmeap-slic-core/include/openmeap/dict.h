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

#ifndef __OPENMEAP_DICT_H__
#define __OPENMEAP_DICT_H__

// NOTE: unless copy function pointers are assigned,
// all pointers returned by get_keys and get should be
// the same as passed in.

typedef struct om_dict_entry_type {
	void * key;
	void * value;
} om_dict_entry, *om_dict_entry_ptr;

typedef struct om_dict_type {
	om_uint64 (*hash_func)(void *keyVal);
	void *    (*copy_value_func)(void *value);
	void *    (*copy_key_func)(void *value);
	/**
	 * Function to use to free the key's and value's
	 * memory.  Defaults to free(void *), but could
	 * easily be replaced by whatever.
	 */
	void   (*release_func)(void *key, void *value, void *release_data);
	/**
	 * Passed to the release function.  May be used for passing context, etc.
	 */
	void * release_data;
	long   bucket_count;
	om_list_ptr * buckets;
} om_dict, *om_dict_ptr;

OM_EXPORT om_uint64 om_dict_hash_string(void * string_key);

OM_EXPORT om_dict_ptr om_dict_new(int size);

OM_EXPORT om_bool om_dict_put(om_dict_ptr dict, void *key, void *val);

OM_EXPORT void * om_dict_get(om_dict_ptr dict, void *key);

OM_EXPORT void * om_dict_remove(om_dict_ptr dict, void *key);

OM_EXPORT om_bool om_dict_clear(om_dict_ptr dict);

/**
 * @return a list that the caller must release
 */
OM_EXPORT om_list_ptr om_dict_get_keys(om_dict_ptr dict);

OM_EXPORT int om_dict_count(om_dict_ptr dict);

OM_EXPORT void om_dict_release(om_dict_ptr dict);

OM_EXPORT void om_dict_release_default_func(void *key, void *value, void *release_data);

OM_EXPORT void om_dict_release_prefs_by_name_func(void *key, void *value, void *release_data);

OM_EXPORT om_dict_ptr om_dict_from_query_string(const char *queryString);

#endif
