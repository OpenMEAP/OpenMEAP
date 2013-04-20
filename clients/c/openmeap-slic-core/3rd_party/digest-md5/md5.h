/* See md5.c for explanation and copyright information.  */

/*
 * $FreeBSD: src/contrib/cvs/lib/md5.h,v 1.2 1999/12/11 15:10:02 peter Exp $
 */

#ifndef MD5_H
#define MD5_H

/* Add prototype support.  */
#ifndef PROTO
	#if defined (USE_PROTOTYPES) ? USE_PROTOTYPES : defined (__STDC__)
		#define PROTO(ARGS) ARGS
	#else
		#define PROTO(ARGS) ()
	#endif
#endif

/* Unlike previous versions of this code, uint32 need not be exactly
 32 bits, merely 32 bits or more.  Choosing a data type which is 32
 bits instead of 64 is not important; speed is considerably more
 important.  ANSI guarantees that "unsigned long" will be big enough,
 and always using it seems to have few disadvantages.  */
typedef unsigned long cvs_uint32;

typedef struct cvs_MD5Context {
	cvs_uint32 buf[4];
	cvs_uint32 bits[2];
	unsigned char in[64];
} cvs_MD5Context_t;

void cvs_MD5Init PROTO ((struct cvs_MD5Context *context));
void cvs_MD5Update PROTO ((struct cvs_MD5Context *context,
						   unsigned char const *buf, unsigned len));
void cvs_MD5Final PROTO ((unsigned char digest[16],
						  struct cvs_MD5Context *context));
void cvs_MD5Transform PROTO ((cvs_uint32 buf[4], const unsigned char in[64]));

#endif /* !MD5_H */

