OpenMEAP.updates.originalOnUpdate=OpenMEAP.updates.onUpdate;

/**
 * The container will go ahead and call this.
 * But we want REQUIRED and OPTIONAL updates
 * to wait till the test routines are called.
 */
OpenMEAP.updates.onUpdate=function(update) {
    OpenMEAP_update = update;
}
OpenMEAP.updates.onNoUpdate=function() {
    OpenMEAP_update = null;
}
OpenMEAP.updates.onCheckError=function(error) {
    OpenMEAP_updateError = error;
}

OpenMEAP.notifyReadyForUpdateCheck();