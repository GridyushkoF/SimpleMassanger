//Глобальные переменные
let selectedContactName = null
let selectionType = 'mono'
let stompClient = null
let messageOperationType = 'send'
let messageIdToEdit = null
let myUsername = null
let myAvatarFilename = null
let myUserJson = null
let selectedMessageId = null
let lastMessageIndex = 0

// Упрощение работы с документом HTML DOM
const $ = (selector) => {return document.querySelector(selector)};
const $all = (selector) => {
    let elements = document.querySelectorAll(selector)
    return [... elements]
}

function createEl(elementType, className) {
    const element = document.createElement(elementType);
    element.className = className;
    return element;
}
function createButtonWithText(text, className) {
    const button = createEl("button", className);
    button.textContent = text;
    return button;
}
function openLink(link) {
    window.location.href = link
}

//Прослушиватели событий
$('.delete-chat-btn').onclick = () => {
    const selected_option = $('input[type="radio"]:checked')
    let isLocallyDeletion = selected_option.id === 'delete-locally-option'
    fetch(
        isLocallyDeletion
            ?
            ('/delete-chat-locally/' + selectedContactName)
            :
            ('/delete-chat-request/' + selectedContactName)
    ).then()
}
$('.show-profile-popup-btn').onclick = () => {
    showPopup($('.profile-overlay'))
}
$(".send-message-btn").onclick = () => {
    if(messageOperationType === 'send') {
        sendMessage()
    } else {
        editMessage()
        messageOperationType = 'send'
        $(".send-message-btn").textContent = 'Отправить'
    }
    $('.message-text-input').value = ''
}
$('.update-profile-avatar-btn').onclick = () => {
    sendNewAvatar()
}
$('.pin-image-btn').onclick = () => {
    showPopup($('.pin-image-overlay'))
}
$('.image-input').onchange = (event) => {
        let target = event.target;
        if (!FileReader) {
            alert('FileReader не поддерживается — облом');
            return;
        }
        if (!target.files.length) {
            alert('Ничего не загружено');
            return;
        }
        let  fileReader = new FileReader();
        fileReader.onload = () => {
            $('.pinned-image').src = fileReader.result;
        }
        fileReader.readAsDataURL(target.files[0]);
}
$('.send-message-with-image-btn').onclick = () => {
    sendMessageWithImage()
}
$('.messages-container').addEventListener('contextmenu', (event) => {
    selectedMessageId = findMessageContainerByCursor(event).getAttribute('message-id')
})
$('.delete-message-btn').onclick = () => {
    deleteMessage(selectedMessageId)
    hidePopup($('.message-options-unique-popup'))
}
$('.edit-message-btn').onclick = () => {
    $all('.message-container').forEach((container) => {
        let messageId = container.getAttribute('message-id');
        if(messageId.toString() === selectedMessageId.toString()) {
            const messageText = container.querySelector('.message-text').textContent
            messageOperationType = 'edit';
            $(".send-message-btn").textContent = 'Изменить';
            messageIdToEdit = messageId
            $('.message-text-input').value = messageText
        }
    })
    hidePopup($('.message-options-unique-popup'))
}
function initializeAvatarOrDefault(avatarPath,avatarElement) {
    if(avatarPath !== null && avatarPath !== undefined && !avatarPath.includes('null')) {
        avatarElement.src = avatarPath
    } else {
        avatarElement.src = '/static/img/noImage.png'
    }
}
//Методы конвертации JSON с сервера в удобочитаемый вид
function showDeletionRequestUI(messageContainer) {
    let confirm_deletion_btn = createButtonWithText('Подтвердить','confirm-chat-deletion-btn')
    confirm_deletion_btn.onclick = () => {
        sendChatDeletionAnswer("true")
    }
    messageContainer.append(confirm_deletion_btn)

    let decline_deletion_btn = createButtonWithText('Отклонить', 'decline-chat-deletion-btn')
    decline_deletion_btn.onclick = () => {
        sendChatDeletionAnswer("false")
    }
    messageContainer.append(decline_deletion_btn)
}
function showMessage(targetedMessageJson) {
    const { messageText: message,
            targetId: messageId,
            target: messageTarget,
            sender : sender,
            dateTime : dateTime,
            pinnedImageFilename : pinnedImageFilename} = targetedMessageJson;
    //Отправитель - не выделенный пользователь и отправитель - не я
    console.log(myUsername)
    if (sender.username !== selectedContactName && sender.username !== myUsername) {
        return;
    }
    console.log(JSON.stringify(targetedMessageJson))
    console.log('pinnedImageFilename: ' + pinnedImageFilename)
    const isDateUpdated = dateTime.includes('изм. ');
    const datePartIndex = isDateUpdated ? 1 : 0
    const datePart = dateTime.split(' ')[datePartIndex]
    const timePart = dateTime.split(' ')[datePartIndex + 1]
    const messageTime = createEl('p','message-time')
    messageTime.textContent = timePart
    if (messageTarget !== 'CREATE' && messageTarget !== 'CHAT_DELETE') {
        console.log('messageTarget !== \'CREATE\' && messageTarget !== \'CHAT_DELETE\'')
        const allSingleMessageContainers = $all('.message-container')
        for (let i = 0; i < allSingleMessageContainers.length; i++) {
            let messageContainer = allSingleMessageContainers[i]
            if(messageContainer.getAttribute('message-id').toString() !== messageId.toString()) {
                console.log('message-container-id: ' + messageContainer.getAttribute('message-id'))
                console.log('current message id: ' + messageId.toString())
                continue;
            }
            if (messageContainer) {
                if (messageTarget === 'DELETE') {
                    $('.messages-container').removeChild(messageContainer);
                } else if (messageTarget === 'UPDATE') {
                    let messageText = messageContainer.querySelector('.message-text');
                    messageText.textContent = message;
                    messageContainer.querySelector('.message-time').textContent = messageTime.textContent = 'изм. ' + timePart;
                }
                console.log('returning void')

            }
        }
        return;
    }
    const allMessagesContainer = $(".messages-container");

    const singleMessageContainer = createEl('div', 'message-container');
    singleMessageContainer.setAttribute("message-id", messageId);
    lastMessageIndex += 1
    singleMessageContainer.dataset.index = lastMessageIndex

    const messageText = createEl("p", "message-text");
    messageText.textContent = message;

    if(pinnedImageFilename !== null) {
        const pinnedImage = createEl('img','pinned-image-to-message')
        pinnedImage.src = '/pinned-images/' + pinnedImageFilename
        singleMessageContainer.append(pinnedImage)
    }
    singleMessageContainer.append(messageText,messageTime);
    if(messageTarget === 'CHAT_DELETE') {
        showDeletionRequestUI(singleMessageContainer)
    }
    allMessagesContainer.append(singleMessageContainer);
}
function showContact(contactJson) {
    lastMessageIndex = 0
    console.log(contactJson)
    const contactName = contactJson['username']
    const contactAvatarName = contactJson['avatarName']

    const allContactsContainer = $('.contacts-container')
    let singleContactContainer = createEl("div","contact-container")
    let deleteContactBtn = createButtonWithText('del','delete-contact-btn')
    let contactUsernameText = createEl("p","contact-username")
    let contactAvatarImg = createEl('img','contact-avatar')

    singleContactContainer.setAttribute("contact-name",contactName)
    contactUsernameText.textContent = contactName;
    
    singleContactContainer.append(
        contactAvatarImg,
        contactUsernameText,
        deleteContactBtn)

    allContactsContainer.append(singleContactContainer)
    initializeAvatarOrDefault('/avatars/' + contactAvatarName,contactAvatarImg)
    singleContactContainer.onclick =  () => {
        selectedContactName = singleContactContainer.getAttribute("contact-name")
        if(selectionType === 'mono') {
            $all(".contact-container").forEach(container => {
                container.classList.remove('selected')
            })
        }
        singleContactContainer.classList.add("selected")
        getMessageHistory()
    }
    deleteContactBtn.onclick = () => {
        showPopup($('.delete-contact-overlay'))
    }

}
function clearAllContactsContainer() {
    const allContactsContainer = $('.contacts-container')
    $all(".contact-container").forEach(singleContactContainer => {
        allContactsContainer.removeChild(singleContactContainer)
    })
}

