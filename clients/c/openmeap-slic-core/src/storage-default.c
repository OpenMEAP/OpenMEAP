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

#include <openmeap-slic-core.h>

#include <stdio.h>
#include <stdlib.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>

/*******/

om_storage_ptr default_om_storage_alloc(om_config_ptr *cfg) {
	
	om_storage_ptr stg = om_malloc( sizeof(om_storage) );
	if(stg==OM_NULL) {
		return OM_NULL;
	}
	stg->cfg = cfg;
	
	stg->open_file_input_stream=default_om_storage_open_file_input_stream;
	stg->open_file_output_stream=default_om_storage_open_file_output_stream;
	stg->write=default_om_storage_write;
	stg->read=default_om_storage_read;
	stg->close_file_input_stream=default_om_storage_close_file_input_stream;
	stg->close_file_output_stream=default_om_storage_close_file_output_stream;
	stg->delete_file=default_om_storage_delete_file;
	stg->create_directories_for_path=default_om_storage_create_directories_for_path;
	stg->get_import_archive_input_stream=default_om_storage_get_import_archive_input_stream;
	stg->get_import_archive_output_stream=default_om_storage_get_import_archive_output_stream;
	stg->delete_import_archive=default_om_storage_delete_import_archive;
	stg->get_current_location=default_om_storage_get_current_location;
	stg->reset_storage=default_om_storage_reset_storage;
	stg->delete_directory=default_om_storage_delete_directory;
	stg->file_get_contents=default_om_storage_file_get_contents;
	
	/* 
     The following 4 are implemented in the device-specific storage source (storage.m for ios),
     and are thus provided by that implementations om_storage_alloc:
	stg->get_localstorage_path
	stg->get_bundleresources_path
	stg->get_bytes_free
	stg->get_current_assets_prefix
	 */
	
	return stg;
}

/******
 * DEFAULT IMPLEMENTATION BELOW
 */

om_file_input_stream_ptr default_om_storage_get_import_archive_input_stream(om_storage_ptr storage) {
	char * location = om_config_get(storage->cfg,OM_CFG_IMPORT_ARCHIVE_PATH);
	om_file_input_stream_ptr ret = om_storage_open_file_input_stream(storage,location);
	om_free(location);
	return ret;
}

om_file_output_stream_ptr default_om_storage_get_import_archive_output_stream(om_storage_ptr storage) {
	char * location = om_config_get(storage->cfg,OM_CFG_IMPORT_ARCHIVE_PATH);
	om_file_output_stream_ptr ret = om_storage_open_file_output_stream(storage,location,OM_TRUE);
	om_free(location);
	return ret;
}

om_bool default_om_storage_delete_import_archive(om_storage_ptr storage) {
	char * importArchLoc = om_config_get((om_config_ptr)storage->cfg,OM_CFG_IMPORT_ARCHIVE_PATH);
	om_bool result = om_storage_delete_file(storage, importArchLoc);
	om_free(importArchLoc);
	return result;
}

char * default_om_storage_get_current_location(om_storage_ptr storage) {
	om_error_clear();
	char *locPtr = om_config_get((om_config_ptr)storage->cfg,OM_CFG_CURRENT_STORAGE);
	return locPtr;
}

om_bool default_om_storage_delete_directory(om_storage_ptr storage, char *rootPath, om_bool include_root) {
	
	DIR *dp = OM_NULL;
	struct dirent *ep = OM_NULL;
	char *currentEntry=OM_NULL;
	char *tmp=OM_NULL;
	om_bool retVal=OM_FALSE;
	
	struct stat *currentEntryStat = om_malloc(sizeof(struct stat));
	if( currentEntryStat==OM_NULL )
		return OM_NULL;
	
	dp = opendir (rootPath);
	if (dp != NULL)
	{
		while (ep = readdir (dp)) {
			
			if( strcmp(ep->d_name,"..")==0 
			   || strcmp(ep->d_name,".")==0 )
				continue;
			
			currentEntry = om_string_append(rootPath,(char[]){OM_FS_FILE_SEP,'\0'});
			tmp=currentEntry;
			currentEntry = om_string_append(tmp,ep->d_name);
			om_free(tmp);
			
			if( 0==stat(currentEntry,currentEntryStat) ) {
				if( S_ISDIR(currentEntryStat->st_mode) ) {
					om_storage_delete_directory(storage,currentEntry,OM_TRUE);
				} else if ( S_ISREG(currentEntryStat->st_mode) ) {
					unlink(currentEntry);
				} 
			} 
			
			memset(currentEntryStat,0,sizeof(struct stat));
			om_free(currentEntry);
		}
		closedir (dp);
		if( include_root==OM_TRUE ) {
			rmdir(rootPath);
        }
		
		retVal=OM_TRUE;
		
	} else om_error_set_format(OM_ERR_DIR_DEL_RECURSE,"Could not open \"%s\" as a directory.",rootPath);
	
	om_free(currentEntryStat);
	return retVal;
}

