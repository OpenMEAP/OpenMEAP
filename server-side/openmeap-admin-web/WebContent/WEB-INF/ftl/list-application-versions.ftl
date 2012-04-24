[#ftl]
[#--
Displays a list of modifiable application versions.
The radio select is the Application.currentVersion.

Input params:
@param Long applicationId - The application to pull versions for
@param Long versions - A List<ApplicationVersion>
--]
[#if application??]
[#if versions??]

<form name="listApplicationVersions" action="${(deploymentsAnchor.url)!}" method="post">
	<input type="hidden" name="processTarget" value="${processTarget?html}"/>
	[#if mayCreateDeployments]
		<select name="deploymentType">
			<option selected>REQUIRED</option>
			<option>OPTIONAL</option>
			<option>IMMEDIATE</option>
		</select>
		<input type="submit" name="submit" value="Create a New Deployment!"/>
	[/#if]
	<table class="application-version">
		<tr>
			<th>&nbsp;</th>
			<th>Last Modifier</th>
			<th>Identifier</th>
			<th>Hash Alg.</th>
			<th>Hash</th>
			<th>View</th>
			<th>Url</th>
		</tr>
		[#list versions?values as version]
		[#if version.identifier?? && version.activeFlag]
			<tr>
				<td>[#if mayCreateDeployments]
				<input type="radio" name="versionId" value="${version.id}"/>
				[#else]&nbsp;[/#if]</td>
				<td>${(version.lastModifier)!}</td>
				<td><a href="?bean=addModifyAppVersionPage&applicationId=${application.id}&versionId=${version.id}">${version.identifier}</a></td>
				[#if version.archive??]
					<td>${version.archive.hashAlgorithm}</td>
					<td>${version.archive.hash}</td>
					<td>[#if (viewUrls[version.identifier])??]
						<a href="${viewUrls[version.identifier]}">Web View</a>
						[#else]&nbsp;[/#if]</td>
					<td><a href="${downloadUrls[version.identifier]}">Download</a></td>
				[#else]
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				[/#if]
			</tr>
		[/#if]
		[/#list]
	</table>
</form>

[#else]
<em>${application.name} has no versions associated to it</em>
[/#if]
[#else]
<em>No application found</em>
[/#if]