//Методы манипуляции вебсокета
function connectToWebSocket() {
    let socket = new SockJS('/messages');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/topic/private-messages', function (message) {
            let messageJson = JSON.parse(message.body)
            console.log("parsed message: " + messageJson)
            console.log("parsed message text: " + messageJson.messageText)
            console.log("parsed message target: " + messageJson.target)
            console.log("parsed message id: " + messageJson.targetId)
            showMessage(messageJson);
        });
        stompClient.subscribe('/user/topic/contacts', () => {
            updateContacts()
        })
    });
}

//Отправка запросов на сервер:
function searchContactByUsername() {
    const searchQueryInput = $(".search-query-input")
    let username = searchQueryInput.value;
    openLink('/users/' + username)
}
function updateContacts() {
    clearAllContactsContainer()
    fetch("/get-my-contacts")
        .then(response => response.json())
        .then(result => {
            let allContactsJson = result.my_user_contacts;
            allContactsJson.forEach(contactJson => {
                showContact(contactJson)
            });
        })
}
function getMessageHistory() {
    $(".messages-container").innerHTML = ''
    fetch("/get-message-history/" + selectedContactName)
        .then(response => response.json())
        .then(result => {
            let allMessagesJson = result.message_history;
            allMessagesJson.forEach(messageJson => {
                showMessage(messageJson)
            })
        })
}
function deleteMessage(message_id) {
    console.log('Id сообщения на удаление: ' + message_id)
    let message_dto = {"messageId" : message_id}
    fetch("/delete-private-message/" + selectedContactName, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(message_dto)
        })
        .then()
}
function sendMessage() {
    let messageText = $(".message-text-input").value
    if(selectedContactName !== null && messageOperationType === 'send') {
        fetch("/send-private-message/" + selectedContactName
            , {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                "messageText" : messageText
            })
        })
            .then(response => {
                console.log(response)
            })
            .catch(error => {
                console.error(error)
            });
    } else {
        console.log('Невозможно отправить сообщение пользователю, который не выделен')
    }
}
function sendMessageWithImage() {
    const formData = new FormData();
    formData.append('caption', $('.caption-input').value);
    formData.append('image', $('.image-input').files[0]);
    formData.append('receiverName',selectedContactName)

    // Отправка POST-запроса на сервер с использованием fetch
    fetch('/send-message-with-image', {
        method: 'POST',
        body: formData
    })
        .then(response => {
            if (response.ok) {
                console.log('Запрос успешно выполнен');
            } else {
                console.error('Произошла ошибка при выполнении запроса');
            }
        })
        .catch(error => {
            console.error('Произошла ошибка:', error);
        });
}
function editMessage() {
    let newMessageText = $(".message-text-input").value
    fetch('/edit-private-message/' + selectedContactName,
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                "messageText" : newMessageText,
                "messageId" : messageIdToEdit
            })
        }
        ).then(response => console.log('Response of edit:' + response.json()))

}
function initializeMyUser() {
    fetch('/get-my-user')
        .then(response => response.json())
        .then(myUser => {
            console.log('my_user_prop: ' + JSON.stringify(myUser))
            myUserJson = myUser['my_user'];
            myUsername = myUserJson['username']
            myAvatarFilename = myUserJson['avatarName']
            initializeAvatarOrDefault('/avatars/' + myAvatarFilename,$('.my-profile-avatar'))
        })
        .catch(e => console.error(e))
}
function sendChatDeletionAnswer (answer) {
    fetch('/chat-deletion-voting/' + selectedContactName + "/" + answer).then()
}
function sendNewAvatar() {
    let formData = new FormData();
    formData.append('profile-avatar', $('.update-profile-avatar-input').files[0]);
    console.log(formData.get('profile-avatar'));

    fetch('/update-profile-avatar', {
        method: 'POST',
        body: formData
    }).then(response => response.json())
        .then(r => {
            myAvatarFilename = '/avatars/' + r['avatar_path']
            initializeAvatarOrDefault(myAvatarFilename,$('.my-profile-avatar'))
        })
        .catch(e => console.log(e));
}
//Инициализация страницы
document.addEventListener("DOMContentLoaded",() => {
    updateContacts();
    connectToWebSocket();
    initializeMyUser()
})
//popup works
function showPopup(popup) {
    popup.classList.remove('disabled')
    popup.classList.add('enabled')
}
function hidePopup(popup) {
    popup.classList.remove('enabled')
    popup.classList.add('disabled')
}
$all('.popup-overlay').forEach(popupOverlay => {
    popupOverlay.onclick = () => {
        hidePopup(popupOverlay);
    };
    const popupMenu = popupOverlay.querySelector('.popup-menu');
    popupMenu.onclick = (event) => {
        event.stopPropagation();
    };
    const popupExitBtn = popupOverlay.querySelector('.exit-popup-btn')
    popupExitBtn.onclick = () => {
        hidePopup(popupOverlay)
    }
});
//unique popup works
function findMessageContainerByCursor(event) {
    event.preventDefault(); // Предотвращаем показ стандартного контекстного меню
    const clickX = event.clientX + window.scrollX - 70;
    const clickY = event.clientY + window.scrollY - 70;

    const popupMenu = $('.message-options-unique-popup');
    showPopup(popupMenu);

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
    console.log(clickedMessageContainer);
    console.log(clickedMessageContainer.getAttribute('message-id'));
    return clickedMessageContainer;
}
//TEST
function init_random_fake_contacts() {
    showContact('1')
    showContact('12')
    showContact('13')
    showContact('LETTERS')
}