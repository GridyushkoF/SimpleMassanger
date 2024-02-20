let isMouseDown = false;
let startMessageId = null;
let endMessageId = null;
const leftMouseButtonIndex = 0

document.addEventListener('mousedown', function(event) {
    if (event.button === leftMouseButtonIndex) {
        isMouseDown = true;
        startMessageId = event.target.index;

        if (!event.shiftKey) {
            clearSelection();
        }

        event.target.classList.add('selected');
        endMessageId = event.target.dataset.index;

        disableTextSelection();
    }
});

document.addEventListener('mouseup', function(event) {
    if (event.button === 0) { // Левая кнопка мыши
        isMouseDown = false;
        startMessageId = null;

        enableTextSelection();
    }
});

document.addEventListener('mouseover', function(event) {
    if (isMouseDown && event.target.classList.contains('message')) {
        const currentMessageId = event.target.dataset.id;
        const messages = document.querySelectorAll('.message-container');

        if (event.shiftKey && endMessageId !== null) {
            const startIndex = Array.from(messages).findIndex(message => message.dataset.id === startMessageId);
            const endIndex = Array.from(messages).findIndex(message => message.dataset.id === currentMessageId);
            const [minIndex, maxIndex] = [startIndex, endIndex].sort((a, b) => a - b);

            for (let i = minIndex; i <= maxIndex; i++) {
                messages[i].classList.add('selected');
            }
        } else {
            clearSelection();
            event.target.classList.add('selected');
        }

        endMessageId = currentMessageId;

        disableTextSelection();
    }
});

document.addEventListener('mouseout', function(event) {
    if (isMouseDown && event.target.classList.contains('message')) {
        enableTextSelection();
    }
});

function clearSelection() {
    const selectedMessages = document.querySelectorAll('.message.selected');
    selectedMessages.forEach(message => {
        message.classList.remove('selected');
    });
}

function enableTextSelection() {
    document.body.style.userSelect = 'text';
}

function disableTextSelection() {
    document.body.style.userSelect = 'none';
}