[#ftl]
<h2>Login</h2>
<form method="POST" action="j_security_check">
	[#if (request.parameterMap.invalidLogin[0])?? && (request.parameterMap.j_username[0])??]
		<ul>
			<li>Incorrect username/password for "${request.parameterMap.j_username[0]}".</li>
		</ul>
	[/#if]
	<table border="0">
		<label class="block" for="username">Username:</label>
        <input type="text" name="j_username" class="large"/>
        <label class="block" for="password">Password:</label>
        <input type="password" name="j_password" class="large"/>
	</table>
	<input type="submit" value="Login"/>
</form>