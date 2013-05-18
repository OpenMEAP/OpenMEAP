[#ftl]
[#--
Add/Modify Application Version form template

Template Variables / Inputs:
@param String                 formAction    : action of the form, defaults to the same page
@param String                 processTarget : a unique string indicating that the post originated from this form
@param Application            application   : the actual application being modified, create is assumed if empty
@param ApplicationVersion     version       : the version being modified, create is assumed if empty
@param List<Option>           hashTypes     : a list of options configured for the available hash types...the hash type of the current version is "selected"
@param List<Option>           deviceTypes   : a list of options configured for the available device types...the device type of the current version is "selected"

Parameter Values / Outputs:
@param String processTarget  - an identifier so the backing can recognize it's post
@param Long   applicationId  - the application id, if blank a new application is being created
@param Long   versionId      - the application id, if blank a new application is being created
@param String identifier     - the name of the application
@param String url            - the url where the version archive can be retrieved from
@param String hashType       - the hash algorithm selected that can be used to validate the archive
@param String hash           - the hash value of the archive content that can be used for validation
@param String notes          - notes about the version
--]

[#if application??]
<!-- BEGIN ADD/MODIFY VERSION FORM -->
<form method="POST" ${(encodingType)!} action="${(formAction?html)!}" name="addModifyApplicationVersion_${(application.id?html)!}_${(version.id?html)!}">
	<input type="hidden" name="processTarget" value="${processTarget!}"/>
	<input type="hidden" name="applicationId" value="${(application.id?html)!}"/>
	<input type="hidden" name="versionId" value="${(version.id?html)!}"/>
	<fieldset>
		<legend>Application Version[#if (version.identifier)??]- ${version.identifier!}[/#if]</legend>
		<dl>
			<dt>Identifier:</dt><dd><input type="text" name="identifier" value="${(version.identifier?html)!}"/></dd>
			<dt>Notes:</dt><dd><textarea cols="25%" rows="5" name="notes">${(version.notes?html)!}</textarea></dd>
			[#if willProcess]
			<dt>Upload (have me do all below)</dt><dd><input type="file" name="uploadArchive"/></dd>
			<dt><input type="image" src="/openmeap-admin-web/img/btn/action_submit.gif" name="submit" value="Submit!"/></dt>
			[/#if]
			<br/>
			<dt>Download Url:</dt><dd><input type="text" name="url" value="${(version.archive.url?html)!}"/></dd>
			<dt>Content-length:</dt><dd><input type="text" name="bytesLength" value="${(version.archive.bytesLength?c)!}"/></dd>
			<dt>Uncompressed Content-length:</dt><dd><input type="text" name="bytesLengthUncompressed" value="${(version.archive.bytesLengthUncompressed?c)!}"/></dd>
			<dt>Hash Type:</dt><dd><select name="hashType"><option></option>
				[#if hashTypes??]
					[#list hashTypes as option]
						<option ${option.selected!} value="${option.value!}">${option.innerText!}</option>
					[/#list]
				[/#if]
			</select></dd>
			<dt>Hash Value:</dt><dd><input type="text" name="hash" value="${(version.archive.hash?html)!}"/></dd>
			[#if willProcess && (version.id)??]
				<dt>
					Delete Confirm:<br/>
					<span class="copy">Type "delete the version" here.</span>
				</dt>
				<dd><input type="text" name="deleteConfirm" value=""/></dd>
				<dt>
					Delete:<br/>
					<span class="copy">After filling in the "Delete Confirm" field, click here.
					This will delete the application archive from the server, so you'll need to
					re-upload the zip file if you want to re-create the version.
					</span>
				</dt>
				<dd>
					<input type="hidden" name="delete" value="Delete!">	
					<input type="image" src="/openmeap-admin-web/img/btn/action_delete.gif" value="Delete!"/>
				</dd>
			[/#if]
		</dl>
	</fieldset>
</form>
<!-- END ADD/MODIFY VERSION FORM -->
[#else]
<em>Application not found</em>
[/#if]
