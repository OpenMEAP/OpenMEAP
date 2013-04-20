[#ftl]
<ul class="${ulClasses!}">
[#if links??]
	[#list links as link]
		<li><a title="${link.title}" href="${link.url}">${link.content}</a></li>
	[/#list]
[/#if]
</ul>
