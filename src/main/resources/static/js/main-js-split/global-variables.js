//global variables:
let selectedContactName = null
let selectionType = 'mono'
let stompClient = null
let messageOperationType = 'send'
let messageIdToEdit = null
let myUsername = null
let myAvatarFilename = null
let myUserJson = null
let selectedMessageIdSet = new Set()
let shouldUpdateMessageHistoryOnScroll = true
let writingTimeOutBeforeUpdating = null
const maxPinnedImageWidth = 600//px
const maxPinnedImageHeight = 200//px
const maxLastMessageLength = 32//symbols
let lastMessageDate = getCurrentDate()
let currentChatFirstMessageId = null
function getCurrentDate() {
    let currentDate = new Date()
    return `${currentDate.getDate().toString().padStart(2, '0')}.${(currentDate.getMonth() + 1).toString().padStart(2, '0')}.${currentDate.getFullYear()}`;
}
const sound = new Howl({
    src: ['/static/sounds/NewMessageSound.mp3'],
    volume: 0.5,
    loop: false,
});