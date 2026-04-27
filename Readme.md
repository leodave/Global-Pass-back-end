# GlobalPass Backend

Spring Boot 4 REST API with JWT authentication, Supabase PostgreSQL, and Supabase Storage.

## Package Structure

```
global_pass/
├── auth/           AuthController, AuthService, JwtUtil, JwtFilter
├── bookings/       BookingEntity, BookingRepository, BookingController (read-only)
├── config/         SecurityConfig, ServiceCatalog, H2ConsoleConfig, ApiResponseDto
├── contact/        ContactController, ContactEntity, ContactRepository
├── exception/      GlobalExceptionHandler, custom exceptions
├── payments/       PaymentController, PaymentService, FileStorageService
└── users/          User, UserController, UserService, UserMapper
```

## API Endpoints

### Auth (public)
| Method | Endpoint            | Description       |
|--------|---------------------|-------------------|
| POST   | `/api/auth/signup`  | Register new user |
| POST   | `/api/auth/login`   | Login, get JWT    |

### Users (authenticated)
| Method | Endpoint                    | Description        |
|--------|-----------------------------|--------------------|
| GET    | `/api/users/{id}`           | Get user profile   |
| PUT    | `/api/users/{id}`           | Update profile     |
| PUT    | `/api/users/{id}/password`  | Change password    |

### Payments (authenticated)
| Method | Endpoint                       | Description              |
|--------|--------------------------------|--------------------------|
| POST   | `/api/payments`                | Upload payment proof     |
| GET    | `/api/payments/my/{userId}`    | Get user's payments      |
| GET    | `/api/payments`                | Get all payments (admin) |
| PUT    | `/api/payments/{id}/status`    | Approve/reject (admin)   |
| GET    | `/api/payments/{id}/file`      | Download proof file      |

### Bookings (authenticated)
| Method | Endpoint          | Description                |
|--------|-------------------|----------------------------|
| GET    | `/api/bookings`   | Get all bookings (admin)   |

### Contact (public)
| Method | Endpoint        | Description          |
|--------|-----------------|----------------------|
| POST   | `/api/contact`  | Submit contact form  |

## Configuration

### Profiles
- `dev` — H2 in-memory, local file storage, schema auto-created
- `prod` — Supabase PostgreSQL, Supabase Storage, schema managed by Hibernate

### Key Properties
```yaml
jwt.secret          # JWT signing key
app.supabase.url    # Supabase project URL
app.supabase.key    # Supabase service_role JWT key
app.supabase.bucket # Storage bucket name (default: payments)
```

## Static Service Catalog

Services are defined in `ServiceCatalog.java` — a static map of 38 service IDs to names. No database table needed. Used by `PaymentService` to validate booking IDs and resolve service names.

## Running

```bash
mvn clean spring-boot:run
```

Default profile is `prod` (Supabase). Switch to `dev` for local H2:
```yaml
spring.profiles.active: dev
```
