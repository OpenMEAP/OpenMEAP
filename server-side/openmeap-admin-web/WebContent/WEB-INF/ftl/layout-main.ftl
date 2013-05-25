[#ftl]
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" >
	<meta name="viewport" content="width=device-width, initial-scale=1">
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
	<div id="wrap">
        <div id="main">
    <header>
    <div class="container_16 clearfix">
        <div class="clearfix">
               <a id="logo"><img src="../img/logo-OpenMEAP.png" ></a>
		</div>
        
        <nav>
            <div id="navcontainer" class="clearfix">
            <div id="user" class="clearfix">
                <strong class="username">Welcome, ${request.userPrincipal.name}</strong>
                <ul class="piped">
                        <li><a href="?bean=settingsPage">Settings</a></li>
                        <li><a href="?logout">Logout</a></li>
                        </ul>
                    </div>
                    
                    <div id="navclose"></div>
                    
                    <ul class="sf-menu">
                        <li class="">
                            <a href="?bean=mainOptionsPage">
                                <span class="icon"><img src="../img/menu/dashboard.png" /></span>
                                <span class="title">Dashboard</span>
                            </a>
                        </li>
                        <li class="">
                            <a href="?bean=appListingsPage">
                                <span class="icon"><img src="../img/menu/applications.png" /></span>
                                <span class="title">Applications</span>
                            </a>
                        </li>
                        <li class="">
                            <a href="?bean=addModifyAppPage">
                                <span class="icon"><img src="../img/menu/addapp.png" /></span>
                                <span class="title">Add Application</span>
                            </a>
                        </li>
                       <li class="">
                            <a href="https://github.com/OpenMEAP/OpenMEAP/issues/"target="_blank">
                                <span class="icon"><img src="../img/menu/bugs.png" /></span>
                                <span class="title">Issues & Bugs</span>
                            </a>
                        </li>
                        <li class="">
                            <a href="http://wiki.openmeap.com/"target="_blank">
                                <span class="icon"><img src="../img/menu/wiki.png" /></span>
                                <span class="title">OpenMEAP Wiki</span>
                            </a>
                        </li>
                        <li class="">
                            <a href="http://forum.openmeap.com/"target="_blank">
                                <span class="icon"><img src="../img/menu/forum.png" /></span>
                                <span class="title">Support Forum</span>
                            </a>
                        </li>
                    </ul>
                    </div>
            </div>
        </header>
        <div class="container_16 clearfix" id="actualbody">

    <div class="grid_16 widget first">
        <div class="widget_title clearfix">
        </div>
        <div class="widget_body">
            <div class="widget_content">
            			${children.mainOptionsRow!}
						${children.messages!}
						${children.subMenu!}				
						${children.bodyPreface!}
						${children.body!}
						${children.bodyPostface!}
			</div>
		</div>			
	</div>
    <footer>
        <div class="container_20">
            <div class="grid_12 clearfix">
                <class="left"><img src="../img/powerlogo.png"</p>
                <p class="right">
                    ${children.footer!}
                </p>
            </div>
        </div>
    </footer>
</body>
</html>
