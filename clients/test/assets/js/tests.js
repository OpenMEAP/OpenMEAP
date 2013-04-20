function testNonImmediateUpdates() {
    document.body.innerHTML="<h6>Update Check Callback</h6><div id=\"update-callback\">No update</div>";
    checks = 0;
    interval = setInterval(function() {
        checks++;
        if(OpenMEAP_update!='undefined') {
            clearInterval(interval);
            if(OpenMEAP_update!=null) {
                OpenMEAP.updates.originalOnUpdate(OpenMEAP_update);
            } else {
                document.getElementById('update-callback').innerHTML = "Update check returned no update.";
            }
        } else {
            document.getElementById('update-callback').innerHTML = "No update after "+checks+" check(s)";
        }
    },250);
}

function testJavascriptUpdates() {
    document.body.innerHTML="<h6>Javascript Updates API Tests</h6><div id=\"update-callback\"></div>";
    setTimeout(function() {
        var isTime = OpenMEAP.isTimeForUpdateCheck();
    },1000);
    OpenMEAP.updates.onUpdate=OpenMEAP.updates.originalOnUpdate;
    OpenMEAP.updates.onNoUpdate=function() {
        OpenMEAP_update = null;
    }
    OpenMEAP.updates.onCheckError=function(error) {
        OpenMEAP_updateError = error;
        OpenMEAP.doToast(error.code+': '+error.message);
    }
    OpenMEAP.checkForUpdates();
    var isTime = OpenMEAP.isTimeForUpdateCheck();
}
    
function testPreferences() {

    document.body.innerHTML="<h6>Test Preferences</h6><div id='result'></div>";
    
    var innerHtml = "";
    
	if( typeof(OpenMEAP.getPreferences) != 'function' ) {
        innerHtml+="OpenMEAP.getPreferences is not a function<br/>";
    }
    
	var prefs = OpenMEAP.getPreferences("test-prefs");
    if( typeof(prefs) != 'object' ) {
        innerHtml+="OpenMEAP.getPreferences(\"test-prefs\") did not return an object<br/>";
    } else {
        if( prefs.get("key")==null ) {
            if( prefs.put("key","value") ) { 
                testPrefs.put("evalOnInit","testPreferences();");
                OpenMEAP.reload();
            } else {
                innerHtml+="prefs.put key=value returned false or null<br/>";
            }
        } else if( prefs.get("key")=="value" ) {
            innerHtml+="prefs.put passes<br/>";
            prefs.remove("key");
        }
    }
    
    document.getElementById("result").innerHTML=innerHtml;
}

function runTests() {
    testPrefs = OpenMEAP.getPreferences("tests");
    try {
        var evalOnInit = testPrefs.get("evalOnInit");
        if(evalOnInit!=null) {
            try {
                testPrefs.remove("evalOnInit");
                eval(evalOnInit);
            } catch(e) {
                OpenMEAP.doToast(evalOnInit);
                OpenMEAP.doToast(e);
            }
        } else {
            var innerHtml = "typeof(OpenMEAP) = "+typeof(OpenMEAP)+"<br/>";
            innerHtml = "<a href=\"javascript:testPreferences();\">Test Preferences</a><br/>";
            innerHtml += "<a href=\"javascript:testNonImmediateUpdates();\">Test Non-IMMEDIATE Updates</a><br/>";
            innerHtml += "<a href=\"javascript:testJavascriptUpdates();\">Test Javascript Update Checks</a><br/>";
            document.body.innerHTML=innerHtml;
        }
    } catch(e) {
        OpenMEAP.doToast(e);
    }
}

