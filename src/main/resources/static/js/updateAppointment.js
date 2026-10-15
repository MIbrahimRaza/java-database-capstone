// updateAppointment.js
import { updateAppointment } from "./services/appointmentRecordService.js";
import { getDoctors } from "./services/doctorServices.js";

document.addEventListener("DOMContentLoaded", initializePage);

async function initializePage() {
    const token = localStorage.getItem("token");

    // Values passed in the URL by patientAppointment.js
    const urlParams = new URLSearchParams(window.location.search);
    const appointmentId = urlParams.get("appointmentId");
    const patientId = urlParams.get("patientId");
    const doctorId = urlParams.get("doctorId");
    const patientName = urlParams.get("patientName");
    const doctorName = urlParams.get("doctorName");
    const appointmentDate = urlParams.get("appointmentDate");
    const appointmentTime = urlParams.get("appointmentTime");

    if (!token || !patientId) {
        alert("Missing session data, redirecting to appointments page.");
        window.location.href = "/pages/patientAppointments.html";
        return;
    }

    try {
        // Only the doctor's available times are offered
        const doctors = await getDoctors();
        const doctor = doctors.find((d) => d.id == doctorId);

        if (!doctor) {
            alert("Doctor not found.");
            return;
        }

        // Fill the form
        document.getElementById("patientName").value = patientName || "You";
        document.getElementById("doctorName").value = doctorName;
        document.getElementById("appointmentDate").value = appointmentDate;

        const timeSelect = document.getElementById("appointmentTime");
        (doctor.availableTimes || []).forEach((time) => {
            const option = document.createElement("option");
            option.value = time;
            option.textContent = time;
            // Pre-select the slot that starts at the current appointment time
            if (appointmentTime && time.startsWith(appointmentTime.substring(0, 5))) {
                option.selected = true;
            }
            timeSelect.appendChild(option);
        });

        // Submit the update
        document.getElementById("updateAppointmentForm").addEventListener("submit", async (e) => {
            e.preventDefault();

            const date = document.getElementById("appointmentDate").value;
            const time = document.getElementById("appointmentTime").value;

            if (!date || !time) {
                alert("Please select both date and time.");
                return;
            }

            const startTime = time.split("-")[0];
            const updatedAppointment = {
                id: Number(appointmentId),
                doctor: { id: doctor.id },
                patient: { id: Number(patientId) },
                appointmentTime: `${date}T${startTime}:00`,
                status: 0
            };

            const result = await updateAppointment(updatedAppointment, token);

            if (result.success) {
                alert("Appointment updated successfully!");
                window.location.href = "/pages/patientAppointments.html";
            } else {
                alert("❌ Failed to update appointment: " + result.message);
            }
        });
    } catch (error) {
        console.error("Error fetching doctors:", error);
        alert("❌ Failed to load doctor data.");
    }
}