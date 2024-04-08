$all('.popup-overlay').forEach(popupOverlay => {
    popupOverlay.onclick = () => {
        hideElement(popupOverlay);
    };
    const popupMenu = popupOverlay.$('.popup-menu');
    popupMenu.onclick = (event) => {
        event.stopPropagation();
    };
    const popupExitBtn = popupOverlay.$('.exit-popup-btn')
    popupExitBtn.onclick = () => {
        hideElement(popupOverlay)
        if(popupExitBtn.classList.contains('content-delete-after-exit')) {
            $('.pinned-image').src = ''
        }
    }
});