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

$('.update-description-btn').onclick = () => {
    updateDescription()
}
$('.add-user-to-contacts-btn').onclick = () => {
    addUserToMyContactList()
}

function scaleElementByMouseWheel(selector) {
    function addOnWheel(element, handler) {
        if (element.addEventListener) {
            if ('onwheel' in document) { // IE9+, FF17+
                element.addEventListener("wheel", handler);
            } else if ('onmousewheel' in document) { // устаревший вариант события
                element.addEventListener("mousewheel", handler);
            } else { // 3.5 <= Firefox < 17
                element.addEventListener("MozMousePixelScroll", handler);
            }
        } else { // IE8-
            element.attachEvent("onmousewheel", handler);
        }
    }

    let scale = 1;
    const minScale = 0.3;
    const maxScale = 5;
    function scaleElement(e) {
        let delta = e.deltaY || e.detail || e.wheelDelta;
        if (delta > 0) {
            scale = scale >= minScale ? scale - 0.1 : scale;
        } else {
            scale = scale < maxScale ? scale + 0.1 : scale;
        }
        element.style.transform = 'scale(' + scale + ')';
        e.preventDefault();
    }

    // Замените 'element' на реальный элемент, к которому вы хотите применить масштабирование
    let element = $(selector)
    addOnWheel(element, function(e) { scaleElement(e); });
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
        event.stopPropagation()
        $('.full-image-view-img').src = pinnedImage.getAttribute('file-download-link')
        showElement($('.full-image-view-overlay'))
    }
}
function hideButtonIfSelectedMoreThan1Message(buttonElement) {
    if (selectedMessageIdSet.size > 1) {
        hideElement(buttonElement);
    } else {
        showElement(buttonElement);
    }
}
