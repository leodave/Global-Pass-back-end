-- Seed data for dev profile (H2)
-- Passwords are BCrypt-hashed: "Password1"

-- Admin user (id=1)
INSERT INTO users (name, email, password, role, active, created_at, updated_at) VALUES
('Admin', 'admin@globalpass.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', true, NOW(), NOW());

-- Regular user (id=2)
INSERT INTO users (name, email, password, role, active, created_at, updated_at) VALUES
('Test User', 'user@globalpass.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', true, NOW(), NOW());

-- =============================================
-- SERVICES (created by admin, visible to all)
-- =============================================

-- Study Abroad Payments
INSERT INTO bookings (id, user_id, name, description, page_link, login_username, login_password, amount, currency, other_details) VALUES
('s01', 1, 'Tuition Fee Payment', 'Pay your university tuition fees to any institution worldwide. We handle the international transfer securely.', 'https://globalpass.com', 'N/A', 'N/A', 500.00, 'USD', 'Amount varies per university. Contact us with your invoice.'),
('s02', 1, 'University Deposit Payment', 'Secure your university admission by paying the required deposit on time.', 'https://globalpass.com', 'N/A', 'N/A', 250.00, 'USD', 'Provide your admission letter and payment details.'),
('s03', 1, 'Application Fee Payment', 'Pay university or college application fees to institutions that require international payment methods.', 'https://globalpass.com', 'N/A', 'N/A', 75.00, 'USD', 'Covers single application. Bundle discounts available.'),
('s04', 1, 'Exam Registration (IELTS, TOEFL, GRE)', 'Register and pay for international exams including IELTS, TOEFL, GRE, SAT, and GMAT.', 'https://globalpass.com', 'N/A', 'N/A', 260.00, 'USD', 'Price varies by exam type and location.'),
('s05', 1, 'Student Housing Reservation', 'Pay deposits and reservation fees for student accommodation abroad.', 'https://globalpass.com', 'N/A', 'N/A', 300.00, 'USD', 'Provide housing provider details and invoice.');

-- Digital Subscription Payments
INSERT INTO bookings (id, user_id, name, description, page_link, login_username, login_password, amount, currency, other_details) VALUES
('s06', 1, 'Netflix / Spotify Subscription', 'Subscribe or renew your Netflix, Spotify, or other streaming service accounts.', 'https://globalpass.com', 'N/A', 'N/A', 15.99, 'USD', 'Monthly or annual plans available.'),
('s07', 1, 'Adobe / Microsoft / Canva', 'Pay for Adobe Creative Cloud, Microsoft 365, Canva Pro, and other productivity tools.', 'https://globalpass.com', 'N/A', 'N/A', 54.99, 'USD', 'Monthly or annual billing.'),
('s08', 1, 'AI Tools Subscription', 'Subscribe to ChatGPT Plus, Claude Pro, Midjourney, and other AI platforms.', 'https://globalpass.com', 'N/A', 'N/A', 20.00, 'USD', 'Monthly billing. Provide account details.'),
('s09', 1, 'App Store / Google Play', 'Purchase apps, games, or in-app content from Apple App Store or Google Play.', 'https://globalpass.com', 'N/A', 'N/A', 10.00, 'USD', 'Amount varies. Gift cards also available.'),
('s10', 1, 'Domains & Hosting Renewals', 'Renew your domain names, web hosting, and SSL certificates.', 'https://globalpass.com', 'N/A', 'N/A', 30.00, 'USD', 'Supports GoDaddy, Namecheap, Vercel, Netlify, etc.'),
('s11', 1, 'Online Course Subscription', 'Pay for courses on Udemy, Coursera, Skillshare, LinkedIn Learning, and more.', 'https://globalpass.com', 'N/A', 'N/A', 29.99, 'USD', 'Single course or subscription plans.');

-- Freelancer & Remote Work
INSERT INTO bookings (id, user_id, name, description, page_link, login_username, login_password, amount, currency, other_details) VALUES
('s12', 1, 'Freelance Platform Fees', 'Pay membership or service fees on Upwork, Fiverr, Toptal, and other freelance platforms.', 'https://globalpass.com', 'N/A', 'N/A', 20.00, 'USD', 'Connects or membership fees.'),
('s13', 1, 'Developer Tools', 'Subscribe to JetBrains, GitHub Pro, Vercel Pro, AWS, and other developer tools.', 'https://globalpass.com', 'N/A', 'N/A', 25.00, 'USD', 'Monthly or annual plans.'),
('s14', 1, 'Portfolio & Domain Hosting', 'Set up and pay for your professional portfolio website and custom domain.', 'https://globalpass.com', 'N/A', 'N/A', 15.00, 'USD', 'Includes domain + hosting setup.'),
('s15', 1, 'Professional Certifications', 'Pay for professional certification exams like AWS, Google Cloud, PMP, and more.', 'https://globalpass.com', 'N/A', 'N/A', 150.00, 'USD', 'Price varies by certification.'),
('s16', 1, 'LinkedIn Premium', 'Subscribe to LinkedIn Premium Career, Business, or Sales Navigator plans.', 'https://globalpass.com', 'N/A', 'N/A', 29.99, 'USD', 'Monthly billing.');

