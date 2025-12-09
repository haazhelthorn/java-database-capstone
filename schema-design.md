Clinic Management System Database Design
## MySQL Database Design

### Table: patients
id: INT, Primary Key, AUTO_INCREMENT

first_name: VARCHAR(50), NOT NULL

last_name: VARCHAR(50), NOT NULL

email: VARCHAR(100), NOT NULL, UNIQUE

phone: VARCHAR(20), NOT NULL

date_of_birth: DATE, NOT NULL

address: TEXT

emergency_contact: VARCHAR(20)

blood_type: ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-')

created_at: DATETIME, DEFAULT CURRENT_TIMESTAMP

updated_at: DATETIME, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

Justification: Patients are soft-deleted (using a status flag instead of actual deletion) to preserve historical data. Email is unique to prevent duplicate accounts. Blood type is optional but useful for medical emergencies.

### Table: doctors
id: INT, Primary Key, AUTO_INCREMENT

first_name: VARCHAR(50), NOT NULL

last_name: VARCHAR(50), NOT NULL

email: VARCHAR(100), NOT NULL, UNIQUE

phone: VARCHAR(20), NOT NULL

specialization: VARCHAR(100), NOT NULL

license_number: VARCHAR(50), NOT NULL, UNIQUE

clinic_location_id: INT, Foreign Key → clinic_locations(id)

is_active: BOOLEAN, DEFAULT TRUE

created_at: DATETIME, DEFAULT CURRENT_TIMESTAMP

Justification: Doctors are not deleted but marked inactive when they leave. License number is unique and required for verification. Clinic location association allows doctors to work at multiple locations.

### Table: appointments
id: INT, Primary Key, AUTO_INCREMENT

doctor_id: INT, Foreign Key → doctors(id), NOT NULL

patient_id: INT, Foreign Key → patients(id), NOT NULL

appointment_time: DATETIME, NOT NULL

duration_minutes: INT, DEFAULT 30

status: ENUM('scheduled', 'confirmed', 'in_progress', 'completed', 'cancelled', 'no_show'), DEFAULT 'scheduled'

reason: TEXT

clinic_location_id: INT, Foreign Key → clinic_locations(id)

created_at: DATETIME, DEFAULT CURRENT_TIMESTAMP

updated_at: DATETIME, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

Justification: Appointment history is preserved forever for medical records. Overlapping appointments for the same doctor are prevented at application level, not database level (requires checking business logic). Foreign keys have RESTRICT constraints to prevent deletion of doctors/patients with appointments.

### Table: admin
id: INT, Primary Key, AUTO_INCREMENT

username: VARCHAR(50), NOT NULL, UNIQUE

email: VARCHAR(100), NOT NULL, UNIQUE

password_hash: VARCHAR(255), NOT NULL

role: ENUM('super_admin', 'clinic_manager', 'receptionist'), NOT NULL

clinic_location_id: INT, Foreign Key → clinic_locations(id)

last_login: DATETIME

is_active: BOOLEAN, DEFAULT TRUE

created_at: DATETIME, DEFAULT CURRENT_TIMESTAMP

Justification: Admin users are separated from patients/doctors for security. Passwords are hashed (not plaintext). Role-based access control implemented via enum.

### Table: clinic_locations
id: INT, Primary Key, AUTO_INCREMENT

name: VARCHAR(100), NOT NULL

address: TEXT, NOT NULL

phone: VARCHAR(20), NOT NULL

email: VARCHAR(100)

opening_hours: TEXT

is_active: BOOLEAN, DEFAULT TRUE

created_at: DATETIME, DEFAULT CURRENT_TIMESTAMP

Justification: Clinic locations are stored separately to support multi-location clinics. Opening hours stored as text for flexibility (e.g., "Mon-Fri: 9AM-6PM, Sat: 9AM-1PM").

### Table: payments
id: INT, Primary Key, AUTO_INCREMENT

appointment_id: INT, Foreign Key → appointments(id), NOT NULL

patient_id: INT, Foreign Key → patients(id), NOT NULL

amount: DECIMAL(10,2), NOT NULL

payment_method: ENUM('cash', 'credit_card', 'debit_card', 'insurance', 'online'), NOT NULL

status: ENUM('pending', 'completed', 'failed', 'refunded'), DEFAULT 'pending'

transaction_id: VARCHAR(100)

insurance_provider: VARCHAR(100)

insurance_policy_number: VARCHAR(50)

paid_at: DATETIME

created_at: DATETIME, DEFAULT CURRENT_TIMESTAMP

Justification: Payment records are preserved for accounting and audit purposes. Insurance information is stored but optional. Transaction ID for digital payment tracking.


## MongoDB Collection Design

### Collection: prescriptions

{
  "_id": ObjectId("64abc123456def7890123456"),
  "appointmentId": 105,
  "patientId": 42,
  "doctorId": 15,
  "issueDate": ISODate("2023-10-15T14:30:00Z"),
  "medications": [
    {
      "name": "Amoxicillin",
      "dosage": "500mg",
      "frequency": "Every 8 hours",
      "duration": "7 days",
      "instructions": "Take after meals",
      "refillsAllowed": 1,
      "refillsUsed": 0
    },
    {
      "name": "Ibuprofen",
      "dosage": "200mg",
      "frequency": "As needed for pain",
      "instructions": "Do not exceed 6 tablets in 24 hours"
    }
  ],
  "doctorNotes": "Patient presented with bacterial sinus infection. Advised to complete full course of antibiotics even if symptoms improve earlier.",
  "patientAllergies": ["Penicillin"],
  "diagnosisCodes": ["J01.90"],
  "attachments": [
    {
      "type": "lab_result",
      "filename": "blood_test_101523.pdf",
      "uploadDate": ISODate("2023-10-15T14:45:00Z"),
      "uploadedBy": "dr_smith"
    }
  ],
  "followUp": {
    "required": true,
    "recommendedDate": ISODate("2023-10-22T00:00:00Z"),
    "reason": "Monitor antibiotic effectiveness"
  },
  "metadata": {
    "createdBy": "dr_smith",
    "createdAt": ISODate("2023-10-15T14:35:00Z"),
    "updatedAt": ISODate("2023-10-15T14:35:00Z"),
    "version": 1
  }
}