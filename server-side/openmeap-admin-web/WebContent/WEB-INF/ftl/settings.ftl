[#ftl]
<form method="POST">
	<input type="hidden" name="processTarget" value="${processTarget!}"/>
	<fieldset>
		<legend>Global Settings &amp; Preferences</legend>
		<dl>
			<dt>External Service Url:</dt>
			<dd>
				<input type="text" name="externalServiceUrlPrefix" value="${(externalServiceUrlPrefix?html)!}"/><br/>
			</dd>
		
			<dt>Max File Upload Size:</dt>
			<dd><input type="text" name="maxFileUploadSize" value="${(maxFileUploadSize?string.computer)!}"/></dd>
		
			<dt>File-system Storage Path Prefix:</dt>
			<dd><input type="text" name="tempStoragePathPrefix" value="${(tempStoragePathPrefix?html)!}"/></dd>
			
			<dt>Authentication Salt:</dt>
			<dd>
				<input type="password" name="authSalt" value="${(authSalt?html)!}"/><br/>
				<input type="password" name="authSaltVerify" value="${(authSaltVerify?html)!}"/>
			</dd>
		</dl>
	</fieldset>
	<fieldset>
		<legend>Cluster Nodes</legend>
			
		<div id="clusterNodesPlaceholderId">
		</div>
	</fieldset>
	[#if mayModify]
		<input type="submit" value="Save"/>
	[/#if]
</form>

<div style="display:none;" id="clusterNodeTemplateId">
	<fieldset>
		<dl>
			<dt>Admin Server Accessible Service Url Prefix:</dt>
			<dd><input class="cluster-node-url" type="text" name="clusterNodeUrl[]" value="${(thisUrl?html)!}"/></dd>
			<dt>File-system Storage Path Prefix:</dt>
			<dd><input class="cluster-node-path" type="text" name="clusterNodePath[]" value="${(thisPath?html)!}"/></dd>
			[#if mayModify]
			[ <a class="minus" href="javascript:void(0);">-</a> | <a class="plus" href="javascript:void(0);">+</a> ]
			[/#if]
		</dl>
	</fieldset>
</div>

<script>
var createClusterNodeTemplate = function(url,path) {
	var thisClusterHtml = $( $("#clusterNodeTemplateId").html() );
	
	$("input.cluster-node-url",thisClusterHtml).attr("value",url);
	$("input.cluster-node-path",thisClusterHtml).attr("value",path);
	
	var fieldSet = $("fieldset",thisClusterHtml);
	$("a.minus",thisClusterHtml).click(function() {
			$(this.parentNode.parentNode).remove();
		});
	$("a.plus",thisClusterHtml).click(function() {
			$(this.parentNode.parentNode).after(createClusterNodeTemplate("",""));
		});
	return thisClusterHtml;
}

var clusterNodes = [];
[#if clusterNodes??]
	[#list clusterNodes?values as node]
		clusterNodes.push({url:"${node.serviceWebUrlPrefix!}",path:"${node.fileSystemStoragePathPrefix!}"});
	[/#list]
[/#if]
if( clusterNodes.length==0 )
	clusterNodes.push({url:"",path:""});
for( var i=0; i<clusterNodes.length; i++ ) {
	var node = createClusterNodeTemplate(clusterNodes[i].url,clusterNodes[i].path);
	$("#clusterNodesPlaceholderId").append(node);
}
</script>
