package avandra.core.service;

import avandra.core.DTO.CoordinateDTO;
import avandra.core.port.DBHandlerPort;

import java.util.List;

/**
 * thin service layer over DBHandlerPort
 * keeps the rest of the app from talking to the handler directly
 * easy to swap handler later if we change storage
 */
public class DBService {
    private final DBHandlerPort handler;

    /**
     * wire up with a handler
     * @param handler the db handler we delegate to
     */
    public DBService(DBHandlerPort handler) {
        this.handler = handler;
    }

    /**
     * create a user
     * @param userID id for the user
     * @param adminUser whether the user is admin
     */
    public void createUser(String userID, boolean adminUser) {
        handler.createUser(userID,adminUser);
    }

    /**
     * create a user with some initial favorite data
     * @param userID id for the user
     * @param adminUser whether the user is admin
     * @param favoriteDestination name of an initial favorite destination
     * @param address address for the favorite destination
     * @param latitude latitude for the favorite destination
     * @param longitude longitude for the favorite destination
     */
    public void createUser(String userID, boolean adminUser, String favoriteDestination, String address, double latitude, double longitude) {
        handler.createUser(userID, adminUser, favoriteDestination, address, latitude, longitude);
    }

    /**
     * add or overwrite a field for a user
     * @param id user id
     * @param addKey field key to set
     * @param addValue value to write
     */
    public void appendData(String id, String addKey, Object addValue) {
        handler.appendData(id, addKey, addValue);
    }

    /**
     * fetch everything the handler returns for the collection
     * naming kept as is
     * @return whatever the handler returns for all data
     */
    public Object retriveALLData() {
        return handler.retrieveAllData();
    }

    /**
     * add a destination to a user’s favorites
     * @param userID user id
     * @param destinationName destination key or name
     * @param address destination address
     * @param latitude destination latitude
     * @param longitude destination longitude
     */
    public void addDestinationToFavorites(String userID, String destinationName, String address, double latitude, double longitude) {
        handler.addDestinationToFavorites(userID, destinationName, address, latitude, longitude);
    }

    /**
     * set or update coordinates for an existing favorite destination
     * @param userID user id
     * @param destinationName destination key or name
     * @param latitude destination latitude
     * @param longitude destination longitude
     */
    public void addCoordinatesToDestination(String userID, String destinationName, double latitude, double longitude) {
        handler.addCoordinatesToFavDestination(userID, destinationName, latitude, longitude);
    }

    /**
     * look up a destination’s coordinates for a user
     * @param userID user id
     * @param destinationID destination key or name
     * @return CoordinateDTO if found, otherwise null
     */
    public CoordinateDTO searchDestination(String userID, String destinationID) {
        return handler.searchFavDestination(userID, destinationID);
    }


    /**
     * delete a user document by id
     * @param userID user id to remove
     */
    public void removeData(String userID) {
        handler.removeData(userID);
    }

    /**
     * remove a single field from a user
     * @param userID user id
     * @param keyToRemove field key to unset
     */
    public void removeData(String userID, String keyToRemove) {
        handler.removeData(userID, keyToRemove);
    }

    /**
     * remove a field under a path like favorites.someKey
     * @param userID user id
     * @param keyToRemove field to remove
     * @param destinationType path prefix like favoritter
     */
    public void removeData(String userID, String keyToRemove, String destinationType) {
        handler.removeData(userID, keyToRemove, destinationType);
    }

    /**
     * remove a field under a deeper path like favorites.destKey.field
     * @param userID user id
     * @param keyToRemove field to remove
     * @param destinationType first path segment like favoritter
     * @param destinationKey second path segment identifying the destination
     */
    public void removeData(String userID, String keyToRemove, String destinationType, String destinationKey) {
        handler.removeData(userID, keyToRemove, destinationType, destinationKey);
    }

    public List<String> listUserDestinations(String userId) {
        return handler.listUserFavDestinations(userId);
    }

    public List<String> listLitebrukereForAdmin(String adminId) {
        return handler.listLitebrukereForAdmin(adminId);
    }



    public boolean isAdmin(String userId) { return handler.isAdmin(userId); }




}
