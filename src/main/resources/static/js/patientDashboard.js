import { createDoctorCard } from "./components/doctorCard.js";
import { openModal } from "./components/modals.js";
import { getDoctors, filterDoctors } from "./services/doctorServices.js";
import { patientLogin, patientSignup } from "./services/patientServices.js";

// ===== Load doctors on page load =====
document.addEventListener("DOMContentLoaded", () => {
    loadDoctorCards();
});

// ===== Signup / login modal triggers =====
document.addEventListener("DOMContentLoaded", () => {
    const btn = document.getElementById("patientSignup");
    if (btn) btn.addEventListener("click", () => openModal("patientSignup"));
});

document.addEventListener("DOMContentLoaded", () => {
    const loginBtn = document.getElementById("patientLogin");
    if (loginBtn) loginBtn.addEventListener("click", () => openModal("patientLogin"));
});

// ===== Load all doctors =====
async function loadDoctorCards() {
    try {
        const doctors = await getDoctors();
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Failed to load doctors:", error);
    }
}

// ===== Render a list of doctor cards =====
function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = "";

    if (!doctors || doctors.length === 0) {
        contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
        return;
    }

    doctors.forEach((doctor) => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
    });
}

// ===== Search and filter listeners =====
document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);

function filterDoctorsOnChange() {
    const name = document.getElementById("searchBar").value.trim() || null;
    const time = document.getElementById("filterTime").value || null;
    const specialty = document.getElementById("filterSpecialty").value || null;

    filterDoctors(name, time, specialty)
        .then((doctors) => {
            renderDoctorCards(doctors);
        })
        .catch((error) => {
            console.error("Failed to filter doctors:", error);
            alert("Something went wrong while filtering doctors.");
        });
}

// ===== Patient signup =====
window.signupPatient = async function () {
    try {
        const name = document.getElementById("name").value;
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;
        const phone = document.getElementById("phone").value;
        const address = document.getElementById("address").value;

        const data = { name, email, password, phone, address };
        const result = await patientSignup(data);

        if (result.success) {
            alert(result.message);
            document.getElementById("modal").style.display = "none";
            window.location.reload();
        } else {
            alert(result.message);
        }
    } catch (error) {
        console.error("Signup failed:", error);
        alert("Something went wrong during signup.");
    }
};

// ===== Patient login =====
window.loginPatient = async function () {
    try {
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;

        const response = await patientLogin({ email, password });

        if (response.ok) {
            const result = await response.json();
            localStorage.setItem("token", result.token);
            localStorage.setItem("userRole", "loggedPatient");
            window.location.href = "/pages/loggedPatientDashboard.html";
        } else {
            alert("Invalid credentials!");
        }
    } catch (error) {
        console.error("Login failed:", error);
        alert("Something went wrong during login.");
    }
};