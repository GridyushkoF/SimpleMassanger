const auth_btn = document.querySelector(".auth-btn");
const login_input = document.querySelector(".auth-input.login");
const password_input = document.querySelector(".auth-input.password");
auth_btn.addEventListener('click', () => {
    let name = login_input.value;
    let password = password_input.value;
    let userData = {
        "username" : name,
        "password" : password
    };
    console.log("Имя пользователя: " + name)
    console.log("Пароль пользователя: " + password)
    console.log("Userdata: " + userData)
    console.log(JSON.stringify(userData))
    fetch("/sign-up", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(userData)
    }).then(response => response.json())
        .then(result => {
            console.log(result)
            if (result["is_signup_success"] === "false") {
                const alert_container = document.querySelector(".alert-container");
                let error_alert = document.querySelector(".error-alert");
                if (!error_alert) {
                    error_alert = document.createElement("div");
                    error_alert.className = "error-alert";
                    alert_container.append(error_alert);
                }
                error_alert.innerHTML = result["error_message"];
            } else {
                fetch('/sign-in-page', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: 'username=' + encodeURIComponent(name) + '&password=' + encodeURIComponent(password)
                })
                    .then(function(response) {
                        // Handle the response here
                        console.log(response);
                        window.location.href = "/main";
                    })
                    .catch(function(error) {
                        console.log('Error:', error);
                    });
            }
        })
        .catch(exception => console.log("ОШИБКА: " + exception));
});