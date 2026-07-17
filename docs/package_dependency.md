# Package Dependency Matrix

This document outlines structural dependencies and package coupling limits within the architecture.

---

## 1. Package Reference Map

In order to maintain strict separation of concerns, dependencies should flow in one direction: Controllers invoke Services, Services invoke Repositories, and Repositories access Models.

```mermaid
graph TD
    classDef config fill:#fff9c4,stroke:#fbc02d,stroke-width:2px;
    classDef layer fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px;
    classDef util fill:#efebe9,stroke:#5d4037,stroke-width:2px;

    Config[com.insurance.demo.config]:::config
    Security[com.insurance.demo.security]:::config
    Verification[com.insurance.demo.verification]:::config
    
    Controller[com.insurance.demo.controller]:::layer
    Service[com.insurance.demo.service]:::layer
    ServiceImpl[com.insurance.demo.serviceimpl]:::layer
    Repository[com.insurance.demo.repository]:::layer
    Model[com.insurance.demo.model]:::layer
    Dto[com.insurance.demo.dto]:::layer
    
    Exception[com.insurance.demo.exception]:::util
    Util[com.insurance.demo.util]:::util
    Enums[com.insurance.demo.enums]:::util

    %% Configuration dependencies
    Config --> Security
    Config --> Controller
    Security --> Service
    Verification --> Repository

    %% Clean Architecture flows
    Controller --> Dto
    Controller --> Service
    
    Service --> Dto
    ServiceImpl --> Service
    ServiceImpl --> Repository
    ServiceImpl --> Model
    ServiceImpl --> Verification
    ServiceImpl --> Security
    
    Repository --> Model
    
    %% Cross-cutting utilities
    Controller -.-> Exception
    ServiceImpl -.-> Exception
    Model -.-> Enums
    Dto -.-> Enums
    ServiceImpl -.-> Util
```

---

## 2. Layer Separation Constraints

To preserve backend architecture integrity, follow these coding guidelines:

1.  **No Direct Repository Injection into Controllers**:
    *   *Constraint*: Controllers must never access `com.insurance.demo.repository` classes directly. All DB fetches must pass through the service layer interface.
2.  **No Entity Serialization**:
    *   *Constraint*: Controllers should never return model objects (`com.insurance.demo.model`) in their method signatures. All payloads should map to `com.insurance.demo.dto.response` classes first.
3.  **Strict Speciality Isolation**:
    *   *Constraint*: Claim and Policy operations must verify the staff member's speciality (`com.insurance.demo.enums.ProductType`) before editing or returning query listings.
4.  **Decoupled Verification Dispatch**:
    *   *Constraint*: OTP generation and verification logics inside `com.insurance.demo.verification` depend on JPA models (`AppUser` and `OtpVerification`) but must remain isolated from direct policy or claim evaluation loops.
5.  **Centralized String References**:
    *   *Constraint*: Avoid magic strings. Reference all messages and field keys through `MessageConstants` inside the utility package.