om_bool default_om_storage_reset_storage(om_storage_ptr storage) {
	char *pathToStorage = om_storage_get_current_location(storage);
	om_bool ret = om_storage_delete_directory(storage,pathToStorage,OM_TRUE);
	om_free(pathToStorage);
	return ret;
}

///////////////////////////
//

/**
 * Opens a file for writing
 *
 * @param filePath the path to the file to open
 * @param truncate truncate the file if it exists, else position to the end.
 * @return A pointer to the om_file_output_stream struct or OM_NULL.  Sets the error flag.
 */
om_file_output_stream_ptr default_om_storage_open_file_output_stream(om_storage_ptr storage, const char *filePath, om_bool truncate) {
	
	char *path = om_string_copy(filePath);
	if( path == OM_NULL ) {
		return OM_NULL;
	}
	
	FILE *f = fopen( filePath, truncate ? "w" : "a" );
	if( f==NULL ) {
		om_error_set(OM_ERR_FILE_OPEN,"Could not open the file");
		return OM_NULL;
	}	
	
	// at this point, the file is successfully open
	// and we can safely allocate an om_file_input_stream_ptr
	om_file_output_stream_ptr ret = om_malloc(sizeof(om_file_output_stream));
	if( ret==OM_NULL ) {
		return OM_NULL;
	}
	
	ret->device_data = f;
	ret->device_path = path;
	ret->storage = storage;
	
	return ret;
	
}

/**
 * Write num_bytes from bytes, starting at index, to the om_file_output_stream pointer passed in.
 *
 * @param ostream The om_file_output_stream pointer to write to
 * @param bytes The bytes to write to the output stream
 * @param index The index to start writing at
 * @param num_bytes The number of bytes to write
 */
om_uint32 default_om_storage_write(om_file_output_stream_ptr ostream, void *bytes, om_uint32 index, om_uint32 num_bytes) {
	clearerr((FILE*)ostream->device_data);
	om_uint32 ret = fwrite(bytes+index,1,num_bytes,(FILE*)ostream->device_data);
	int errInt;
	if( ret!=num_bytes && (errInt=ferror((FILE*)ostream->device_data)) ) {
		om_error_set_format(OM_ERR_FILE_WRITE,"Error occurred writing to file output stream : %s", strerror(errInt));
		clearerr((FILE*)ostream->device_data);
	}
	return ret;
}

/**
 *
 */
void default_om_storage_close_file_output_stream(om_file_output_stream_ptr file) {
	fclose((FILE*)file->device_data);
	om_free(file->device_path);
	om_free(file);
}

/**
 * Opens a file for reading
 *
 * @param storage the storage context
 * @param filePath the path to the file to open
 */
om_file_input_stream_ptr default_om_storage_open_file_input_stream(om_storage_ptr storage, const char *filePath) {
	
	char *path = om_string_copy(filePath);
	if( path == OM_NULL ) {
		return path;
	}
	
	FILE *f = fopen(filePath,"r");
	if( f==NULL ) {
		om_error_set_format(OM_ERR_FILE_OPEN,"Could not open the file \"%s\".",filePath);
		return OM_NULL;
	}	
	
	// at this point, the file is successfully open
	// and we can safely allocate an om_file_input_stream_ptr
	om_file_input_stream_ptr ret = om_malloc(sizeof(om_file_input_stream));
	if( ret==OM_NULL ) {
		return OM_NULL;
	}
	
	ret->device_data = f;
	ret->device_path = path;
	ret->storage = storage;
	
	return ret;
}

/**
 * Read num_bytes into bytes from the om_file_input_stream pointer passed in
 *
 * @param istream The om_file_input_stream to read from
 * @param bytes A pointer to the pointer to allocate and pass back
 * @param num_bytes The number of bytes preferred to read
 * @return The number of bytes read.  Sets an error code on error
 */
