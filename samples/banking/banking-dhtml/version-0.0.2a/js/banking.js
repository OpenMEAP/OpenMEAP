function Banking() { this.instantiate(); }
Banking.prototype.instantiate = function() {
	this.readyState=false;

	this.isNetworkEnabled = function() { 
		return (OpenMEAP.getDeviceType()!="Browser"); 
	}
	
	// for development, it's desirable that the application
	// work within a browser, in isolation from the web-service
	if( this.isNetworkEnabled() ) {
		this.urls = {service:"http://dev.openmeap.com:8081/banking-web/interface/"};
		this.ajaxData = {};//{username:"friend",password:"fr13nd"};
	} else {
		this.urls = {
			loginResult:"xml/login-result.xml",
			completed:"xml/completed-",
			refresh:"xml/accounts-refresh.xml",
		};
	}
	
	this.pages = {
		loginForm:{
			component:"components/login-form.html",
			runMethod:"runLoginForm"
		},
		accountsOverview:{
			component:"components/account-overview.html",
			runMethod:"runAccountOverviews"
		},
		transactionListing:{
			component:"components/transaction-listing.html",
			runMethod:"runTransactionsListing"
		}
	};
}

/* FRONT CONTROLLER */

Banking.prototype.run = function() {
	var fragmentParams = this.getFragmentParams();
	
	if( this.getVariable("page") ) 
		fragmentParams["page"]=this.getVariable("page");
	
	if( ! this.isLoggedIn() )
		this.toPage("loginForm");
	else this.toPage(fragmentParams["page"],"accountsOverview");
	
	var obj = this;
	
	this.hashIntervalId = setInterval(
		function() { 
			obj.hashInterval();
		},100);
}
Banking.prototype.hashInterval = function() {
	var curHash = document.location.hash+"";
	if( ! this.isLoggedIn() ) {
		this.toPage("loginForm");
		$(".logoutLink").css("display","none");
	} else $(".logoutLink").css("display","block");
	if( !this.lastHash || this.lastHash!=curHash ) {
		this.lastHash=curHash;
		var fragParams = this.getFragmentParams();
		if( fragParams.page ) {
			this.loadPage(fragParams["page"]);
		}
	}
}

Banking.prototype.getServiceUrl = function() {
	OpenMEAP.clearCache();
	return this.urls.service+"?"+Math.random();
}

/* SUPPORT METHODS */
Banking.prototype.debug = function(msg) {
	OpenMEAP.doToast(msg);
}
Banking.prototype.isLoggedIn = function(loggedIn) {
	if( loggedIn )
		this.setVariable("isLoggedIn",true);
	return this.getVariable("isLoggedIn");
}

Banking.prototype.setUser = function(user) {
	this.setVariable("user",OpenMEAP.utils.toJSON(user));
}
Banking.prototype.getUser = function() {
	eval("var user = "+this.getVariable("user"));
	return user;
}

Banking.prototype.setAccountOverviews = function(map) {
	this.setVariable("accountOverviews",OpenMEAP.utils.toJSON(map));
}
Banking.prototype.getAccountOverviews = function() {
	eval("var accounts = "+this.getVariable("accountOverviews"));
	return accounts;
}

Banking.prototype.setSelectedAccount = function(acct) {
	this.setVariable("selectedAccount", acct.number);
}
Banking.prototype.getSelectedAccount = function() {
	var selectedAccountNumber = this.getVariable("selectedAccount");
	if( selectedAccountNumber ) {
		var accounts = this.getAccountOverviews();
		for( var i=0; i<accounts.length; i++ )
			if( selectedAccountNumber == accounts[i].number )
				return accounts[i];
	}
}

Banking.prototype.toPage = function(pageName,defaultName) {
	var pageName = pageName?pageName:defaultName;
	document.location="#page="+pageName;
}
Banking.prototype.loadPage = function(pageName,defaultName) {
	var pageName = pageName ? pageName : defaultName;
	this.setVariable("page",pageName);
	var url = this.pages[pageName].component
	var method = this.pages[pageName].runMethod;
	var obj = this;

	$.get(url, null, function(data) {
			$("div#mainBodyDiv").html(data);
			this.readyState=true;
			eval("obj."+method+"();");
		}, "html" );
}

Banking.prototype.getFragmentParams = function() {
	var src = document.location;
	var parts = (""+src).split("#");
	var fragmentParams = {};
	if( parts.length > 1) {
		fragmentParams = OpenMEAP.utils.paramsToObj(parts[1]);
	}
	return fragmentParams;
}
Banking.prototype.replaceVariables = function(obj,context) {
	for( i in obj ) {
		$("span[class~="+i+"]",context).html(obj[i]);
	}
	$("span[class~=dollarFormat]",context).each(function(){
		$(this).html(Format.toUSDollars($(this).html()))
	});
}

/** Session variables ***************/

Banking.prototype.setVariable = function(key,value) {
	if( ! this.sessionVariables ) {
		this.sessionVariables = {};
	}
	this.sessionVariables[key]=value;
}
Banking.prototype.getVariable = function(key) {
	if ( ! this.sessionVariables ) {
		return null;
	}
	return this.sessionVariables[key];
}
Banking.prototype.clearVariables = function() {
	this.sessionVariables = {};
}

/** XML PARSING *********************/

