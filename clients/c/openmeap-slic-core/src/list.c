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

#include <openmeap-slic-core.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* non-exported function declarations */

OM_PRIVATE_FUNC om_list_item_ptr __om_list_new_item(void *item);
OM_PRIVATE_FUNC om_list_item_ptr __om_list_get_end(om_list_ptr list);
OM_PRIVATE_FUNC om_bool __om_list_remove_item(om_list_ptr list, om_list_item_ptr cur, om_list_item_ptr prev);
OM_PRIVATE_FUNC om_list_item_ptr __om_list_get_item_by_index(om_list_ptr list, long index);

/* exported function definitions */

om_list_ptr om_list_new() {
	om_list_ptr ptr = om_malloc(sizeof(om_list));
	if( ptr == NULL ) {
		om_error_set(OM_ERR_MALLOC,"Could not allocate a new list item");
		return OM_NULL;
	}
	ptr->release_func = om_list_release_default_func;
	return ptr;
}

void om_list_release(om_list_ptr list) {
	om_list_item_ptr cur = list->top;
	om_list_item_ptr next = list->top;
	void *item_ptr;
	while(next!=NULL) {
		next = cur->next;
		item_ptr = cur->item;
		om_free(cur);
		if( list->release_func!=OM_NULL )
			list->release_func(item_ptr,list->release_data);
		cur=next;
	}
	om_free(list);
}

om_bool om_list_prepend(om_list_ptr list, void *item) {
	om_list_item_ptr new_item = __om_list_new_item(item);
	if( new_item==OM_NULL ) {
		return OM_FALSE;
	}
	new_item->next = list->top;
	list->top = new_item;
	return OM_TRUE;
}

om_bool om_list_append(om_list_ptr list, void *item) {
	om_list_item_ptr new_item = __om_list_new_item(item);
	if( new_item == NULL ) {
		return OM_FALSE;
	}
	
	// either we have no top yet
	if( list->top==OM_NULL ) {
		list->top = new_item;
	} 
	// or we should tack this onto the end
	else {
		om_list_item_ptr end = __om_list_get_end(list);
		end->next = new_item;
	}
	
	new_item->next = OM_NULL;
	
	return OM_TRUE;
}

om_bool om_list_remove(om_list_ptr list, void *item) {
	om_list_item_ptr cur = list->top;
	om_list_item_ptr prev = list->top;
	while( cur!=NULL ) {
		if( cur->item==item ) {
			return __om_list_remove_item(list,cur,prev);
		} 
		prev=cur;
		cur=cur->next;
	}
	return OM_FALSE;
}

om_bool om_list_remove_index(om_list_ptr list, long item) {
	om_list_item_ptr ptr = __om_list_get_item_by_index(list,item);
	if( ptr!=OM_NULL )
		return om_list_remove(list,ptr->item);
	else return OM_FALSE;
}

om_bool om_list_replace(om_list_ptr list, void *item, void *new_item) {
	om_list_item_ptr cur = list->top;
	while( cur!=NULL ) {
		if( cur->item == item ) {
			cur->item = new_item;
			return OM_TRUE;
		}
		cur=cur->next;
	}
	return OM_FALSE;
}

long om_list_get_index(om_list_ptr list, void *item) {
	om_list_item_ptr cur = list->top;
	long count = 0;
	while( cur!=NULL ) {
		if( cur->item==item )
			return count;
		cur=cur->next;
		count++;
	}
	return (-1);
}

void * om_list_get(om_list_ptr list, long item) {
	om_list_item_ptr cur = list->top;
	long count = 0;
	while( cur!=NULL ) {
		if( count==item )
			return cur->item;
		cur=cur->next;
		count++;
	}
	return OM_NULL;
}

long om_list_count(om_list_ptr list) {
	om_list_item_ptr cur = list->top;
	long count = 0;
	while( cur!=NULL ) {
		cur=cur->next;
		count++;
	}
	return count;
}

void om_list_release_default_func(void *value, void *release_data) {
	om_free(value);
}

/* private functions */

om_list_item_ptr __om_list_get_item_by_index(om_list_ptr list, long index) {
	om_list_item_ptr cur = list->top;
	long count=0;
	while( cur!=NULL ) {
		if( count==index )
			return cur;
		cur=cur->next;
		count++;
	}
	return OM_NULL;
}

om_list_item_ptr __om_list_get_end(om_list_ptr list) {
	om_list_item_ptr cur = list->top;
	om_list_item_ptr last = list->top;
	while( cur!=NULL ) {
		last=cur;
		cur=cur->next;
	}
	return last;
}

om_list_item_ptr __om_list_new_item(void *item) {
	om_list_item_ptr toret = om_malloc(sizeof(om_list_item));
	if( toret==OM_NULL ) {
		om_error_set(OM_ERR_MALLOC,"Failed to allocate a new list item");		
		return OM_NULL;
	}
	toret->item = item;
	return toret;
}

om_bool __om_list_remove_item(om_list_ptr list, om_list_item_ptr cur, om_list_item_ptr prev) {
	
	if( list->release_func!=NULL )
		list->release_func(cur->item,list->release_data);
	
	// item is top
	if( cur==list->top ) {
		list->top = cur->next;		
	} 
	// item is last
	else if( cur->next==OM_NULL ) {
		prev->next = OM_NULL;
	}
	// item is somewhere between
	else {
		prev->next = cur->next;
	}
	om_free(cur);
	return OM_TRUE;
}

