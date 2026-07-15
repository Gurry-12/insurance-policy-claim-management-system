# End-to-End Enterprise API Testing Scenario

This document contains a complete, chronological sequence of API requests to simulate a full real-world business scenario for the Insurance Policy Claim Management System.

You can copy and paste these payloads directly into Postman or Thunder Client to populate your database and test every workflow.

---

## Glossary & Global Variables
For Postman, set these variables in your environment:
- `{{baseUrl}}`: `http://localhost:8081` (or your backend URL)
- `{{adminToken}}`: Extracted from Admin Login
- `{{staffToken_health}}`, `{{staffToken_vehicle}}`, `{{staffToken_life}}`: Extracted from Staff Logins
- `{{customerToken_1}}` ... `{{customerToken_10}}`: Extracted from Customer Logins
- All passwords are `password123`, which is base64 encoded as `cGFzc3dvcmQxMjM=`

---

# Phase 1: Administration & Staff Setup

### 1.1 Admin Login
**Endpoint:** `POST {{baseUrl}}/api/auth/login`  
**Role:** PUBLIC  
**Headers:** `Content-Type: application/json`  
**Business Context:** Authenticate the master admin to set up the system.
```json
{
  "email": "admin@example.com",
  "password": "cGFzc3dvcmQxMjM="
}
```
*Expected action: Save the `data.token` as `{{adminToken}}`.*

### 1.2 Create Staff 1 (Health Specialist)
**Endpoint:** `POST {{baseUrl}}/api/users/staff`  
**Role:** ADMIN (Requires `Authorization: Bearer {{adminToken}}`)  
**Business Context:** Internal staff handle claims and issue policies for their specific product domains.
```json
{
  "fullName": "Dr. Sarah Health",
  "email": "sarah.health@insurance.com",
  "password": "cGFzc3dvcmQxMjM=",
  "mobileNumber": "+919876543201",
  "productSpeciality": "HEALTH"
}
```

### 1.3 Create Staff 2 (Vehicle Specialist)
**Endpoint:** `POST {{baseUrl}}/api/users/staff`  
**Role:** ADMIN
```json
{
  "fullName": "Mike Auto",
  "email": "mike.auto@insurance.com",
  "password": "cGFzc3dvcmQxMjM=",
  "mobileNumber": "+919876543202",
  "productSpeciality": "VEHICLE"
}
```

### 1.4 Create Staff 3 (Life Specialist)
**Endpoint:** `POST {{baseUrl}}/api/users/staff`  
**Role:** ADMIN
```json
{
  "fullName": "Emma Life",
  "email": "emma.life@insurance.com",
  "password": "cGFzc3dvcmQxMjM=",
  "mobileNumber": "+919876543203",
  "productSpeciality": "LIFE"
}
```

*(Note: In a real flow, Staff must verify OTPs to log in. For testing, assuming staff accounts are verified or you check DB for OTP).*

---

# Phase 2: Product Catalog & Policy Plans Setup

*All requests in Phase 2 require `Authorization: Bearer {{adminToken}}`.*

### 2.1 Create Insurance Products (10 Products)
**Endpoint:** `POST {{baseUrl}}/api/products`

