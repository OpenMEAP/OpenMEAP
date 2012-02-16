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

package com.openmeap.admin.web.backing;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.*;

import com.openmeap.admin.web.ProcessingTargets;
import com.openmeap.admin.web.backing.AddModifyApplicationVersionBacking;
import com.openmeap.constants.FormConstants;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelTestUtils;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.web.ProcessingEvent;
import com.openmeap.web.ProcessingUtils;
import com.openmeap.web.html.Option;
import com.openmeap.protocol.dto.HashAlgorithm;

public class AddModifyApplicationVersionsBackingTest {
	
	static ModelManager modelManager = null;
	
	@BeforeClass static public void beforeClass() {
		if( modelManager==null ) {
			ModelTestUtils.resetTestDb();
			ModelTestUtils.createModel(null);
			modelManager = ModelTestUtils.createModelManager();
		}
	}
	
	@AfterClass static public void afterClass() {
		ModelTestUtils.resetTestDb();
	}
	
	@Test public void testFormSetup() {
		ModelManager mm = modelManager;
		
		//////////////////
		// Verify the correct templateVariables are produced when no applicationId is passed in
		// You cannot modify an app version without an application, so there will be a minimal return
		Map<Object,Object> vars = new HashMap<Object,Object>();
		Map<Object,Object> parms = new HashMap<Object,Object>();
		AddModifyApplicationVersionBacking amab = new AddModifyApplicationVersionBacking();
		amab.setModelManager( mm );
		Collection<ProcessingEvent> events = amab.process(null, vars, parms);
		Assert.assertTrue(events.size()==1 && ProcessingUtils.containsTarget(events,ProcessingTargets.MESSAGES) );
		Assert.assertTrue(vars.size()==1 && vars.get("encodingType").equals("enctype=\""+FormConstants.ENCTYPE_MULTIPART_FORMDATA+"\""));
		
		///////////////////////
		// verify the correct templateVariables are produced with an invalid applcationId is passed
		vars = new HashMap<Object,Object>();
		parms = new HashMap<Object,Object>();
		parms.put("applicationId", new String[]{"666"});
		amab = new AddModifyApplicationVersionBacking();
		amab.setModelManager( mm );
		events = amab.process(null, vars, parms);
		Assert.assertTrue(events.size()==1 && ProcessingUtils.containsTarget(events,ProcessingTargets.MESSAGES) );
		Assert.assertTrue(vars.size()==1 && vars.get("encodingType").equals("enctype=\""+FormConstants.ENCTYPE_MULTIPART_FORMDATA+"\""));
		
		/////////////////////
		// verify the correct templateVariables are produced with an valid applcationId, 
		// but invalid versionId is passed
		vars = new HashMap<Object,Object>();
		parms = new HashMap<Object,Object>();
		parms.put("applicationId", new String[]{"1"});
		parms.put("versionId", new String[]{"666"});
		amab = new AddModifyApplicationVersionBacking();
		amab.setModelManager( mm );
		events = amab.process(null, vars, parms);
		Assert.assertTrue(events.size()==3 && ProcessingUtils.containsTarget(events, ProcessingTargets.MESSAGES) );
		Assert.assertTrue(vars.size()==6);
		Assert.assertTrue(vars.get("encodingType").equals("enctype=\""+FormConstants.ENCTYPE_MULTIPART_FORMDATA+"\""));
		Assert.assertTrue(vars.get("application")!=null && ((Application)vars.get("application")).getName().compareTo("Application.name")==0 );
		Assert.assertTrue(vars.get("version")!=null && ((ApplicationVersion)vars.get("version")).getIdentifier()==null);
		Assert.assertTrue(vars.get("hashTypes")!=null && ((List)vars.get("hashTypes")).size()==HashAlgorithm.values().length);
		Assert.assertTrue(((String)vars.get("processTarget")).compareTo(ProcessingTargets.ADDMODIFY_APPVER)==0);
		
		//////////////////////
		// verify the correct templateVariables are produced when 
		// both a valid app id and version id are passed in
		vars = new HashMap<Object,Object>();
		parms = new HashMap<Object,Object>();
		parms.put("applicationId", new String[]{"1"});
		parms.put("versionId", new String[]{"1"});
		amab = new AddModifyApplicationVersionBacking();
		amab.setModelManager( mm );
		events = amab.process(null, vars, parms);
		Assert.assertTrue(events.size()==2);
		Assert.assertTrue(vars.size()==6);
		Assert.assertTrue(vars.get("encodingType").equals("enctype=\""+FormConstants.ENCTYPE_MULTIPART_FORMDATA+"\""));
		Assert.assertTrue(vars.get("application")!=null && ((Application)vars.get("application")).getName().compareTo("Application.name")==0 );
		Assert.assertTrue(vars.get("version")!=null && ((ApplicationVersion)vars.get("version")).getIdentifier().compareTo("ApplicationVersion.identifier.1")==0 );
		Assert.assertTrue(vars.get("hashTypes")!=null && ((List)vars.get("hashTypes")).size()==HashAlgorithm.values().length);
		Assert.assertTrue(((String)vars.get("processTarget")).compareTo(ProcessingTargets.ADDMODIFY_APPVER)==0);
	}
	
