document.addEventListener("DOMContentLoaded",  () => {
    updateSelectedContactNameOnServer()
    updateContacts();
    connectToWebSocket();
    initializeMyUser()
    scaleElementByMouseWheel ('.full-image-view-img')
    scaleImageOnClick($('.found-user-avatar'))
    dragula([$('.contacts-container')])
})