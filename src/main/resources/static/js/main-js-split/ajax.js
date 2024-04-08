function searchContactByUsername() {
    let username = $(".search-query-input").value;
    fetch('/users/' + username)
        .then(response => response.json())
        .then(result => {
            showFoundUser(result['found_user'])
        })
}
function updateContacts(containerToShow = $('.contacts-container'), shouldUpdateMessageHistory = true) {
    clearAllContactsInContainer(containerToShow)

    fetch("/get-my-contacts")
        .then(response => response.json())
        .then(result => {
            let allContactsJson = result.my_user_contacts;
            allContactsJson.forEach(contactJson => {
                showContact(contactJson, containerToShow, shouldUpdateMessageHistory)
            });
            if(allContactsJson.length === 0) {
                hideElement($('.chat-menu'))
                showElement($('.contact-not-selected-alert'))
            }
        })

}
function lockMessageHistoryUpdatesByScroll() {
    shouldUpdateMessageHistoryOnScroll = false
}
function unlockMessageHistoryUpdatesByScroll() {
    shouldUpdateMessageHistoryOnScroll = true
}
function getMessageHistory(shouldResetPageNumber = false,messageIdBeforeWhichLoad = null) {
    $error('запущен процесс получения истории сообщений, ' + ((shouldResetPageNumber) ? 'с очисткой' : 'без очистки') + ', ' + ((!shouldResetPageNumber) ? (' с пагинацией в размере 30 элементов') : (' без пагинации')))

    if (shouldResetPageNumber) {
        $(".messages-container").innerHTML = ''
        lockMessageHistoryUpdatesByScroll()
    }

    return fetch("/get-message-history/" + selectedContactName +  "?" + new URLSearchParams({
        shouldResetPageNumber : shouldResetPageNumber,
        messageIdBeforeWhichLoad: messageIdBeforeWhichLoad
    }))
        .then(response => response.json())
        .then(result => {
            const allMessagesJson = result['message_history'];
            if(!shouldResetPageNumber) {
                insertMessagesJsonAtBeginOfMessagesContainer(allMessagesJson);
                const allMessageContainers = $all('.message-container')
                scrollToMessageById(allMessageContainers[27].getAttribute('message-id'),false,'auto')
            } else {
                $error('message history and pagination page cleaned!')
                $error($all('.message-container').length)
                insertMessagesJsonAtEndOfMessagesContainer(allMessagesJson);
            }
        })
        .then(() => {
            if(shouldResetPageNumber || messageIdBeforeWhichLoad !== null) {
                scrollToBottom($('.chat-space'))
            }
            unlockMessageHistoryUpdatesByScroll()
        })
}
function deleteAllMessages(messageIdList) {
    let messageIdsData = {"messageIdList": messageIdList}
    fetch("/delete-messages-list", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(messageIdsData)
    })
        .then()

}
function sendMessage(messageIdToReply = null, withImage = false) {
    if (selectedContactName !== null) {
        const formData = new FormData();
        let messageText
        if(withImage) {
            messageText =  $('.caption-input').value
            formData.append('image', $('.image-input').files[0]);
        } else {
            messageText = $('.message-text-area').value
        }
        formData.append('messageText',messageText);
        formData.append('receiverName', selectedContactName);
        formData.append("messageIdToReply",messageIdToReply)
        if(messageText === '' && !withImage) {
            return;
        }
        fetch("/send-private-message", {
            method: 'POST',
            body: formData
        }).then(() => {
            console.log('Text message sent successfully');
        })
            .catch(error => {
                console.error('Error:', error);
            });
    }
}
function editMessage() {
    let newMessageText = $(".message-text-area").value
    fetch('/edit-private-message/' + selectedContactName,
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                "messageText": newMessageText,
                "messageId": messageIdToEdit
            })
        }
    ).then(response => console.log('Response of edit:' + response.json()))

}
function initializeMyUser() {
    fetch('/get-my-user')
        .then(response => response.json())
        .then(myUser => {
            myUserJson = myUser['my_user'];
            myUsername = myUserJson['username']
            myAvatarFilename = myUserJson['avatarName']
            setAvatarPathOrDefault('/avatars/' + myAvatarFilename, $('.avatar'))
            console.log('myUserDescription: ' + myUserJson['description'])
            $('.description-textarea').value = myUserJson['description']
        })
        .catch(e => console.error(e))
}
function sendChatDeletionAnswer(answer) {
    fetch('/chat-deletion-voting/' + selectedContactName + "/" + answer).then()
}
function sendNewAvatar() {
    let formData = new FormData()
    formData.append('profile-avatar', $('.update-profile-avatar-input').files[0]);
    console.log(formData.get('profile-avatar'));

    fetch('/update-profile-avatar', {
        method: 'POST',
        body: formData
    }).then(response => response.json())
        .then(r => {
            myAvatarFilename = '/avatars/' + r['avatar_path']
            setAvatarPathOrDefault(myAvatarFilename, $('.my-profile-avatar'))
        })
        .catch(e => console.log(e));
}
function forwardMessagesToContacts(contactNameList, messageIdList) {
    let formData = new FormData()
    formData.append('contactNameList', contactNameList)
    formData.append('messageIdList', messageIdList)
    fetch('/forward-messages-to-contacts', {
        method: 'POST',
        body: formData
    }).catch(e => console.log(e));
}
function updateDescription() {
    fetch('/update-description/' + $('.description-textarea').value)
        .catch(e => console.log(e))
}
function updateSelectedContactNameOnServer() {
    fetch('/update-selected-contact-name/' + selectedContactName)
        .catch(e => console.error(e))
}
function getLastMessage(contactName) {
    return fetch('/get-last-message/' + contactName)
        .then(response => response.json())
        .then(result => {
            return result['last_message_text'];
        })
        .catch(error => {
            console.error(error);
            return ''; // Возвращаем пустую строку в случае ошибки
        });
}
function getUnreadMessagesAmount(username) {
    return fetch('/get-unread-messages/' + username)
        .then(response => response.json())
        .then(result => {
            return result['unread_messages_amount'];
        })
        .catch(error => {
            console.error('Ошибка получения количества непрочитанных сообщений:', error);
            return 0; // Возвращаем 0 в случае ошибки
        });
}
function clearUnreadMessage(contactName) {
    fetch('/clear-unread-messages-amount/' + contactName)
        .catch(e => console.log(e))
}
function sendChatDeletionRequest(isLocallyDeletion) {
    fetch(
        isLocallyDeletion
            ?
            ('/delete-chat-locally/' + selectedContactName)
            :
            ('/delete-chat-request/' + selectedContactName)
    ).then()
}
function addUserToMyContactList() {
    let username = $('.found-user-info').getAttribute('username')
    fetch("/add-user-to-my-contacts/" + username)
        .then(response => response.json())
        .then(() => {
            updateContacts()
            hideElement($('.found-users-overlay'))
        })
}
function connectToWebSocket() {

    let socket = new SockJS('/messages');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/topic/private-messages', function (message) {
            let messageJson = JSON.parse(message.body)
            if(messageJson['operations_batch'] !== null && messageJson['operations_batch'] !== undefined) {
                sound.play(1,false)
                const batch = messageJson['operations_batch']
                batch.forEach(message => {
                    showMessage(message)
                })
                return;
            }
            if(messageJson.sender) {
                if(messageJson.sender.username !== myUsername && messageJson.target === 'CREATE') {
                    sound.play(1,false)
                }
            } else {
                hideElement($('.chat-menu'))
                showElement($('.contact-not-selected-alert'))
                return
            }

            showMessage(messageJson);
        });
        stompClient.subscribe('/user/topic/contacts', () => {
            $log('UPDATE_CONTACTS_REQUEST!')
            updateContacts()
        })
        stompClient.subscribe('/user/topic/chat-clearing', () => {
            $log('CHAT_CLEANING_REQUEST!')
            $all('.message-container').forEach(container => container.remove())
            hideElement($('.chat-menu'))
            showElement($('.contact-not-selected-alert'))
        })
    });
}