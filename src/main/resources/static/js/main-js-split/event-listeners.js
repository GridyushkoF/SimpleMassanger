const scalingApi = (() => {
    // current view transform
    const m = [1, 0, 0, 1, 0, 0];             // alias
    let scale = 1;              // current scale
    const pos = { x: 0, y: 0 }; // current position of origin
    let dirty = true;
    return {
        applyTo(el) {
            if (dirty) {
                this.update()
            }
            el.style.transform = `matrix(${m[0]},${m[1]},${m[2]},${m[3]},${m[4]},${m[5]})`;
        },
        update() {
            dirty = false;
            m[3] = m[0] = scale;
            m[2] = m[1] = 0;
            m[4] = pos.x;
            m[5] = pos.y;
        },
        pan(amount) {
            if (dirty) {
                this.update()
            }
            pos.x += amount.x;
            pos.y += amount.y;
            dirty = true;
        },
        scaleAt(at, amount) { // at in screen coords
            if (dirty) {
                this.update()
            }
            scale *= amount;
            pos.x = at.x - (at.x - pos.x) * amount;
            pos.y = at.y - (at.y - pos.y) * amount;
            dirty = true;
        },
        reset() {
            scale = 1;
            pos.x = 0;
            pos.y = 0;
            m[0] = 1; // scale x
            m[1] = 0; // skew y
            m[2] = 0; // skew x
            m[3] = 1; // scale y
            m[4] = 0; // translate x
            m[5] = 0; // translate y
            dirty = true;
        }
    };
})();

$('.start-search-by-name-btn').onclick = () => {
    searchContactByUsername()
}
$('.delete-chat-btn').onclick = () => {
    const selected_option = $('input[type="radio"]:checked')
    let isLocallyDeletion = selected_option.id === 'delete-locally-option'
    sendChatDeletionRequest(isLocallyDeletion)
    hideElement($('.delete-contact-overlay'))
}
$('.show-profile-popup-btn').onclick = () => {
    showElement($('.profile-overlay'))
}
$(".send-message-btn").onclick = () => {
    if (messageOperationType === 'send') {
        sendMessage()
    } else {
        if(messageOperationType === 'edit') {
            editMessage()
        } else if(messageOperationType === 'reply') {
            if(selectedMessageIdSet.size < 2) {
                const selectedMessageId = [...selectedMessageIdSet][0]
                sendMessage(selectedMessageId)
                    const replyingMessageText = $('.replying-message-text');
                replyingMessageText.textContent = ''
                hideElement(replyingMessageText)
            }
            
        }
        messageOperationType = 'send'
        $(".send-message-img").src = '/static/img/edit_pencil.png'
        unselectAllMessages()
    }
    $('.message-text-area').value = ''
}
$('.update-profile-avatar-btn').onclick = () => {
    sendNewAvatar()
}
$('.pin-image-btn').onclick = () => {
    showElement($('.pin-image-overlay'))
}
$('.image-input').onchange = (event) => {
    let target = event.target;
    if (!FileReader) {
        alert('FileReader не поддерживается');
        return;
    }
    if (!target.files.length) {
        alert('Ничего не загружено');
        return;
    }
    let fileReader = new FileReader();
    fileReader.onload = () => {
        $('.pinned-image').src = fileReader.result;
    }
    fileReader.readAsDataURL(target.files[0]);
}
$('.send-message-with-image-btn').onclick = () => {
    sendMessage(null,true)
    hideElement($('.pin-image-overlay'))
    $('.pinned-image').src = ''
}
$('.messages-container').addEventListener('contextmenu', (event) => {
    const foundContainer = getMessageContainerByCursor(event)
    selectedMessageIdSet.add(foundContainer.getAttribute('message-id'))
    hideButtonIfSelectedMoreThan1Message($('.edit-message-btn'))
    hideButtonIfSelectedMoreThan1Message($('.reply-to-message-btn'))
})
$('.delete-message-btn').onclick = () => {
    deleteAllMessages([...selectedMessageIdSet])
    selectedMessageIdSet.forEach(messageId => {
        selectedMessageIdSet.delete(messageId)
    })

    hideElement($('.message-options-unique-popup'))
}
$('.edit-message-btn').onclick = () => {
    $all('.message-container').forEach((container) => {
        let messageId = container.getAttribute('message-id');
        if (selectedMessageIdSet.has(messageId.toString()) && selectedMessageIdSet.size < 2) {
            const messageText = container.$('.message-text').textContent
            messageOperationType = 'edit';
            $(".send-message-img").src = '/static/img/edit_pencil.png'
            messageIdToEdit = messageId
            $('.message-text-area').value = messageText
        }
    })
    hideElement($('.message-options-unique-popup'))
}
$('.reply-to-message-btn').onclick = () => {
    const selectedMessageId = [...selectedMessageIdSet][0]
    const messageContainer = getMessageContainerById(selectedMessageId)
    const replyingMessageText = $('.replying-message-text')
    $(".send-message-img").src = '/static/img/reply.png'
    replyingMessageText.textContent = messageContainer.$('.message-text').textContent
    showElement(replyingMessageText)
    messageOperationType = 'reply'
    hideElement($('.message-options-unique-popup'))
}
$('.exit-message-options-btn').onclick = () => {
    hideElement($('.message-options-unique-popup'))
    unselectAllMessages()
    selectionType = 'mono'
}
$('.activate-message-forwarding-btn').onclick = () => {
    hideElement($('.message-options-unique-popup'))
    showElement($('.forward-message-overlay'))
    updateContacts($('.contacts-to-forwarding'), false)
    selectionType = 'poly'

}
$('.forward-message-to-contacts-btn').onclick = () => {
    let contactNameList = []
    $('.contacts-to-forwarding').$all('.contact-container').forEach(contactContainer => {
        if(contactContainer.classList.contains('selected')) {
            let contactName = contactContainer.getAttribute('contact-name')
            contactNameList.push(contactName)
        }

    })
    forwardMessagesToContacts(contactNameList, [...selectedMessageIdSet])
    contactNameList = []
    selectionType = 'mono'
    hideElement($('.forward-message-overlay'))
    unselectAllMessages()
}
$('.send-message-img').onmouseover = () => {
    const sendMessageImg = $('.send-message-img');
    if(messageOperationType === 'send') {
        sendMessageImg.src = '/static/img/blue_send_msg.png'
    } else if (messageOperationType === 'edit') {
        sendMessageImg.src = '/static/img/blue_pencil.png'
    }
}
$('.send-message-img').onmouseout = () => {
    const sendMessageImg = $('.send-message-img');
    if(messageOperationType === 'send') {
        sendMessageImg.src = '/static/img/send_msg.png'
    } else if (messageOperationType === 'edit') {
        sendMessageImg.src = '/static/img/edit_pencil.png'
    }
}
$('.chat-space').addEventListener('scroll', () => {
    if ($('.chat-space').scrollTop === 0 && shouldUpdateMessageHistoryOnScroll) {
        getMessageHistory(false).catch(e => $error(e))
    }
});
$all('.avatar').forEach(avatar => {
    if(!avatar.classList.contains('loadable')) {
        scaleImageOnClick(avatar)
    }
})

