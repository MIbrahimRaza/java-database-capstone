// appointmentRecord.js
// Optional script: lists the logged-in patient's upcoming or past appointments.
import { getAppointments } from "./components/appointmentRow.js";
import { getPatientData, getPatientAppointments } from "./services/patientServices.js";

const tableBody = document.getElementById("patientTableBody");
const filterSelect = document.getElementById("appointmentFilter");
const token = localStorage.getItem("token");

async function loadAppointments(filter = "upcoming") {
    const patient = await getPatientData(token);
    const appointments = patient ? (await getPatientAppointments(patient.id, token, "patient")) || [] : [];

    if (appointments.length === 0) {
        tableBody.innerHTML = `<tr><td class="noPatientRecord" colspan="5">No appointments found.</td></tr>`;
        return;
    }

    const today = new Date().setHours(0, 0, 0, 0);
    let filtered = appointments;

    if (filter === "upcoming") {
        filtered = appointments.filter((app) => new Date(app.appointmentDate) >= today);
    } else if (filter === "past") {
        filtered = appointments.filter((app) => new Date(app.appointmentDate) < today);
    }

    if (filtered.length === 0) {
        tableBody.innerHTML = `<tr><td class="noPatientRecord" colspan="5">No ${filter} appointments found.</td></tr>`;
        return;
    }

    tableBody.innerHTML = "";
    filtered.forEach((appointment) => {
        tableBody.appendChild(getAppointments(appointment));
    });
}

// Filter change
if (filterSelect) {
    filterSelect.addEventListener("change", (e) => loadAppointments(e.target.value));
}

// Load upcoming appointments by default
loadAppointments("upcoming");
