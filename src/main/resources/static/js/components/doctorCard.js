import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";
import { showBookingOverlay } from "../loggedPatient.js";

export function createDoctorCard(doctor) {
    // Main card container
    const card = document.createElement("div");
    card.classList.add("doctor-card");

    // Current user role
    const role = localStorage.getItem("userRole");

    // Doctor info section
    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info");

    const name = document.createElement("h3");
    name.textContent = doctor.name;

    const specialization = document.createElement("p");
    specialization.textContent = "Specialty: " + doctor.specialty;

    const email = document.createElement("p");
    email.textContent = "Email: " + doctor.email;

    const availability = document.createElement("p");
    availability.textContent =
        "Available: " + (doctor.availableTimes ? doctor.availableTimes.join(", ") : "N/A");

    infoDiv.appendChild(name);
    infoDiv.appendChild(specialization);
    infoDiv.appendChild(email);
    infoDiv.appendChild(availability);

    // Button container
    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    if (role === "admin") {
        // Admin: delete doctor
        const removeBtn = document.createElement("button");
        removeBtn.textContent = "Delete";

        removeBtn.addEventListener("click", async () => {
            // 1. Confirm deletion
            if (!confirm(`Delete Dr. ${doctor.name}?`)) return;

            // 2. Get token from localStorage
            const token = localStorage.getItem("token");

            // 3. Call API to delete
            const result = await deleteDoctor(doctor.id, token);

            // 4. On success: remove the card from the DOM
            if (result && result.success) {
                alert("Doctor deleted successfully.");
                card.remove();
            } else {
                alert((result && result.message) || "Failed to delete doctor.");
            }
        });

        actionsDiv.appendChild(removeBtn);

    } else if (role === "patient") {
        // Patient (not logged in): login required
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";

        bookNow.addEventListener("click", () => {
            alert("Patient needs to login first.");
        });

        actionsDiv.appendChild(bookNow);

    } else if (role === "loggedPatient") {
        // Logged-in patient: real booking
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";

        bookNow.addEventListener("click", async (e) => {
            const token = localStorage.getItem("token");
            const patientData = await getPatientData(token);
            showBookingOverlay(e, doctor, patientData);
        });

        actionsDiv.appendChild(bookNow);
    }

    // Final assembly
    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    return card;
}