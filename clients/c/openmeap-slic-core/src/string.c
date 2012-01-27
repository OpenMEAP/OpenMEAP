/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

#include <openmeap-slic-core.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <memory.h>

om_substring_ptr om_substring_new(const char *str, int len) {
	om_substring_ptr ret = om_malloc(sizeof(om_substring));
	if( ret==NULL ) {
		om_error_set(OM_ERR_MALLOC,"Could not malloc() a new substring");
		return OM_NULL;
	}
	ret->str = str;
	ret->len = len;
	return ret;
}

void om_substring_release(om_substring_ptr str) {
	om_free(str);
}

om_bool om_substring_equals(om_substring_ptr ptr, char *match) {
	for( int i=0; i<ptr->len; i++ ) {
		if( ptr->str[i] != match[i] ) {
			return OM_FALSE;
		}
	}
	return OM_TRUE;
}

char * om_substring_copy(om_substring_ptr ptr) {
	char * ret = om_malloc((ptr->len+1)*sizeof(char));
	if( ret==NULL ) {
		om_error_set(OM_ERR_MALLOC,"could not allocate to copy string in om_substring_copy()");
		return OM_NULL;
	}
	memcpy(ret,ptr->str,ptr->len*sizeof(char));
	return ret;
}

char * om_string_format(const char *formatting, ...) {
	char *str = om_malloc(OM_STRING_FORMAT_LIMIT+1);
	if( str==OM_NULL )
		return OM_NULL;
	va_list ap;
	va_start(ap,formatting);
	vsnprintf(str, OM_STRING_FORMAT_LIMIT, formatting, ap);
	va_end(ap);
	char *ret = om_string_copy(str);
	om_free(str);
	if( ret==OM_NULL )
		return OM_NULL;
	return ret;
}

char * om_string_copy(const char *str) {
	char *ret = om_malloc(sizeof(char)*strlen(str)+1);
	if( ret==NULL ) {
		om_error_set(OM_ERR_MALLOC,"Could not allocate to copy string in om_string_copy()");
		return OM_NULL;
	}
	strcpy(ret,str);
	return ret;
}

char * om_string_append(const char *str, const char *app) {
	int size = sizeof(char)*(strlen(str)+strlen(app)+1);
	char * ret = om_malloc(size);
	if( ret==OM_NULL ) {
		om_error_set_code(OM_ERR_MALLOC);
		return OM_NULL;
	}
	memcpy(ret,str,strlen(str));
	strcat(ret,app);
	return ret;	
}

char * om_string_substr(const char *str, int start, int length) {
	char * ret = om_malloc(length+1);
	if( ret==OM_NULL )
		return ret;
    
    int len = strlen(str);
    len = len >= start+length ? length : (len+1)-start;
    
	memcpy(ret,str+start,len);
	return ret;
}

char * om_string_tolower(const char *str_in) {
	char *str = om_string_copy(str_in);
	int i;	
	for (i = 0; str[i] != '\0'; ++i) {
		if (str[i] >= 'A' && str[i] <= 'Z') {
			str[i] = str[i] + ('a'-'A');	
		} 
 	}
	return str;
}

char * om_string_toupper(const char *str_in) {
	char *str = om_string_copy(str_in);
	int i;	
	for (i = 0; str[i] != '\0'; ++i) {
		if (str[i] >= 'a' && str[i] <= 'z') {
			str[i] = str[i] - ('a'-'A');	
		} 
 	}
	return str;
}

om_list_ptr om_string_explode(const char *str, const char sep) {
	om_list_ptr list = om_list_new();
	if( list==OM_NULL )
		return OM_NULL;
	
	char * p = OM_NULL;
	char * seg = str;
	char * start = seg;
	while(1) {
		
		if(*seg==sep||(*seg=='\0'&&start!=seg)) {			
			
			char * p = om_string_substr(start,0,(seg-start));
			if( p == OM_NULL ) {
				// all or nothing
				om_list_release(list);
				return OM_NULL;
			}
			
			if( ! om_list_append(list,p) ) {
				// all or nothing
				om_list_release(list);
				return OM_NULL;
			}
			
			// we may be at the end, so check
			if(*seg=='\0')
				break;
			
			seg++;       // get beyond the '/'
			start = seg; // start tracking a new segment
		}
		
		// we may be at the end, so check
		if(*seg=='\0')
			break;
		
		seg++;
	}
	
	return list;
}

char * om_string_implode(om_list_ptr list, const char sep) {

	int items = om_list_count(list);
	char separ[] = {sep,'\0'};
	char *ret=OM_NULL;
	char *t=OM_NULL;
	
	for( int i=0; i<items; i++ ) {
		char *ptr = om_string_copy( om_list_get(list,i) );
		
		if(ret==OM_NULL) {
			ret=ptr;
		} else {
			t=ret;
			ret = om_string_append(t,ptr);
			om_free(t);
            om_free(ptr);
			if( ret==OM_NULL ) {
				return OM_NULL;
            }
		}
		
		if( i<items-1 ) {
			t=ret;
			ret = om_string_append(t,separ);
			om_free(t);
			if( ret==OM_NULL ) {
				return OM_NULL;
            }
		}
	}
	

	return ret;
}

char * om_string_encodeURI(const char *str) {
	
	const char* reserved = "!’\"();:@&=+$,/?%#[]% \0";
	
	// count characters to encode
	int i=0, encodePadding=0, reservedLen=strlen(reserved);
	while(str[i]!=0) {
		for(int j=0; j<reservedLen; j++) {
			if(reserved[j]==str[i]) {
				encodePadding+=3;
			}
		}
		i++;
	}
	
	// allocate new string
	char *newStr = om_malloc( sizeof(char) * (strlen(str)+encodePadding+1) );
	
	// copy each character, encoding as we go
	i=0;
	char *pos = newStr;
	om_bool charHandled;
	while(str[i]!=0) {
		charHandled=OM_FALSE;
		for(int j=0; j<reservedLen; j++) {
			if(str[i]==reserved[j]) {
				sprintf(pos,"%%%X",str[i]);
				pos+=3;
				charHandled=OM_TRUE;
				break;
			} 
		}
		if( !charHandled ) {
			*pos=str[i];
			pos++;
		}
		i++;
	}
	
	// return new string
	return newStr;
}

char * om_string_decodeURI(const char *str) {
	
	// count characters to decode
	int i=0, charCount=0, len=strlen(str), lenMinus2=len-2;
	while(str[i]!=0) {
        if('%'==str[i]) {
            charCount+=1;
        }
		i++;
	}
	
	// allocate new string
	char *newStr = om_malloc( sizeof(char) * (len-(charCount*2)+1) );
	
	// copy each character, decoding as we go
    i=0;
    for( int p=0; p<len; p++ ) {
        char *p2 = str+p;
        if( *p2=='%' && p<=lenMinus2 && om_string_is_hex_char(*(p2+1)) && om_string_is_hex_char(*(p2+2)) ) {
            char tmp[3]={om_char_toupper(*(p2+1)),om_char_toupper(*(p2+2)),'\0'};
            sscanf(&tmp,"%X",newStr+i);
            p+=2;
        } else {
            newStr[i]=*p2;
        }
        i++;
    }
	
	// return new string
	return newStr;
}
