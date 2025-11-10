package avandra.Controllers;

import avandra.core.DTO.CoordinateDTO;
import avandra.core.DTO.TripPartDTO;
import avandra.core.service.DBService;
import avandra.core.service.FindBestTripService;
import avandra.core.service.JourneyPlannerService;
import avandra.core.service.TripFileHandler;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AvandraController is the main layer that connects everything together
 * handles database access trip planning and choosing the best route so the UI stays clean
 * storage logic is abstracted by DBService so this controller is storage independent
 */
public class AvandraController {

    // handles database read and write operations for users favorites and roles
    private final DBService databaseService;

    // deals with trip pattern files and generating trip alternatives
    private final TripFileHandler tripPatternFileHandler;

    // finds the best trip from several alternatives using a scoring algorithm
    private final FindBestTripService bestTripSelectionService;

    // responsible for resolving start and end coordinates for a user and destination
    private final JourneyPlannerService journeyEndpointService;

    // default values used if not specified by caller
    private final int defaultRoutePatternCount;
    private final boolean defaultIncludeRequestMetadata;

    /**
     * constructor with default settings
     * @param dbService database service used for storing user data
     * @param tripFileHandler handles reading trip data and generating alternatives
     * @param findBestTripService chooses the best trip option
     * @param journeyPlannerService finds coordinates for start and destination
     */
    public AvandraController(DBService dbService,
                             TripFileHandler tripFileHandler,
                             FindBestTripService findBestTripService,
                             JourneyPlannerService journeyPlannerService) {
        this(dbService, tripFileHandler, findBestTripService, journeyPlannerService, 3, true);
    }

    /**
     * full constructor when default values need to be customized
     * @param dbService database service
     * @param tripFileHandler generates trip patterns
     * @param findBestTripService picks best option from alternatives
     * @param journeyPlannerService locates coordinates for user and destination
     * @param defaultNumPatterns how many route patterns to try
     * @param defaultIncludeRequestMeta if true includes request metadata
     */
    public AvandraController(DBService dbService,
                             TripFileHandler tripFileHandler,
                             FindBestTripService findBestTripService,
                             JourneyPlannerService journeyPlannerService,
                             int defaultNumPatterns,
                             boolean defaultIncludeRequestMeta) {
        this.databaseService = Objects.requireNonNull(dbService);
        this.tripPatternFileHandler = Objects.requireNonNull(tripFileHandler);
        this.bestTripSelectionService = Objects.requireNonNull(findBestTripService);
        this.journeyEndpointService = Objects.requireNonNull(journeyPlannerService);
        this.defaultRoutePatternCount = defaultNumPatterns;
        this.defaultIncludeRequestMetadata = defaultIncludeRequestMeta;
    }

    // Journey logic


    /**
     * finds the best journey for a given user and destination
     * steps get endpoints generate route options and pick the top one
     * @param userId id of the user
     * @param destId id of the destination
     * @return best journey as a list of trip parts
     * @throws Exception when inputs are invalid or a service call fails
     */
    public List<TripPartDTO> bestJourney(String userId, String destId) throws Exception {
        if (userId == null || userId.isBlank() || destId == null || destId.isBlank()) {
            throw new IllegalArgumentException("userId and destId must not be blank");
        }

        // get start and end coordinates for this trip
        List<CoordinateDTO> startAndEndCoordinates =
                journeyEndpointService.fetchStartingPointAndEndPoint(userId, destId);

        // generate multiple route alternatives from those coordinates
        List<List<TripPartDTO>> routeAlternatives =
                tripPatternFileHandler.planTrip(startAndEndCoordinates, defaultRoutePatternCount, defaultIncludeRequestMetadata);

        // pick the best alternative using the selection service
        return bestTripSelectionService.pickBest(routeAlternatives);
    }

    // Read only destinations for any user


    /**
     * returns a list of user favorites from the database
     * @param userId user to look up
     * @return map of favorite name to coordinates
     * @throws Exception if database lookup fails or user id missing
     */
    public List<String> listUserDestinations(String userId) throws Exception {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        return databaseService.listUserDestinations(userId);
    }

    // Admin management for favorites and litebrukere


    /**
     * adds or updates a favorite for an admin or their lite user
     * verifies permissions before saving
     * requireAdmin makes sure its an admin that tries to run the function
     * @param adminId id of admin performing action
     * @param targetUserId lite user id or blank to target admin
     * @param favoriteKey key name for favorite
     * @param address address text
     * @param lat latitude
     * @param lon longitude
     * @throws Exception if permissions or database calls fail
     */
    public void adminAddFavorite(String adminId,
                                 String targetUserId,
                                 String favoriteKey,
                                 String address,
                                 double lat,
                                 double lon) throws Exception {
        requireAdmin(adminId);

        String resolvedTargetUserId = targetUserId;
        if (resolvedTargetUserId == null || resolvedTargetUserId.isBlank()) {
            resolvedTargetUserId = adminId;
        } else if (!resolvedTargetUserId.equals(adminId)) {
            requireAdminHasLiteUser(adminId, resolvedTargetUserId);
        }

        if (favoriteKey == null || favoriteKey.isBlank()) {
            throw new IllegalArgumentException("favoriteKey must not be blank");
        }

        databaseService.addDestinationToFavorites(resolvedTargetUserId, favoriteKey, address, lat, lon);
    }

