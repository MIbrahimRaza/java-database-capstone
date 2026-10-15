import { openModal } from "./components/modals.js";
import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

// ===== Page load =====
document.addEventListener("DOMContentLoaded", () => {
    // "Add Doctor" button opens the modal
    const addDocBtn = document.getElementById("addDocBtn");
    if (addDocBtn) {
        addDocBtn.addEventListener("click", () => {
            openModal("addDoctor");
        });
    }

    loadDoctorCards();

    // Search and filter listeners
    document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
    document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
    document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);
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
        contentDiv.innerHTML = "<p>No doctors found</p>";
        return;
    }

    doctors.forEach((doctor) => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
    });
}

// ===== Search and filter =====
async function filterDoctorsOnChange() {
    const name = document.getElementById("searchBar").value.trim() || null;
    const time = document.getElementById("filterTime").value || null;
    const specialty = document.getElementById("filterSpecialty").value || null;

    try {
        const doctors = await filterDoctors(name, time, specialty);
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Failed to filter doctors:", error);
        alert("Something went wrong while filtering doctors.");
    }
}

// ===== Add doctor (called by the modal form) =====
window.adminAddDoctor = async function () {
    // Collect form values
    const name = document.getElementById("doctorName").value.trim();
    const email = document.getElementById("doctorEmail").value.trim();
    const phone = document.getElementById("doctorPhone").value.trim();
    const password = document.getElementById("doctorPassword").value;
    const specialty = document.getElementById("specialization").value;

    // Selected availability checkboxes
    const availableTimes = Array.from(
        document.querySelectorAll('input[name="availability"]:checked')
    ).map((checkbox) => checkbox.value);

    // Admin must be logged in
    const token = localStorage.getItem("token");
    if (!token) {
        alert("Admin not authenticated. Please log in again.");
        return;
    }

    const doctor = { name, email, phone, password, specialty, availableTimes };

    try {
        const result = await saveDoctor(doctor, token);

        if (result.success) {
            alert(result.message || "Doctor added successfully.");
            document.getElementById("modal").style.display = "none";
            loadDoctorCards();
        } else {
            alert(result.message || "Failed to add doctor.");
        }
    } catch (error) {
        console.error("Error adding doctor:", error);
        alert("Something went wrong while adding the doctor.");
    }
};