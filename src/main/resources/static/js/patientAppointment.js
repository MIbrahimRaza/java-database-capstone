// patientAppointment.js
import { getPatientAppointments, getPatientData, filterAppointments } from "./services/patientServices.js";

const tableBody = document.getElementById("patientTableBody");
const token = localStorage.getItem("token");

let allAppointments = [];
let patientId = null;

document.addEventListener("DOMContentLoaded", initializePage);

async function initializePage() {
    try {
        if (!token) throw new Error("No token found");

        const patient = await getPatientData(token);
        if (!patient) throw new Error("Failed to fetch patient details");

        patientId = Number(patient.id);

        const appointmentData = (await getPatientAppointments(patientId, token, "patient")) || [];
        allAppointments = appointmentData.filter((app) => app.patientId === patientId);

        renderAppointments(allAppointments);
    } catch (error) {
        console.error("Error loading appointments:", error);
        alert("❌ Failed to load your appointments.");
    }
}

function renderAppointments(appointments) {
    tableBody.innerHTML = "";

    if (!appointments.length) {
        tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center;">No Appointments Found</td></tr>`;
        return;
    }

    appointments.forEach((appointment) => {
        const tr = document.createElement("tr");

        tr.innerHTML = `
      <td>${appointment.patientName || "You"}</td>
      <td>${appointment.doctorName}</td>
      <td>${appointment.appointmentDate}</td>
      <td>${appointment.appointmentTimeOnly}</td>
      <td>${
            appointment.status == 0
                ? `<img src="/assets/images/edit/edit.png" alt="Edit" class="prescription-btn" data-id="${appointment.id}">`
                : "-"
        }</td>
    `;

        // Only scheduled appointments (status 0) can be edited
        if (appointment.status == 0) {
            tr.querySelector(".prescription-btn")
                .addEventListener("click", () => redirectToUpdatePage(appointment));
        }

        tableBody.appendChild(tr);
    });
}

function redirectToUpdatePage(appointment) {
    const queryString = new URLSearchParams({
        appointmentId: appointment.id,
        patientId: appointment.patientId,
        patientName: appointment.patientName || "You",
        doctorName: appointment.doctorName,
        doctorId: appointment.doctorId,
        appointmentDate: appointment.appointmentDate,
        appointmentTime: appointment.appointmentTimeOnly
    }).toString();

    window.location.href = `/pages/updateAppointment.html?${queryString}`;
}

// ===== Search and filter =====
document.getElementById("searchBar").addEventListener("input", handleFilterChange);
document.getElementById("appointmentFilter").addEventListener("change", handleFilterChange);

async function handleFilterChange() {
    const searchValue = document.getElementById("searchBar").value.trim();
    const filterValue = document.getElementById("appointmentFilter").value;

    const name = searchValue || null;
    const condition = filterValue === "allAppointments" ? null : filterValue || null;

    try {
        const appointments = await filterAppointments(condition, name, token);
        renderAppointments(appointments.filter((app) => app.patientId === patientId));
    } catch (error) {
        console.error("Failed to filter appointments:", error);
        alert("❌ An error occurred while filtering appointments.");
    }
}