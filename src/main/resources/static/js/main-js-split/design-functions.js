function setAvatarPathOrDefault(avatarPath, avatarElement) {
    if (avatarPath !== null && avatarPath !== undefined && !avatarPath.includes('null')) {
        avatarElement.src = avatarPath
    } else {
        avatarElement.src = '/static/img/noImage.png'
    }
}
function getRandomColor() {
    const colors = ['#00ff71','#ff0000','#0566ff','#d900ff','#b59717']

    return colors[Math.floor(Math.random() * colors.length)]
}
function highlightContainer(container) {
    let oldBg = container.style.backgroundColor
    let oldTransition = container.style.transition
    container.style.animationName = 'highlight-animation';
    container.style.animationDuration = '1s';

    setTimeout(function() {
        container.style.animationName = '';
        container.style.backgroundColor = '';

        // Добавляем эффект временного фейда
        container.style.transition = oldTransition
        container.style.backgroundColor = oldBg;

        setTimeout(function() {
            container.style.transition = '';
        }, 500);
    }, 1500);
}