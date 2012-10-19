OpenMEAP.updates.originalOnUpdate=OpenMEAP.updates.onUpdate;

/**
 * The container will go ahead and call this.
 * But we want REQUIRED and OPTIONAL updates
 * to wait till the test routines are called.
 */
OpenMEAP.updates.onUpdate=function(update) {
    OpenMEAP.doToast("Update available");
    OpenMEAP_update = update;
}
OpenMEAP.updates.onNoUpdate=function() {
    OpenMEAP.doToast("No update");
    OpenMEAP_update = null;
}
OpenMEAP.updates.onUpdateError=function(update) {
    OpenMEAP.doToast(update.error.type+' : '+update.error.message);
    OpenMEAP_updateError = error;
}
OpenMEAP.updates.onCheckError=function(error) {
    OpenMEAP.doToast(error.code+' : '+error.message);
    OpenMEAP_updateError = error;
}

OpenMEAP.notifyReadyForUpdateCheck();