Banking.prototype.parseLoginResponse = function(data) {
	var loginNodes = data.getElementsByTagName("login");
	if( loginNodes.length > 0 && loginNodes.item(0).attributes["auth-token"].textContent.length>0 ) {
		var loginNode = loginNodes.item(0);
		var user = { username:loginNode.attributes["owner"].textContent };
		var accounts = this.parseAccounts( data.getElementsByTagName("account") );
		return { user:user, accounts:accounts };
	} 
}
Banking.prototype.parseAccounts = function(accts) {
	var accountOverviews = [];
	for( var i=0; i<accts.length; i++ ) {
		var acct={};
		for(attr in accts[i].attributes) {
			var thisAttr = accts[i].attributes[attr];
			if( typeof thisAttr.nodeName != 'undefined' ) {
				var name = thisAttr.nodeName;
				if( name=="type" )
					name="name";
				acct[name]=thisAttr.textContent;
			}
		}
		accountOverviews.push(acct);
	}
	return accountOverviews;
}
Banking.prototype.parseTransactions = function (data) {
	// make sure the xml fragment returned is a set of transactions
	var transArr = [];
	var trans = data.getElementsByTagName("trans");
	if( trans.length > 0 ) {
		
		for( var i=0; i<trans.length; i++ ) {
			var tran = trans[i].attributes;
			var tranObj = {};
			for( var j=0; j<tran.length; j++ ) {
				var nodeName = tran.item(j).nodeName;
				var nodeValue = tran.item(j).textContent;
				if( nodeName=="amount" ) {
					if( trans[i].attributes['type'].textContent=="deposit" )
						nodeName="deposit";
					else nodeName="withdraw";
				}
				if( nodeName=="status" )
					nodeName="type";
				tranObj[nodeName] = nodeValue;
			}
			transArr.push(tranObj);
		}
	} // TODO: else some error condition
	return transArr;
}
Banking.prototype.parseError = function(data) {
	var errs = data.getElementsByTagName("error");
	if( errs!=null && errs.length > 0 ) {
		return {
			message:errs.item(0).attributes['message'].textContent,
			code:errs.item(0).attributes['code'].textContent
		};
	} 
	return null;
}

/** CONTROLLER LOGIC ***************/

/* LOGIN FORM CONTROLLER */

Banking.prototype.runLoginForm = function() {
	this.replaceVariables(this.getUser());
}
Banking.prototype.processLoginForm = function(form) {
	var obj = this;
	if( this.isNetworkEnabled() ) {
		$.ajax(this.getServiceUrl(),
				$.extend({},{
					data:{
						action:"login",
						username:form.username.value,
						password:form.password.value
					},
					success:function(data){obj.processLoginResult(data)},
					dataType:"xml"
				},this.ajaxData)
			);
	} else {
		$.get(this.urls.loginResult,{},function(data){obj.processLoginResult(data)},"xml");
	}
}
Banking.prototype.processLoginResult = function(data) {
	var error = this.parseError(data);
	if( error != null ) {
		OpenMEAP.doToast(error.message);
	} else {
		var response = this.parseLoginResponse(data);
		if( response ) {
			this.setUser(response.user);
			this.setAccountOverviews(response.accounts);
			this.isLoggedIn(true);
			this.toPage("accountsOverview");
		} else OpenMEAP.doToast("Failed to parse account overview data");
	}
}

/* ACCOUNT OVERVIEWS CONTROLLER */

Banking.prototype.runAccountOverviews = function() {
	this.replaceVariables(this.getUser());
	var accounts = this.getAccountOverviews();
	var obj = this;
	for( var i=0; i<accounts.length; i++ ) {
		var templateHtml = $($("#accountRowTemplate").html());
		this.replaceVariables(accounts[i],templateHtml);
		$("a[class=accountName]",templateHtml).each(function() {
			this.account = accounts[i];
		});
		$("a[class=accountName]",templateHtml).click(function() {
			obj.setSelectedAccount(this.account);
			obj.toPage("transactionListing");
		});
		$("#accountRows").append(templateHtml);
	}			
}

/* ******************************* */
/* TRANSACTION LISTINGS CONTROLLER */
/* ******************************* */

Banking.prototype.runTransactionsListing = function() {
	var acct = this.getSelectedAccount();
	var user = this.getUser();
	var obj = this;
	this.replaceVariables(acct);

	if(this.isNetworkEnabled()) {
		$.ajax(this.getServiceUrl(),
				$.extend({},{
					data:{
						action:"completed",
						acct:acct.number,
						username:user.username,
						auth:"doesntmatterfordemo"
					},
					success:function(data){obj.processTransactionsListing(data)},
					dataType:"xml"
				},this.ajaxData)
			);
	} else {
		var url = this.urls.completed+acct.name+".xml";
		$.get(url,{},function(data){obj.processTransactionsListing(data);},"xml");
	}
}
Banking.prototype.processTransactionsListing = function(data) {
	// we'll cache this so we don't have to pull it each row
	var templateRowHtml = "<tr>"+$("#transTemplateRow").html()+"</tr>";
	var trans = this.parseTransactions(data);
	for( var i=0; i<trans.length; i++ ) {
		var tranObj = trans[i];
		var templateRow = $(templateRowHtml);
		this.replaceVariables(tranObj,templateRow);
		$("#transTable").append( "<tr>"+templateRow.html()+"</tr>" );
	}
}

/* ************************************ */
/* ACCOUNT REFRESH CONTROLLER FUNCTIONS */
/* ************************************ */

Banking.prototype.processAccountsUpdateResult = function(data) {
	var obj=this;
	var error = this.parseError(data);
	if( error!=null ) {
		OpenMEAP.doToast(error.message);
		return;
	} 
	var accounts = this.parseAccounts( data.getElementsByTagName("account") );
	this.setAccountOverviews( accounts );
	this.toPage("accountsOverview");
}

/**************************/
/*** APPLICTATION SETUP ***/
/**************************/

var banking = new Banking();
function startupApplication() { 
	banking.run();
}