om_uint32 default_om_storage_read(om_file_input_stream_ptr istream, void *bytes, om_uint32 num_bytes) {
	clearerr((FILE*)istream->device_data);
	om_uint32 ret = fread(bytes,1,num_bytes,istream->device_data);
	int errInt;
	if( ret!=num_bytes && (errInt=ferror((FILE*)istream->device_data)) ) {
		om_error_set_format(OM_ERR_FILE_READ,"Error occurred reading from file \"%s\" : %s",istream->device_path,strerror(errInt));
	}
	return ret;
}

/**
 * Closes a file input stream and releases associated resources
 *
 * @param storage
 * @param file
 */
void default_om_storage_close_file_input_stream(om_file_input_stream_ptr file) {
	int ret = fclose((FILE*)file->device_data);
	if( ret ) {
		int errInt = ferror((FILE*)file->device_data);
		om_error_set_format(OM_ERR_FILE_CLOSE,"Error occurred closing file \"%s\" : %s",file->device_path,strerror(errInt));
	}
	om_free(file->device_path);
	om_free(file);
}

/**
 * Delete the file or directory passed in.  If directory, then it must be empty.
 * Implemented in device library
 */
om_bool default_om_storage_delete_file(om_storage_ptr storage, const char *filePath) {
	if( remove(filePath)!=0 ) {
		om_error_set_format(OM_ERR_FILE_CLOSE,"Error occurred deleting file \"%s\".",filePath);
		return OM_FALSE;
	} else {
		return OM_TRUE;
	}
}

/**
 * Creates the directory structure for a path.  It is assumed that the trailing slash is not used
 *
 * @param path The full path to be created.
 * @return om_bool true if successful, else false.
 */
om_bool default_om_storage_create_directories_for_path(om_storage_ptr stg, char *path) {
    
	om_list_ptr segments = om_string_explode(path,OM_FS_FILE_SEP);
    if( segments==OM_NULL ) {
        return OM_FALSE;
    } 
    
    int count = om_list_count(segments);
    char * pathInProgress = OM_NULL;
    
    struct stat *currentEntryStat = om_malloc(sizeof(struct stat));
	if( currentEntryStat==OM_NULL ) {
		return OM_FALSE;
    }
    
    for( int i=0; i<count; i++ ) {
        char * segment = om_list_get(segments,i);
        if( strlen(segment)==0 ) {
            continue;
        }
        if( pathInProgress!=OM_NULL ) {
            char * t = pathInProgress;
            pathInProgress=om_string_format("%s%c%s",pathInProgress,OM_FS_FILE_SEP,segment);
            om_free(t);
        } else {
            pathInProgress=om_string_format("%c%s",OM_FS_FILE_SEP,segment);
        }
        
        if( 0==stat(pathInProgress,currentEntryStat) ) {
            if( !S_ISDIR(currentEntryStat->st_mode) ) {
                mkdir(path,0700);
            }
            memset(currentEntryStat,0,sizeof(struct stat));
        } else {
            mkdir(path,0700);
        }
    }
    
    om_list_release(segments);
    om_free(currentEntryStat);
    if(pathInProgress!=OM_NULL) {
        om_free(pathInProgress);
    }
    return OM_TRUE;
}

char * default_om_storage_file_get_contents(om_storage_ptr storage, const char *fullFilePath) {
	char * toret = om_malloc(256);
	if( toret==OM_NULL )
		return OM_NULL;
	char * p = toret;
	om_uint32 size = 0;
	char bytes[256];
	int numRead = 0;
	int numToRead = 256;
	
	om_file_input_stream_ptr is = om_storage_open_file_input_stream(storage,fullFilePath);
	if( is==OM_NULL ) {
		om_free(toret);
		return OM_NULL;
	}
	
	while( (numRead=om_storage_read(is,bytes,256)) > 0 ) {
		p = toret;
		toret = om_malloc(size+numRead+1);
		if( toret==OM_NULL ) {
			om_free(toret);
			return OM_NULL;
		}
		memcpy(toret,p,size);
		memcpy(toret+size,bytes,numRead);
		size+=numRead;
		om_free(p);
		if( numRead<numToRead ) 
			break;
	}
	
	om_storage_close_file_input_stream(is);
	
	return toret;
}
