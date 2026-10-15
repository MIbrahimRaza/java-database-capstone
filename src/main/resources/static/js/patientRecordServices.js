// patientRecordServices.js
import { getPatientAppointments } from "./services/patientServices.js";
import { createPatientRecordRow } from "./components/patientRecordRow.js";

const tableBody = document.getElementById("patientTableBody");
const token = localStorage.getItem("token");

const urlParams = new URLSearchParams(window.location.search);
const patientId = urlParams.get("id");
const doctorId = urlParams.get("doctorId");

document.addEventListener("DOMContentLoaded", initializePage);

async function initializePage() {
    try {
        if (!token) throw new Error("No token found");

        const appointmentData = (await getPatientAppointments(patientId, token, "doctor")) || [];

        // Keep only this doctor's appointments for the patient
        const filtered = appointmentData.filter((app) => app.doctorId == doctorId);
        renderAppointments(filtered);
    } catch (error) {
        console.error("Error loading appointments:", error);
        alert("❌ Failed to load the patient's appointments.");
    }
}

function renderAppointments(appointments) {
    tableBody.innerHTML = "";

    if (!appointments.length) {
        tableBody.innerHTML = `<tr><td colspan="4" style="text-align:center;">No Appointments Found</td></tr>`;
        return;
    }

    appointments.forEach((appointment) => {
        tableBody.appendChild(createPatientRecordRow(appointment));
    });
}