-- Gaming & Entertainment
INSERT INTO bookings (id, user_id, name, description, page_link, login_username, login_password, amount, currency, other_details) VALUES
('s17', 1, 'PlayStation / Xbox Store', 'Purchase games, DLCs, and subscriptions from PlayStation Store or Xbox Marketplace.', 'https://globalpass.com', 'N/A', 'N/A', 59.99, 'USD', 'Gift cards and direct purchases available.'),
('s18', 1, 'Steam Game Purchases', 'Buy games, bundles, and in-game items on Steam.', 'https://globalpass.com', 'N/A', 'N/A', 39.99, 'USD', 'Steam wallet top-ups also available.'),
('s19', 1, 'In-Game Currency', 'Purchase V-Bucks (Fortnite), FIFA Points, Robux, and other in-game currencies.', 'https://globalpass.com', 'N/A', 'N/A', 19.99, 'USD', 'Specify game and amount needed.'),
('s20', 1, 'YouTube Premium / Disney+', 'Subscribe to YouTube Premium, Disney+, HBO Max, and other streaming platforms.', 'https://globalpass.com', 'N/A', 'N/A', 13.99, 'USD', 'Individual or family plans.'),
('s21', 1, 'Streaming Upgrades & Add-ons', 'Upgrade your existing streaming plans or add premium channels and features.', 'https://globalpass.com', 'N/A', 'N/A', 9.99, 'USD', 'Provide current plan details.');

-- Online Shopping & Delivery
INSERT INTO bookings (id, user_id, name, description, page_link, login_username, login_password, amount, currency, other_details) VALUES
('s22', 1, 'Amazon / AliExpress / eBay', 'Purchase products from Amazon, AliExpress, eBay, and other international marketplaces.', 'https://globalpass.com', 'N/A', 'N/A', 50.00, 'USD', 'Share product link and we handle the purchase.'),
('s23', 1, 'Personal Electronics & Gadgets', 'Buy phones, laptops, accessories, and gadgets from international stores.', 'https://globalpass.com', 'N/A', 'N/A', 200.00, 'USD', 'Amount varies. Provide product details.'),
('s24', 1, 'Books & Study Materials', 'Purchase textbooks, e-books, and study materials from international publishers.', 'https://globalpass.com', 'N/A', 'N/A', 35.00, 'USD', 'Kindle, Amazon, Chegg, etc.'),
('s25', 1, 'Clothing & Fashion Items', 'Shop from international fashion brands and retailers.', 'https://globalpass.com', 'N/A', 'N/A', 75.00, 'USD', 'Provide store link and item details.'),
('s26', 1, 'Shipping & Forwarding Fees', 'Pay for international shipping, package forwarding, and customs fees.', 'https://globalpass.com', 'N/A', 'N/A', 40.00, 'USD', 'DHL, FedEx, UPS, and freight forwarders.');

-- Relocation & Setup Payments
INSERT INTO bookings (id, user_id, name, description, page_link, login_username, login_password, amount, currency, other_details) VALUES
('s27', 1, 'Accommodation Booking Deposit', 'Pay deposits for apartments, Airbnb, or temporary housing when relocating abroad.', 'https://globalpass.com', 'N/A', 'N/A', 350.00, 'USD', 'Provide booking confirmation and payment link.'),
('s28', 1, 'Temporary Housing Reservation', 'Reserve short-term housing, hostels, or co-living spaces in your destination city.', 'https://globalpass.com', 'N/A', 'N/A', 200.00, 'USD', 'Weekly or monthly stays.'),
('s29', 1, 'Transport Card Setup', 'Pay for public transport cards, metro passes, and commuter subscriptions abroad.', 'https://globalpass.com', 'N/A', 'N/A', 50.00, 'USD', 'City-specific transport cards.'),
('s30', 1, 'SIM / Onboarding Services', 'Purchase international SIM cards, eSIMs, and onboarding service packages.', 'https://globalpass.com', 'N/A', 'N/A', 25.00, 'USD', 'Airalo, Holafly, local carriers.');

-- Travel & Hotel Bookings
INSERT INTO bookings (id, user_id, name, description, page_link, login_username, login_password, amount, currency, other_details) VALUES
('s31', 1, 'International Flight Booking', 'Book international flights through any airline or travel agency.', 'https://globalpass.com', 'N/A', 'N/A', 450.00, 'USD', 'Provide flight details or let us find the best deal.'),
('s32', 1, 'Hotel Reservation', 'Book hotels on Booking.com, Airbnb, Expedia, and other platforms.', 'https://globalpass.com', 'N/A', 'N/A', 120.00, 'USD', 'Share booking link and dates.'),
('s33', 1, 'Travel Insurance', 'Purchase travel insurance for international trips, study abroad, or relocation.', 'https://globalpass.com', 'N/A', 'N/A', 80.00, 'USD', 'Single trip or annual coverage.'),
('s34', 1, 'Event & Conference Tickets', 'Buy tickets for international events, conferences, concerts, and exhibitions.', 'https://globalpass.com', 'N/A', 'N/A', 100.00, 'USD', 'Provide event details and ticket type.'),
('s35', 1, 'Car Rental Reservation', 'Reserve rental cars through Hertz, Enterprise, Sixt, and other providers.', 'https://globalpass.com', 'N/A', 'N/A', 65.00, 'USD', 'Provide dates, location, and car preference.');

-- Other / Custom Payment
INSERT INTO bookings (id, user_id, name, description, page_link, login_username, login_password, amount, currency, other_details) VALUES
('s36', 1, 'Custom International Payment', 'Any international payment you need — tell us what to pay and we handle it securely.', 'https://globalpass.com', 'N/A', 'N/A', 0.00, 'USD', 'Contact us with payment details. Amount set after review.'),
('s37', 1, 'Donations & Crowdfunding', 'Make donations to GoFundMe, Patreon, charities, or crowdfunding campaigns worldwide.', 'https://globalpass.com', 'N/A', 'N/A', 25.00, 'USD', 'Provide campaign or charity link.'),
('s38', 1, 'Recurring Payment Setup', 'Set up recurring international payments for subscriptions, rent, or services.', 'https://globalpass.com', 'N/A', 'N/A', 10.00, 'USD', 'Monthly management fee. Payment amount separate.');
