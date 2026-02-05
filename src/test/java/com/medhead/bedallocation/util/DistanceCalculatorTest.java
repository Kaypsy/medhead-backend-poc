package com.medhead.bedallocation.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class DistanceCalculatorTest {

    @Test
    void haversineKm_sameCoordinates_returnsZero() {
        double lat = 48.8566;
        double lon = 2.3522;
        double distance = DistanceCalculator.haversineKm(lat, lon, lat, lon);
        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    void haversineKm_parisToSartrouville_returnsCorrectDistance() {
        // Paris (Notre-Dame): 48.8529, 2.3500
        // Sartrouville (Gare): 48.9388, 2.1585
        // Distance théorique environ 16.9 km
        double distance = DistanceCalculator.haversineKm(48.8529, 2.3500, 48.9388, 2.1585);
        assertThat(distance).isCloseTo(16.9, offset(0.5));
    }

    @Test
    void haversineKm_antipodes_returnsEarthSemiCircumference() {
        // Paris: 48.8566, 2.3522
        // Antipode de Paris: -48.8566, -177.6478 (180 - 2.3522 = 177.6478)
        double distance = DistanceCalculator.haversineKm(48.8566, 2.3522, -48.8566, -177.6478);
        // Circonférence ~ 40075 km, semi ~ 20037 km
        // Haversine avec R=6371 donne PI * R = 20015 km
        assertThat(distance).isCloseTo(20015.0, offset(5.0));
    }

    @Test
    void haversineKm_poles_returnsDistance() {
        // North Pole to South Pole
        double distance = DistanceCalculator.haversineKm(90.0, 0.0, -90.0, 0.0);
        assertThat(distance).isCloseTo(20015.0, offset(1.0));
    }

    @Test
    void estimateTravelTimeMinutes_returnsReasonableValue() {
        // 50 km at 50 km/h should be 60 minutes
        assertThat(DistanceCalculator.estimateTravelTimeMinutes(50.0)).isEqualTo(60);
        // 25 km at 50 km/h should be 30 minutes
        assertThat(DistanceCalculator.estimateTravelTimeMinutes(25.0)).isEqualTo(30);
        // 0 km should be 1 minute (minimum)
        assertThat(DistanceCalculator.estimateTravelTimeMinutes(0.0)).isEqualTo(1);
    }
}
