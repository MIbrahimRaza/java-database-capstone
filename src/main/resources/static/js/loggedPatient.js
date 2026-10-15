// loggedPatient.js
import { getDoctors, filterDoctors } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";
import { bookAppointment } from "./appointmentRecordService.js";

// ===== Render a list of doctor cards =====
export function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = "";

    if (!doctors || doctors.length === 0) {
        contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
        return;
    }

    doctors.forEach((doctor) => {
        contentDiv.appendChild(createDoctorCard(doctor));
    });
}

// ===== Load all doctors =====
async function loadDoctorCards() {
    try {
        const doctors = await getDoctors();
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Failed to load doctors:", error);
    }
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
        alert("❌ An error occurred while filtering doctors.");
    }
}

// ===== Booking overlay (called from doctorCard.js) =====
export function showBookingOverlay(e, doctor, patient) {
    // Ripple effect from the click position
    const ripple = document.createElement("div");
    ripple.classList.add("ripple-overlay");
    ripple.style.left = `${e.clientX}px`;
    ripple.style.top = `${e.clientY}px`;
    document.body.appendChild(ripple);
    setTimeout(() => ripple.classList.add("active"), 50);

    // Bottom pop-up booking form
    const modalApp = document.createElement("div");
    modalApp.classList.add("modalApp");

    modalApp.innerHTML = `
    <h2>Book Appointment</h2>
    <input class="input-field" type="text" value="${patient.name}" disabled />
    <input class="input-field" type="text" value="${doctor.name}" disabled />
    <input class="input-field" type="text" value="${doctor.specialty}" disabled />
    <input class="input-field" type="email" value="${doctor.email}" disabled />
    <input class="input-field" type="date" id="appointment-date" />
    <select class="input-field" id="appointment-time">
      <option value="">Select time</option>
      ${(doctor.availableTimes || []).map((t) => `<option value="${t}">${t}</option>`).join("")}
    </select>
    <button class="confirm-booking">Confirm Booking</button>
  `;

    document.body.appendChild(modalApp);
    setTimeout(() => modalApp.classList.add("active"), 600);

    modalApp.querySelector(".confirm-booking").addEventListener("click", async () => {
        const date = modalApp.querySelector("#appointment-date").value;
        const time = modalApp.querySelector("#appointment-time").value;
        const token = localStorage.getItem("token");

        if (!date || !time) {
            alert("Please select both a date and a time.");
            return;
        }

        const startTime = time.split("-")[0]; // "09:00-10:00" -> "09:00"
        const appointment = {
            doctor: { id: doctor.id },
            patient: { id: patient.id },
            appointmentTime: `${date}T${startTime}:00`,
            status: 0
        };

        const { success, message } = await bookAppointment(appointment, token);

        if (success) {
            alert("Appointment booked successfully");
            ripple.remove();
            modalApp.remove();
        } else {
            alert("❌ Failed to book an appointment :: " + message);
        }
    });
}

// ===== Page-level code =====
// doctorCard.js imports this file on other pages too,
// so the listeners are attached only on the logged-in patient dashboard.
if (window.location.pathname.endsWith("loggedPatientDashboard.html")) {
    document.addEventListener("DOMContentLoaded", () => {
        loadDoctorCards();

        document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
        document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
        document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);
    });
}