$('.add-user-to-contacts-btn').onclick = () => {
    addUserToMyContactList()
}
$('.update-username-input').oninput = () => {
    setImageGray($('.tick'))
    clearTimeout(writingTimeOutBeforeUpdating);
    writingTimeOutBeforeUpdating = setTimeout(function() {
        updateUsernameAndContacts()
        setImageNotGray($('.tick'))
    }, 1500);
}

$('.description-textarea').oninput = () => {
    clearTimeout(writingTimeOutBeforeUpdating);
    writingTimeOutBeforeUpdating = setTimeout(function() {
        updateDescription()
        alert('Успешно сохранено новое описание')
    }, 1500);
}


function scaleElementByMouseWheel(element) {
    element.addEventListener("mousemove", mouseEvent, {passive: false});
    element.addEventListener("mousedown", mouseEvent, {passive: false});
    element.addEventListener("mouseup", mouseEvent, {passive: false});
    element.addEventListener("mouseout", mouseEvent, {passive: false});
    element.addEventListener("wheel", mouseWheelEvent, {passive: false});
    const mouse = {x: 0, y: 0, oldX: 0, oldY: 0, button: false};
    function mouseEvent(event) {
        if (event.type === "mousedown") { mouse.button = true }
        if (event.type === "mouseup" || event.type === "mouseout") { mouse.button = false }
        mouse.oldX = mouse.x;
        mouse.oldY = mouse.y;
        mouse.x = event.pageX;
        mouse.y = event.pageY;
        if(mouse.button) { // pan
            scalingApi.pan({x: mouse.x - mouse.oldX, y: mouse.y - mouse.oldY});
            scalingApi.applyTo(element);
        }
        event.preventDefault();
    }
    function mouseWheelEvent(event) {
        const x = event.pageX - (element.width / 2);
        const y = event.pageY - (element.height / 2);
        if (event.deltaY < 0) {
            scalingApi.scaleAt({x, y}, 1.1);
            scalingApi.applyTo(element);
        } else {
            scalingApi.scaleAt({x, y}, 1 / 1.1);
            scalingApi.applyTo(element);
        }
        event.preventDefault();
    }
}
function scaleImageOnClick(image) {
    image.onclick = (event) => {
        event.stopPropagation();
        $all('.popup-overlay').forEach(popupOverlay => hideElement(popupOverlay))
        showElement($('.full-image-view-overlay'))
        $('.full-image-view-img').src = image.src
    }
}
function hoverContactDeletionBtnOnClick(contactContainer) {
    const deleteContactBtn = contactContainer.$('.delete-contact-btn')
    deleteContactBtn.classList.add('disabled')
    contactContainer.onmouseover = () => {
        deleteContactBtn.classList.remove('disabled')
        deleteContactBtn.classList.add('enabled')
    }
    contactContainer.onmouseout = () => {
        deleteContactBtn.classList.remove('enabled')
        deleteContactBtn.classList.add('disabled')
    }
}
function scalePinnedImageOnClick(pinnedImage) {
    pinnedImage.onclick = (event) => {
        event.stopPropagation();
        const fullImageViewImg = $('.full-image-view-img');
        fullImageViewImg.src = pinnedImage.getAttribute('file-download-link');
        scalingApi.reset(); // Сбросить масштаб и позицию
        scalingApi.applyTo(fullImageViewImg); // Применить сброс к текущему изображению
        showElement($('.full-image-view-overlay'));
    }
}
function hideButtonIfSelectedMoreThan1Message(buttonElement) {
    if (selectedMessageIdSet.size > 1) {
        hideElement(buttonElement);
    } else {
        showElement(buttonElement);
    }
}
