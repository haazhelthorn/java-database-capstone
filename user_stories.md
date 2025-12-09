## Admin User Stories
Title:
As an admin, I want to log into the portal with my username and password, so that I can manage the platform securely.
Acceptance Criteria:
- Login form accepts valid credentials
- Invalid credentials show error messages
- Successful login redirects to admin dashboard
Priority: High
Story Points: 3
Notes:
- Consider adding CAPTCHA or 2FA for enhanced security

Title:
As an admin, I want to log out of the portal, so that I can protect system access.
Acceptance Criteria:
- Logout button is visible on all admin pages
- Clicking logout ends the session
- User is redirected to login page
Priority: High
Story Points: 2
Notes:
- Ensure session tokens are invalidated

Title:
As an admin, I want to add doctors to the portal, so that they can start managing appointments.
Acceptance Criteria:
- Admin can access doctor registration form
- Form validates required fields
- New doctor appears in the doctor list
Priority: High
Story Points: 5
Notes:
- Include fields for specialization and contact info

Title:
As an admin, I want to delete a doctor's profile from the portal, so that I can manage active users.
Acceptance Criteria:
- Admin can view doctor profiles
- Delete button is available
- Confirmation prompt before deletion
Priority: Medium
Story Points: 4
Notes:
- Consider soft delete for audit purposes

Title:
As an admin, I want to run a stored procedure in MySQL CLI to get the number of appointments per month, so that I can track usage statistics.
Acceptance Criteria:
- Stored procedure exists in MySQL
- Admin can execute it via CLI
- Output shows monthly appointment counts
Priority: Medium
Story Points: 3
Notes:
- Procedure should be optimized for performance

---

## Patient User Stories
Title:
As a patient, I want to view a list of doctors without logging in, so that I can explore options before registering.
Acceptance Criteria:
- Doctor list is publicly accessible
- List includes name, specialization, and availability
- Search and filter options are available
Priority: High
Story Points: 4
Notes:
- Consider caching for performance

Title:
As a patient, I want to sign up using my email and password, so that I can book appointments.
Acceptance Criteria:
- Registration form validates inputs
- Email verification is sent
- Successful signup redirects to dashboard
Priority: High
Story Points: 3
Notes:
- Password strength validation recommended

Title:
As a patient, I want to log into the portal, so that I can manage my bookings.
Acceptance Criteria:
- Login form accepts valid credentials
- Invalid credentials show error messages
- Successful login redirects to patient dashboard
Priority: High
Story Points: 2
Notes:
- Session timeout should be configurable

Title:
As a patient, I want to log out of the portal, so that I can secure my account.
Acceptance Criteria:
- Logout button is visible
- Session ends on logout
- Redirect to homepage or login
Priority: Medium
Story Points: 2
Notes:
- Ensure tokens are cleared

Title:
As a patient, I want to book an hour-long appointment with a doctor, so that I can consult with them.
Acceptance Criteria:
- Booking form shows available slots
- Patient selects date and time
- Confirmation is sent after booking
Priority: High
Story Points: 5
Notes:
- Prevent double-booking

Title:
As a patient, I want to view my upcoming appointments, so that I can prepare accordingly.
Acceptance Criteria:
- Dashboard shows upcoming appointments
- Details include doctor name, time, and location
- Option to cancel or reschedule
Priority: Medium
Story Points: 3
Notes:
- Include reminders via email or SMS

---

## Doctor User Stories
Title:
As a doctor, I want to log into the portal, so that I can manage my appointments.
Acceptance Criteria:
- Login form accepts valid credentials
- Successful login redirects to doctor dashboard
- Invalid credentials show error messages
Priority: High
Story Points: 2
Notes:
- Consider role-based access control

Title:
As a doctor, I want to log out of the portal, so that I can protect my data.
Acceptance Criteria:
- Logout button is visible
- Session ends on logout
- Redirect to login page
Priority: Medium
Story Points: 2
Notes:
- Ensure session tokens are invalidated

Title:
As a doctor, I want to view my appointment calendar, so that I can stay organized.
Acceptance Criteria:
- Calendar shows daily/weekly appointments
- Includes patient names and times
- Option to filter by date
Priority: High
Story Points: 4
Notes:
- Sync with external calendars if possible

Title:
As a doctor, I want to mark my unavailability, so that patients only see available slots.
Acceptance Criteria:
- Doctor can block time slots
- Blocked slots are hidden from booking view
- Confirmation of changes is shown
Priority: High
Story Points: 5
Notes:
- Include recurring unavailability options

Title:
As a doctor, I want to update my profile with specialization and contact information, so that patients have up-to-date information.
Acceptance Criteria:
- Editable profile form
- Fields include specialization, phone, email
- Changes are saved and reflected immediately
Priority: Medium
Story Points: 3
Notes:
- Validate contact info format

Title:
As a doctor, I want to view patient details for upcoming appointments, so that I can be prepared.
Acceptance Criteria:
- Appointment view includes patient name and medical history
- Accessible only to assigned doctor
- Data is read-only
Priority: High
Story Points: 4
Notes:
- Ensure data privacy and access control
