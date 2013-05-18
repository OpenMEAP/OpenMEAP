[#ftl]
[#--
Add/Modify Application form template

Template Variables / Inputs:
@param String                 formAction    : action of the form, defaults to the same page
@param String                 processTarget : a unique string indicating that the post originated from this form
@param Application            application   : the actual application being modified, create is assumed if empty
@param Map<DeviceType,String> deviceTypes   : the recognized device types.  value is "selected" or ""

Parameter Values / Outputs:
@param String processTarget  - an identifier so the backing can recognize it's post
@param Long   applicationId  - the application id, if blank a new application is being created
@param String name           - the name of the application
@param String proxiedBaseUrl - the base of the url to proxy through to.
@param String description    - the description of the application
--]

<!-- BEGIN ADD/MODIFY FORM -->
<form method="POST" action="${(formAction?html)!}" name="addModifyApplication_${(application.id?html)!}">
	<input type="hidden" name="processTarget" value="${processTarget!}"/>
	<input type="hidden" name="applicationId" value="${(application.id?html)!}"/>
	<fieldset>
		<legend>Application[#if (application.name)??] - ${application.name}[/#if]</legend>
		<dl>
				<dt>Name:</dt>
				<dd>
				<div>
					This is used by SLIC to identify which application it is requesting updates for.
					It is important that it does not change after initial deployment (through independent device app stores) of SLIC.
				</div> 
				<input type="text" name="name" value="${(application.name?html)!}"/>
				</dd>
				<dt>Admins (may modify version admins as well):</dt>
				<dd>
				<div>
					Application admins may modify any aspect of the application, including deleting it.
					The only thing an application admin may not do is remove their own admin privileges.
				</div>
				<textarea cols="25%" rows="3" name="admins">${(application.admins?html)!}</textarea>
				</dd>
				<dt>Version Admins:</dt>
				<dd>
				<div>
                Version admins may create, modify, and delete application versions.  They may not make deployments.
				</div>
					<textarea cols="25%" rows="3" name="versionAdmins">${(application.versionAdmins?html)!}</textarea>
				</dd>
				<dt>Description:</dt>
				<dd><textarea cols="25%" rows="5" name="description">${(application.description?html)!}</textarea></dd>
				<dt>Initial Version Identifier:</dt>
				<dd>
				<div>
                Must be the original version identifier bundled into the SLIC.
				</div>
				<input type="text" name="initialVersionIdentifier" value="${(application.initialVersionIdentifier?html)!}"/>
				</dd>
				<dt>Deployment History Length:</dt>
				<dd>
				<div>
                The number of deployments to maintain in the deployment history table.  As old archives fall off the end, 
                they are deleted from the admin and cluster nodes, if they are not being used by any other versions.
				</div>
                <input type="text" name="deploymentHistoryLength" value="${(application.deploymentHistoryLength?string.computer)!}"/>
				</dd>
				[#if willProcess]
				<dt>Submit:</dt>
				<dd>
					<input type="hidden" name="submit" value="false">
					<input type="image" onclick="this.form.submit.value='true';this.form.submit();"
						src="/openmeap-admin-web/img/btn/action_submit.gif"/>
					</dd>
					[#if (application.id)??]
				<dt>
                Delete Confirm:<br/>
					<span class="copy">Type "delete the application" here.</span>
				</dt>
				<dd><input type="text" name="deleteConfirm" value=""/></dd>
				<dt>
					Delete:<br/>
					<span class="copy">After filling in the "Delete Confirm" field, click here.  Warning: this
					is not an "undoable" action and will erase, permanently, the version and deployment
					history of the application.  Only do this if you really, really mean it!</span>
				</dt>
				<dd>
					<input type="hidden" name="delete" value="false">
					<input type="image" onclick="this.form.delete.value='true';this.form.submit();"
						src="/openmeap-admin-web/img/btn/action_delete.gif"/>
				</dd>
				[/#if]
		[/#if]

	</fieldset>
</form>
<!-- END ADD/MODIFY FORM -->
