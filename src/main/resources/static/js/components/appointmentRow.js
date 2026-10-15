// appointmentRow.js
export function getAppointments(appointment) {
    const tr = document.createElement("tr");

    tr.innerHTML = `
    <td class="patient-id">${appointment.patientName || "You"}</td>
    <td>${appointment.doctorName}</td>
    <td>${appointment.appointmentDate}</td>
    <td>${appointment.appointmentTimeOnly}</td>
    <td>
      <img src="/assets/images/edit/edit.png" alt="Edit" class="prescription-btn" data-id="${appointment.id}">
    </td>
  `;

    tr.querySelector(".prescription-btn").addEventListener("click", () => {
        const query = new URLSearchParams({
            appointmentId: appointment.id,
            patientId: appointment.patientId,
            patientName: appointment.patientName || "You",
            doctorName: appointment.doctorName,
            doctorId: appointment.doctorId,
            appointmentDate: appointment.appointmentDate,
            appointmentTime: appointment.appointmentTimeOnly
        }).toString();

        window.location.href = `/pages/updateAppointment.html?${query}`;
    });

    return tr;
}