//NOT JQuery, CUSTOM DOM LIBRARY!
/*
    $('.selector') = document.querySelector('.selector')
    $all('.selector') = document.querySelectorAll('.selector')
    element.$('.selector') = element.querySelector('.selector')
    element.$all('.selector') = element.querySelectorAll('.selector')
    $create('div','test-container') = {
        element = document.createElement('div')
        element.className = 'test-container';
    }
    createButtonWithText('TEXT-TEST','test-btn') = {
        const el = document.createElement('button')
        element.className = 'test-btn';
        element.textContent = 'TEXT-TEST';
    }
    show/hide element use classes enabled/disabled
 */
const $ = (selector) => document.querySelector(selector);
const $all = (selector) => [...document.querySelectorAll(selector)];
HTMLElement.prototype.$ = function(selector) {
    return this.querySelector(selector);
};
HTMLElement.prototype.$all = function(selector) {
    return this.querySelectorAll(selector);
};
function $create(elementType, className) {
    const element = document.createElement(elementType);
    element.className = className;
    return element;
}
function createButtonWithText(text, className) {
    const button = $create("button", className);
    button.textContent = text;
    return button;
}
function showElement(element) {
    element.classList.remove('disabled')
    element.classList.add('enabled')
}
function hideElement(element) {
    element.classList.remove('enabled')
    element.classList.add('disabled')
}
function $log(text) {
    console.log(text)
}
function $error(errorText) {
    console.error(errorText)
}
