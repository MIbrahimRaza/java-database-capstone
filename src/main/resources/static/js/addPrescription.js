// addPrescription.js
import { savePrescription, getPrescription } from "./prescriptionServices.js";

document.addEventListener("DOMContentLoaded", async () => {
    const savePrescriptionBtn = document.getElementById("savePrescription");
    const patientNameInput = document.getElementById("patientName");
    const medicinesInput = document.getElementById("medicines");
    const dosageInput = document.getElementById("dosage");
    const notesInput = document.getElementById("notes");
    const heading = document.getElementById("heading");

    const urlParams = new URLSearchParams(window.location.search);
    const appointmentId = urlParams.get("appointmentId");
    const mode = urlParams.get("mode");
    const patientName = urlParams.get("patientName");
    const token = localStorage.getItem("token");

    // Heading depends on the mode
    if (heading) {
        heading.innerHTML =
            mode === "view" ? `View <span>Prescription</span>` : `Add <span>Prescription</span>`;
    }

    // Pre-fill the patient name
    if (patientNameInput && patientName) {
        patientNameInput.value = patientName;
    }

    // Pre-fill an existing prescription, if there is one
    if (appointmentId && token) {
        try {
            const response = await getPrescription(appointmentId, token);

            if (response.prescription && response.prescription.length > 0) {
                const existing = response.prescription[0];
                patientNameInput.value = existing.patientName || patientName || "";
                medicinesInput.value = existing.medication || "";
                dosageInput.value = existing.dosage || "";
                notesInput.value = existing.doctorNotes || "";
            }
        } catch (error) {
            console.warn("No existing prescription found or failed to load:", error);
        }
    }

    // View mode: read-only form
    if (mode === "view") {
        patientNameInput.disabled = true;
        medicinesInput.disabled = true;
        dosageInput.disabled = true;
        notesInput.disabled = true;
        savePrescriptionBtn.style.display = "none";
    }

    // Save the prescription
    savePrescriptionBtn.addEventListener("click", async (e) => {
        e.preventDefault();

        const prescription = {
            patientName: patientNameInput.value,
            medication: medicinesInput.value,
            dosage: dosageInput.value,
            doctorNotes: notesInput.value,
            appointmentId: Number(appointmentId)
        };

        const { success, message } = await savePrescription(prescription, token);

        if (success) {
            alert("✅ Prescription saved successfully.");
            selectRole("doctor");
        } else {
            alert("❌ Failed to save prescription. " + message);
        }
    });
});