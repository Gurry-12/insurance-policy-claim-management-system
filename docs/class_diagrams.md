# UML Class Diagrams

This document outlines structural relationships, compositions, inheritances, and dependencies across all layers of the Insurance Policy Claim Management System.

---

## 1. Domain Entities & Database Model Layer

Represents JPA entities, mappings, and relationships mapping to the tables of `insurance_db`.

```mermaid
classDiagram
    direction TB

    class AppUser {
        -Long id
        -String fullName
        -String email
        -String password
        -String mobileNumber
        -Boolean isActive
        -LocalDateTime createdDate
        -LocalDateTime updatedDate
        -Role role
        -Customer customer
        -StaffSpeciality staffSpeciality
        -Boolean emailVerified
        -Boolean phoneVerified
        +onCreate() void
    }

    class Customer {
        -Long id
        -AppUser user
        -LocalDate dateOfBirth
        -String address
        -String city
        -String state
        -String pinCode
        -String nomineeName
        -String nomineeRelation
        -LocalDateTime createdDate
        -LocalDateTime updatedDate
    }

    class StaffSpeciality {
        -Long id
        -AppUser staff
        -ProductType productSpeciality
    }

    class InsuranceProduct {
        -Long id
        -String productName
        -ProductType productType
        -String description
        -Boolean isActive
        -LocalDateTime createdDate
        -LocalDateTime updatedDate
        -List[PolicyPlan] policyPlans
    }

    class PolicyPlan {
        -Long id
        -InsuranceProduct insuranceProduct
        -String planName
        -BigDecimal coverageAmount
        -BigDecimal premiumAmount
        -PremiumType premiumType
        -Integer duration
        -String termsAndConditions
        -Boolean isActive
        -LocalDateTime createdDate
        -LocalDateTime updatedDate
        -List[Policy] policies
    }

    class Policy {
        -Long id
        -String policyNumber
        -Customer customer
        -PolicyPlan policyPlan
        -LocalDate startDate
        -LocalDate endDate
        -PolicyStatus policyStatus
        -BigDecimal totalPremiumPaid
        -LocalDateTime createdDate
        -LocalDateTime updatedDate
        -List[PremiumPayment] payments
        -List[Claim] claims
        -Long version
    }

    class PremiumPayment {
        -Long id
        -Policy policy
        -BigDecimal amount
        -LocalDateTime paymentDate
        -PaymentMode paymentMode
        -String transactionReference
        -PaymentStatus paymentStatus
        -LocalDateTime createdDate
    }

    class Claim {
        -Long id
        -String claimNumber
        -BigDecimal claimAmount
        -String claimReason
        -LocalDateTime incidentDate
        -ClaimStatus claimStatus
        -String staffRemarks
        -String adminRemarks
        -LocalDateTime createdDate
        -LocalDateTime updatedDate
        -Policy policy
        -AppUser assignedStaff
        -List[ClaimDocument] claimDocuments
        -List[ClaimStatusHistory] claimStatusHistories
        -Long version
    }

    class ClaimDocument {
        -Long id
        -String name
        -String documentType
        -String documentReference
        -String publicId
        -LocalDateTime uploadedDate
        -Claim claim
    }

    class ClaimStatusHistory {
        -Long id
        -String previousStatus
        -String newStatus
        -String remarks
        -String updatedBy
        -LocalDateTime updatedDate
        -Claim claim
    }

    class OtpVerification {
        -Long id
        -AppUser user
        -String emailOtp
        -String phoneOtp
        -LocalDateTime expiresAt
        -boolean used
        -int sendCount
        -LocalDateTime lastSentAt
        -LocalDateTime createdAt
        +onCreate() void
    }

    %% Relationships
    AppUser "1" *-- "0..1" Customer : Composition (OneToOne)
    AppUser "1" *-- "0..1" StaffSpeciality : Composition (OneToOne)
    AppUser "1" *-- "0..*" OtpVerification : Aggregation (OneToMany)
    
    Customer "1" *-- "0..*" Policy : Aggregation (OneToMany)
    
    InsuranceProduct "1" *-- "0..*" PolicyPlan : Composition (OneToMany)
    PolicyPlan "1" o-- "0..*" Policy : Association (OneToMany)
    
    Policy "1" *-- "0..*" PremiumPayment : Composition (OneToMany)
    Policy "1" *-- "0..*" Claim : Composition (OneToMany)
    
    Claim "1" *-- "0..*" ClaimDocument : Composition (OneToMany)
    Claim "1" *-- "0..*" ClaimStatusHistory : Composition (OneToMany)
    Claim "0..*" o-- "0..1" AppUser : Assigned Staff (ManyToOne)
```

