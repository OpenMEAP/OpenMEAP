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

#include <unistd.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>

#ifndef __UNITTESTS_H__
#define __UNITTESTS_H__

extern int om_malloc_fail;
extern int om_malloc_count;

#define UNIT_TEST(TEST_NAME) TEST_NAME();

/** 
  * When a specific unit-test is failing
  * flip this to true so that fork() is not invoked
  * and you can more easily debug your code.
  */
#define DEBUG_UNIT_TESTS TRUE

#if DEBUG_UNIT_TESTS==FALSE

	#define START_TEST(TEST_NAME) \
		void TEST_NAME() \
		{ \
			char *test_name = #TEST_NAME; \
			char assert_free_str[400]; \
			PRINT_TEST_OPEN(test_name); \
			pid_t test_pid = fork(); \
			if( test_pid == 0 ) { 

	#define END_TEST() \
				exit(0); \
			} else { \
				if( test_pid == (-1) ) { \
					printf("failed to create child process for %s\n",test_name); \
					exit(1); \
				} \
				int test_result = 0; \
				pid_t wait_pid = 0; \
				PRINT_TEST_PID(test_pid); \
				while( (wait_pid = waitpid(test_pid,&test_result,0)) == (-1) ) { \
					switch (errno) { \
						case EINTR: continue; break; \
						default: PRINT_ERROR(strerror(errno)); exit(EXIT_FAILURE); break; \
					} \
				} \
				if( WIFEXITED(test_result)!=0 && WEXITSTATUS(test_result)==EXIT_SUCCESS ) { \
					PRINT_TEST_PASS(); \
				} else { \
					PRINT_TEST_FAIL();\
				} \
				PRINT_TEST_CLOSE(); \
			} \
		}

	#define ASSERT(COND,MESG) if( ! ( COND ) ) { \
			LOG(MESG); \
			exit(1); \
		}
#else
	#define START_TEST(TEST_NAME) \
		void TEST_NAME() { \
			if( TEST_NAME##_run()==1 ) { \
				PRINT_TEST_PASS(); \
			} else { \
				PRINT_TEST_FAIL();\
			} \
			PRINT_TEST_CLOSE(); \
		} \
		om_bool TEST_NAME##_run(); \
		om_bool TEST_NAME##_run() { \
			char *test_name = #TEST_NAME; \
			char assert_free_str[400]; \
			PRINT_TEST_OPEN(test_name); 
			
	#define END_TEST() return 1; } 

	#define ASSERT(COND,MESG) if( ! ( COND ) ) { \
			LOG(MESG); \
			return 0; \
		}
#endif

/**
 * Macro provided to assert that all pointers allocated with om_malloc were freed
 */
#define ASSERT_FREE \
	om_error_clear();\
	if( om_mallocs!=OM_NULL && om_dict_count(om_mallocs) > 0 ) { \
		int om_malloc_initial_count = om_dict_count(om_mallocs); \
		om_list_ptr om_malloc_keys = om_dict_get_keys(om_mallocs); \
		void * om_malloc_cur = OM_NULL; \
		sprintf(assert_free_str,"%i allocated pointers remain.",om_malloc_initial_count); \
		int om_malloc_idx = 0; \
		for( om_malloc_idx = 0; om_malloc_idx < om_malloc_initial_count; om_malloc_idx++ ) { \
			om_malloc_cur = om_list_get(om_malloc_keys,om_malloc_idx); \
			PRINTF_LOG( "%u %s", om_malloc_cur, om_dict_get(om_mallocs,om_malloc_cur) ); \
		} \
		om_list_release(om_malloc_keys); \
		om_dict_release(om_mallocs); \
		om_mallocs=OM_NULL;	\
		ASSERT(1==0,assert_free_str); \
	} else if( om_mallocs!=OM_NULL ) { \
		om_dict_release(om_mallocs); \
		om_mallocs=OM_NULL; \
	}

#define LOG(MESG) PRINT_LOG(MESG);

#define PRINT_TEST_OPEN(TEST_NAME) printf("<test name=\"%s\" file=\"%s\">\n",TEST_NAME,__FILE__); 
#define PRINT_TEST_CLOSE()         printf("</test>\n"); 
#define PRINT_TEST_PID(PID)        printf("\t<pid>%i</pid>\n",PID);
#define PRINT_TEST_PASS()          printf("\t<result>PASS</result>\n");
#define PRINT_TEST_FAIL()          printf("\t<result>FAIL</result>\n");
#define PRINT_LOG(MESG)            printf("\t<log line=\"%i\">%s</log>\n",__LINE__,MESG);
#define PRINTF_LOG(FMT,ARGS...)    printf("\t<log line=\"%i\">" #FMT "</log>\n",__LINE__,##ARGS);
#define PRINT_ERROR(MESG)          printf("\t<error>%s</error>\n",MESG);

#endif
