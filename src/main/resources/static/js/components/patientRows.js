// patientRows.js
export function createPatientRow(patient, appointmentId, doctorId) {
    const tr = document.createElement("tr");

    tr.innerHTML = `
    <td class="patient-id">${patient.id}</td>
    <td>${patient.name}</td>
    <td>${patient.phone}</td>
    <td>${patient.email}</td>
    <td>
      <img src="/assets/images/addPrescriptionIcon/addPrescription.png"
           alt="addPrescriptionIcon" class="prescription-btn" data-id="${patient.id}">
    </td>
  `;

    // Click the patient ID to open the patient record
    tr.querySelector(".patient-id").addEventListener("click", () => {
        window.location.href = `/pages/patientRecord.html?id=${patient.id}&doctorId=${doctorId}`;
    });

    // Click the icon to add a prescription for this appointment
    tr.querySelector(".prescription-btn").addEventListener("click", () => {
        window.location.href =
            `/pages/addPrescription.html?appointmentId=${appointmentId}&patientName=${encodeURIComponent(patient.name)}`;
    });

    return tr;
}