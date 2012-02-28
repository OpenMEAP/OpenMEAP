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

/* private function declarations */

OM_PRIVATE_FUNC om_dict_entry_ptr __om_dict_new_entry(void * key, void * val);
OM_PRIVATE_FUNC om_list_ptr __om_dict_get_bucket_for_key(om_dict_ptr dict, void *key);
OM_PRIVATE_FUNC om_dict_entry_ptr __om_dict_get_entry(om_dict_ptr ptr, void *key);
OM_PRIVATE_FUNC void __om_dict_release_entry_func(void *entry, void *release_data);
OM_PRIVATE_FUNC void __om_dict_free_key_value(om_dict_ptr dict, void *key, void *val);

/* implementation */

/**
 * @return om_dict_ptr where the hash_func is the default om_dict_hash_string function
 */
om_dict_ptr om_dict_new(int size) {
	om_dict_ptr ret = om_malloc(sizeof(om_dict));
	if( ret==OM_NULL ) {
		om_error_set( OM_ERR_MALLOC, "Could not allocate a new dict" );
		return OM_NULL;
	}
	ret->hash_func = om_dict_hash_string;
	ret->bucket_count = size;
	ret->buckets = om_malloc(sizeof(om_list_ptr)*size);
	return ret;
}

void om_dict_release(om_dict_ptr dict) {
	om_dict_clear(dict);
	om_free(dict->buckets);
	om_free(dict);
}

void om_dict_release_default_func(void *key, void *value, void *release_data) {
	om_free(key);
	om_free(value);
}

void om_dict_release_prefs_by_name_func(void *key, void *value, void *release_data) {
	om_free(key);
	om_prefs_release(value);
}

om_dict_ptr om_dict_from_query_string(const char *queryString) {
    
    om_list_ptr keyValPairs = om_string_explode(queryString,'&');
    om_dict_ptr ret = om_dict_new(5);
    ret->release_func=om_dict_release_default_func;
    int c = om_list_count(keyValPairs);
    for( int i=0; i<c; i++ ) {
        char * val = om_list_get(keyValPairs,i);
        om_list_ptr keyVal = om_string_explode( val, '=' );
        if( om_list_count(keyVal)==2 ) {
           om_dict_put( ret, om_string_copy(om_list_get(keyVal,0)), om_string_copy(om_list_get(keyVal,1)) );
        } else if( om_list_count(keyVal)==1 ) {
           om_dict_put( ret, om_string_copy(om_list_get(keyVal,0)), "" ); 
        }
        om_list_release(keyVal);
    }
    om_list_release(keyValPairs);
    return ret;
}

om_bool om_dict_put(om_dict_ptr dict, void *key, void *val) {
	
	om_uint64 hash = dict->hash_func(key);
	int index = hash % dict->bucket_count;
	om_list_ptr bucket = __om_dict_get_bucket_for_key(dict,key);
	void * value = val;
	void * key_ins = key;
	
	// if the bucket has not been initialized
	if( bucket==OM_NULL ) {
		bucket = dict->buckets[index] = om_list_new();
		if( bucket==OM_NULL ) {
			return OM_FALSE;
		}
		bucket->release_func = __om_dict_release_entry_func;
		bucket->release_data = dict;
	}
	
	// if a copy function for incoming values has been assigned
	if( dict->copy_value_func!=OM_NULL ) {
		value = dict->copy_value_func(val);
		if( value == OM_NULL ) {
			return OM_FALSE;
		}
	}
	
	// if a key copy function has been assigned
	if( dict->copy_key_func!=OM_NULL ) {
		key_ins = dict->copy_key_func(val);
		if( key_ins == OM_NULL ) {
			__om_dict_free_key_value(dict,key,value);
			return OM_FALSE;
		}
	}
	
	// try to find an existing dictionary entry for the key
	om_dict_entry_ptr ent = __om_dict_get_entry(dict,key_ins);
	if( ent==OM_NULL ) {
		
		ent = __om_dict_new_entry(key_ins,value);
		if( ent==OM_NULL ) {
			return OM_FALSE;
		}
		
		if( ! om_list_append(bucket,ent) ) {
			return OM_FALSE;
		}
	} 
	
	// an existing entry was found, so we'll just replace the value
	else {
		
		__om_dict_free_key_value(dict,ent->key,ent->value);
		ent->key=key_ins;
		ent->value=val;
	}
	return OM_TRUE;				   
}

void * om_dict_get(om_dict_ptr dict, void *key) {
	om_dict_entry_ptr ptr = __om_dict_get_entry(dict,key);
	if( ptr==OM_NULL )
		return OM_NULL;
	return ptr->value;
}

