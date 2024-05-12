document.addEventListener("DOMContentLoaded",  () => {
    updateSelectedContactNameOnServer()
    updateContacts();
    connectToWebSocket();
    initializeMyUser()
    scaleElementByMouseWheel ($('.zoomable'))
    scaleImageOnClick($('.found-user-avatar'))
    dragula([$('.contacts-container')])
})