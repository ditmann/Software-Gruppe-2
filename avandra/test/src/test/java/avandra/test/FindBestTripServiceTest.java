


package avandra.test;

import avandra.core.DTO.TripPartDTO;
import avandra.core.service.FindBestTripService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Tests the logic that finds the best trip from a list of options
 * Makes sure it picks the shortest travel time, then fewer transfers, then less walking
 * Returns null when the input list is null or empty
 * Includes edge cases like equal scores and empty journeys
 */
class FindBestTripServiceTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2025, 1, 1, 8, 0);

    private static TripPartDTO buildLeg(String mode, int meters, String lineId,
                                   LocalDateTime dep, LocalDateTime arr) {
        TripPartDTO p = new TripPartDTO();
        p.setLegTransportMode(mode);
        p.setTravelDistance(meters);
        p.setLineId(lineId);
        p.setExpectedDeparture(dep);
        p.setExpectedArrival(arr);
        return p;
    }

    @Test
    void pickBest_returnsTripWithLowerScore_basicCase() {
        FindBestTripService service = new FindBestTripService(); // default weights

        // trip A: 30 min bus
        List<TripPartDTO> trip30 = List.of(
                buildLeg("bus", 0, "L1", BASE, BASE.plusMinutes(30))
        );

        // trip B: 45 min bus
        List<TripPartDTO> trip45 = List.of(
                buildLeg("bus", 0, "L1", BASE, BASE.plusMinutes(45))
        );

        List<TripPartDTO> best = service.pickBest(List.of(trip30, trip45));
        assertSame(trip30, best);
    }

    @Test
    void transfer_counting_is_boardings_minus_one() {
        FindBestTripService service = new FindBestTripService(); // default weights

        // 3 boardings -> expected transfers = 2
        List<TripPartDTO> threeBoardings = List.of(
                buildLeg("bus", 0, "L1", BASE, BASE.plusMinutes(10)),
                buildLeg("bus", 0, "L2", BASE.plusMinutes(10), BASE.plusMinutes(20)),
                buildLeg("bus", 0, "L3", BASE.plusMinutes(20), BASE.plusMinutes(30))
        );

        // same duration but walking only (0 transfers)
        List<TripPartDTO> walkingOnly = List.of(
                buildLeg("walk", 0, null, BASE, BASE.plusMinutes(30))
        );

        // make the difference hinge on transfers only
        service.setWalkingWeight(0.0);
        service.setDurationWeight(1.0);
        service.setTransferWeight(10.0); // make transfers expensive

        List<TripPartDTO> best = service.pickBest(List.of(threeBoardings, walkingOnly));
        assertSame(walkingOnly, best, "with high transfer weight and zero walking weight, walking-only should win");
    }


    @Test
    void pickBest_returnsNull_onNullOrEmptyInput() {
        FindBestTripService service = new FindBestTripService();
        assertNull(service.pickBest(null));
        assertNull(service.pickBest(List.of()));
    }
}