	@Test public void testFormPost() {
		
		ApplicationVersion version = null;
		Map<Object,Object> vars = null;
		Map<Object,Object> parms = null;
		
		////////////
		// Verify that we can create a new ApplicationVersion
		vars = new HashMap<Object,Object>();
		parms = new HashMap<Object,Object>();
		parms.put("processTarget", new String[]{ProcessingTargets.ADDMODIFY_APPVER});
		parms.put("applicationId", new String[]{"1"});
		parms.put("versionId", new String[]{""});
		parms.put("identifier", new String[]{"ApplicationVersion.identifier.1"});
		parms.put("url", new String[]{"ANewDownloadUrl"});
		parms.put("hashType", new String[]{"MD5"});
		parms.put("hash", new String[]{"ANewHashValue"});
		parms.put("notes", new String[]{"These are them application version notes"});
		parms.put("deviceTypes", new String[]{"1","2"});
		parms.put("bytesLength", new String[]{"13456342"});
		parms.put("bytesLengthUncompressed", new String[]{"13456342"});
		AddModifyApplicationVersionBacking amab = new AddModifyApplicationVersionBacking();
		amab.setModelManager(modelManager);
		Collection<ProcessingEvent> events = amab.process(null, vars, parms);
		Assert.assertTrue(events.size()==3 && ProcessingUtils.containsTarget(events, ProcessingTargets.MESSAGES));
		Assert.assertTrue(vars.get("hashTypes")!=null);
		Assert.assertTrue(vars.get("processTarget")!=null && ((String)vars.get("processTarget")).compareTo(ProcessingTargets.ADDMODIFY_APPVER)==0);
		Assert.assertTrue(vars.get("version")!=null);
		version = ((ApplicationVersion)vars.get("version"));
		Assert.assertTrue(version.getIdentifier().compareTo("ApplicationVersion.identifier.1")==0);
		Assert.assertTrue(version.getArchive()!=null);
		
		/////////////
		// Verify that we can change pretty much everything
		Long ourVersionId = ((ApplicationVersion)vars.get("version")).getId();
		parms.put("versionId", new String[]{ourVersionId.toString()});
		parms.put("identifier", new String[]{"ApplicationVersion.new_version.identifier"});
		parms.put("url", new String[]{"AnotherNewDownloadUrl"});
		parms.put("hashType", new String[]{"SHA1"});
		parms.put("hash", new String[]{"AnotherNewHashValue"});
		parms.put("notes", new String[]{"New notes"});
		parms.put("deviceTypes", new String[]{"1"});
		parms.put("bytesLength", new String[]{"10000"});
		parms.put("bytesLengthUncompressed", new String[]{"10000"});
		amab = new AddModifyApplicationVersionBacking();
		amab.setModelManager(modelManager);
		events = amab.process(null, vars, parms);
		version = modelManager.getModelService().findByPrimaryKey(ApplicationVersion.class,ourVersionId);
		
		Assert.assertTrue(version.getIdentifier().compareTo("ApplicationVersion.new_version.identifier")==0);
		Assert.assertTrue(version.getArchive().getUrl().compareTo("AnotherNewDownloadUrl")==0);
		Assert.assertTrue(version.getArchive().getHashAlgorithm().compareTo("SHA1")==0);
		Assert.assertTrue(version.getArchive().getHash().compareTo("AnotherNewHashValue")==0);
		Assert.assertTrue(version.getNotes().compareTo("New notes")==0);
		Assert.assertTrue(version.getArchive().getBytesLength()==10000);
		Assert.assertTrue(version.getArchive().getBytesLengthUncompressed()==10000);
		
		/////////////
		// Verify that we cannot change an inactive version
		ourVersionId = ((ApplicationVersion)vars.get("version")).getId();
		((ApplicationVersion)vars.get("version")).setActiveFlag(false);
		parms.put("versionId", new String[]{ourVersionId.toString()});
		parms.put("identifier", new String[]{"ApplicationVersion.new_version_2.identifier"});
		parms.put("url", new String[]{"AnotherNewDownloadUrl2"});
		parms.put("hashType", new String[]{"MD5"});
		parms.put("hash", new String[]{"AnotherNewHashValue2"});
		parms.put("notes", new String[]{"New notes2"});
		parms.put("deviceTypes", new String[]{"12"});
		parms.put("bytesLength", new String[]{"100002"});
		parms.put("bytesLengthUncompressed", new String[]{"100002"});
		amab = new AddModifyApplicationVersionBacking();
		amab.setModelManager(modelManager);
		events = amab.process(null, vars, parms);
		version = modelManager.getModelService().findByPrimaryKey(ApplicationVersion.class,ourVersionId);
		
		Assert.assertTrue(version.getIdentifier().compareTo("ApplicationVersion.new_version.identifier")==0);
		Assert.assertTrue(version.getArchive().getUrl().compareTo("AnotherNewDownloadUrl")==0);
		Assert.assertTrue(version.getArchive().getHashAlgorithm().compareTo("SHA1")==0);
		Assert.assertTrue(version.getArchive().getHash().compareTo("AnotherNewHashValue")==0);
		Assert.assertTrue(version.getNotes().compareTo("New notes")==0);
		Assert.assertTrue(version.getArchive().getBytesLength()==10000);
		Assert.assertTrue(version.getArchive().getBytesLengthUncompressed()==10000);
	}
}
