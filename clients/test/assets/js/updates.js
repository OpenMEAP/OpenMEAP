function OpenMEAP_onUpdateCheck(update) {
    if(update!=null) {
    alert(update);
        OpenMEAP_update = update;
    } else {
        OpenMEAP_update = null;
    }
}