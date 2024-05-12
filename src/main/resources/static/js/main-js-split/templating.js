function showContact(contactJson, containerToAppend, shouldUpdateMessageHistory = true) {
    const {username: contactName, avatarName: contactAvatarName} = contactJson;

    let singleContactContainer = $create("div", "contact-container")
    let wrapContactContainer = $create('div','wrap-contact-container')
    singleContactContainer.setAttribute("contact-name", contactName)

    let avatarNameAndDeletionBtnContainer = $create('div','contact-main-info')
    let deleteContactBtn = $create('button', 'delete-contact-btn')
    let deleteContactImage = $create('img','delete-contact-img')
    deleteContactImage.src = '/static/img/delete_bin.png'
    deleteContactBtn.append(deleteContactImage)

    let unreadMessagesAmountText = $create('p','unread-messages-amount')
    getUnreadMessagesAmount(contactName)
        .then(unreadMessagesAmount => {
            if(unreadMessagesAmount > 0) {
                unreadMessagesAmountText.textContent = unreadMessagesAmount
            }
        })

    let contactUsernameText = $create("p", "contact-username")
    contactUsernameText.textContent = contactName;

    let contactAvatarImg = $create('img', 'avatar')
    let lastMessageText = $create('p','last-message-paragraph')

    getLastMessage(contactName)
        .then(lastMessage => {
            lastMessageText.textContent =
                lastMessage.length < maxLastMessageLength ? lastMessage : lastMessage.slice(0,maxLastMessageLength) + '...'
        })


    avatarNameAndDeletionBtnContainer.append(contactUsernameText, unreadMessagesAmountText, deleteContactBtn)
    wrapContactContainer.append(avatarNameAndDeletionBtnContainer,lastMessageText)
    singleContactContainer.append(contactAvatarImg, wrapContactContainer)

    singleContactContainer.onclick = () => {
        selectContactContainer(singleContactContainer)
        if (shouldUpdateMessageHistory === true) {
            getMessageHistory(true).catch(e => $error(e))
        }

    }
    deleteContactBtn.onclick = (event) => {
        event.stopPropagation()
        showElement($('.delete-contact-overlay'))
    }

    setAvatarPathIfExistsOrDefaultAvatar('/avatars/' + contactAvatarName, contactAvatarImg,contactName)

    scaleImageOnClick(contactAvatarImg);
    hoverContactDeletionBtnOnClick(singleContactContainer)


    containerToAppend.append(singleContactContainer)

}
function selectContactContainer(contactContainer) {
    selectedContactName = contactContainer.getAttribute("contact-name")
    if (selectionType === 'mono') {
        $all(".contact-container").forEach(container => {
            container.classList.remove('selected')
        })
        contactContainer.classList.add("selected")
        updateSelectedContactNameOnServer()
        clearUnreadMessage(selectedContactName)
        const unreadMessagesAmount = contactContainer.$('.contact-main-info').$('.unread-messages-amount')
        if(unreadMessagesAmount){unreadMessagesAmount.remove()}
    } else if (selectionType === 'poly') {
        if (contactContainer.classList.contains('selected')) {
            contactContainer.classList.remove('selected')
        } else {
            contactContainer.classList.add('selected')
        }
    }
    showElement($('.chat-menu'))
    hideElement($('.contact-not-selected-alert'))
}

function appendByInsertMode(insertMode, singleMessageContainer, conditionalInsertContainer) {
    const allMessagesContainer = $('.messages-container')
    if (insertMode === 'end') {
        allMessagesContainer.append(singleMessageContainer)
    } else if (insertMode === 'afterContainer') {
        conditionalInsertContainer.after(singleMessageContainer)
    } else if (insertMode === 'begin') {
        allMessagesContainer.insertBefore(singleMessageContainer, allMessagesContainer.firstChild)
    }
}

