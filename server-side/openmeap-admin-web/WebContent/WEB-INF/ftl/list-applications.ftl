[#ftl]
[#if applications??]
	<table class="application-list">
		<tr>
			<th>Application</th>
			<th>Version</th>
			<th>Description</th>
			<th>Other Operations</th>
		</tr>
		[#list applications as application]
		<tr>
			<td>
				<a title="Modify settings for ${application.name?html}" href="?bean=addModifyAppPage&applicationId=${application.id}">${application.name}</a>
			</td>
			<td>
				[#if deplUrls?? && (deplUrls[application.name])??]
					<a href="${deplUrls[application.name].url}">${deplUrls[application.name].content}</a>
				[#else]
					<em><a href="?bean=addModifyAppVersionPage&applicationId=${application.id}">Add a version</a></em>
				[/#if]
			</td>
			<td>${application.description!}</td>
			<td>
				<a href="?bean=appVersionListingsPage&applicationId=${application.id}">Version Listings</a>
			</td>
		</tr>
		[/#list]
	</table>
[#else]
	<em>There are no applications yet</em>
[/#if]