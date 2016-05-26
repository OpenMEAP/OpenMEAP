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

#include <stdio.h>
#include "unzip.h"
#include <openmeap-slic-core.h>
#include <errno.h>
#include <dirent.h>
#include <sys/stat.h>

OM_PRIVATE_FUNC int __om_unzip_mymkdir(om_unzip_archive_ptr archive, const char* dirname);
OM_PRIVATE_FUNC int __om_unzip_makedir(om_unzip_archive_ptr archive, const char *rootExportPath, char *newdir);
OM_PRIVATE_FUNC int __om_unzip_extract_currentfile(om_unzip_archive_ptr archive, const char *rootExportPath, om_uint32 options, const char* password);
OM_PRIVATE_FUNC int __om_unzip_extract(om_unzip_archive_ptr archive, const char *rootExportPath, om_uint32 options, const char* password);

om_unzip_archive_ptr om_unzip_open_archive(const char *file_path) {
	
	int err;
	
	unz_global_info * gip = om_malloc(sizeof(unz_global_info));
	if( gip==OM_NULL ) {
		return OM_NULL;
	}
	
	unzFile uf = unzOpen(file_path);
	err = unzGetGlobalInfo(uf,gip);
	if( err!=UNZ_OK ) {
		om_free(gip);
		om_error_set(OM_ERR_ZIP_GLOBALINFO,"Error getting global info for the zip archive.");
		return OM_NULL;
	}
	
	om_list_ptr error_log = om_list_new();
	if( error_log==OM_NULL ) {
		om_free(gip);
		return OM_NULL;
	}
	
	om_unzip_archive_ptr arch = om_malloc(sizeof(om_unzip_archive));
	if( arch==OM_NULL ) {
		om_list_release(error_log);
		om_free(gip);
		return OM_NULL;
	}
	
	arch->error_log = error_log;
	arch->file = uf;
	arch->global_info_ptr = gip;
	
	return arch;
}

om_bool om_unzip_close_archive(om_unzip_archive_ptr archive) {
	om_bool ret = OM_TRUE;
	om_list_release(archive->error_log);
	om_free(archive->global_info_ptr);
	if( unzClose(archive->file) != UNZ_OK ) {
		ret=OM_FALSE;
	}
    om_free(archive);
	return ret;
}

om_bool om_unzip_archive_into_path(om_unzip_archive_ptr archive, const char *exportBasePath) {
	int err = __om_unzip_extract(archive,exportBasePath,0,OM_NULL);
	if (err!=UNZ_OK) {
		return OM_FALSE;
	}
	return OM_TRUE;
}




/*****************
 * Everything below here was imported from miniunz.c,
 * the sample application from MiniZip
 * The functions have been renamed, but the code is 
 * still mostly the product of 
 */

om_bool __om_unzip_append_error(om_unzip_archive_ptr archive, char * err) {
	return om_list_append(archive->error_log,err);
}

int __om_unzip_mymkdir(om_unzip_archive_ptr archive, const char* dirname) {
	
    int ret=0;
    ret = mkdir (dirname,0700);
    return ret;
}

int __om_unzip_makedir (om_unzip_archive_ptr archive, const char *rootExportPath, char *newdir) {
	
	char *buffer = om_string_copy(newdir);
	if( buffer==OM_NULL ) {
		return UNZ_INTERNALERROR;
	}
	char *p;
	int len = (int)strlen(newdir);
	
	if (len <= 0)
		return 0;
	
	// prefix with the location where we want to create the dir structure
	if( buffer[0]==OM_FS_FILE_SEP ) {
		p = om_string_append(rootExportPath,buffer);
	} else {
		p = om_string_format("%s%c%s",rootExportPath,OM_FS_FILE_SEP,buffer);
	}
	om_free(buffer);
	buffer = p;
	p = OM_NULL;
	
	// if the path ended with a '/',
	// then take that off
	if (buffer[len-1] == '/') {
		buffer[len-1] = '\0';
	}
	if (__om_unzip_mymkdir(archive,buffer) == 0) {
		om_free(buffer);
		return 1;
    }
	
	p = buffer+1;
	while (1) {
		char hold;
		
		// we shouldn't need to create the rootExportPath
		if( strcmp(rootExportPath,buffer)==0 )
			break;
		
		while(*p && *p != '\\' && *p != '/')
			p++;
		hold = *p;
		*p = 0;
		if ( (__om_unzip_mymkdir(archive,buffer) == -1) && (errno == ENOENT) ) {
			__om_unzip_append_error(archive,om_string_format("couldn't create directory %s\n",buffer));
			om_free(buffer);
			return 0;
        }
		if ( hold == 0 )
			break;
		*p++ = hold;
    }
	om_free(buffer);
	return 1;
}

