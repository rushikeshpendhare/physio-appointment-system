# Physiotherapy Appointment System

A full-stack web application with two roles, **User (Patient)** and **Physiotherapist**. Patients sign up, pick a physiotherapist, book a slot, pay and receive a confirmation. Physiotherapists set up their schedule, manage the day's appointments, mark days off and change their fees.

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 4.0.8, Spring Web, Spring Data JPA, Spring Security, Bean Validation, Spring Mail |
| Auth | JWT (jjwt 0.12.6), BCrypt password hashing |
| Database | MySQL 8 |
| Frontend | React (Vite), React Router, Axios |
| Build | Maven (backend), npm (frontend) |

## Architecture

```
React (port 5173)
   |   JSON over HTTP, JWT in the Authorization header
   v
Spring Boot REST API (port 8080)
   Controller  ->  Service  ->  Repository
   |   JPA / Hibernate
   v
MySQL (physio_db)
```

- **Controller** layer exposes REST endpoints and returns only the fields the UI needs (no entities, so password hashes never leave the server).
- **Service** layer holds the business rules (slot generation, booking, re-ordering).
- **Repository** layer is Spring Data JPA.
- **JWT filter** reads the token on every request and sets the logged-in user and role. Sessions are stateless.
- **CORS** allows the React dev server (`http://localhost:5173`).

## Features

### Authentication and onboarding
- Landing page with **Sign In** and **Sign Up**.
- Sign up as User or Physiotherapist. The form shows the profile fields for the chosen role:
  - User: name, email, password, age, gender, contact number.
  - Physiotherapist: name, email, password, qualification, specialization, clinic address, fees per appointment, contact number.
- A verification email is sent on signup. The link opens a confirmation page in the React app.
- **Login is blocked until the email is verified.**
- After login, users are redirected by role: User Dashboard or Physiotherapist Dashboard.

### User (Patient) dashboard
- Cards for up to three verified physiotherapists (name, specialization, clinic, fees).
- Pick a physiotherapist and a date to see the free slots.
- Select a slot, continue to a **mock payment** screen and pay.
- On success: the booking is stored, and confirmation emails go to both the patient and the physiotherapist.
- **Upcoming appointments** list with date and time, physiotherapist name and status.

### Physiotherapist dashboard
- Appointments of any selected day, in sequence, with **Move up / Move down** and **Save order**.
- **Set up daily schedule:** date, start time, end time and minutes per slot. Slots are generated automatically and become visible to patients.
- **Mark a day off:** closes the open slots of that date.
- **Update fees:** applies to new bookings.

## Business rules

- A slot can only be booked while its status is `AVAILABLE`. Booking sets it to `BOOKED`, so the same slot cannot be booked twice.
- Booking and slot update happen in one transaction.
- The booking amount is read from the physiotherapist's fees on the server, never from the client.
- Saving the schedule again for a date replaces the open slots and keeps booked ones.
- Day off changes only `AVAILABLE` slots to `OFF`. Booked appointments are kept.
- Changing fees does not change the amount of past bookings.
- Verification tokens are random UUIDs, expire after 24 hours and are deleted once used.
- Re-ordering needs every appointment id of that day exactly once and only works on the logged-in physiotherapist's own appointments.

## Database design

| Table | Key columns |
|---|---|
| `users` | id, name, email (unique), password (BCrypt hash), role (USER / PHYSIO), contact_number, email_verified, age, gender, qualification, specialization, clinic_address, fees |
| `verification_token` | id, token (unique), user_id, expiry_date |
| `slot` | id, physio_id, date, start_time, end_time, status (AVAILABLE / BOOKED / OFF) |
| `appointment` | id, user_id, physio_id, slot_id (one-to-one), status, payment_status, amount, sequence_no, created_at |

Relations: a physiotherapist has many slots; a slot has at most one appointment; an appointment links one patient and one physiotherapist (both rows of `users`).

Tables are created by Hibernate (`ddl-auto=update`), which is fine for development. A production setup would use Flyway or Liquibase migrations.

## API reference

| Access | Method and path | Purpose |
|---|---|---|
| Public | POST `/api/auth/signup` | Register a user or physiotherapist |
| Public | GET `/api/auth/verify?token=` | Verify email |
| Public | POST `/api/auth/login` | Returns JWT, role, name and user id |
| Patient | GET `/api/patient/physios` | List verified physiotherapists |
| Patient | GET `/api/patient/physios/{id}/slots?date=YYYY-MM-DD` | Free slots of a day |
| Patient | POST `/api/patient/book` | Body `{ slotId, paymentMethod }` |
| Patient | GET `/api/patient/appointments` | The patient's appointments |
| Physio | POST `/api/physio/slots` | Body `{ date, startTime, endTime, durationMinutes }` |
| Physio | GET `/api/physio/appointments?date=` | Appointments of a day, in sequence |
| Physio | PUT `/api/physio/appointments/reorder` | Body `{ date, appointmentIds[] }` |
| Physio | POST `/api/physio/day-off` | Body `{ date }` |
| Physio | PUT `/api/physio/fees` | Body `{ fees }` |

