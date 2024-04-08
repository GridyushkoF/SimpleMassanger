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
const maxPinnedImageWidth = 600//px
const maxPinnedImageHeight = 200//px
const maxLastMessageLength = 32//symbols
const sound = new Howl({
    src: ['/static/sounds/NewMessageSound.mp3'],
    volume: 0.5,
    loop: false,
});
const paginationLoadingMessagesAmount = 30