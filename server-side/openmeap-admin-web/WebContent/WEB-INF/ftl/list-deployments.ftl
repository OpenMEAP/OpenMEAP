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
			<td>${depl.versionIdentifier}</td>
			<td><a href="${(archiveUrls[depl.applicationArchive.hash])!}">[${(depl.applicationArchive.hashAlgorithm)!"NONE"}]${(depl.applicationArchive.hash)!"NONE"}</a>
			<td>${depl.type}</td>
		</tr>
	[/#list]
	</table>
[/#if]