//---------------------message showing group---------------------
function showMessage(targetedMessageJson,conditionalInsertContainer = null,insertMode = 'end') {
    // insert mode can be equals: end,begin, afterContainer
    let {
        messageText: message,
        targetId: messageId,
        target: messageTarget,
        sender: sender,
        receiver: receiver,
        dateTime: dateTime,
        pinnedImageFilename: pinnedImageFilename,
        forwarder: forwarder,
        originalMessageWeReplied: originalMessageWeReplied,
        originalMessageStatus: originalMessageStatus
    } = targetedMessageJson;

    if (shouldNotLoadMessageHistory(sender, selectedContactName)) {
        initUnreadMessagesAmount(sender);
        return;
    }
    const messageTime = createMessageTimeElement(dateTime);

    if (messageTarget !== 'CREATE' && messageTarget !== 'CHAT_DELETE') {
        deleteOrUpdateMessageIfNeed(messageId, messageTarget, message, dateTime);
        showLastMessage(messageTarget, message, sender, receiver);
        return;
    }
    if(insertMode === 'end') {
        showLastMessage(messageTarget, message, sender, receiver);
    }

    const singleMessageContainer = createMessageContainer(messageId);
    const messageText = $create("p", "message-text");
    messageText.textContent = message;


    if (pinnedImageFilename !== null) {
        $log('CREATING IMAGE!')
        addPinnedImageToMessage(pinnedImageFilename, singleMessageContainer).onload = () => {
            scrollToBottom($('.chat-space'))
        };
    }
    if (forwarder !== null) {
        createForwardedByContainer(forwarder.username, singleMessageContainer)
    }
    singleMessageContainer.append(messageText, messageTime);
    createReplyContainer(originalMessageWeReplied, singleMessageContainer,originalMessageStatus)
    if (messageTarget === 'CHAT_DELETE') {
        createDeclineChatDeletionBtn(singleMessageContainer);
        createConfirmChatDeletionBtn(singleMessageContainer);
    }
    singleMessageContainer.classList.add(sender.username === myUsername ? 'right' : 'left')

    appendByInsertMode(insertMode, singleMessageContainer, conditionalInsertContainer);
    createDateElementIfNeed(dateTime,singleMessageContainer)
    scrollToBottom($('.chat-space'))
}
function showLastMessage(messageTarget, message, sender, receiver) {
    const contactName = myUsername === sender.username ? receiver.username : sender.username
    const contactContainer = getContactContainerByName(contactName)
    if(messageTarget === 'DELETE') {
        const allMessageContainers = $all('.message-container')
        contactContainer.$('.wrap-contact-container').$('.last-message-paragraph').textContent = allMessageContainers.length > 0 ? allMessageContainers[allMessageContainers.length - 1].$('.message-text').textContent : ''
        return
    }
    if(contactContainer !== null) {
        contactContainer.$('.wrap-contact-container').$('.last-message-paragraph').textContent =
            message.length < maxLastMessageLength ? message : message.slice(0, maxLastMessageLength) + '...'
    }

}
function initUnreadMessagesAmount(sender) {
    $all('.contact-container').forEach(contact => {
        if (contact.getAttribute('contact-name') === sender.username) {
            getUnreadMessagesAmount(sender.username)
                .then(unreadMessagesAmount => {
                    contact.$('.unread-messages-amount').textContent = unreadMessagesAmount > 0 ? unreadMessagesAmount : ''
                })
        }
    })
}
function deleteOrUpdateMessageIfNeed(messageId, messageTarget, messageText, dateTime) {
    const messageContainer = getMessageContainerById(messageId);
    if(!messageContainer) {return}
    if (messageTarget === 'DELETE') {
        findAllReplyMessagesAndDetachOfOriginal(messageId)
        $('.messages-container').removeChild(messageContainer);
    } else if (messageTarget === 'UPDATE') {
        updateExistingMessage(messageContainer, messageText, dateTime);
        findAllReplyMessagesAndUpdateByOriginal(messageId,messageText)
    }
}
function findAllReplyMessagesAndDetachOfOriginal(originalMessageId) {
    findAllReplyMessageContainersByOriginalMessageId(originalMessageId).forEach(replyMessage => {
        replyMessage.$('.replied-message-info-container').$('.message-of-reply-original-text').textContent = 'Сообщение удалено'
    })
}
function findAllReplyMessagesAndUpdateByOriginal(originalMessageId,newMessageText) {
    findAllReplyMessageContainersByOriginalMessageId(originalMessageId).forEach(replyMessage => {
        replyMessage.$('.replied-message-info-container').$('.message-of-reply-original-text').textContent = newMessageText
    })
}
function findAllReplyMessageContainersByOriginalMessageId(searchingOriginalMessageId) {
    const foundMessageContainers = []
    const allMessageContainers = $all('.message-container')
    for(let i = 0; i < allMessageContainers.length; i++) {
        let messageContainer = allMessageContainers[i]
        let repliedMessageContainerInfo = messageContainer.$('.replied-message-info-container')
        $log('findAllReplyMessageContainersByOriginalMessageId/messageContainer: ' + messageContainer)
        if(!repliedMessageContainerInfo || !repliedMessageContainerInfo.hasAttribute('original-message-id')) {
            continue
        }
        if(messageContainer.$('.replied-message-info-container').getAttribute('original-message-id').toString() === searchingOriginalMessageId.toString()) {
            foundMessageContainers.push(messageContainer)
        }
    }
    $log('findAllReplyMessageContainersByOriginalMessageId/foundMessageContainers(search result): ' + foundMessageContainers)
    return foundMessageContainers
}
function addReplyContainerIfMessageContainerFound(originalMessageJsonWeReplied, currentMessageContainer,originalMessageStatus) {

    const foundOriginalMessageContainer = originalMessageStatus !== 'DELETED' && originalMessageJsonWeReplied !== null ? getMessageContainerById(originalMessageJsonWeReplied.id) : null
    if (foundOriginalMessageContainer !== null || originalMessageStatus === 'DELETED') {
        $log('addReplyContainerIfMessageContainerFound: \n\t(foundOriginalMessageContainer !== null || originalMessageStatus === \'DELETED\') === true')
        const replyingMessageIcon = $create('img', 'replying-message-icon')
        const originalMessageText = $create('p', 'message-of-reply-original-text')
        const repliedMessageInfoContainer = $create('div', 'replied-message-info-container')
        if(originalMessageStatus !== 'DELETED') {
            repliedMessageInfoContainer.setAttribute('original-message-id',originalMessageJsonWeReplied.id)
            originalMessageText.onclick = (event) => {
                event.stopPropagation()
                scrollToMessageById(Number(originalMessageJsonWeReplied.id))
            }
        }
        originalMessageText.style.backgroundColor = getRandomColor()
        replyingMessageIcon.src = '/static/img/reply.png'

        originalMessageText.textContent = originalMessageStatus !== 'DELETED' ? originalMessageJsonWeReplied.messageText : 'Сообщение удалено'
        repliedMessageInfoContainer.append(replyingMessageIcon, originalMessageText)
        currentMessageContainer.insertBefore(repliedMessageInfoContainer,currentMessageContainer.firstChild)
    }

}