---

## 2. Controllers & API Services Layer

REST Controllers expose web endpoints and delegate processing logic directly to service layers.

```mermaid
classDiagram
    direction LR
    class AuthController {
        -AuthService authService
        +login(LoginRequestDTO) ResponseEntity
        +registerUser(UserRequestDTO) ResponseEntity
        +verifyOtp(VerifyOtpRequest) ResponseEntity
        +resendOtp(ResendOtpRequestDTO) ResponseEntity
        +forgotPassword(ForgotPasswordRequestDTO) ResponseEntity
        +resetPassword(ResetPasswordRequestDTO) ResponseEntity
    }

    class CustomerController {
        -CustomerService customerService
        +createProfile(CustomerRequestDTO) ResponseEntity
        +updateProfile(Long, CustomerRequestDTO) ResponseEntity
        +getProfile() ResponseEntity
        +getAllCustomers(int, int) ResponseEntity
        +getPaginatedCustomers(int, int, String, String, String) ResponseEntity
        +getCustomerById(Long) ResponseEntity
    }

    class ClaimController {
        -ClaimService claimService
        +raiseClaim(ClaimRequestDTO, List[MultipartFile]) ResponseEntity
        +getMyClaims(int, int, String, String) ResponseEntity
        +getAllClaims(int, int, String, String, String, String) ResponseEntity
        +getClaimById(Long) ResponseEntity
        +getClaimHistory(Long, int, int, String, String, String, String) ResponseEntity
        +underReview(Long) ResponseEntity
        +assignStaff(Long) ResponseEntity
        +reviewClaim(Long, ClaimReviewRequestDTO) ResponseEntity
        +finalDecision(Long, ClaimReviewRequestDTO) ResponseEntity
    }

    class PolicyController {
        -PolicyService policyService
        +purchasePolicy(PolicyPurchaseRequestDTO) ResponseEntity
        +issuePolicy(PolicyIssueRequestDTO) ResponseEntity
        +getMyPolicies(int, int, String, String) ResponseEntity
        +getPoliciesByCustomer(Long, int, int, String, String) ResponseEntity
        +getAllPolicies(int, int, String, String, String, String, String) ResponseEntity
        +getPolicyById(Long) ResponseEntity
        +getClaimsByPolicyId(Long, int, int, String, String) ResponseEntity
        +cancelPolicy(Long) ResponseEntity
    }

    class PremiumPaymentController {
        -PremiumPaymentService paymentService
        +recordPayment(PaymentRequestDTO) ResponseEntity
        +getPaymentsByPolicyId(Long, int, int, String, String) ResponseEntity
        +getPaymentById(Long) ResponseEntity
        +getPaginatedPayments(int, int, String, String, String, String) ResponseEntity
        +getMyPayments(int, int, String, String) ResponseEntity
        +getMyPaymentsByPolicyId(Long, int, int, String, String) ResponseEntity
    }

    class InsuranceProductController {
        -InsuranceProductService productService
        +createProduct(InsuranceRequestDTO) ResponseEntity
        +updateProduct(Long, ProductRequestDTO) ResponseEntity
        +deactivateProduct(Long) ResponseEntity
        +activateProduct(Long) ResponseEntity
        +getActiveProducts() ResponseEntity
        +getPaginatedProducts(int, int, String, String, String) ResponseEntity
        +getProductById(Long) ResponseEntity
    }

    class PolicyPlanController {
        -PolicyPlanService planService
        +createPlan(PlanRequestDTO) ResponseEntity
        +updatePlan(Long, PlanRequestDTO) ResponseEntity
        +deactivatePlan(Long) ResponseEntity
        +activatePlan(Long) ResponseEntity
        +getActivePlans() ResponseEntity
        +getActivePlansByProduct(Long) ResponseEntity
        +getPaginatedPlans(int, int, String, String, String, String) ResponseEntity
        +getPlanById(Long) ResponseEntity
    }

    class UserController {
        -UserService userService
        +getAllUsers(int, int, String, String) ResponseEntity
        +getUserById(Long) ResponseEntity
        +updateUserStatus(Long, UserStatusUpdateRequestDTO) ResponseEntity
        +createInternalStaff(CreateStaffRequestDTO) ResponseEntity
    }

    class ClaimDocumentController {
        -ClaimDocumentService claimDocumentService
        +uploadDocument(Long, String, MultipartFile) ResponseEntity
    }

    %% Dependency mapping to Service contracts
    AuthController ..> AuthService : depends
    CustomerController ..> CustomerService : depends
    ClaimController ..> ClaimService : depends
    PolicyController ..> PolicyService : depends
    PremiumPaymentController ..> PremiumPaymentService : depends
    InsuranceProductController ..> InsuranceProductService : depends
    PolicyPlanController ..> PolicyPlanService : depends
    UserController ..> UserService : depends
    ClaimDocumentController ..> ClaimDocumentService : depends
```

