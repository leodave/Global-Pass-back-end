Global pass project

Global pass

User

1. View services
2. Select service
    1. University application
    2. Udemy or any course
    3. Subscription Ai tools, Netflix etc..
    4. Hotel booking
    5. Exam fee
3. Book service with requirements
    1. Service name
    2. Login link, with user name password
    3. Amount
    4. Currency
    5. Description on steps to follow
    6. Any more detailed information required
4. View booked services
5. View finalized services
6. Send Payment proof
7. Customer support page

Admin
1. View bookings
2. Select booking
3. Accept booking - > send notification via email and redirection to app
4. Reject booking with information -> send notification
5. Send proof of payment
6. View users - with their bookings


Backend APIs (MVP)
🔐 Auth (3 APIs)
POST /auth/register
POST /auth/login
GET /auth/me
👉 Handles users + sessions

🧑 User Features (6 APIs)
Services
GET /services → list all services
GET /services/:id → service details
Bookings
POST /bookings → create booking
GET /bookings → user’s bookings
GET /bookings/:id → booking details
Payment Proof
POST /bookings/:id/payment-proof
🛠️ Admin Features (7 APIs)
Booking Management
GET /admin/bookings
GET /admin/bookings/:id
PATCH /admin/bookings/:id/accept
PATCH /admin/bookings/:id/reject
PATCH /admin/bookings/:id/complete
Proof Upload
POST /admin/bookings/:id/proof
Users
GET /admin/users
💬 Support (Optional MVP: 2 APIs)
POST /support/ticket
GET /support/tickets
