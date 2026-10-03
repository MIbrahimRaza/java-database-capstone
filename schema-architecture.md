# Schema Architecture

## Section 1: Architecture Summary

This Spring Boot application follows a three-tier architecture (presentation, application, and data) and uses both MVC and REST controllers. Thymeleaf templates render the Admin and Doctor dashboards on the server, while REST APIs serve JSON to the other modules, such as Appointments, Patient Dashboard, and Patient Records.

All controllers, whether Thymeleaf or REST, route requests through a common service layer. This layer applies business rules and validations, then delegates to the appropriate repositories. The application uses two databases: MySQL stores structured data (patients, doctors, appointments, and admin) through Spring Data JPA entities, and MongoDB stores flexible, document-based prescription data through Spring Data MongoDB document models.

## Section 2: Numbered Flow of Data and Control

1. The user accesses the AdminDashboard or DoctorDashboard (Thymeleaf pages), or a REST client such as the Appointments or Patient Dashboard module.
2. The request is routed by URL path and HTTP method to the appropriate Thymeleaf Controller (for HTML views) or REST Controller (for JSON APIs).
3. The controller calls the service layer, which applies business rules and validations (for example, checking doctor availability before booking an appointment).
4. The service layer delegates data access to the appropriate repository: MySQL repositories (Spring Data JPA) for patients, doctors, appointments, and admin, or the MongoDB repository for prescriptions.
5. The repository accesses its underlying database: MySQL for structured relational data, MongoDB for flexible document data.
6. The retrieved data is bound to Java model classes: JPA entities (`@Entity`) for MySQL and document objects (`@Document`) for MongoDB.
7. The bound models are returned through the service and controller layers. In MVC flows they are passed to Thymeleaf templates and rendered as HTML; in REST flows they are serialized (as models or DTOs) into JSON and sent back as the HTTP response.