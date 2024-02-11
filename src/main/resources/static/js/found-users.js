function addUserToMyContactList() {
    let found_user_info = document.querySelector('.found-user-info');
    let username = found_user_info.getAttribute('data-username');
    fetch("/add-user-to-my-contacts/" + username)
        .then(response => response.json())
        .then(() => {
            window.location.href = '/main'
        })
}