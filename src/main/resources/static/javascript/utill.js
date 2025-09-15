
function redirectHome() {
    document.location = '/'
}


function newElement(type, parent, name, value, text) {
    let element;
    switch (type) {
        case 'label':
            element = document.createElement("label");
            break;
        case 'input':
            element = document.createElement("input");
            element.type = "text";
            element.value = value;
            break;
        case 'select':
            element = document.createElement("select");
            break;
        case 'option':
            element = document.createElement("option");
            element.value = value
            break;
        case 'textarea':
            element = document.createElement("textarea");
            break;
        case 'p':
            element = document.createElement('p');
            break;
        case 'image':
            element = document.createElement('input')
            element.type = 'file';
            element.accept = 'image/png, image/jpeg'
            break;
        default:
            return;
    }
    element.name = name;
    element.innerHTML = text;
    parent.appendChild(element);
    return element;
}

async function fetchTableSummary(n) {
    let params = new URLSearchParams();
    params.append("tableName", n);
    let response = await fetch("/analyse/stats", {
        method: "POST",
        credentials: "include",
        body: params
    });
    response = await response.json();
    return response;
}

async function fetchEventContext(n) {

    let params = new URLSearchParams();
    params.append("eventName", n);
    let response = await fetch("/analyse/eventcontext", {
        method: 'POST',
        credentials: "include",
        body: params
    });

    response = await response.json();
    return response;

}



window.addEventListener('DOMContentLoaded', function () {
    console.log(document.querySelector('.logo'));
    document.querySelector('.logo').addEventListener("click", redirectHome)
});