    /**
     * removes a favorite for a user or lite user
     * checks admin permissions first
     * requireAdmin makes sure its an admin that tries to run the function
     * @param adminId id of admin
     * @param targetUserId id of user to modify or blank for admin
     * @param favoriteKey key to delete
     * @throws Exception if unauthorized or database fails
     */
    public void adminRemoveFavorite(String adminId,
                                    String targetUserId,
                                    String favoriteKey) throws Exception {
        requireAdmin(adminId);

        String resolvedTargetUserId = targetUserId;
        if (resolvedTargetUserId == null || resolvedTargetUserId.isBlank()) {
            resolvedTargetUserId = adminId;
        } else if (!resolvedTargetUserId.equals(adminId)) {
            requireAdminHasLiteUser(adminId, resolvedTargetUserId);
        }

        if (favoriteKey == null || favoriteKey.isBlank()) {
            throw new IllegalArgumentException("favoriteKey must not be blank");
        }

        databaseService.removeData(resolvedTargetUserId, favoriteKey, "favoritter");
    }

    /**
     * updates coordinates for an existing favorite belonging to a user or lite user
     * requireAdmin makes sure its an admin that tries to run the function
     * @param adminId id of admin
     * @param targetUserId id of lite user or admin
     * @param favoriteKey key name to update
     * @param lat new latitude
     * @param lon new longitude
     * @throws Exception if permission or database call fails
     */
    public void adminUpdateFavoriteCoordinates(String adminId,
                                               String targetUserId,
                                               String favoriteKey,
                                               double lat,
                                               double lon) throws Exception {
        requireAdmin(adminId);

        String resolvedTargetUserId = targetUserId;
        if (resolvedTargetUserId == null || resolvedTargetUserId.isBlank()) {
            resolvedTargetUserId = adminId;
        } else if (!resolvedTargetUserId.equals(adminId)) {
            requireAdminHasLiteUser(adminId, resolvedTargetUserId);
        }

        if (favoriteKey == null || favoriteKey.isBlank()) {
            throw new IllegalArgumentException("favoriteKey must not be blank");
        }

        databaseService.addCoordinatesToDestination(resolvedTargetUserId, favoriteKey, lat, lon);
    }

    /**
     * lists all favorites for a lite user linked to an admin
     * checks relationship in litebrukere
     * requireAdmin makes sure its an admin that tries to run the function
     * @param adminId id of admin
     * @param liteUserId id of lite user
     * @return map of favorites for the lite user
     * @throws Exception if unauthorized or database call fails
     */
    public List<String> adminListLiteUserDestinations(String adminId,
                                                      String liteUserId) throws Exception {
        requireAdmin(adminId);
        if (liteUserId == null || liteUserId.isBlank()) {
            throw new IllegalArgumentException("liteUserId must not be blank");
        }
        requireAdminHasLiteUser(adminId, liteUserId);
        return databaseService.listUserDestinations(liteUserId);
    }

    /**
     * returns all lite users connected to an admin
     * requireAdmin makes sure its an admin that tries to run the function
     * @param adminId admin id
     * @return list of lite user ids
     * @throws Exception if admin validation fails
     */
    public java.util.List<String> listLitebrukereForAdmin(String adminId) throws Exception {
        requireAdmin(adminId);
        return databaseService.listLitebrukereForAdmin(adminId);
    }

    // Authorization and relationships

    /**
     * checks if user is an admin throws error if not
     * @param userId id to check
     * @throws Exception if database fails or user not admin
     */
    private void requireAdmin(String userId) throws Exception {
        if (!databaseService.isAdmin(userId)) {
            throw new SecurityException("User " + userId + " is not an admin");
        }
    }

    /**
     * returns true if user is admin false otherwise
     * logs error when check fails to avoid breaking UI
     * @param userId id to check
     * @return true if admin else false
     */
    public boolean isAdminUser(String userId) {
        try {
            return databaseService.isAdmin(userId);
        } catch (Exception e) {
            System.err.println("Error checking admin flag for " + userId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * ensures that an admin actually owns the lite user
     * prevents editing other users litebrukere
     * @param adminId id of admin
     * @param liteUserId id of lite user
     * @throws Exception if relationship not found or database call fails
     */
    private void requireAdminHasLiteUser(String adminId, String liteUserId) throws Exception {
        java.util.List<String> linkedLiteUserIds = databaseService.listLitebrukereForAdmin(adminId);
        if (linkedLiteUserIds == null || !linkedLiteUserIds.contains(liteUserId)) {
            throw new SecurityException("Admin " + adminId + " has no access to lite user " + liteUserId);
        }
    }

    // Utilities for UI layer (Only in use for the MVP)

    /**
     * returns list of all user ids for use in login menu or admin panels
     * converts database documents into simple list of strings
     * wraps checked exceptions into runtime since this is a UI helper
     * @return list of user ids
     */
    public List<String> listAllUserIds() {
        try {
            Object allDocumentsRaw = databaseService.retriveALLData();
            List<String> userIds = new ArrayList<String>();
            if (allDocumentsRaw instanceof Iterable<?>) {
                for (Object documentObject : (Iterable<?>) allDocumentsRaw) {
                    if (documentObject instanceof Document) {
                        String userIdFromDocument = ((Document) documentObject).getString("id");
                        if (userIdFromDocument != null) userIds.add(userIdFromDocument);
                    }
                }
            }
            return userIds;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list users", e);
        }
    }
}
