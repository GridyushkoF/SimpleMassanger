document.addEventListener("DOMContentLoaded",  () => {
    updateSelectedContactNameOnServer()
    updateContacts();
    initializeMyUser()
    connectToWebSocket();
    scaleElementByMouseWheel ($('.zoomable'))
    scaleImageOnClick($('.found-user-avatar'))
    dragula([$('.contacts-container')])
    monitorUserActivity()
})