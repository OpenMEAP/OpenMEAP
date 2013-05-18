[#ftl]
<!doctype html>
<html>
<head>
	<title>${page.title}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" >
	<meta name="viewport" content="width=device-width, initial-scale=1">
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
<body id="loginpage">

		<div class="container_18 clearfix">
			<div class="push_5 grid_6">
                <a href="#"><img src="../img/logo-OpenMEAP.png" ></a>
            </div>
            <div class="clear"></div>
            <div class="widget push_8 grid_18" id="login">
                <div class="widget_title">
	</div>
		<div class="widget_body">
			<div class="widget_content">
				${children.messages!}
				${children.subMenu!}				
				${children.bodyPreface!}
				${children.body!}
				${children.bodyPostface!}			
            
            </div>
                    <div class="clear"></div>
                    </div>
                </div>
            </div>

        </div>
    </div> <!-- main -->
</div> <!-- wrap -->

</body>
</html>
