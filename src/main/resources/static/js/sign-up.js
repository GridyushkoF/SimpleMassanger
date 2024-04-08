

$(".auth-btn").onclick = () => {
    let name = $(".auth-input.login").value;
    let password = $(".auth-input.password").value;
    let userData = {
        'username' : name,
        'password' : password
    };
    console.log('Имя пользователя: ' + name)
    console.log('Пароль пользователя: ' + password)
    console.log('Userdata: ' + userData)
    console.log(JSON.stringify(userData))
    fetch('/sign-up', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData)
    }).then(response => response.json())
        .then(result => {
            console.log(result)
            if (result['is_signup_success'] === 'false') {
                const alertContainer = $('.alert-container');
                let errorAlert = $('.error-alert');
                if (!errorAlert) {
                    errorAlert = $create('div');
                    errorAlert.className = 'error-alert';
                    alertContainer.append(errorAlert);
                }
                errorAlert.innerHTML = result['error_message'];
            } else {
                fetchSignInPage(name,password)
            }
        })
        .catch(exception => console.log('ОШИБКА: ' + exception));
};
function fetchSignInPage(name,password) {
    fetch('/sign-in-page', {
        method: 'POST',
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: 'username=' + encodeURIComponent(name) + '&password' + encodeURIComponent(password)
    })
        .then(function(response) {
            // Handle the response here
            console.log(response);
            window.location.href = '/main';
        })
        .catch(function(error) {
            console.log("Error:", error);
        });
}