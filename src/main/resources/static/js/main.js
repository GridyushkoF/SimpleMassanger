//Глобальные переменные
let selected_contact_name = null
let selection_type = 'mono'
let stompClient = null
let message_operation = 'send'
let message_id_to_edit = null
let my_username = null;


// Упрощение работы с документом HTML DOM
function doc_create(elementType, className) {
    const element = document.createElement(elementType);
    element.className = className;
    return element;
}
function doc_select(className) {
    return document.querySelector(className)
}
function doc_select_all(className) {
    return document.querySelectorAll(className)
}
function create_button_with_text(text, className) {
    const button = doc_create("button", className);
    button.textContent = text;
    return button;
}

//Прослушиватели событий

function add_contact_click_listener(contact_container) {
    contact_container.addEventListener("click", () => {
        selected_contact_name = contact_container.getAttribute("contact-name")
        if(selection_type === 'mono') {
            doc_select_all(".contact-container").forEach(container => {
                console.log("classlist before:" + container.classList)
                container.classList.remove('selected')
                console.log("classlist after: " + container.classList)
            })
        }
        contact_container.classList.add("selected")
        get_message_history()
        console.log("selected contact name: " + selected_contact_name)
    })
}
function add_delete_message_btn_click_listener(delete_message_btn,message_id) {
    delete_message_btn.addEventListener('click',() => {
        delete_message(message_id)
    })
}
function add_edit_message_btn_click_listener(edit_btn, message_id, message_text) {
    edit_btn.addEventListener('click', () => {
        message_operation = 'edit';
        doc_select(".send-message-btn").textContent = 'Изменить';
        message_id_to_edit = message_id
        doc_select('.message-text-input').value = message_text
    });
}
function add_confirm_deletion_btn_click_listener(confirm_deletion_btn) {
    confirm_deletion_btn.onclick = () => {

    }
}
//Методы конвертации JSON с сервера в удобочитаемый вид
function show_message(message_dto_with_goal) {
    const
        { messageText: message,
            goalId: message_id,
            goal: message_goal,
            senderName : sender_name,
            dateTime : date_time } = message_dto_with_goal;
    //Отправитель - не выделенный пользователь и отправитель - не я
    if (sender_name !== selected_contact_name && sender_name !== my_username) {
        return;
    }

    const all_message_containers = doc_select_all('.message-container');

    const is_date_updated = date_time.includes('изм. ');
    const date_part_index = is_date_updated ? 1 : 0
    const date_part = date_time.split(' ')[date_part_index]
    const time_part = date_time.split(' ')[date_part_index + 1]
    const message_time = doc_create('p','message-time')
    message_time.textContent = time_part

    if (message_goal !== 'CREATE' && message_goal !== 'CHAT_DELETE') {
        for (let i = 0; i < all_message_containers.length; i++) {
            const message_container = all_message_containers[i];
            if (message_container.getAttribute('message-id') == message_id) {
                if (message_goal === 'DELETE') {
                    doc_select('.messages-container').removeChild(message_container);
                } else if (message_goal === 'UPDATE') {
                    let message_text = message_container.querySelector('.message-text');
                    message_text.textContent = message;
                    message_container.querySelector('.message-time').textContent = message_time.textContent = 'изм. ' + time_part
                }
                return;
            }
        }
    }

    const messages_container = doc_select(".messages-container");
    const message_container = doc_create('div', 'message-container');
    message_container.setAttribute("message-id", message_id);

    const delete_message_btn = create_button_with_text('Удалить', 'delete-message-btn');
    add_delete_message_btn_click_listener(delete_message_btn, message_id);

    const message_text = doc_create("p", "message-text");
    message_text.textContent = message;

    const edit_message_btn = create_button_with_text('Изменить', 'edit-message-btn');
    add_edit_message_btn_click_listener(edit_message_btn, message_id, message);

    if(message_goal === 'CHAT_DELETE') {
        let confirm_deletion_btn = doc_create('button','confirm-chat-deletion-btn')
        confirm_deletion_btn.textContent = 'Я подтверждаю!'
        confirm_deletion_btn
    }
    message_container.append(message_text);
    message_container.append(delete_message_btn);
    message_container.append(edit_message_btn);
    message_container.append(message_time)
    messages_container.append(message_container);
}
function show_contact(contact_name) {
    const contacts_container = doc_select('.contacts-container')
    let contact_containers = doc_select_all(".contact-container")
    contact_containers.forEach(container => {
        container.remove()
    })
    let new_contact_container = doc_create("div","contact-container")
    let contact_username = doc_create("p","contact-username")
    new_contact_container.setAttribute("contact-name",contact_name)
    contact_username.textContent = contact_name;
    new_contact_container.append(contact_username)
    contacts_container.append(new_contact_container)
    add_contact_click_listener(new_contact_container)
}

//Методы манипуляции вебсокета
function connect_to_websocket() {
    let socket = new SockJS('/messages');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/topic/private-messages', function (message) {
            let parsed_message = JSON.parse(message.body)
            console.log("parsed message: " + parsed_message)
            console.log("parsed message text: " + parsed_message.messageText)
            console.log("parsed message id: " + parsed_message.goalId)
            show_message(parsed_message);
        });
        stompClient.subscribe('/user/topic/contacts', () => {
            get_my_contacts()
        })
    });
}

//Отправка запросов на сервер:
function search_contacts_by_username() {
    const search_query_input = doc_select(".search-query-input")
    let username = search_query_input.value;
    window.location.href = '/users/' + username
}
function get_my_contacts() {
    fetch("/get-my-contacts")
        .then(response => response.json())
        .then(result => {
            let myUserContacts = result.my_user_contacts;
            myUserContacts.forEach(username => {
                console.log("username: " + username)
                show_contact(username)
            });
        })
}
function get_message_history() {
    doc_select(".messages-container").innerHTML = ''
    fetch("/get-message-history/" + selected_contact_name)
        .then(response => response.json())
        .then(result => {
            let messageHistory = result.message_history;
            messageHistory.forEach(parsed_message => {
                show_message(parsed_message)
            })

        })
}
function delete_message (message_id) {
    console.log('Id сообщения на удаление: ' + message_id)
    let message_dto = {"messageId" : message_id}
    fetch("/delete-private-message/" + selected_contact_name, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(message_dto)
        })
        .then()
}
function send_private_message() {
    let message_text = doc_select(".message-text-input").value
    if(selected_contact_name !== null && message_operation === 'send') {
        fetch("/send-private-message/" + selected_contact_name
            , {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                "messageText" : message_text
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
function edit_message() {
    let new_message_text = doc_select(".message-text-input").value
    fetch('/edit-private-message/' + selected_contact_name,
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                "messageText" : new_message_text,
                "messageId" : message_id_to_edit
            })
        }
        ).then(response => console.log('Response of edit:' + response.json()))

}
function initialize_my_username() {
    fetch("/get-my-username")
        .then(response => response.json())
        .then(result => {
            my_username = result.my_username;
            console.log("Мое имя пользователя: " + my_username);
        })
        .catch(error => {
            console.error(error);
        });
}
function send_chat_deletion_answer () {
    fetch('')
}
//Инициализация страницы
document.addEventListener("DOMContentLoaded",() => {
    get_my_contacts();
    connect_to_websocket();
    initialize_my_username()
    let send_message_btn = doc_select(".send-message-btn")
    send_message_btn.addEventListener('click', () => {
        if(message_operation === 'send') {
            send_private_message()
            doc_select('.message-text-input').value = ''
        } else {
            edit_message()
            message_operation = 'send'
            doc_select(".send-message-btn").textContent = 'Отправить'
        }
    })
})