[#ftl]
[#if deployments??]
	<table class="application-list">
		<tr>
			<th>Creator</th>
			<th>Date/Time</th>
			<th>Version</th>
			<th>Archive Hash</th>
			<th>Deployment Type</th>
		</tr>
	[#list deployments as depl]
		<tr>
			<td>${(depl.creator)!}</td>
			<td>${depl.createDate?datetime}</td>
			<td><a href="?bean=addModifyAppVersionPage&applicationId=${depl.application.id}&versionId=${depl.applicationVersion.id}">${depl.applicationVersion.identifier}</a></td>
			<td><a href="${(depl.downloadUrl)!}">[${(depl.hashAlgorithm)!"NONE"}]${(depl.hash)!"NONE"}</a>
			<td>${depl.type}</td>
		</tr>
	[/#list]
	</table>
[/#if]