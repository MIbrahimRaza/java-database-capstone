## MySQL Database Design

### Table: patients
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(100), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone: VARCHAR(20), Not Null
- address: VARCHAR(255)
- date_of_birth: DATE

### Table: doctors
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(100), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone: VARCHAR(20), Not Null
- specialty: VARCHAR(50), Not Null

### Table: doctor_available_times
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id) ON DELETE RESTRICT, Not Null
- day_of_week: TINYINT, Not Null (1 = Monday ... 7 = Sunday)
- start_time: TIME, Not Null
- end_time: TIME, Not Null
- Unique (doctor_id, day_of_week, start_time)

### Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id) ON DELETE RESTRICT, Not Null
- patient_id: INT, Foreign Key → patients(id) ON DELETE RESTRICT, Not Null
- appointment_time: DATETIME, Not Null
- status: INT, Not Null (0 = Scheduled, 1 = Completed, 2 = Cancelled)
- active_slot: DATETIME, Generated Stored (appointment_time when status <> 2, otherwise NULL)
- Unique (doctor_id, active_slot)  <!-- prevents overlapping bookings, allows rebooking cancelled slots -->

### Table: admin
- id: INT, Primary Key, Auto Increment
- username: VARCHAR(50), Not Null, Unique
- password: VARCHAR(255), Not Null

### Table: payments
- id: INT, Primary Key, Auto Increment
- appointment_id: INT, Foreign Key → appointments(id) ON DELETE RESTRICT, Not Null
- amount: DECIMAL(10,2), Not Null
- payment_status: INT, Not Null (0 = Pending, 1 = Paid, 2 = Refunded)
- payment_date: DATETIME

<!--
Design notes:
- Deleting a patient or doctor is restricted while appointments exist (ON DELETE RESTRICT), to preserve history.
- Appointment history is kept forever; status changes instead of deleting rows.
- active_slot is NULL for cancelled appointments. MySQL allows multiple NULLs in a unique index,
  so cancelled slots can be rebooked while active appointments still cannot overlap.
- Email format and phone format are validated in Java code (Bean Validation), not in SQL.
- Passwords are stored as hashed values, never plain text.
-->

## MongoDB Collection Design

### Collection: prescriptions
```json
{
  "_id": "64abc123456789abcdef0123",
  "patientId": 12,
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "refillCount": 2,
  "tags": ["fever", "pain-relief"],
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  },
  "createdAt": "2026-10-03T10:30:00Z"
}
```

<!--
Design notes:
- Only patientId and appointmentId (references to MySQL) are stored, not patient details, to avoid duplicated data.
- Nested objects and arrays (pharmacy, tags) can change per prescription without altering a schema.
- New fields (e.g. allergies, attachments) can be added later without migration.
-->

### Collection: messages (optional)
```json
{
  "_id": "64def789012345abcdef0456",
  "appointmentId": 51,
  "sender": { "role": "patient", "id": 12 },
  "message": "Should I take it before or after meals?",
  "attachments": [],
  "sentAt": "2026-10-03T11:00:00Z"
}