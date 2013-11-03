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

#include "../3rd_party/digest-md5/md5.h"
#include "../3rd_party/digest-sha1/sha1.h"

#define OM_DIGEST_BUFFER_LEN_MD5 16
#define OM_DIGEST_BUFFER_LEN_SHA1 1

const char * OmHashTypeMd5 = "MD5";
const char * OmHashTypeSha1 = "SHA1";

OM_PRIVATE_FUNC char * __om_digest_md5_file(om_file_input_stream *istream);
OM_PRIVATE_FUNC char * __om_digest_sha1_file(om_file_input_stream *istream);
OM_PRIVATE_FUNC char * __om_digest_md5_string(char *str);
OM_PRIVATE_FUNC char * __om_digest_sha1_string(char *str);

char * om_digest_hash_file(struct om_file_input_stream_type *istream, om_hash_enum algorithm) {
	switch(algorithm) {
		case OM_HASH_MD5:  return __om_digest_md5_file(istream);
		case OM_HASH_SHA1: return __om_digest_sha1_file(istream);
	}
	return OM_NULL;
}

char * om_digest_hash_string(char *str, om_hash_enum algorithm) {
	switch(algorithm) {
		case OM_HASH_MD5:  return __om_digest_md5_string(str);
		case OM_HASH_SHA1: return __om_digest_sha1_string(str);
	}
	return OM_NULL;
}

const char * om_hash_enum_to_string(om_hash_enum algorithm) {
    switch(algorithm) {
        case OM_HASH_MD5:  return OmHashTypeMd5;
        case OM_HASH_SHA1: return OmHashTypeSha1;
    }
}

om_hash_enum om_hash_string_to_enum(const char * algorithm) {
    char *copy = om_string_toupper(algorithm);
    if( strcmp(copy,OmHashTypeMd5)==0 ) {
        om_free(copy);return OM_HASH_MD5;
    }
    if( strcmp(copy,OmHashTypeSha1)==0 ) {
        om_free(copy);return OM_HASH_SHA1;
    }
    om_free(copy);return 0;
}

OM_PRIVATE_FUNC char * __om_digest_md5_file(om_file_input_stream *istream) {
	
	cvs_MD5Context_t ctx;
	cvs_MD5Context_t *pctx=&ctx;
	memset(pctx,sizeof(cvs_MD5Context_t),0);
	unsigned char buffer[OM_DIGEST_BUFFER_LEN_MD5];
	memset(buffer,0,OM_DIGEST_BUFFER_LEN_MD5);
	int r=0;
	unsigned char digest[16];
	
	// update the md5 context for each chunk
	// of the md5 context
	cvs_MD5Init(pctx);
	while( (r=om_storage_read(istream,buffer,OM_DIGEST_BUFFER_LEN_MD5)) && r!=(-1) ) {
		
		// try to align to 32 bits
		//int len = r/4;
		//if( r%4!=0 ) r+=4;
		
		cvs_MD5Update(pctx,buffer,r);
		memset(buffer,0,OM_DIGEST_BUFFER_LEN_MD5);
	}
	if( r==(-1) )
		return OM_NULL;
	cvs_MD5Final(digest,pctx);
	
	// put the final md5 sum into a character array
	// and pass back
	int i;
	char * p = om_malloc(33);
	if( p==NULL )
		return OM_NULL;
	unsigned char * ret = p;
	memset(p,33,0);
	for (i = 0; i < 16; i++) {
		sprintf (p, "%02x", digest[i]);
		p+=2;
	}
	return ret;
	
}