int __om_unzip_extract_currentfile(
		om_unzip_archive_ptr archive,
		const char *rootExportPath, 
		om_uint32 options,
		const char *password
) {
	unzFile uf = archive->file;
    char filename_inzip[256];
    char *filename_withoutpath;
    char *p;
    int err=UNZ_OK;
    FILE *fout=NULL;
    void *buf;
    uInt size_buf;
	
    unz_file_info file_info;
    uLong ratio = 0;
	
    err = unzGetCurrentFileInfo(uf,&file_info,filename_inzip,sizeof(filename_inzip),NULL,0,NULL,0);
    if (err!=UNZ_OK) {
        __om_unzip_append_error(archive,om_string_format("error %d with zipfile in unzGetCurrentFileInfo\n",err));
        return err;
    }
	
    size_buf = OM_ZIP_WRITEBUFFERSIZE;
    buf = (void*)om_malloc(size_buf);
    if (buf==NULL) {
        // Error allocating memory for the write buffer
        return UNZ_INTERNALERROR;
    }
	
	// this appears to be a string copy
	// some zip files do not put in a path
    p = filename_withoutpath = filename_inzip;
    while ((*p) != '\0') {
		// if the current character is a dir path separator
		// then the next character starts either a directory segment
		// or a file name
        if (((*p)=='/') || ((*p)=='\\'))
            filename_withoutpath = p+1;
        p++;
    }
	
	// if the element after the last '/' was a '\0', then this is a directory
    if ( (*filename_withoutpath) == '\0' ) {
        if( ! options & OM_ZIP_OPTS_NOPATH ) {
			char * full_file_path = om_string_format("%s%c%s",rootExportPath,OM_FS_FILE_SEP,filename_inzip);
			if(full_file_path==OM_NULL) {
				om_free(buf);
				return UNZ_INTERNALERROR;
			}
            __om_unzip_mymkdir(archive,full_file_path);
			om_free(full_file_path);
        }
    }
	// otherwise it was a file name
	else {
        char* write_filename;
        int skip=0;
		
		if( options & OM_ZIP_OPTS_NOPATH ) {
			write_filename = filename_withoutpath;
		} else {
			write_filename = filename_inzip;
		}
		
        err = unzOpenCurrentFilePassword(uf,password);
        if ( err != UNZ_OK ) {
            __om_unzip_append_error(archive,om_string_format("error %d with zipfile in unzOpenCurrentFilePassword\n",err));
			return err;
        }
		
		// removed a file existence test here
		
        if ( skip == 0 && err == UNZ_OK )
        {			
			// the write_filename should, at this point,
			// have the relative directory on it
			// now we have to prepend with our rootExportPath
			char * full_file_path = om_string_format("%s%c%s",rootExportPath,OM_FS_FILE_SEP,write_filename);
            fout = fopen(full_file_path,"wb");
			
            // some zipfile don't contain the directory alone before file
            if ( 
				(fout==NULL) 
				&& (!(options & OM_ZIP_OPTS_NOPATH)) 
				&& (filename_withoutpath!=(char*)filename_inzip)
			) {
                char c = *(filename_withoutpath-1);
                *(filename_withoutpath-1)='\0';
                __om_unzip_makedir(archive,rootExportPath,write_filename);
                *(filename_withoutpath-1)=c;
				
                fout=fopen(full_file_path,"wb");
            }
			
			om_free(full_file_path);
			
            if (fout==NULL) {
				__om_unzip_append_error(archive,om_string_format("error opening %s",write_filename));
				om_free(buf);
				return UNZ_INTERNALERROR;
            }
        }
		
        if (fout!=NULL) {
            //printf(" extracting: %s\n",write_filename);
			
            do {
                err = unzReadCurrentFile(uf,buf,size_buf);
                if (err<0) {
					__om_unzip_append_error(archive,om_string_format("error %d with zipfile in unzReadCurrentFile\n",err));
                    break;
                }
                if (err>0) {
                    if (fwrite(buf,err,1,fout)!=1) {
                        __om_unzip_append_error(archive,om_string_format("error in writing extracted file\n"));
                        err=UNZ_ERRNO;
                        break;
                    }
				}
            } while (err>0);
            if (fout)
				fclose(fout);
			
			// OpenMEAP doesn't care if the date of the files on the device
			// are the same as in the archive.
            // if (err==0)
            //    change_file_date(write_filename,file_info.dosDate, file_info.tmu_date);
        }
		
        if (err==UNZ_OK) {
            err = unzCloseCurrentFile (uf);
            if (err!=UNZ_OK) {
                __om_unzip_append_error(archive,om_string_format("error %d with zipfile in unzCloseCurrentFile\n",err));
            }
        } else {
			unzCloseCurrentFile(uf); /* don't lose the error */
		}
    }
	
    om_free(buf);
    return err;
}


int __om_unzip_extract(
	om_unzip_archive_ptr archive, 
	const char* rootExtractPath,
	om_uint32 options, 
	const char* password
)
{
	unzFile uf = archive->file;
    uLong i;
    unz_global_info * gi = archive->global_info_ptr;
    int err;
	
	err = unzGoToFirstFile(uf);
	if (err!=UNZ_OK)	
	{
		__om_unzip_append_error(archive,om_string_format("error %d with zipfile in unzGoToNextFile\n",err));
		return err;
	}
	
    for (i=0;i<gi->number_entry;i++)
    {
        if ( (err=__om_unzip_extract_currentfile(archive,rootExtractPath,options,password)) != UNZ_OK )
            break;
		
        if ((i+1)<gi->number_entry)
        {
            err = unzGoToNextFile(uf);
            if (err!=UNZ_OK)
            {
                __om_unzip_append_error(archive,om_string_format("error %d with zipfile in unzGoToNextFile\n",err));
                break;
            }
        }
    }
	
    return err;
}


