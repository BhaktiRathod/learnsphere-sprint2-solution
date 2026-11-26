# Practice: Configure Service Discovery & API Gateway

## Problem Statement

Enhance the **LearnSphere** microservices architecture by introducing **service discovery** and a centralized **API Gateway**.

In this practice, you will add a **Eureka Server** to maintain a registry of the LearnSphere services and create a **Spring Cloud Gateway** as the single entry point for client requests.

The **Course, Learner, Enrollment, and Notification** services must register with Eureka. The API Gateway must use service discovery to route client requests to the Course, Learner, and Enrollment services.

---

## Task-1: Create the Eureka Server

### Task

Create a dedicated Eureka Server for service discovery.

### Requirements

The Eureka Server must:

* Be implemented as a Spring Boot application.
* Include the required Eureka Server dependency.
* Be configured as a Eureka Server.
* Run on a dedicated port.
* Provide the Eureka dashboard to view registered services.
* Be independently bootable.

---

## Task-2: Register All Services with Eureka

### Task

Configure all LearnSphere services to register themselves with the Eureka Server.

### Requirements

Register the following services with Eureka:

* Course Service
* Learner Service
* Enrollment Service
* Notification Service

Each service must:

* Include the required Eureka client dependency.
* Be configured as a Eureka client.
* Have a unique service name.
* Specify the Eureka Server location.
* Successfully register with Eureka when the service starts.

Verify that all four services appear in the Eureka dashboard.

---

## Task-3: Create the API Gateway

### Task

Create a Spring Cloud Gateway that acts as the single entry point for client requests.

### Requirements

The API Gateway must:

* Be implemented as a Spring Boot application.
* Include the required Spring Cloud Gateway dependency.
* Be configured as a Eureka client.
* Connect to the Eureka Server.
* Run on a dedicated port.
* Use service discovery to locate backend services.

Clients should access the Course, Learner, and Enrollment services through the Gateway rather than directly accessing the individual services.

---

## Task-4: Configure the API Gateway

### Task

Configure Gateway routes for the Course, Learner, and Enrollment services.

### Requirements

Configure routes using appropriate **predicates** and **service-discovery-based URIs**.

| Service            | Gateway Path      | Target Service            |
| ------------------ | ----------------- | ------------------------- |
| Course Service     | `/courses/**`     | `lb://COURSE-SERVICE`     |
| Learner Service    | `/learners/**`    | `lb://LEARNER-SERVICE`    |
| Enrollment Service | `/enrollments/**` | `lb://ENROLLMENT-SERVICE` |

The routes must:

* Match requests using path predicates.
* Use Eureka service discovery to locate the target service.
* Route requests to the corresponding service.
* Not use fixed backend hostnames or IP addresses.

### Backend Endpoint Paths

The Gateway forwards the request path **unchanged** to the target service. Each service must therefore expose its endpoints on the same base path that its Gateway route matches:

| Service            | Controller base path | Required endpoint                                                  |
| ------------------ | -------------------- | ------------------------------------------------------------------ |
| Course Service     | `/courses`           | `GET /courses` returns the list of courses                         |
| Learner Service    | `/learners`          | `GET /learners` returns the list of learners                       |
| Enrollment Service | `/enrollments`       | `GET /enrollments` returns `"Enrollment Service reached"`          |

* If your Sprint 1 controllers are mapped only to `/api/...` paths, add the plain path as well, for example `@RequestMapping({"/courses", "/api/courses"})`. This keeps the Sprint 1 endpoints working.
* The Enrollment Service has only a status endpoint after Sprint 1. Add a `GET /enrollments` endpoint that returns `"Enrollment Service reached"`, and make the Sprint 1 status endpoint also available at `GET /enrollments/status`.
* No Gateway path-rewrite filters are required.

---

## Task-5: Verify the Services Through the Gateway

### Task

Verify that requests sent to the API Gateway are successfully routed to the appropriate services using Eureka service discovery.

### Requirements

Start the following applications:

```text
Eureka Server
Course Service
Learner Service
Enrollment Service
Notification Service
API Gateway
```

Verify the following requests:

| Service    | Request Through Gateway                       | Expected Result                                              |
| ---------- | --------------------------------------------- | ------------------------------------------------------------ |
| Course     | `GET http://localhost:8080/courses`           | Request reaches Course Service and returns the course list   |
| Learner    | `GET http://localhost:8080/learners`          | Request reaches Learner Service and returns the learner list |
| Enrollment | `GET http://localhost:8080/enrollments`       | Request reaches Enrollment Service: `Enrollment Service reached` |
| Enrollment | `GET http://localhost:8080/enrollments/status` | `Enrollment Service is running`                             |

Also verify that:

* Course, Learner, Enrollment, and Notification services appear in the Eureka dashboard.
* Requests sent through the Gateway reach the corresponding services.
* The Gateway discovers backend services through Eureka.

---

## Constraints

* The application must use **Eureka Server** for service discovery.
* All four LearnSphere services must register with Eureka.
* The API Gateway must be implemented using **Spring Cloud Gateway**.
* The API Gateway must register with Eureka as a client.
* Gateway routes must be configured for Course, Learner, and Enrollment services.
* Gateway routes must use path-based predicates.
* Service routing must use Eureka service discovery.
* Fixed backend hostnames or IP addresses must not be used for Gateway routing.
* Each backend service must expose its endpoints on the same base path as its Gateway route (`/courses`, `/learners`, `/enrollments`).
* Course, Learner, Enrollment, and Notification services must remain independently bootable.
* Client requests for Course, Learner, and Enrollment must be verified through the API Gateway.

---

## Project Structure

```text
learnsphere/
├── pom.xml
│
├── discovery-server/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── DiscoveryServerApplication.java
│           └── resources/
│               └── application.properties
│
├── api-gateway/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── ApiGatewayApplication.java
│           └── resources/
│               └── application.properties
│
├── course-service/
│   ├── pom.xml
│   └── src/
│
├── learner-service/
│   ├── pom.xml
│   └── src/
│
├── enrollment-service/
│   ├── pom.xml
│   └── src/
│
├── notification-service/
│   ├── pom.xml
│   └── src/
│
└── README.md
```

---

## Architecture

```text
                         Client
                           │
                           ▼
                    ┌─────────────┐
                    │ API Gateway │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │    Eureka   │
                    │    Server   │
                    └──────┬──────┘
                           │
             ┌─────────────┼─────────────┬─────────────┐
             │             │             │             │
             ▼             ▼             ▼             ▼
          Course        Learner      Enrollment    Notification
          Service       Service        Service       Service
             │             │             │             │
             ▼             ▼             ▼             ▼
        Course DB     Learner DB    Enrollment DB  Notification DB
```

The **API Gateway** is the entry point for client requests, while **Eureka Server** maintains the registry of available services.

---

## Evaluation Criteria

* A Eureka Server is created and configured correctly as the service registry.
* Course, Learner, Enrollment, and Notification services successfully register with Eureka.
* All four services are visible in the Eureka dashboard.
* A Spring Cloud Gateway is created and configured as a Eureka client.
* Gateway routes are configured for Course, Learner, and Enrollment services.
* Gateway routes use appropriate path predicates.
* Gateway routes use Eureka service discovery instead of fixed backend addresses.
* Requests sent through the Gateway successfully reach the Course, Learner, and Enrollment services.
* The complete **Client → API Gateway → Eureka → Service** flow is successfully verified.
* All services and infrastructure components remain independently bootable.
