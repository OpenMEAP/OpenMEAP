[#ftl]
<div id="messages">
	[#if messages??]
		<ul style="color:green;”>
		[#list messages as message]
			<li>${message}</li>
		[/#list]
		</ul>
	[/#if]
</div>