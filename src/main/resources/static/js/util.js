// util.js - role helpers stored in localStorage
function setRole(role) {
    localStorage.setItem("userRole", role);
}

function getRole() {
    return localStorage.getItem("userRole");
}

function clearRole() {
    localStorage.removeItem("userRole");
}