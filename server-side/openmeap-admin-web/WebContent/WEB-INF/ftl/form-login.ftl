[#ftl]
<h2>Login Page</h2>
<form method="POST" action="j_security_check">
	[#if (request.parameterMap.invalidLogin[0])?? && (request.parameterMap.j_username[0])??]
		<ul>
			<li>Incorrect username/password for "${request.parameterMap.j_username[0]}".</li>
		</ul>
	[/#if]
	<table border="0">
		<tr><td>Enter your user name:</td><td><input type="text" style="width:25em;" name="j_username"/></td></tr>
		<tr><td>Enter your password:</td><td><input type="password" style="width:25em;" name="j_password"/></td></tr>
	</table>
	<input type="submit"/>
</form>