All non-public endpoints need `Authorization: Bearer <token>`. `/api/physio/**` is restricted to the PHYSIO role.

## Security

- Passwords are hashed with BCrypt.
- Stateless JWT authentication with a signed token (24 hours).
- Role-based access with `hasRole("PHYSIO")` on physiotherapist endpoints.
- Unverified accounts are disabled at the Spring Security level, so they cannot log in.
- Input validation on signup and login (`@Email`, `@NotBlank`, password minimum length).
- Secrets are kept out of Git: `application.properties` is git-ignored and only a `.example` file is committed.

## Project structure

```
physio-appointment-system
├── backend
│   ├── pom.xml
│   └── src/main
│       ├── java/com/btpl/physio
│       │   ├── controller   (AuthController, PatientController, PhysioController)
│       │   ├── service      (AuthService, BookingService, SlotService, PhysioService, EmailService)
│       │   ├── repository   (User, VerificationToken, Slot, Appointment)
│       │   ├── model        (entities and enums)
│       │   ├── dto          (request and response objects)
│       │   └── security     (JwtUtil, JwtAuthFilter, SecurityConfig, CustomUserDetailsService)
│       └── resources
│           └── application.properties.example
└── frontend
    └── src
        ├── api.js           (Axios instance that attaches the JWT)
        ├── App.jsx          (routes)
        └── pages            (Landing, Signup, Login, Verify, UserDashboard, PhysioDashboard)
```

## Getting started

**Prerequisites:** Java 17, Maven (or the included `mvnw`), Node.js 18+, MySQL 8.

### 1. Database

```sql
CREATE DATABASE physio_db;
```

### 2. Backend

1. Copy `backend/src/main/resources/application.properties.example` to `application.properties` in the same folder.
2. Fill in your MySQL password, Gmail address, Gmail app password and a random JWT secret (at least 32 characters).
3. Start the API:

```
cd backend
mvnw spring-boot:run
```

The API runs on http://localhost:8080 and creates the tables on first start.

### 3. Frontend

```
cd frontend
npm install
npm run dev
```

Open http://localhost:5173.

### Email setup

Verification and booking emails use Gmail SMTP with an **app password** (Google Account, Security, 2-Step Verification, App passwords). Use the 16-character app password, not your normal Gmail password.

If email is not configured or fails, the application keeps working and prints the email text, including the verification link, in the backend console.

## Demo walkthrough

1. Sign up as a **Physiotherapist** and open the verification link from the email.
2. Sign in. In **Set up daily schedule**, choose a date, 10:00 to 13:00 and 30 minutes, then save.
3. Sign out. Sign up as a **User**, verify the email and sign in.
4. On the User Dashboard pick the physiotherapist and the date. Select a free slot, continue to payment and pay.
5. The booking appears under **Your upcoming appointments**, and both parties get a confirmation email.
6. Sign in as the physiotherapist again. The appointment shows under **Appointments**. Re-order, mark a day off and update fees to see the changes on the patient side.

## Assumptions

- **MySQL instead of PostgreSQL.** The data layer is JPA/Hibernate, so switching to PostgreSQL needs only the driver dependency and the datasource URL and username in `application.properties`.
- **Payment is mocked.** It always succeeds and the appointment is stored with payment status `PAID`. A real gateway (for example Razorpay) would plug in at the booking step.
- The patient dashboard lists up to three verified physiotherapists.
- Slots are generated from a start time, end time and duration chosen by the physiotherapist.
- Appointments are bookable per date; there is no cancellation flow yet.

## Known limitations and next steps

- Two patients booking the same slot at the exact same moment would need optimistic locking (`@Version`) or a database lock. The current check is a status check inside a transaction.
- Re-creating the schedule for a date replaces open slots, which also removes an earlier day off for that date.
- Email links point to `localhost`, so verification works on the machine running the app. A deployed version would set `app.frontend-url` to the public URL.
- Planned improvements: real payment gateway, appointment cancellation, global exception handler, automated tests, Docker setup and cloud deployment.

## Troubleshooting

| Problem | Fix |
|---|---|
| `Access denied for user 'root'` on start | Check the MySQL password in `application.properties`. |
| `Unknown database 'physio_db'` | Create the database (step 1). |
| `Could not resolve placeholder` on start | A property from `application.properties.example` is missing in your file. |
| Login says "Please verify your email first" | Open the verification link from the email or the backend console. |
| Email `Authentication failed` in console | Use a Gmail app password, not the normal password. |
| Browser shows a CORS error | Run the frontend on http://localhost:5173. |
| Frontend calls fail with 401 | Sign in again; the token is missing or expired. |