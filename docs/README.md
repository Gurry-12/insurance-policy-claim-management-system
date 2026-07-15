# 📚 Insurance Policy Claim Management System — Developer Knowledge Base

> **Version:** 0.0.1-SNAPSHOT | **Spring Boot:** 4.0.6 | **Java:** 17 | **Database:** MySQL

---

## 🎯 Purpose of This Documentation

This knowledge base is designed to help:

- **New developers** understand the backend without reading every source file
- **Feature developers** safely implement new functionality
- **Bug fixers** quickly identify root causes and affected components
- **Code reviewers** understand WHY decisions were made, not just WHAT was coded

---

## 🗂️ Documentation Index

| Section | File | Description |
|---|---|---|
| **Architecture** | [architecture/overview.md](architecture/overview.md) | System architecture, layers, tech stack |
| **Authentication** | [authentication/auth-flow.md](authentication/auth-flow.md) | Registration, Login, OTP, Password Reset |
| **Security** | [security/security-overview.md](security/security-overview.md) | JWT, Filters, RBAC, Ownership Rules |
| **Entities** | [entities/entities-overview.md](entities/entities-overview.md) | All JPA entities, relationships, lifecycle |
| **Controllers** | [controllers/controllers-overview.md](controllers/controllers-overview.md) | All REST endpoints with role restrictions |
| **Services** | [services/services-overview.md](services/services-overview.md) | Business logic, rules, decisions |
| **Repositories** | [repositories/repositories-overview.md](repositories/repositories-overview.md) | Data access layer, custom queries |
| **DTOs** | [dto/dto-overview.md](dto/dto-overview.md) | Request/Response DTOs, validation, mapping |
| **Database** | [database/database-overview.md](database/database-overview.md) | Tables, relationships, constraints |
| **Workflows** | [workflows/workflows-overview.md](workflows/workflows-overview.md) | Step-by-step business process flows |
| **Utilities** | [utilities/utilities-overview.md](utilities/utilities-overview.md) | Generators, validators, constants |
| **Exceptions** | [exceptions/exception-handling.md](exceptions/exception-handling.md) | Custom exceptions, global handler |
| **Enums** | [enums/enums-overview.md](enums/enums-overview.md) | All enums and their business meanings |
| **API Reference** | [api/api-reference.md](api/api-reference.md) | Complete API endpoint reference |
| **Deployment** | [deployment/deployment-guide.md](deployment/deployment-guide.md) | Environment setup, configuration |
| **Testing** | [testing/testing-guide.md](testing/testing-guide.md) | Testing strategy and approach |
| **Best Practices** | [best-practices/best-practices.md](best-practices/best-practices.md) | Naming conventions, patterns |
| **Developer Notes** | [developer-notes/developer-notes.md](developer-notes/developer-notes.md) | How to safely modify each module |

---

## 🏗️ System Overview

```
Insurance Policy Claim Management System
│
├── User Management (ADMIN, INTERNAL_STAFF, CUSTOMER)
│   ├── Self-Registration (CUSTOMER only)
│   ├── OTP Email + SMS Verification
│   └── JWT-based stateless authentication
│
├── Product & Plan Management (ADMIN only)
│   ├── Insurance Products (HEALTH, LIFE, VEHICLE, etc.)
│   └── Policy Plans (coverage, premium, duration, T&C)
│
├── Policy Management
│   ├── Customer Self-Purchase → PENDING_PAYMENT
│   ├── Staff/Admin Issue → PENDING_PAYMENT
│   └── Payment → ACTIVE
│
├── Premium Payment
│   ├── ONE_TIME or ANNUAL payment types
│   ├── Payment window enforcement (15 days early)
│   └── Policy auto-activation on first SUCCESS payment
│
└── Claims Management
    ├── Customer Raises Claim (with documents)
    ├── Staff Reviews → RECOMMENDED_FOR_APPROVAL / REJECTION
    └── Admin Final Decision → APPROVED / REJECTED
```

---

## 👥 User Roles

| Role | Database Value | Description |
|---|---|---|
| `ROLE_CUSTOMER` | `ROLE_CUSTOMER` | Self-registered customer who owns policies |
| `ROLE_INTERNAL_STAFF` | `ROLE_INTERNAL_STAFF` | Insurance staff who review claims, issue policies |
| `ROLE_ADMIN` | `ROLE_ADMIN` | System administrator with full access |

> **Important:** Staff are created by Admin only, never self-registered. Staff have a `ProductSpeciality` (e.g., HEALTH, LIFE) that limits their scope.

---

## 🚀 Quick Start for New Developers

1. Read [architecture/overview.md](architecture/overview.md) first
2. Read [security/security-overview.md](security/security-overview.md) to understand access control
3. Read the module documentation matching your feature area
4. Read [developer-notes/developer-notes.md](developer-notes/developer-notes.md) before touching any existing code
5. Read [best-practices/best-practices.md](best-practices/best-practices.md) to align with project conventions

---

## 📦 Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 4.0.6 | Application framework |
| Spring Security | (Boot-managed) | Authentication & Authorization |
| Spring Data JPA | (Boot-managed) | Database ORM |
| Hibernate | (Boot-managed) | JPA provider |
| MySQL | 8.x | Relational database |
| JJWT | 0.12.6 | JWT token generation & validation |
| Lombok | Latest | Boilerplate code reduction |
| ModelMapper | 3.2.0 | Entity ↔ DTO mapping |
| Cloudinary | 1.39.0 | Document/image cloud storage |
| Twilio | 11.0.0 | SMS OTP delivery |
| SpringDoc OpenAPI | 3.0.2 | Swagger UI / API documentation |
| Jakarta Validation | (Boot-managed) | Bean validation |
| Spring Mail | (Boot-managed) | Email OTP delivery |

---

## 📁 Package Structure

```
com.insurance.demo/
├── DemoApplication.java          ← Spring Boot entry point
├── config/                       ← Security, Cloudinary, CORS, OpenAPI, DataInit
├── controller/                   ← REST controllers (9 controllers)
├── dto/
│   ├── request/                  ← 18 request DTOs
│   └── response/                 ← 16 response DTOs
├── enums/                        ← 8 domain enums
├── exception/                    ← Custom exceptions + GlobalExceptionHandler
├── model/                        ← 11 JPA entities
├── repository/                   ← 11 Spring Data repositories
├── security/                     ← JWT service, filter, user details service
├── service/                      ← 10 service interfaces
├── serviceimpl/                  ← 10 service implementations
├── util/                         ← Generators, validators, MessageConstants
└── verification/                 ← Email, SMS, OTP services
```

---

## ⚠️ Critical Business Rules (Read Before Coding)

1. **Customer profile must be complete** before purchasing any policy
2. **Health insurance**: Only one active or pending policy per customer per plan
3. **Claim coverage**: Total active claims cannot exceed the plan's `coverageAmount`
4. **Annual payment**: 15-day early payment window before each anniversary
5. **Staff speciality**: Staff can ONLY work on claims matching their `productSpeciality`
6. **Claim cannot be cancelled** while it has open (SUBMITTED/UNDER_REVIEW) claims
7. **Passwords are BCrypt-hashed** before storage
8. **JWT tokens embed**: email (subject), roles, fullName, productSpeciality
