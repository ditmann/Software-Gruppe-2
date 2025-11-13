package avandra.test;

import avandra.core.DTO.CoordinateDTO;
import avandra.core.port.LocationPort;
import avandra.core.service.DBService;
import avandra.core.service.JourneyPlannerService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 * Tests how the journey planner service puts everything together
 * Checks that start and destination are handled in the right order
 * Makes sure it passes data to the ports and forwards any important errors
 * Skips later steps if an earlier dependency fails
 * Helps confirm that the controller layer gets clear error messages
 */

class JourneyPlannerServiceTest {

    @Test
    void returnsStartThenEnd_andCallsDepsOnceInOrder() throws Exception {
        LocationPort location = mock(LocationPort.class);
        DBService db = mock(DBService.class);

        CoordinateDTO start = new CoordinateDTO();
        CoordinateDTO end = new CoordinateDTO();

        when(location.currentCoordinate()).thenReturn(start);
        when(db.searchDestination("user1", "dest42")).thenReturn(end);

        JourneyPlannerService svc = new JourneyPlannerService(location, db);

        List<CoordinateDTO> result = svc.fetchStartingPointAndEndPoint("user1", "dest42");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(start, result.get(0));
        assertSame(end, result.get(1));

        InOrder inOrder = inOrder(location, db);
        inOrder.verify(location).currentCoordinate();
        inOrder.verify(db).searchDestination("user1", "dest42");
        verifyNoMoreInteractions(location, db);
    }

    @Test
    void propagatesExceptionFromLocation_andSkipsDbCall() throws Exception {
        LocationPort location = mock(LocationPort.class);
        DBService db = mock(DBService.class);

        when(location.currentCoordinate()).thenThrow(new RuntimeException("GPS down"));

        JourneyPlannerService svc = new JourneyPlannerService(location, db);

        assertThrows(RuntimeException.class,
                () -> svc.fetchStartingPointAndEndPoint("u", "d"));

        verify(location).currentCoordinate();
        verifyNoInteractions(db);
    }

    @Test
    void propagatesExceptionFromDb_afterCallingLocation() throws Exception {
        LocationPort location = mock(LocationPort.class);
        DBService db = mock(DBService.class);

        when(location.currentCoordinate()).thenReturn(new CoordinateDTO());
        when(db.searchDestination("u", "d")).thenThrow(new IllegalStateException("DB error"));

        JourneyPlannerService svc = new JourneyPlannerService(location, db);

        assertThrows(IllegalStateException.class,
                () -> svc.fetchStartingPointAndEndPoint("u", "d"));

        InOrder inOrder = inOrder(location, db);
        inOrder.verify(location).currentCoordinate();
        inOrder.verify(db).searchDestination("u", "d");
    }

    @Test
    void allowsNullEndPoint_withoutExploding() throws Exception {
        LocationPort location = mock(LocationPort.class);
        DBService db = mock(DBService.class);

        CoordinateDTO start = new CoordinateDTO();
        when(location.currentCoordinate()).thenReturn(start);
        when(db.searchDestination(anyString(), anyString())).thenReturn(null);

        JourneyPlannerService svc = new JourneyPlannerService(location, db);

        List<CoordinateDTO> result = svc.fetchStartingPointAndEndPoint("user", "dest");
        assertEquals(2, result.size());
        assertSame(start, result.get(0));
        assertNull(result.get(1));
    }
}
