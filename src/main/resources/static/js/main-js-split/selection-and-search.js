let isMouseDown = false;
function getMessageContainerById(id) {
    const allMessageContainers = $all('.message-container')
    for (let i = 0; i < allMessageContainers.length; i++) {
        let messageContainer = allMessageContainers[i]
        if(messageContainer.getAttribute('message-id').toString() === id.toString()) {
            return messageContainer
        }
    }
    return null
}
function getContactContainerByName(contactName) {
    let resultContainer = null
    $all('.contact-container').forEach(contactContainer => {
        if(contactContainer.getAttribute('contact-name').toString() === contactName.toString()) {
            resultContainer = contactContainer
        }
    })
    return resultContainer
}
function getMessageContainerByCursor(event) {
    event.preventDefault(); // Предотвращаем показ стандартного контекстного меню
    const clickX = event.clientX + window.scrollX - 70;
    const clickY = event.clientY + window.scrollY - 70;

    const popupMenu = $('.message-options-unique-popup');
    showElement(popupMenu);

    const popupMenuRect = popupMenu.getBoundingClientRect();

    // Проверяем, чтобы позиция попапа не выходила за пределы экрана слева
    if (clickX < 0) {
        popupMenu.style.left = '0';
    } else if (clickX + popupMenuRect.width > window.innerWidth) {
        // Проверяем, чтобы позиция попапа не выходила за пределы экрана справа
        popupMenu.style.left = `${window.innerWidth - popupMenuRect.width}px`;
    } else {
        popupMenu.style.left = `${clickX}px`;
    }

    // Проверяем, чтобы позиция попапа не выходила за пределы экрана сверху
    if (clickY < 0) {
        popupMenu.style.top = '0';
    } else if (clickY + popupMenuRect.height > window.innerHeight) {
        // Проверяем, чтобы позиция попапа не выходила за пределы экрана снизу
        popupMenu.style.top = `${window.innerHeight - popupMenuRect.height}px`;
    } else {
        popupMenu.style.top = `${clickY}px`;
    }

    const clickedMessageContainer = event.target.closest(".message-container");
    clickedMessageContainer.classList.add('selected')
    return clickedMessageContainer;
}
function addMessageSelectionListener(messageContainer) {
    messageContainer.onmousedown = () => {
        isMouseDown = true;
    };
    messageContainer.onmouseup = () => {
        isMouseDown = false;
        enableTextSelection();
    };
    messageContainer.onmouseover = () => {
        if (!isMouseDown) {
            return;
        }

        messageContainer.classList.add('selected');
        const messageId = messageContainer.getAttribute('message-id')
        selectedMessageIdSet.add(messageId)
        disableTextSelection();
    };
    messageContainer.onclick = () => {

        const messageId = messageContainer.getAttribute('message-id')
        if (messageContainer.classList.contains('selected')) {
            messageContainer.classList.remove('selected')
            selectedMessageIdSet.delete(messageId)
        } else {
            messageContainer.classList.add('selected')
            selectedMessageIdSet.add(messageId)
        }
    }
}
function unselectOnEscapeClick(event) {
    if (event.key === 'Escape') {
        unselectAllMessages();
        enableTextSelection();
    }
}
function unselectAllMessages() {
    const messageContainers = $all('.message-container');
    messageContainers.forEach((messageContainer) => {
        messageContainer.classList.remove('selected');
        const messageId = messageContainer.getAttribute('message-id')
        selectedMessageIdSet.delete(messageId)
    });
}
function enableTextSelection() {
    document.body.style.userSelect = 'text';
}
function disableTextSelection() {
    document.body.style.userSelect = 'none';
}
document.addEventListener('keydown', unselectOnEscapeClick);
document.addEventListener('dblclick', unselectAllMessages);



