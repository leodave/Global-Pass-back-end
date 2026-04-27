package global_pass.config;

import java.util.Map;

public final class ServiceCatalog {

    private ServiceCatalog() {}

    private static final Map<String, String> SERVICES = Map.ofEntries(
        Map.entry("s01", "Tuition Fee Payment"),
        Map.entry("s02", "University Deposit Payment"),
        Map.entry("s03", "Application Fee Payment"),
        Map.entry("s04", "Exam Registration (IELTS, TOEFL, GRE)"),
        Map.entry("s05", "Student Housing Reservation"),
        Map.entry("s06", "Netflix / Spotify Subscription"),
        Map.entry("s07", "Adobe / Microsoft / Canva"),
        Map.entry("s08", "AI Tools Subscription"),
        Map.entry("s09", "App Store / Google Play"),
        Map.entry("s10", "Domains & Hosting Renewals"),
        Map.entry("s11", "Online Course Subscription"),
        Map.entry("s12", "Freelance Platform Fees"),
        Map.entry("s13", "Developer Tools"),
        Map.entry("s14", "Portfolio & Domain Hosting"),
        Map.entry("s15", "Professional Certifications"),
        Map.entry("s16", "LinkedIn Premium"),
        Map.entry("s17", "PlayStation / Xbox Store"),
        Map.entry("s18", "Steam Game Purchases"),
        Map.entry("s19", "In-Game Currency"),
        Map.entry("s20", "YouTube Premium / Disney+"),
        Map.entry("s21", "Streaming Upgrades & Add-ons"),
        Map.entry("s22", "Amazon / AliExpress / eBay"),
        Map.entry("s23", "Personal Electronics & Gadgets"),
        Map.entry("s24", "Books & Study Materials"),
        Map.entry("s25", "Clothing & Fashion Items"),
        Map.entry("s26", "Shipping & Forwarding Fees"),
        Map.entry("s27", "Accommodation Booking Deposit"),
        Map.entry("s28", "Temporary Housing Reservation"),
        Map.entry("s29", "Transport Card Setup"),
        Map.entry("s30", "SIM / Onboarding Services"),
        Map.entry("s31", "International Flight Booking"),
        Map.entry("s32", "Hotel Reservation"),
        Map.entry("s33", "Travel Insurance"),
        Map.entry("s34", "Event & Conference Tickets"),
        Map.entry("s35", "Car Rental Reservation"),
        Map.entry("s36", "Custom International Payment"),
        Map.entry("s37", "Donations & Crowdfunding"),
        Map.entry("s38", "Recurring Payment Setup")
    );

    public static String getNameById(String id) {
        return SERVICES.getOrDefault(id, "Unknown Service");
    }

    public static boolean exists(String id) {
        return SERVICES.containsKey(id);
    }
}