**Product 1: Comprehensive Health**
```json
{
  "productName": "Comprehensive Health",
  "productType": "HEALTH",
  "description": "Full medical coverage including OPD and hospitalization.",
  "isActive": true
}
```
**Product 2: Senior Citizen Health**
```json
{
  "productName": "Senior Citizen Health",
  "productType": "HEALTH",
  "description": "Specialized health insurance for citizens over 60.",
  "isActive": true
}
```
**Product 3: Family Floater Health**
```json
{
  "productName": "Family Floater Health",
  "productType": "HEALTH",
  "description": "One policy covers the entire family.",
  "isActive": true
}
```
**Product 4: Term Life Insurance**
```json
{
  "productName": "Pure Term Life",
  "productType": "LIFE",
  "description": "High coverage life insurance at low premiums.",
  "isActive": true
}
```
**Product 5: Whole Life Insurance**
```json
{
  "productName": "Whole Life Plus",
  "productType": "LIFE",
  "description": "Life coverage up to 99 years with wealth accumulation.",
  "isActive": true
}
```
**Product 6: Private Car Insurance**
```json
{
  "productName": "Private Car Shield",
  "productType": "VEHICLE",
  "description": "Comprehensive bumper-to-bumper car insurance.",
  "isActive": true
}
```
**Product 7: Two-Wheeler Insurance**
```json
{
  "productName": "Bike Protect",
  "productType": "VEHICLE",
  "description": "Affordable bike insurance with zero dep.",
  "isActive": true
}
```
**Product 8: Commercial Vehicle Insurance**
```json
{
  "productName": "Truck & Fleet Guard",
  "productType": "VEHICLE",
  "description": "Coverage for commercial transport vehicles.",
  "isActive": true
}
```
**Product 9: Home Property Insurance**
```json
{
  "productName": "Home Secure",
  "productType": "PROPERTY",
  "description": "Protect your home against fire, theft, and natural disasters.",
  "isActive": true
}
```
**Product 10: International Travel Insurance**
```json
{
  "productName": "Global Voyager",
  "productType": "TRAVEL",
  "description": "Medical and baggage coverage for international travel.",
  "isActive": true
}
```
*Expected action: Note the IDs of these products for the next step.*

### 2.2 Create Policy Plans (Examples)
**Endpoint:** `POST {{baseUrl}}/api/plans`

**Plan 1.1: Health Basic (For Product 1)**
```json
{
  "productId": 1,
  "planName": "Health Basic 5L",
  "coverageAmount": 500000,
  "premiumAmount": 8000,
  "premiumType": "ANNUAL",
  "duration": 5,
  "termsAndConditions": "Room rent capped at 1% of sum insured. Pre-existing diseases covered after 3 years.",
  "isActive": true
}
```
**Plan 1.2: Health Premium (For Product 1)**
```json
{
  "productId": 1,
  "planName": "Health Premium 10L",
  "coverageAmount": 1000000,
  "premiumAmount": 14000,
  "premiumType": "ANNUAL",
  "duration": 5,
  "termsAndConditions": "No room rent cap. Day care procedures covered.",
  "isActive": true
}
```
**Plan 4.1: Term Life One-Time (For Product 4)**
```json
{
  "productId": 4,
  "planName": "Term Life 1Cr (Single Premium)",
  "coverageAmount": 10000000,
  "premiumAmount": 250000,
  "premiumType": "ONE_TIME",
  "duration": 20,
  "termsAndConditions": "Suicide clause applicable for first year.",
  "isActive": true
}
```
**Plan 6.1: Car Annual (For Product 6)**
```json
{
  "productId": 6,
  "planName": "Car Comprehensive 1Yr",
  "coverageAmount": 600000,
  "premiumAmount": 12000,
  "premiumType": "ONE_TIME",
  "duration": 1,
  "termsAndConditions": "Zero depreciation included. Engine protect add-on available.",
  "isActive": true
}
```

*(Create ~30 plans total by varying `coverageAmount`, `premiumAmount`, `duration`, and `premiumType` for each product ID).*

---

# Phase 3: Customer Onboarding

### 3.1 Register Customers (x10)
**Endpoint:** `POST {{baseUrl}}/api/auth/register`  
**Role:** PUBLIC

**Customer 1:**
```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "password": "cGFzc3dvcmQxMjM=",
  "mobileNumber": "+918000000001"
}
```
**Customer 2:**
```json
{
  "fullName": "Jane Smith",
  "email": "jane.smith@example.com",
  "password": "cGFzc3dvcmQxMjM=",
  "mobileNumber": "+918000000002"
}
```
*(Repeat for 8 more customers).*

