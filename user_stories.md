# Admin User Stories

## Admin: Login
**Title:**
_As an admin, I want to log into the portal with my username and password, so that I can manage the platform securely._

**Acceptance Criteria:**
1. Admin can log in with valid credentials
2. Invalid credentials show an error message
3. Successful login redirects to the admin dashboard

**Priority:** High
**Story Points:** 3
**Notes:**
- Consider lockout after repeated failed attempts

## Admin: Logout
**Title:**
_As an admin, I want to log out of the portal, so that system access is protected._

**Acceptance Criteria:**
1. Logout button is visible on every admin page
2. Session/token is invalidated on logout
3. User is redirected to the login page

**Priority:** High
**Story Points:** 2
**Notes:**
- Handle expired sessions gracefully

## Admin: Add doctor
**Title:**
_As an admin, I want to add doctors to the portal, so that patients can find and book them._

**Acceptance Criteria:**
1. Admin can submit a form with doctor details
2. Required fields are validated
3. New doctor appears in the doctor list

**Priority:** High
**Story Points:** 5
**Notes:**
- Prevent duplicate doctor emails

## Admin: Delete doctor profile
**Title:**
_As an admin, I want to delete a doctor's profile from the portal, so that inactive doctors are removed._

**Acceptance Criteria:**
1. Admin can select a doctor and delete the profile
2. A confirmation prompt appears before deletion
3. Deleted doctor no longer appears in the doctor list

**Priority:** Medium
**Story Points:** 3
**Notes:**
- Decide how to handle the doctor's existing appointments

## Admin: Monthly appointment statistics
**Title:**
_As an admin, I want to run a stored procedure in the MySQL CLI to get the number of appointments per month, so that I can track usage statistics._

**Acceptance Criteria:**
1. Stored procedure exists in the MySQL database
2. Running it returns appointment counts grouped by month
3. Output is accurate against the appointments table

**Priority:** Medium
**Story Points:** 5
**Notes:**
- Handle months with zero appointments

---

# Patient User Stories

## Patient: View doctors without login
**Title:**
_As a patient, I want to view a list of doctors without logging in, so that I can explore options before registering._

**Acceptance Criteria:**
1. Doctor list is publicly accessible
2. Each doctor shows name and specialization
3. No authentication is required

**Priority:** Medium
**Story Points:** 3
**Notes:**
- Do not expose sensitive doctor data publicly

## Patient: Sign up
**Title:**
_As a patient, I want to sign up using my email and password, so that I can book appointments._

**Acceptance Criteria:**
1. Registration form validates email and password
2. Duplicate emails are rejected
3. Account is created and the patient can log in

**Priority:** High
**Story Points:** 5
**Notes:**
- Passwords must be stored hashed

## Patient: Login
**Title:**
_As a patient, I want to log into the portal, so that I can manage my bookings._

**Acceptance Criteria:**
1. Patient can log in with valid credentials
2. Invalid credentials show an error
3. Login redirects to the patient dashboard

**Priority:** High
**Story Points:** 3
**Notes:**
- Use token-based authentication (JWT)

## Patient: Logout
**Title:**
_As a patient, I want to log out of the portal, so that my account is secure._

**Acceptance Criteria:**
1. Logout option is available when logged in
2. Session/token is invalidated
3. User is redirected to the home or login page

**Priority:** High
**Story Points:** 2
**Notes:**
- Protected pages must not be accessible after logout

## Patient: Book hour-long appointment
**Title:**
_As a patient, I want to log in and book an hour-long appointment, so that I can consult with a doctor._

**Acceptance Criteria:**
1. Patient can select a doctor and an available time slot
2. Appointment duration is fixed at one hour
3. Booked slot becomes unavailable to others

**Priority:** High
**Story Points:** 8
**Notes:**
- Prevent double booking and past-date booking

## Patient: View upcoming appointments
**Title:**
_As a patient, I want to view my upcoming appointments, so that I can prepare accordingly._

**Acceptance Criteria:**
1. Upcoming appointments are listed in date order
2. Each entry shows doctor, date, and time
3. Past appointments are excluded from the list

**Priority:** Medium
**Story Points:** 3
**Notes:**
- Show a friendly message when there are no appointments

---

# Doctor User Stories

## Doctor: Login
**Title:**
_As a doctor, I want to log into the portal, so that I can manage my appointments._

**Acceptance Criteria:**
1. Doctor can log in with valid credentials
2. Invalid credentials show an error
3. Login redirects to the doctor dashboard

**Priority:** High
**Story Points:** 3
**Notes:**
- Use role-based access control

## Doctor: Logout
**Title:**
_As a doctor, I want to log out of the portal, so that my data is protected._

**Acceptance Criteria:**
1. Logout option is visible when logged in
2. Session/token is invalidated
3. User is redirected to the login page

**Priority:** High
**Story Points:** 2
**Notes:**
- Handle expired sessions gracefully

## Doctor: View appointment calendar
**Title:**
_As a doctor, I want to view my appointment calendar, so that I can stay organized._

**Acceptance Criteria:**
1. Calendar shows all booked appointments
2. Doctor can switch between day, week, and month views
3. Entries show patient name and time

**Priority:** Medium
**Story Points:** 5
**Notes:**
- Show an empty state when nothing is booked

## Doctor: Mark unavailability
**Title:**
_As a doctor, I want to mark my unavailability, so that patients only see available slots._

**Acceptance Criteria:**
1. Doctor can block dates or time ranges
2. Blocked slots are hidden from patients
3. Existing appointments are not silently overridden

**Priority:** High
**Story Points:** 5
**Notes:**
- Handle conflicts with already-booked slots

## Doctor: Update profile
**Title:**
_As a doctor, I want to update my profile with specialization and contact information, so that patients have up-to-date information._

**Acceptance Criteria:**
1. Doctor can edit specialization and contact details
2. Changes are validated and saved
3. Updated info is visible on the public doctor list

**Priority:** Medium
**Story Points:** 3
**Notes:**
- Validate phone and email formats

## Doctor: View patient details for upcoming appointments
**Title:**
_As a doctor, I want to view patient details for upcoming appointments, so that I can be prepared._

**Acceptance Criteria:**
1. Doctor can open an appointment to see patient details
2. Only the doctor's own patients are visible
3. Details load without errors

**Priority:** Medium
**Story Points:** 3
**Notes:**
- Limit data to what is necessary (privacy)