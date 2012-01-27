[#ftl]
<div id="messages">
	[#if messages??]
		<ul style="color:red;">
		[#list messages as message]
			<li>${message}</li>
		[/#list]
		</ul>
	[/#if]
</div>