---

## 3. Service Mappings & Implementations

```mermaid
classDiagram
    direction TB

    class AuthService {
        <<interface>>
        +login(LoginRequestDTO) ApiResponseDTO
        +registerUser(UserRequestDTO) ApiResponseDTO
        +verifyOtp(VerifyOtpRequest) ApiResponseDTO
        +resendOtp(ResendOtpRequestDTO) ApiResponseDTO
        +forgotPassword(ForgotPasswordRequestDTO) ApiResponseDTO
        +resetPassword(ResetPasswordRequestDTO) ApiResponseDTO
    }

    class AuthServiceImpl {
        -AppUserRepository userRepository
        -CustomerRepository customerRepository
        -PasswordEncoder passwordEncoder
        -JwtService jwtService
        -UserService userService
        -OtpService otpService
    }
    AuthService <|.. AuthServiceImpl : implements

    class ClaimService {
        <<interface>>
        +raiseClaim(ClaimRequestDTO, List[MultipartFile]) ApiResponseDTO
        +getMyClaims(int, int, String, String) ApiResponseDTO
        +getAllClaims(int, int, String, String, String, String) ApiResponseDTO
        +getClaimById(Long) ApiResponseDTO
        +getClaimHistory(Long, int, int, String, String, String, String) ApiResponseDTO
        +underReviewClaim(Long) ApiResponseDTO
        +assignStaff(Long) ApiResponseDTO
        +reviewClaim(Long, ClaimReviewRequestDTO) ApiResponseDTO
        +finalDecisionOnClaim(Long, ClaimReviewRequestDTO) ApiResponseDTO
    }

    class ClaimServiceImpl {
        -ClaimRepository claimRepository
        -PolicyRepository policyRepository
        -AppUserRepository userRepository
        -ClaimDocumentService documentService
        -ClaimStatusHistoryRepository historyRepository
    }
    ClaimService <|.. ClaimServiceImpl : implements

    class PolicyService {
        <<interface>>
        +purchasePolicy(PolicyPurchaseRequestDTO) ApiResponseDTO
        +issuePolicy(PolicyIssueRequestDTO) ApiResponseDTO
        +getMyPolicies(int, int, String, String) ApiResponseDTO
        +getPoliciesByCustomer(Long, int, int, String, String) ApiResponseDTO
        +getAllPolicies(int, int, String, String, String, String, String) ApiResponseDTO
        +getPolicyById(Long) ApiResponseDTO
        +getClaimsByPolicyId(Long, int, int, String, String) ApiResponseDTO
        +cancelPolicy(Long) ApiResponseDTO
    }

    class PolicyServiceImpl {
        -PolicyRepository policyRepository
        -CustomerRepository customerRepository
        -PolicyPlanRepository planRepository
        -AppUserRepository userRepository
        -ClaimRepository claimRepository
    }
    PolicyService <|.. PolicyServiceImpl : implements

    class PremiumPaymentService {
        <<interface>>
        +recordPayment(PaymentRequestDTO) ApiResponseDTO
        +getPaymentsByPolicyId(Long, int, int, String, String) ApiResponseDTO
        +getPaymentById(Long) ApiResponseDTO
        +getPaginatedPayments(int, int, String, String, String, String) ApiResponseDTO
        +getMyPayments(int, int, String, String) ApiResponseDTO
        +getMyPaymentsByPolicyId(Long, int, int, String, String) ApiResponseDTO
    }

    class PremiumPaymentServiceImpl {
        -PremiumPaymentRepository paymentRepository
        -PolicyRepository policyRepository
        -AppUserRepository userRepository
    }
    PremiumPaymentService <|.. PremiumPaymentServiceImpl : implements
```