function createReplyContainer(originalMessageWeRepliedJson,messageContainer,originalMessageStatus) {
    if(originalMessageWeRepliedJson !== null) {
        let originalMessageContainer = getMessageContainerById(originalMessageWeRepliedJson.id)
        if(originalMessageContainer === null) {
            getMessageHistory(false,originalMessageWeRepliedJson.id)
                .then(() =>  {
                    addReplyContainerIfMessageContainerFound(originalMessageWeRepliedJson, messageContainer,originalMessageStatus);
                })
        } else {
            addReplyContainerIfMessageContainerFound(originalMessageWeRepliedJson, messageContainer,originalMessageStatus);
        }
    } else if (originalMessageStatus === 'DELETED') {
        addReplyContainerIfMessageContainerFound(originalMessageWeRepliedJson, messageContainer,originalMessageStatus);
    }
}
function createDeclineChatDeletionBtn(messageContainer) {
    let declineDeletionBtn = createButtonWithText('Отклонить', 'decline-chat-deletion-btn')
    declineDeletionBtn.onclick = (event) => {
        event.stopPropagation()
        sendChatDeletionAnswer("false")

    }
    messageContainer.append(declineDeletionBtn)
}
function createConfirmChatDeletionBtn(messageContainer) {
    let confirmDeletionBtn = createButtonWithText('Подтвердить', 'confirm-chat-deletion-btn')
    confirmDeletionBtn.onclick = (event) => {
        event.stopPropagation()
        sendChatDeletionAnswer("true")
    }
    messageContainer.append(confirmDeletionBtn)
}
function shouldNotLoadMessageHistory(sender, selectedContactName) {
    return sender.username !== selectedContactName && sender.username !== myUsername;
}
function createMessageTimeElement(dateTime) {
    const messageTime = $create('p', 'message-time');
    messageTime.textContent = extractMessageTimeFromDateTime(dateTime);
    return messageTime;
}
function extractMessageTimeFromDateTime(dateTime) {
    const isDateModified = dateTime.includes('изм. ');
    //NOT CHANGED:(INDEX0-DATE),(INDEX1-TIME);//CHANGED(INDEX0-изм.),(INDEX1-DATE),(INDEX2-TIME)
    const timePartIndex = isDateModified ? 2 : 1;
    return dateTime.split(' ')[timePartIndex]
}
function extractMessageDateFromDateTime(dateTime) {
    const isDateModified = dateTime.includes('изм. ');
    //NOT CHANGED:(INDEX0-DATE),(INDEX1-TIME);//CHANGED(INDEX0-изм.),(INDEX1-DATE),(INDEX2-TIME)
    const datePartIndex = isDateModified ? 1 : 0;
    return dateTime.split(' ')[datePartIndex]
}
function createDateElementIfNeed(dateTime, singleMessageContainer) {
    const messageId = singleMessageContainer.getAttribute('message-id');
    const currentMessageDate = extractMessageDateFromDateTime(dateTime);
    console.log(currentMessageDate + '/' + lastMessageDate + ', ' + 'id = ' + messageId)

    if (lastMessageDate !== currentMessageDate || messageId === currentChatFirstMessageId.toString()) {
        if (lastMessageDate !== currentMessageDate) {
            console.error('adding-date-time: different dates');
        } else if (messageId === currentChatFirstMessageId.toString()) {
            console.error('adding-date-time: ids');
        }

        const messageDate = $create('p', 'message-date');
        const messageDateContainer = $create('div', 'message-date-container');
        messageDate.textContent = currentMessageDate.toString();
        messageDateContainer.append(messageDate);
        singleMessageContainer.insertAdjacentElement('beforebegin', messageDateContainer);
        lastMessageDate = currentMessageDate;
    }
}
function updateExistingMessage(messageContainer, message, dateTime) {
    let messageText = messageContainer.$('.message-text');
    messageText.textContent = message;
    messageContainer.$('.message-time').textContent = 'изм. ' + extractMessageTimeFromDateTime(dateTime);
}
function createMessageContainer(messageId) {
    const singleMessageContainer = $create('div', 'message-container');
    singleMessageContainer.setAttribute("message-id", messageId);
    addMessageSelectionListener(singleMessageContainer);
    return singleMessageContainer;
}
function createForwardedByContainer(forwardedByUsername, messageContainer) {
    const forwarderUsernameParagraph = $create('p', 'message-forwarder-username')
    forwarderUsernameParagraph.textContent = 'Переслано от @' + forwardedByUsername
    messageContainer.append(forwarderUsernameParagraph)
}
function clearAllContactsInContainer(parentContactsContainer = $('.contacts-container')) {
    parentContactsContainer.$all('.contact-container').forEach(childContactContainer => {
        childContactContainer.remove()
    })
}
function scrollToBottom(scrollableContainer) {
    scrollableContainer.scrollTop = scrollableContainer.scrollHeight;
}
function compressImage(img, maxWidth, maxHeight) {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');

    let width = img.width;
    let height = img.height;

    if (width > maxWidth || height > maxHeight) {
        const ratio = Math.min(maxWidth / width, maxHeight / height);
        width *= ratio;
        height *= ratio;
    }

    canvas.width = width;
    canvas.height = height;

    ctx.drawImage(img, 0, 0, width, height);

    return canvas.toDataURL('image/jpeg', 1);
}
function addPinnedImageToMessage(pinnedImageFilename, singleMessageContainer) {
    const pinnedImage = $create('img', 'pinned-image-to-message');

    pinnedImage.src = compressImage(pinnedImage, maxPinnedImageWidth, maxPinnedImageHeight);
    singleMessageContainer.append(pinnedImage);

    pinnedImage.src = '/pinned-images/' + pinnedImageFilename;
    pinnedImage.setAttribute('file-download-link','/pinned-images/' + pinnedImageFilename);
    scalePinnedImageOnClick(pinnedImage);
    return pinnedImage
}
function scrollToMessageById(messageId, shouldHighlight = true, scrollBehavior = 'smooth') {
    if(messageId < 0) {
        return
    }
    const messagesContainer = $('.chat-space')
    const targetContainer = getMessageContainerById(messageId)
    const targetRect = targetContainer.getBoundingClientRect()
    const messagesContainerRect = messagesContainer.getBoundingClientRect()
    const scrollTop = messagesContainer.scrollTop
    const top = targetRect.top - messagesContainerRect.top + scrollTop;
    messagesContainer.scrollTo({
        top: top,
        behavior: scrollBehavior
    });
    if(shouldHighlight) {
        highlightContainer(targetContainer)
    }

}
function insertMessagesJsonAtBeginOfMessagesContainer(allMessagesJson) {
    const reversedMessageHistory = allMessagesJson.reverse();
    reversedMessageHistory.forEach(messageJson => {
        showMessage(messageJson,null,'begin')
    })
}
function insertMessagesJsonAtEndOfMessagesContainer(allMessagesJson) {
    allMessagesJson.forEach(messageJson => {
        showMessage(messageJson, null, 'end')
    })
}
//-------------foundUsers-----------
function showFoundUser(foundUserJson) {
    $('.found-user-username').textContent = '@' + foundUserJson['username']
    setAvatarPathIfExistsOrDefaultAvatar('/avatars/' + foundUserJson['avatarName'],$('.found-user-avatar'), foundUserJson['username'])
    $('.found-user-info').setAttribute('username',foundUserJson['username'])
    const descriptionElement = $('.found-user-description')
    if(descriptionElement) {
        descriptionElement.textContent = foundUserJson['description']
    } else {
        descriptionElement.remove()
    }
    showElement($('.found-users-overlay'))
}