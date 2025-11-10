package avandra.app;

import avandra.Controllers.AvandraController;
import avandra.api.EnturHttpClient;
import avandra.api.IpGeolocationAdapter;
import avandra.core.DTO.TripPartDTO;
import avandra.core.port.DBHandlerPort;
import avandra.core.port.EnturClientPort;
import avandra.core.service.DBService;
import avandra.core.service.FindBestTripService;
import avandra.core.service.JourneyPlannerService;
import avandra.storage.adapter.MongoDBConnectionAdapter;
import avandra.storage.adapter.MongoDBHandlerAdapter;
import avandra.core.service.TripFileHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class APIMain {

    public static void main(String[] args) throws Exception {

        // System setup
        DBHandlerPort mongoHandler = new MongoDBHandlerAdapter(new MongoDBConnectionAdapter());
        DBService dbService = new DBService(mongoHandler);

        FindBestTripService findBestTripService = new FindBestTripService(1,8,2);
        JourneyPlannerService journeyPlannerService =
                new JourneyPlannerService(new IpGeolocationAdapter("HIOF-AVANDRA"), dbService);

        EnturClientPort enturClient = new EnturHttpClient("HIOF-AVANDRA");
        ObjectMapper mapper = new ObjectMapper();
        TripFileHandler tripFileHandler = new TripFileHandler(enturClient, mapper);

        AvandraController controller = new AvandraController(
                dbService, tripFileHandler, findBestTripService, journeyPlannerService
        );

        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Welcome to Avandra ===");

        boolean appRunning = true;
        while (appRunning) {

            // Login Selection
            List<String> allUserIds = controller.listAllUserIds();
            if (allUserIds.isEmpty()) {
                System.out.println("No users found in the database.");
                return;
            }

            System.out.println("\nAvailable users:");
            for (int i = 0; i < allUserIds.size(); i++) {
                System.out.printf("%d) %s%n", i + 1, allUserIds.get(i));
            }

            System.out.print("\nSelect user number to log in as (0 to exit): ");
            int selection = readInt(scanner, 0, allUserIds.size());
            if (selection == 0) {
                appRunning = false;
                break;
            }

            String userId = allUserIds.get(selection - 1);
            boolean isAdmin = controller.isAdminUser(userId);
            System.out.println("\nLogged in as: " + userId + (isAdmin ? " (Admin)" : ""));
            System.out.println("----------------------------------------");

            // For admins: load litebrukere (Can also be empty)
            List<String> litebrukere = Collections.emptyList();
            if (isAdmin) {
                try {
                    litebrukere = controller.listLitebrukereForAdmin(userId);
                } catch (Exception e) {
                    System.err.println("Could not fetch litebrukere for " + userId + ": " + e.getMessage());
                    litebrukere = Collections.emptyList();
                }
                if (!litebrukere.isEmpty()) {
                    System.out.println("Your lite users:");
                    for (int i = 0; i < litebrukere.size(); i++) {
                        System.out.printf(" - %s%n", litebrukere.get(i));
                    }
                }
            }

            boolean userLoggedIn = true;
            while (userLoggedIn) {
                System.out.println("\n--- Menu ---");
                System.out.println("1. View destinations");
                System.out.println("2. Travel now");
                if (isAdmin) {
                    System.out.println("3. Manage destinations (self or lite user)");
                }
                System.out.println("4. Log out");
                System.out.println("0. Exit");
                System.out.print("Select option: ");

                String choice = scanner.nextLine().trim();

                try {
                    switch (choice) {
                        case "1":
                            showUserDestinations(controller, userId);
                            break;

                        case "2":
                            planTrip(controller, scanner, userId);
                            break;

                        case "3":
                            if (isAdmin) manageDestinationsMenu(controller, scanner, userId, litebrukere);
                            break;

                        case "4":
                            userLoggedIn = false;
                            System.out.println("Logged out.\n");
                            break;

                        case "0":
                            userLoggedIn = false;
                            appRunning = false;
                            System.out.println("Exiting application.");
                            break;

                        default:
                            System.out.println("Invalid choice, try again.");
                            break;
                    }

                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }

        scanner.close();
    }


     //  Admin management sub-menu
    private static void manageDestinationsMenu(AvandraController controller, Scanner scanner,
                                               String adminId, List<String> litebrukere) throws Exception {
        while (true) {
            System.out.println("\n--- Destination Management ---");
            System.out.println("1. Manage destinations");
            if (!litebrukere.isEmpty()) {
                System.out.println("2. Manage a lite user's destinations");
            }
            System.out.println("0. Back");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                manageDestinations(controller, scanner, adminId, null);
            } else if ("2".equals(choice) && !litebrukere.isEmpty()) {
                for (int i = 0; i < litebrukere.size(); i++) {
                    System.out.printf("%d) %s%n", i + 1, litebrukere.get(i));
                }
                System.out.print("Select number (0 to cancel): ");
                int sel = readInt(scanner, 0, litebrukere.size());
                if (sel > 0) {
                    String liteId = litebrukere.get(sel - 1);
                    manageDestinations(controller, scanner, adminId, liteId);
                }
            } else if ("0".equals(choice)) {
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private static void manageDestinations(AvandraController controller, Scanner scanner,
                                           String adminId, String targetUserId) throws Exception {
        while (true) {
            String who = (targetUserId == null) ? "your own" : targetUserId + "'s";
            System.out.println("\n--- Managing " + who + " destinations ---");
            System.out.println("1. View destinations");
            System.out.println("2. Add destination");
            System.out.println("3. Remove destination");
            System.out.println("0. Back");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    List<String> list = (targetUserId == null)
                            ? controller.listUserDestinations(adminId)
                            : controller.adminListLiteUserDestinations(adminId, targetUserId);
                    printDestinations(list, targetUserId == null ? adminId : targetUserId);
                    break;
                case "2":
                    addFavorite(controller, scanner, adminId, targetUserId);
                    printDestinations(
                            (targetUserId == null)
                                    ? controller.listUserDestinations(adminId)
                                    : controller.adminListLiteUserDestinations(adminId, targetUserId),
                            targetUserId == null ? adminId : targetUserId);
                    break;
                case "3":
                    removeFavorite(controller, scanner, adminId, targetUserId);
                    printDestinations(
                            (targetUserId == null)
                                    ? controller.listUserDestinations(adminId)
                                    : controller.adminListLiteUserDestinations(adminId, targetUserId),
                            targetUserId == null ? adminId : targetUserId);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // Destination and trip helpers
    private static void showUserDestinations(AvandraController controller, String userId) throws Exception {
        List<String> destinations = controller.listUserDestinations(userId);
        printDestinations(destinations, userId);
    }

    private static void printDestinations(java.util.List<String> destinations, String userId) {
        if (destinations == null || destinations.isEmpty()) {
            System.out.println("No destinations found for " + userId + ".");
        } else {
            System.out.println("Destinations for " + userId + ":");
            for (int i = 0; i < destinations.size(); i++) {
                System.out.printf(" %d) %s%n", i + 1, destinations.get(i));
            }
        }
    }

    private static void planTrip(AvandraController controller, Scanner scanner, String userId) throws Exception {
        List<String> destinations = controller.listUserDestinations(userId);

        String destName;
        if (destinations == null || destinations.isEmpty()) {
            System.out.print("You have no saved destinations. Enter a destination name: ");
            destName = scanner.nextLine().trim();
        } else {
            System.out.println("\nChoose a destination:");
            for (int i = 0; i < destinations.size(); i++) {
                System.out.printf(" %d) %s%n", i + 1, destinations.get(i));
            }
            System.out.println(" 0) Type a custom destination name");
            System.out.print("Select option: ");

            int sel = readInt(scanner, 0, destinations.size());
            if (sel == 0) {
                System.out.print("Enter destination name: ");
                destName = scanner.nextLine().trim();
            } else {
                destName = destinations.get(sel - 1);
            }
        }

        List<TripPartDTO> trip = controller.bestJourney(userId, destName);
        if (trip == null || trip.isEmpty()) {
            System.out.println("No trip could be planned to " + destName);
            return;
        }

        // Pretty ASCII table with overall start/end/duration in header
        printTripTableAscii(trip, "Trip to " + destName);
    }

    private static void addFavorite(AvandraController controller, Scanner scanner,
                                    String adminId, String targetUserId) throws Exception {
        System.out.print("Enter new destination name: ");
        String newDest = scanner.nextLine().trim();
        System.out.print("Enter address: ");
        String address = scanner.nextLine().trim();
        System.out.print("Enter latitude: ");
        double lat = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Enter longitude: ");
        double lon = Double.parseDouble(scanner.nextLine().trim());
        controller.adminAddFavorite(adminId, targetUserId, newDest, address, lat, lon);
        System.out.println("Destination added successfully!");
    }

    private static void removeFavorite(AvandraController controller, Scanner scanner,
                                       String adminId, String targetUserId) throws Exception {
        System.out.print("Enter destination name to remove: ");
        String toRemove = scanner.nextLine().trim();
        controller.adminRemoveFavorite(adminId, targetUserId, toRemove);
        System.out.println("Destination removed successfully!");
    }

    // Utility helpers
    private static int readInt(Scanner scanner, int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {}
            System.out.print("Enter a number between " + min + " and " + max + ": ");
        }
    }

    /* ===========================================================
       Pretty trip table (ASCII) — stop names + walk target (CHATGPT FOR LOGIC ON THIS ONE)
       =========================================================== */

    private static void printTripTableAscii(List<TripPartDTO> trip, String title) {
        DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

        // Compute overall start (first dep expected/aimed) and end (last arr expected/aimed)
        LocalDateTime tripStart = null;
        LocalDateTime tripEnd = null;

        for (TripPartDTO p : trip) {
            LocalDateTime dep = firstNonNull(p.getExpectedDeparture(), p.getAimedDeparture());
            if (dep != null && (tripStart == null || dep.isBefore(tripStart))) {
                tripStart = dep;
            }
            LocalDateTime arr = firstNonNull(p.getExpectedArrival(), p.getAimedArrival());
            if (arr != null && (tripEnd == null || arr.isAfter(tripEnd))) {
                tripEnd = arr;
            }
        }

        String headerSuffix = "";
        if (tripStart != null && tripEnd != null) {
            long mins = Math.max(0, Duration.between(tripStart, tripEnd).toMinutes());
            headerSuffix = " (" + tripStart.format(HHMM) + " \u2192 " + tripEnd.format(HHMM) + ", " + mins + "m)";
        }

        System.out.println();
        System.out.println("=== " + (title == null ? "Trip plan" : title) + headerSuffix + " ===");
        System.out.println(repeat('-', 95));
        System.out.printf("%-3s %-8s %-12s %-24s %-10s %-24s %-10s %-9s%n",
                "#", "MODE", "LINE", "FROM (stop)", "DEPART", "TO (stop)", "ARRIVE", "DIST");
        System.out.println(repeat('-', 95));

        for (int i = 0; i < trip.size(); i++) {
            TripPartDTO p = trip.get(i);

            String mode = readableMode(p.getLegTransportMode());
            String line = joinNonEmpty(p.getLineName(), p.getLineNumber(), p.getLineOwner());

            // Use stop NAMES (no IDs)
            String fromStop = safeFromStopName(p);
            String toStop   = safeToStopName(p);

            // For WALK/FOOT legs, show where you need to walk TO (next leg's origin if present)
            if (isWalkMode(p.getLegTransportMode())) {
                String nextOrigin = nextLegOriginStopName(trip, i);
                if (!nextOrigin.isBlank()) {
                    toStop = nextOrigin;
                }
            }

            String dep = formatTimeWithDelay(p.getAimedDeparture(), p.getExpectedDeparture(), HHMM);
            String arr = formatTimeWithDelay(p.getAimedArrival(),   p.getExpectedArrival(),   HHMM);

            String dist = p.getTravelDistance() > 0 ? (p.getTravelDistance() + " m") : "";

            System.out.printf("%-3d %-8s %-12s %-24s %-10s %-24s %-10s %-9s%n",
                    (i + 1),
                    truncate(mode, 8),
                    truncate(line, 12),
                    truncate(fromStop, 24),
                    dep,
                    truncate(toStop, 24),
                    arr,
                    dist);
        }

        System.out.println(repeat('-', 95));
    }

    /* ===========================================================
       Helpers for stop names + walk target
       =========================================================== */

    // Prefer stop name fields you already have (platform *names* here); never print IDs.
    private static String safeFromStopName(TripPartDTO p) {
        // Your DTO already exposes platform *names*:
        // previously you printed: p.getDepartPlatformName() + " (" + p.getDepartPlatformId() + ")"
        // We now just take the name and avoid the ID entirely.
        return nullToEmpty(p.getDepartPlatformName());
    }

    private static String safeToStopName(TripPartDTO p) {
        return nullToEmpty(p.getArrivePlatformName());
    }

    // Where do we walk to? Prefer next leg's FROM stop; otherwise this leg's TO stop.
    private static String nextLegOriginStopName(List<TripPartDTO> trip, int i) {
        if (i + 1 < trip.size()) {
            TripPartDTO next = trip.get(i + 1);
            String fromNext = safeFromStopName(next);
            if (!fromNext.isBlank()) return fromNext;
        }
        return safeToStopName(trip.get(i));
    }

    private static String nullToEmpty(String s) {
        return (s == null) ? "" : s.trim();
    }

    /* ===========================================================
       Utility helpers for printing
       =========================================================== */

    private static LocalDateTime firstNonNull(LocalDateTime a, LocalDateTime b) {
        return a != null ? a : b;
    }

    private static String formatTimeWithDelay(LocalDateTime aimed,
                                              LocalDateTime expected,
                                              DateTimeFormatter fmt) {
        if (expected != null) {
            String base = expected.format(fmt);
            if (aimed != null) {
                long diff = Duration.between(aimed, expected).toMinutes();
                if (diff != 0) base += " (" + (diff > 0 ? "+" : "") + diff + "m)";
            }
            return base;
        }
        return aimed != null ? aimed.format(fmt) : "";
    }

    private static String readableMode(String mode) {
        if (mode == null) return "";
        String m = mode.toLowerCase();
        return switch (m) {
            case "bus" -> "Bus";
            case "train" -> "Train";
            case "tram" -> "Tram";
            case "metro" -> "Metro";
            case "ferry" -> "Ferry";
            case "walk", "walking", "foot", "footpath" -> "Walk"; // normalize “Foot” etc.
            default -> capitalize(m);
        };
    }

    private static boolean isWalkMode(String mode) {
        if (mode == null) return false;
        String m = mode.toLowerCase();
        return m.equals("walk") || m.equals("walking") || m.equals("foot") || m.equals("footpath");
    }

    private static String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private static String joinNonEmpty(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }

    private static String bracket(String v) {
        if (v == null || v.isBlank()) return "";
        return "(" + v + ")";
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, Math.max(0, max - 1)) + ".";
    }

    private static String repeat(char c, int n) {
        return String.valueOf(c).repeat(Math.max(0, n));
    }
}