---

## 4. DTO & Validation Payload Schemas

Data structures mapped directly to client requests and responses:

```mermaid
classDiagram
    class UserRequestDTO {
        +fullName: String
        +email: String
        +password: String
        +mobileNumber: String
    }
    class CreateStaffRequestDTO {
        +fullName: String
        +email: String
        +password: String
        +mobileNumber: String
        +productSpeciality: ProductType
    }
    class CustomerRequestDTO {
        +dateOfBirth: LocalDate
        +address: String
        +city: String
        +state: String
        +pinCode: String
        +nomineeName: String
        +nomineeRelation: String
    }
    class PlanRequestDTO {
        +productId: Long
        +planName: String
        +coverageAmount: BigDecimal
        +premiumAmount: BigDecimal
        +premiumType: PremiumType
        +duration: Integer
        +termsAndConditions: String
        +isActive: Boolean
    }
    class ClaimRequestDTO {
        +policyId: Long
        +claimAmount: BigDecimal
        +claimReason: String
        +incidentDate: LocalDate
    }
    class PaymentRequestDTO {
        +policyId: Long
        +amount: BigDecimal
        +paymentMode: PaymentMode
        +transactionReference: String
        +paymentStatus: PaymentStatus
    }
```

---

## 5. Security Models & Handlers

This shows how filters, security context mechanisms, and tokens relate structurally.

```mermaid
classDiagram
    class SecurityConfig {
        +securityFilterChain(HttpSecurity, AuthenticationProvider, JwtAuthenticationFilter, HandlerExceptionResolver) SecurityFilterChain
        +authenticationProvider(UserDetailsService, PasswordEncoder) AuthenticationProvider
        +authenticationManager(AuthenticationConfiguration) AuthenticationManager
        +passwordEncoder() PasswordEncoder
    }
    class JwtAuthenticationFilter {
        -JwtService jwtService
        -UserDetailsService userDetailsService
        +doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain) void
    }
    class JwtService {
        -String jwtSecret
        -long jwtExpirationMs
        +generateToken(UserDetails, String, String) String
        +extractUsername(String) String
        +isTokenValid(String, UserDetails) boolean
    }
    class CustomUserDetailsService {
        -AppUserRepository userRepository
        +loadUserByUsername(String) UserDetails
    }
    class GlobalExceptionHandler {
        +handleResourceNotFound(ResourceNotFoundException, HttpServletRequest) ResponseEntity
        +handleDuplicateResource(DuplicateResourceException, HttpServletRequest) ResponseEntity
        +handleBadRequest(BadRequestException, HttpServletRequest) ResponseEntity
        +handleValidation(MethodArgumentNotValidException, HttpServletRequest) ResponseEntity
        +handleAccessDenied(AccessDeniedException, HttpServletRequest) ResponseEntity
        +handleGeneric(Exception, HttpServletRequest) ResponseEntity
    }

    JwtAuthenticationFilter ..> JwtService : uses
    SecurityConfig ..> JwtAuthenticationFilter : configures
    CustomUserDetailsService ..> SecurityConfig : supplies
```