void * om_dict_remove(om_dict_ptr dict, void *key) {
    
    void * toret = OM_NULL;
    
	om_uint64 hash = dict->hash_func(key);
	int bucket_index = hash % dict->bucket_count;
	om_list_ptr bucket = dict->buckets[bucket_index];
    
    // om_list_ptr bucket = om_dict_get_bucket_for_key(dict,key);
	
	if( bucket==OM_NULL ) {
		return OM_NULL;
	}
	
	int end = om_list_count(bucket);
	for( int i=0; i<end; i++ ) {
		om_dict_entry_ptr ent = om_list_get(bucket,i);
		if( dict->hash_func(ent->key) == dict->hash_func(key) ) {
			toret = ent->value;
			om_list_remove(bucket,ent);
            break;
		}
	}
    
    // if it was the only value in the bucket,
    // then go ahead and free-up the list of the
    // bucket
    if(end==1) {
        om_list_release(dict->buckets[bucket_index]);
        dict->buckets[bucket_index] = OM_NULL;			
    }
	
	return toret;
}

om_bool om_dict_clear(om_dict_ptr dict) {
	for( int i=0; i < dict->bucket_count; i++ ) {
		if( dict->buckets[i]!=OM_NULL ) {
			om_list_release(dict->buckets[i]);
			dict->buckets[i]=OM_NULL;
		}
	}
	return OM_TRUE;
}

int om_dict_count(om_dict_ptr dict) {
	int cnt = 0;
	for( int i=0; i<dict->bucket_count; i++ ) {
		if( dict->buckets[i]!=OM_NULL ) {
			cnt += om_list_count(dict->buckets[i]);
		}
	}
	return cnt;
}

om_list_ptr om_dict_get_keys(om_dict_ptr dict) {
	om_list_ptr lst = om_list_new();
	if( lst == OM_NULL ) {
		return OM_NULL;
	}
	// set the release_func to null, 
	// so that the caller doesn't inadvertantly
	// free their keys
	lst->release_func=OM_NULL;
	
	for( int i=0; i<dict->bucket_count; i++ ) {
		if( dict->buckets[i]!=OM_NULL ) {
			int cnt = om_list_count(dict->buckets[i]);
			for( int j=0; j<cnt; j++ ) {
				om_dict_entry_ptr ent = om_list_get(dict->buckets[i],j);
				om_list_append(lst,ent->key);
			}
		}
	}
	return lst;
}

/* Provided hash algorithms */

om_uint64 om_dict_hash_string(void * string_key) {
	char * str = (char *)string_key;
	int i = 0;
	char code = 0;
	while(str[i++]!='\0') {
		code = code ^ str[i-1];
	}
	return (int)code;
}

/* private function definitions */

void __om_dict_release_entry_func(void* entry, void *release_data) {
	om_dict_entry_ptr ent = (om_dict_entry_ptr)entry;
	om_dict_ptr dict = (om_dict_ptr)release_data;
	__om_dict_free_key_value(dict,ent->key,ent->value);
	om_free(ent);
}

void __om_dict_free_key_value(om_dict_ptr dict, void *key, void *val) {
	if( dict->release_func!=OM_NULL ) {
		dict->release_func(key,val,dict->release_data);
	}
}

om_list_ptr __om_dict_get_bucket_for_key(om_dict_ptr dict, void *key) {
	om_uint64 hash = dict->hash_func(key);
	int index = hash % dict->bucket_count;
	om_list_ptr bucket = dict->buckets[index];
	if( bucket==OM_NULL )
		return OM_NULL;
	return bucket;
}

om_dict_entry_ptr __om_dict_get_entry(om_dict_ptr dict, void *key) {
	om_list_ptr bucket = __om_dict_get_bucket_for_key(dict,key);
	if( bucket==OM_NULL )
		return OM_NULL;
	int i = om_list_count(bucket);
	for( int j=0; j<i; j++ ) {
		om_dict_entry_ptr item = om_list_get(bucket,j);
		if( dict->hash_func(item->key)==dict->hash_func(key) ) {
			return item;
		}
	}	
	return OM_NULL;
}

om_dict_entry_ptr __om_dict_new_entry(void * key, void * val) {
	om_dict_entry_ptr ret = om_malloc(sizeof(om_dict_entry));
	if( ret==NULL ) {
		om_error_set(OM_ERR_MALLOC,"Couldn't allocate a new dict entry");
		return OM_NULL;
	}
	ret->key=key;
	ret->value=val;
	return ret;
}

