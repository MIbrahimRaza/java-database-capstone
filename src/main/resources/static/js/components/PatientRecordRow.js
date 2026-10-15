// patientRecordRow.js
export function createPatientRecordRow(appointment) {
    const tr = document.createElement("tr");

    tr.innerHTML = `
    <td class="patient-id">${appointment.appointmentDate}</td>
    <td>${appointment.id}</td>
    <td>${appointment.patientId}</td>
    <td>
      <img src="/assets/images/addPrescriptionIcon/addPrescription.png"
           alt="viewPrescription" class="prescription-btn" data-id="${appointment.id}">
    </td>
  `;

    // View the prescription for this appointment (read-only)
    tr.querySelector(".prescription-btn").addEventListener("click", () => {
        window.location.href = `/pages/addPrescription.html?mode=view&appointmentId=${appointment.id}`;
    });

    return tr;
}