### 3.2 Verify OTP for Customers
*(Note: Check your `otp_verifications` table in the DB for the generated OTPs).*
**Endpoint:** `POST {{baseUrl}}/api/auth/verify-otp`  
**Role:** PUBLIC
```json
{
  "email": "john.doe@example.com",
  "emailOtp": "123456",
  "phoneOtp": "123456"
}
```

### 3.3 Customer Login
**Endpoint:** `POST {{baseUrl}}/api/auth/login`
```json
{
  "email": "john.doe@example.com",
  "password": "cGFzc3dvcmQxMjM="
}
```
*Save `data.token` as `{{customerToken_1}}`.*

### 3.4 Create Customer Profile
**Endpoint:** `POST {{baseUrl}}/api/customers/profile`  
**Role:** CUSTOMER (`Authorization: Bearer {{customerToken_1}}`)  
**Business Context:** Profile must be complete before purchasing a policy.
```json
{
  "dateOfBirth": "1990-05-15",
  "address": "123 Maple Street",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pinCode": "400001",
  "nomineeName": "Mary Doe",
  "nomineeRelation": "Spouse"
}
```

---

# Phase 4: Policy Operations

### 4.1 Purchase Policy (Self-Service)
**Endpoint:** `POST {{baseUrl}}/api/policies/purchase`  
**Role:** CUSTOMER (`{{customerToken_1}}`)  
**Business Context:** Customer buys a health plan. It starts in `PENDING_PAYMENT`.
```json
{
  "planId": 1,
  "startDate": "2024-08-01"
}
```
*Expected: Returns Policy ID (e.g., ID: 1, policyNumber: POL-12345).*

### 4.2 Issue Policy (Admin on behalf of Customer)
**Endpoint:** `POST {{baseUrl}}/api/policies/issue`  
**Role:** ADMIN (`{{adminToken}}`)  
**Business Context:** Admin issues a Vehicle policy to Customer 2.
```json
{
  "customerId": 2,
  "planId": 4,
  "startDate": "2024-08-05"
}
```

### 4.3 Make Premium Payment (Activate Policy)
**Endpoint:** `POST {{baseUrl}}/api/payments`  
**Role:** CUSTOMER (`{{customerToken_1}}`)  
**Business Context:** Policy 1 is `PENDING_PAYMENT`. Customer pays exact premium amount (e.g., 8000) to activate it.
```json
{
  "policyId": 1,
  "amount": 8000,
  "paymentMode": "UPI",
  "paymentStatus": "SUCCESS"
}
```
*Expected: Policy status changes to `ACTIVE`.*

### 4.4 Make Premium Payment (Failed Transaction)
**Endpoint:** `POST {{baseUrl}}/api/payments`
```json
{
  "policyId": 2,
  "amount": 12000,
  "paymentMode": "CARD",
  "paymentStatus": "FAILED"
}
```
*Expected: Payment is recorded as FAILED, policy remains PENDING_PAYMENT.*

---

# Phase 5: The Claims Workflow

### 5.1 Customer Raises a Claim
**Endpoint:** `POST {{baseUrl}}/api/claims/raise`  
**Role:** CUSTOMER (`{{customerToken_1}}`)  
**Headers:** `Content-Type: multipart/form-data`  
**Form Data:**
- `claim` (Text/JSON):
```json
{
  "policyId": 1,
  "claimAmount": 45000,
  "claimReason": "Hospitalization due to Dengue fever",
  "incidentDate": "2024-08-10"
}
```
- `files` (File): *Attach a dummy PDF or Image.*
*Expected: Claim created in `SUBMITTED` status.*

### 5.2 Staff Assigns Claim to Self
**Endpoint:** `PATCH {{baseUrl}}/api/claims/1/assign`  
**Role:** INTERNAL_STAFF (`{{staffToken_health}}`)  
**Headers:** No Body
*Expected: `assignedStaff` set to current staff member.*

