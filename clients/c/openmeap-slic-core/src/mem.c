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

#include <openmeap-slic-core.h>

#include <stdio.h>
#include <stdlib.h>
#include <memory.h>

void * __om_malloc_regular(uint32 length) {
	void * ptr = malloc((size_t)length);
	if( ptr==NULL )
		return OM_NULL;
	else memset(ptr,0,length);
	return ptr;
}

void __om_free_regular(void *ptr) {
	if( ptr!=OM_NULL ) {
		free(ptr);
		ptr=OM_NULL;
	}
}

#if OM_DEBUG == OM_TRUE

/**
 * OM_TRUE if malloc should fail, OM_FALSE if malloc should succeed
 */
om_bool om_malloc_fail = OM_FALSE;

/**
 * A flag that is OM_TRUE if the mem mgmt function is being called for mem mgmt tracking
 * This is so API functionallity, which uses the mem mgmt functions, can be used within
 * the memory management functions.
 */
om_bool om_malloc_tracking = OM_FALSE;

/**
 
 * Memory allocating and freeing tracking information.
 * So that slic core api memory leaks are easier to detect.
 */
struct om_dict_type * om_mallocs = OM_NULL;
int om_malloc_count = 0;

int __om_malloc_debug_hash_func(void *ptr) {
	// default dictionary hashes strings...
	// we are only storing pointers
	// so that is a good enough value
	return (int)ptr;
}

void __om_malloc_debug_release_func(void *key, void *value, void *release_data) {
	// we don't free the key, as the key was a pointer value that had been alloc'd
	om_free(value);
}

void * __om_malloc_debug(uint32 length, const char *file, const int line) {
	
	void * toret = NULL;
	
	if( om_malloc_fail==OM_TRUE ) {
		return NULL;
	} else {
		
		toret = __om_malloc_regular(length);
		
		if( ! om_malloc_tracking && toret!=NULL ) {
			
			om_malloc_tracking = OM_TRUE;
			
			if( om_mallocs == NULL ) {
				om_mallocs = om_dict_new(40);
				((om_dict_ptr)om_mallocs)->release_func=__om_malloc_debug_release_func;
				((om_dict_ptr)om_mallocs)->hash_func=__om_malloc_debug_hash_func;
			}
				
			om_dict_put( om_mallocs, toret, om_string_format("%i:%s",line,file) );
			//printf("adding %8X %s\n",toret,om_dict_get(om_mallocs,toret));
			
			om_malloc_count++;
			
			om_malloc_tracking = OM_FALSE;
		}
	}
	
	return toret;
}

void __om_free_debug(void *ptr, const char *file, const int line) {

	if( ! om_malloc_tracking  && om_mallocs!=OM_NULL ) {
		om_malloc_tracking=OM_TRUE;
		
		//printf("removing %8X %s\n",ptr,om_dict_get(om_mallocs,ptr));
		om_dict_remove(om_mallocs,ptr);
		
		om_malloc_count--;
		if( om_malloc_count==0 ) {
			om_dict_release(om_mallocs);
			om_mallocs=OM_NULL;
		}
		
		om_malloc_tracking=OM_FALSE;
	}
	
	__om_free_regular(ptr);

}

#endif /* OM_DEBUG == OM_TRUE */
