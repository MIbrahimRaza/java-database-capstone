function renderHeader() {
    const headerDiv = document.getElementById("header");
    if (!headerDiv) return;

    const logo = `
    <div class="logo-section">
      <img src="/assets/images/logo/logo.png" alt="Hospital CMS Logo" class="logo-img">
      <span class="logo-title">Hospital CMS</span>
    </div>`;

    // Homepage: clear the session and show only the logo
    if (window.location.pathname.endsWith("/")) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
        headerDiv.innerHTML = `<header class="header">${logo}</header>`;
        return;
    }

    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");

    // Invalid or expired session
    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        alert("Session expired or invalid login. Please log in again.");
        window.location.href = "/";
        return;
    }

    let headerContent = "";

    if (role === "admin") {
        headerContent += `
      <button id="addDocBtn" class="adminBtn" onclick="openModal('addDoctor')">Add Doctor</button>
      <a href="#" onclick="logout()">Logout</a>`;
    } else if (role === "doctor") {
        headerContent += `
      <button class="adminBtn" onclick="selectRole('doctor')">Home</button>
      <a href="#" onclick="logout()">Logout</a>`;
    } else if (role === "patient") {
        headerContent += `
      <button id="patientLogin" class="adminBtn">Login</button>
      <button id="patientSignup" class="adminBtn">Sign Up</button>`;
    } else if (role === "loggedPatient") {
        headerContent += `
      <button class="adminBtn" onclick="window.location.href='/pages/loggedPatientDashboard.html'">Home</button>
      <button class="adminBtn" onclick="window.location.href='/pages/patientAppointments.html'">Appointments</button>
      <a href="#" onclick="logoutPatient()">Logout</a>`;
    }

    headerDiv.innerHTML = `
    <header class="header">
      ${logo}
      <nav>${headerContent}</nav>
    </header>`;

    attachHeaderButtonListeners();
}

function attachHeaderButtonListeners() {
    const loginBtn = document.getElementById("patientLogin");
    if (loginBtn) {
        loginBtn.addEventListener("click", () => openModal("patientLogin"));
    }

    const signupBtn = document.getElementById("patientSignup");
    if (signupBtn) {
        signupBtn.addEventListener("click", () => openModal("patientSignup"));
    }
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    window.location.href = "/";
}

function logoutPatient() {
    localStorage.removeItem("token");
    localStorage.setItem("userRole", "patient");
    window.location.href = "/pages/patientDashboard.html";
}

renderHeader();