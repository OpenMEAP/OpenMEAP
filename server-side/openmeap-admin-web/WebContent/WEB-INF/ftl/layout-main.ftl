[#ftl]
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html lang="en-US" xml:lang="en-US" xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>${page.title}</title>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	[#if linkTags??]
		[#list linkTags as tag]
			${tag!}
		[/#list]
	[/#if]
	[#if scriptTags??]
		[#list scriptTags as tag]
			${tag!}
		[/#list]
	[/#if]
</head>
<body>
	<div id="wrapper">
		<div id="header" class="row container-16">

		</div>
		<div id="content-wrapper" class="row container-16">
			<div id="content" class="grid-16">
				<div id="callout" class="grid-4-inset alpha">
					${children.mainMenu!}
					${children.supportLicense!}
				</div>
				<div id="main" class="grid-12-inset omega">
					<div id="main-content">
						${children.mainOptionsRow!}
						${children.messages!}
						${children.subMenu!}				
						${children.bodyPreface!}
						${children.body!}
						${children.bodyPostface!}
					</div>
				</div>
			</div>	
			<div class="clearfix"></div>
		</div>
		<div id="admin-footer" class="row">
			<div class="container-16">
			${children.footer!}
			</div>
		</div>			
	</div>
</body>
</html>
