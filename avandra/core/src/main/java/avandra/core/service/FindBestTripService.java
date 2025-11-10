package avandra.core.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import avandra.core.DTO.TripPartDTO;

/**
 * Goes through all trip alternatives (each trip = list of TripParts)
 * Calculates a score for each trip and picks the one with the lowest score
 * Scoring can be adjusted for user preferences (like reducing walking importance)
 */
public final class FindBestTripService {

    // weight for each factor in the trip score
    private double durationWeight = 1.0;   // how much total duration matters
    private double transferWeight = 8.0;   // how much each transfer counts
    private double walkingWeight = 0.8;    // how much walking distance matters (per 100m)

    /**
     * lets us create a default service with standard weights
     */
    public FindBestTripService() {}

    /**
     *
     * @param durationWeight  weight for total duration (in minutes)
     * @param transferWeight  weight for number of transfers
     * @param walkingWeight   weight for walking distance (per 100 meters)
     */
    public FindBestTripService(double durationWeight, double transferWeight, double walkingWeight) {
        this.durationWeight = durationWeight;
        this.transferWeight = transferWeight;
        this.walkingWeight = walkingWeight;
    }

    /**
     * picks the best trip (lowest score)
     * @param tripAlternatives list of trips, each trip being a list of TripParts
     * @return the trip with the lowest score or null if no trips
     */
    public List<TripPartDTO> pickBest(List<List<TripPartDTO>> tripAlternatives) {
        if (tripAlternatives == null || tripAlternatives.isEmpty()) return null;

        List<TripPartDTO> bestTrip = null;
        double bestScore = Double.POSITIVE_INFINITY;

        for (List<TripPartDTO> trip : tripAlternatives) {
            double score = calculateTripScore(trip);
            if (score < bestScore) {
                bestScore = score;
                bestTrip = trip;
            }
        }
        return bestTrip;
    }

    /**
     * calculates a score for a given trip
     * lower score = better trip
     */
    private double calculateTripScore(List<TripPartDTO> tripLegs) {
        int totalWalkingMeters = 0;
        int totalBoardings = 0;

        for (TripPartDTO leg : tripLegs) {
            String mode = leg.getLegTransportMode();
            if (mode != null && (mode.equalsIgnoreCase("foot") || mode.equalsIgnoreCase("walk"))) {
                totalWalkingMeters += leg.getTravelDistance();
            }
            if (leg.getLineId() != null && !leg.getLineId().isBlank()) {
                totalBoardings++;
            }
        }

        // first boarding doesn’t count as a transfer
        int totalTransfers = Math.max(0, totalBoardings - 1);

        // find the first departure and last arrival to estimate total trip duration
        LocalDateTime firstDeparture = null;
        LocalDateTime lastArrival = null;
        for (TripPartDTO leg : tripLegs) {
            if (firstDeparture == null && leg.getExpectedDeparture() != null) {
                firstDeparture = leg.getExpectedDeparture();
            }
            if (leg.getExpectedArrival() != null) {
                lastArrival = leg.getExpectedArrival();
            }
        }

        long totalMinutes = 0;
        if (firstDeparture != null && lastArrival != null) {
            totalMinutes = Duration.between(firstDeparture, lastArrival).toMinutes();
        }

        // combine all factors using current weights
        double totalScore =
                (totalMinutes * durationWeight) +
                        (totalTransfers * transferWeight) +
                        ((totalWalkingMeters / 100.0) * walkingWeight);

        return totalScore;
    }


    public void setDurationWeight(double durationWeight) {
        this.durationWeight = durationWeight;
    }

    public void setTransferWeight(double transferWeight) {
        this.transferWeight = transferWeight;
    }

    public void setWalkingWeight(double walkingWeight) {
        this.walkingWeight = walkingWeight;
    }
}