OM_PRIVATE_FUNC char * __om_digest_sha1_file(om_file_input_stream *istream) {

	SHA1Context ctx;
	SHA1Context *pctx=&ctx;
	memset(pctx,0,sizeof(SHA1Context));
	unsigned char buffer[OM_DIGEST_BUFFER_LEN_SHA1];
	memset(buffer,0,OM_DIGEST_BUFFER_LEN_SHA1);
	
	int r=0;
	
	// update the md5 context for each chunk
	// of the md5 context
	SHA1Reset(pctx);
	while( (r=om_storage_read(istream,buffer,OM_DIGEST_BUFFER_LEN_SHA1)) && r!=(-1) ) {
		
		// try to align to 32 bits
		//int len = r/4;
		//if( r%4!=0 ) r+=4;
		
		SHA1Input(pctx,buffer,r);
		memset(buffer,0,OM_DIGEST_BUFFER_LEN_SHA1);
	}
	if( r==(-1) )
		return OM_NULL;
	SHA1Result(pctx);
	
	// put the final sha1 sum into a character array
	// and pass back
	char * p = om_malloc(41);
	if( p==NULL )
		return OM_NULL;
	sprintf(p,"%08x%08x%08x%08x%08x",
			ctx.Message_Digest[0],
			ctx.Message_Digest[1],
			ctx.Message_Digest[2],
			ctx.Message_Digest[3],
			ctx.Message_Digest[4]);
	return p;
}


OM_PRIVATE_FUNC char * __om_digest_md5_string(char *str) {
	
	cvs_MD5Context_t ctx;
	cvs_MD5Context_t *pctx=&ctx;
	memset(pctx,sizeof(cvs_MD5Context_t),0);
	unsigned char buffer[OM_DIGEST_BUFFER_LEN_MD5];
	memset(buffer,0,OM_DIGEST_BUFFER_LEN_MD5);
	int r=0;
	unsigned char digest[16];
	
	// update the md5 context for each chunk
	// of the md5 context
	cvs_MD5Init(pctx);
    char *end = str;
    end += strlen(str);
	for( str=str; str < end; str+=OM_DIGEST_BUFFER_LEN_MD5 ) {
        char * new_str = om_string_substr(str, 0, OM_DIGEST_BUFFER_LEN_MD5);
        r = strlen(new_str);
		cvs_MD5Update(pctx,new_str,r);
		om_free(new_str);
	}
	if( r==(-1) )
		return OM_NULL;
	cvs_MD5Final(digest,pctx);
	
	// put the final md5 sum into a character array
	// and pass back
	int i;
	char * p = om_malloc(33);
	if( p==NULL )
		return OM_NULL;
	unsigned char * ret = p;
	memset(p,33,0);
	for (i = 0; i < 16; i++) {
		sprintf (p, "%02x", digest[i]);
		p+=2;
	}
	return ret;
	
}

OM_PRIVATE_FUNC char * __om_digest_sha1_string(char *str) {
    
	SHA1Context ctx;
	SHA1Context *pctx=&ctx;
	memset(pctx,0,sizeof(SHA1Context));
	unsigned char buffer[OM_DIGEST_BUFFER_LEN_SHA1];
	memset(buffer,0,OM_DIGEST_BUFFER_LEN_SHA1);
	
	int r=0;
	
	// update the md5 context for each chunk
	// of the md5 context
	SHA1Reset(pctx);
    char *end = str;
    end += strlen(str);
    for( str=str; str < end; str+=OM_DIGEST_BUFFER_LEN_MD5 ) {
        char * new_str = om_string_substr(str, 0, OM_DIGEST_BUFFER_LEN_MD5);
        r = strlen(new_str);
		SHA1Input(pctx,new_str,r);
		om_free(new_str);
	}
	if( r==(-1) )
		return OM_NULL;
	SHA1Result(pctx);
	
	// put the final sha1 sum into a character array
	// and pass back
	char * p = om_malloc(41);
	if( p==NULL )
		return OM_NULL;
	sprintf(p,"%08x%08x%08x%08x%08x",
			ctx.Message_Digest[0],
			ctx.Message_Digest[1],
			ctx.Message_Digest[2],
			ctx.Message_Digest[3],
			ctx.Message_Digest[4]);
	return p;
}
