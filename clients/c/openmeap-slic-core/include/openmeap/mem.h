/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
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

#ifndef __OPENMEAP_MEM_H__
#define __OPENMEAP_MEM_H__

#if OM_DEBUG == OM_TRUE
	OM_EXPORT struct om_dict_type * om_mallocs;

	#define om_malloc(PTR) __om_malloc_debug(PTR,__FILE__,__LINE__)
	#define om_free(PTR) __om_free_debug(PTR,__FILE__,__LINE__); PTR=OM_NULL;

	OM_EXPORT void * __om_malloc_debug(om_uint32 length, const char *file, const int line);
	OM_EXPORT void __om_free_debug(void *ptr, const char *file, const int line);
#else
	#define om_malloc(PTR) __om_malloc_regular(PTR);
	#define om_free(PTR) __om_free_regular(PTR); PTR=OM_NULL;
#endif

OM_EXPORT void * __om_malloc_regular(om_uint32 length);
OM_EXPORT void __om_free_regular(void *ptr);

#endif
