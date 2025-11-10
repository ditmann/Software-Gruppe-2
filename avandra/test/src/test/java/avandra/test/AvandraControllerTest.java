package avandra.test;

import avandra.Controllers.AvandraController;
import avandra.core.DTO.CoordinateDTO;
import avandra.core.DTO.TripPartDTO;
import avandra.core.service.DBService;
import avandra.core.service.FindBestTripService;
import avandra.core.service.JourneyPlannerService;
import avandra.core.service.TripFileHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AvandraControllerTest {

    private DBService dbService;
    private TripFileHandler tripFileHandler;
    private FindBestTripService bestTripService;
    private JourneyPlannerService journeyPlannerService;

    private AvandraController controller;

    @BeforeEach
    void setUp() {
        dbService = mock(DBService.class);
        tripFileHandler = mock(TripFileHandler.class);
        bestTripService = mock(FindBestTripService.class);
        journeyPlannerService = mock(JourneyPlannerService.class);

        controller = new AvandraController(dbService, tripFileHandler, bestTripService, journeyPlannerService);
    }

    @Test
    void bestJourney_validatesInputs() {
        assertThrows(IllegalArgumentException.class, () -> controller.bestJourney(null, "Home"));
        assertThrows(IllegalArgumentException.class, () -> controller.bestJourney("u1", null));
        assertThrows(IllegalArgumentException.class, () -> controller.bestJourney("", "Home"));
        assertThrows(IllegalArgumentException.class, () -> controller.bestJourney("u1", "  "));
    }

    @Test
    void bestJourney_callsCollaborators_withDefaults_andReturnsPicked() throws Exception {
        String userId = "u1";
        String destinationId = "Home";

        List<CoordinateDTO> endpoints = List.of(new CoordinateDTO(59.9, 10.7), new CoordinateDTO(59.92, 10.76));
        when(journeyPlannerService.fetchStartingPointAndEndPoint(userId, destinationId)).thenReturn(endpoints);

        List<TripPartDTO> alternativeA = List.of(new TripPartDTO("Walk", 5));
        List<TripPartDTO> alternativeB = List.of(new TripPartDTO("Bus", 15));
        List<List<TripPartDTO>> alternatives = List.of(alternativeA, alternativeB);
        when(tripFileHandler.planTrip(eq(endpoints), anyInt(), anyBoolean())).thenReturn(alternatives);

        when(bestTripService.pickBest(alternatives)).thenReturn(alternativeB);

        List<TripPartDTO> result = controller.bestJourney(userId, destinationId);

        assertEquals(alternativeB, result);

        // defaultRoutePatternCount is 3 and defaultIncludeRequestMetadata is true in the controller ctor
        verify(tripFileHandler).planTrip(eq(endpoints), eq(3), eq(true));
        verify(bestTripService).pickBest(alternatives);
    }

    @Test
    void adminAddFavorite_forSelf_checksAdmin_andDelegates() throws Exception {
        when(dbService.isAdmin("adminA")).thenReturn(true);

        controller.adminAddFavorite("adminA", "", "Home", "Addr", 59.9, 10.7);

        verify(dbService).addDestinationToFavorites("adminA", "Home", "Addr", 59.9, 10.7);
    }

    @Test
    void adminAddFavorite_forLiteUser_checksOwnership() throws Exception {
        when(dbService.isAdmin("adminA")).thenReturn(true);
        when(dbService.listLitebrukereForAdmin("adminA")).thenReturn(List.of("kid1"));

        controller.adminAddFavorite("adminA", "kid1", "School", "Addr2", 59.95, 10.8);

        verify(dbService).addDestinationToFavorites("kid1", "School", "Addr2", 59.95, 10.8);
    }

    @Test
    void adminAddFavorite_rejectsWhenNotAdmin() throws Exception {
        when(dbService.isAdmin("userX")).thenReturn(false);

        assertThrows(SecurityException.class, () ->
                controller.adminAddFavorite("userX", "", "Home", "Addr", 1, 2));
        verifyNoInteractions(tripFileHandler);
    }

    @Test
    void adminRemoveFavorite_targetsFavoritterPath() throws Exception {
        when(dbService.isAdmin("adminA")).thenReturn(true);

        controller.adminRemoveFavorite("adminA", "", "Home");

        verify(dbService).removeData("adminA", "Home", "favoritter");
    }

    @Test
    void adminUpdateFavoriteCoordinates_delegatesCorrectly() throws Exception {
        when(dbService.isAdmin("adminA")).thenReturn(true);

        controller.adminUpdateFavoriteCoordinates("adminA", "", "Home", 60.0, 11.0);

        verify(dbService).addCoordinatesToDestination("adminA", "Home", 60.0, 11.0);
    }

    @Test
    void listUserDestinations_validatesInput() {
        assertThrows(IllegalArgumentException.class, () -> controller.listUserDestinations("  "));
    }
}
