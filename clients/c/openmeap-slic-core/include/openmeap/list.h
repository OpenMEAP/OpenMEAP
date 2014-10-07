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

#ifndef __OPENMEAP_LIST_H__
#define __OPENMEAP_LIST_H__

typedef struct om_list_item_type {
	struct om_list_item_type *next;
	void * item;
} om_list_item, *om_list_item_ptr;

typedef struct om_list_type {
	om_list_item_ptr top;
	void * (*copy_func)(void *value);
	void *release_data;
	void (*release_func)(void *value, void *release_data);
} om_list, *om_list_ptr;

om_list_ptr om_list_new();

/**
 * Releases the entire list.
 * If release_func is null, then free() is called on each item.
 * Elsewise, release_func is called in the place of free.
 *
 * @param ptr An element of the list (doesn't matter, each element has top)
 */
void om_list_release(om_list_ptr list);

om_bool om_list_prepend(om_list_ptr list, void *item);

/**
 * @return OM_TRUE if successful, otherwise OM_FALSE; sets the error flag
 */
om_bool om_list_append(om_list_ptr list, void *item);

om_bool om_list_remove(om_list_ptr list, void *item);

om_bool om_list_remove_index(om_list_ptr list, long index);

om_bool om_list_replace(om_list_ptr list, void *item, void *new_item);

long om_list_get_index(om_list_ptr list, void *item);

void * om_list_get(om_list_ptr list, long index);

long om_list_count(om_list_ptr list);

void om_list_release_default_func(void *value, void *release_data);

#endif
