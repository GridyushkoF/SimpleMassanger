function searchContactByUsername() {
    let username = $(".search-query-input").value;
    fetch('/users/' + username)
        .then(response => response.json())
        .then(result => {
            showFoundUser(result['found_user'])
        })
}
// function sendHeartbeat() {
//     fetch('/mark-online-status', {
//         method: 'GET',
//         headers: {
//             'Content-Type': 'application/json'
//         }
//     })
//     .catch(error => {
//         console.error('Error sending heartbeat:', error);
//     });
// }
//
// setInterval(sendHeartbeat, 5000);
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
function setCurrentFirstMessageId(shouldResetPageNumber) {
    if (shouldResetPageNumber) {
        getFirstMessageInCurrentChat()
            .then(result => {
                const id = result['first-message-id']
                if (id) {
                    currentChatFirstMessageId = id;
                }
            })
    }
}
function getMessageHistory(shouldResetPageNumber = false,messageIdBeforeWhichLoad = null) {
    setCurrentFirstMessageId(shouldResetPageNumber);
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
                console.error("----------loading pages by pagination----------")
                insertMessagesJsonAtBeginOfMessagesContainer(allMessagesJson);

                const allMessageContainers = $all('.message-container')
                if(allMessagesJson.length !== 0)
                scrollToMessageById(allMessageContainers[27].getAttribute('message-id'),false,'auto')

            } else {
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
function getFirstMessageInCurrentChat() {
    return fetch('/get-first-message/' + selectedContactName)
        .then(response => response.json())

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
            console.log('your username: ' + myUsername)
            setAvatarPathIfExistsOrDefaultAvatar('/avatars/' + myAvatarFilename, $('.my-avatar'))
            $('.update-username-input').value = myUsername
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
        .then(json => {
            myAvatarFilename = '/avatars/' + json['avatar_path']
            setAvatarPathIfExistsOrDefaultAvatar(myAvatarFilename, $('.my-avatar'))
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
function monitorUserActivity() {
    const socket = new WebSocket("ws://" + window.location.host + "/user-activity");
    socket.onopen = () => {
        console.log('Connection opened')
    }
    socket.onclose = (event) => {
        console.log("Connection closed. Code: ", event.code, " Reason: ", event.reason);
    };
    socket.onerror = (event) => {
        console.error("Error occurred: ", event);
    };
    socket.onmessage = () => {
        console.log('new message')
    }
}
function connectToWebSocket() {
    const socketJs = new SockJS('/messages');
    stompClient = Stomp.over(socketJs);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/topic/private-messages', function (message) {
            let messageJson = JSON.parse(message.body)
            console.log("Получено новое сообщение, цель - " + messageJson['target'])
            if(messageJson['operations_batch']) {
                sound.play(1,false)
                const batch = messageJson['operations_batch']
                batch.forEach(message => {
                    showMessage(message)
                })
                return;
            }
            if(messageJson.sender && (messageJson.sender.username !== myUsername && messageJson.target === 'CREATE')) {
                sound.play(1,false)
            }
            showMessage(messageJson);
        });
        stompClient.subscribe('/user/topic/contacts', () => {
            updateContacts()
        })
        stompClient.subscribe('/user/topic/chat-clearing', () => {
            $all('.message-container').forEach(container => container.remove())
            hideElement($('.chat-menu'))
            showElement($('.contact-not-selected-alert'))
        })
    });
}
function updateUsernameAndContacts() {
    let newUsername = $('.update-username-input').value
    fetch('/update-my-username/' + newUsername)
        .then(response => response.json())
        .then(result => {
            alert(result['is_successful_updated'] ? "Успешно обновлено" : "Такое имя пользователя уже присутствует, выберите другое")
            if(result['is_successful_updated']) {
                myUsername = $('.update-username-input').value
                updateContacts()
                if(myAvatarFilename === null) {
                    setAvatarPathIfExistsOrDefaultAvatar('/avatars/null',$('.my-avatar'))
                }

                connectToWebSocket()
            }
        }).then(() => {

    })
}