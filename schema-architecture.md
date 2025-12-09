Section 1: Architecture Summary
The Smart Clinic Management System is a Spring Boot application that combines both MVC and RESTful approaches to serve different user interfaces. Admin and Doctor dashboards are rendered using Thymeleaf templates, providing server-side HTML views. Meanwhile, modules such as Appointments, PatientDashboard, and PatientRecord are exposed via REST APIs, enabling scalable and interoperable client-server communication.
The backend is organized into a layered architecture: controllers handle incoming requests, delegate logic to a centralized service layer, and interact with repositories for data access. The system connects to two databases—MySQL for structured relational data (Patient, Doctor, Appointment, Admin) and MongoDB for flexible, document-based data (Prescription). MySQL entities are modeled using JPA annotations, while MongoDB documents use Spring Data MongoDB annotations. This dual-database setup allows the application to leverage the strengths of both relational and NoSQL paradigms.

Section 2: Numbered Flow of Data and Control
- User Interaction
Users access the system through either Thymeleaf-based dashboards (AdminDashboard, DoctorDashboard) or REST API clients (Appointments, PatientDashboard, PatientRecord).
- Controller Routing
Requests are routed to the appropriate controller: Thymeleaf Controllers for HTML views or REST Controllers for JSON-based API responses.
- Service Layer Invocation
Controllers delegate processing to the Service Layer, which applies business logic, validations, and coordinates workflows across entities.
- Repository Access
The Service Layer interacts with MySQL Repositories (via Spring Data JPA) and MongoDB Repository (via Spring Data MongoDB) to perform data operations.
- Database Communication
Repositories communicate with the underlying databases—MySQL for structured data and MongoDB for document-based data—to retrieve or persist information.
- Model Binding
Retrieved data is mapped into Java model classes: JPA entities for MySQL and @Document-based models for MongoDB, enabling object-oriented manipulation.
- Response Delivery
Models are used to generate responses: Thymeleaf templates render dynamic HTML for MVC flows, while REST flows serialize models into JSON for API clients.