import { getAllAppointments } from "./appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

// ===== Global variables =====
const tableBody = document.getElementById("patientTableBody");
let selectedDate = new Date().toLocaleDateString("en-CA"); // today, YYYY-MM-DD (local time)
const token = localStorage.getItem("token");
let patientName = null;

// ===== Search bar =====
document.getElementById("searchBar").addEventListener("input", (e) => {
    const value = e.target.value.trim();
    patientName = value ? value : "null";
    loadAppointments();
});

// ===== "Today's Appointments" button =====
document.getElementById("todayButton").addEventListener("click", () => {
    selectedDate = new Date().toLocaleDateString("en-CA");
    document.getElementById("datePicker").value = selectedDate;
    loadAppointments();
});

// ===== Date picker =====
document.getElementById("datePicker").addEventListener("change", (e) => {
    selectedDate = e.target.value;
    loadAppointments();
});

// ===== Load appointments =====
async function loadAppointments() {
    try {
        const appointments = await getAllAppointments(selectedDate, patientName, token);

        tableBody.innerHTML = "";

        if (!appointments || appointments.length === 0) {
            tableBody.innerHTML = `
        <tr>
          <td colspan="5" class="noPatientRecord">No Appointments found for today</td>
        </tr>`;
            return;
        }

        appointments.forEach((appointment) => {
            const patient = {
                id: appointment.patientId,
                name: appointment.patientName,
                phone: appointment.patientPhone,
                email: appointment.patientEmail
            };

            const row = createPatientRow(patient, appointment.id, appointment.doctorId);
            tableBody.appendChild(row);
        });
    } catch (error) {
        console.error("Error loading appointments:", error);
        tableBody.innerHTML = `
      <tr>
        <td colspan="5" class="noPatientRecord">Error loading appointments. Try again later.</td>
      </tr>`;
    }
}

// ===== Initial render =====
document.addEventListener("DOMContentLoaded", () => {
    if (typeof renderContent === "function") renderContent();
    document.getElementById("datePicker").value = selectedDate;
    loadAppointments();
});