### 5.3 Staff Moves Claim to Under Review
**Endpoint:** `PATCH {{baseUrl}}/api/claims/1/under-review`  
**Role:** INTERNAL_STAFF (`{{staffToken_health}}`)  
**Headers:** No Body
*Expected: Status changes to `UNDER_REVIEW`.*

### 5.4 Staff Recommends Approval
**Endpoint:** `PATCH {{baseUrl}}/api/claims/1/review`  
**Role:** INTERNAL_STAFF (`{{staffToken_health}}`)  
**Body:**
```json
{
  "recommendedStatus": "RECOMMENDED_FOR_APPROVAL",
  "remarks": "Documents verified. Hospitalization bills are genuine."
}
```

### 5.5 Admin Final Decision (Approval)
**Endpoint:** `PATCH {{baseUrl}}/api/claims/1/final-decision`  
**Role:** ADMIN (`{{adminToken}}`)  
**Body:**
```json
{
  "recommendedStatus": "APPROVED",
  "remarks": "Final approval granted based on staff verification."
}
```

### 5.6 Variation: Staff Recommends Rejection
**Endpoint:** `PATCH {{baseUrl}}/api/claims/2/review`  
**Role:** INTERNAL_STAFF
```json
{
  "recommendedStatus": "RECOMMENDED_FOR_REJECTION",
  "remarks": "Incident date falls outside the active policy period. Suspected fraud."
}
```

### 5.7 Variation: Admin Final Decision (Rejection)
**Endpoint:** `PATCH {{baseUrl}}/api/claims/2/final-decision`  
**Role:** ADMIN
```json
{
  "recommendedStatus": "REJECTED",
  "remarks": "Rejected as per policy terms."
}
```

---

# Phase 6: Pagination, Search, and History

### 6.1 View Claim History
**Endpoint:** `GET {{baseUrl}}/api/claims/1/history`  
**Role:** CUSTOMER / STAFF / ADMIN  
*Expected: Returns a list of `ClaimStatusHistory` objects showing the journey from SUBMITTED -> UNDER_REVIEW -> REC_APPROVAL -> APPROVED.*

### 6.2 Paginated Customers Search
**Endpoint:** `GET {{baseUrl}}/api/customers?pageNumber=0&pageSize=10&sortBy=fullName&sortDirection=asc&fullName=John`  
**Role:** ADMIN / STAFF

### 6.3 Admin Views All Policies (Filtered)
**Endpoint:** `GET {{baseUrl}}/api/policies?policyStatus=ACTIVE&pageNumber=0&pageSize=5`  
**Role:** ADMIN

### 6.4 Customer Views Own Policies
**Endpoint:** `GET {{baseUrl}}/api/policies/my-policies`  
**Role:** CUSTOMER

---

# Phase 7: Administrative Maintenance

### 7.1 Deactivate an Insurance Product
**Endpoint:** `PATCH {{baseUrl}}/api/products/3/deactivate`  
**Role:** ADMIN  
**Business Context:** Stops new plans from being added to this product. Existing policies are unaffected.

### 7.2 Deactivate a Policy Plan
**Endpoint:** `PATCH {{baseUrl}}/api/plans/5/deactivate`  
**Role:** ADMIN  
**Business Context:** Stops customers from purchasing this specific plan.

### 7.3 Cancel a Policy
**Endpoint:** `PATCH {{baseUrl}}/api/policies/3/cancel`  
**Role:** ADMIN / STAFF  
**Business Context:** Cancels an active policy. Fails if there are open (unresolved) claims.
```json
{} // No body required
```

### 7.4 Deactivate a Staff/User Account
**Endpoint:** `PATCH {{baseUrl}}/api/users/2/status`  
**Role:** ADMIN
```json
{
  "isActive": false
}
```
*Expected: Staff user can no longer log in or process claims.*

---
*End of End-to-End Scenario.*
