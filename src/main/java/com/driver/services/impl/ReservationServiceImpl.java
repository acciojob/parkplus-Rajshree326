package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ParkingLotRepository;
import com.driver.repository.ReservationRepository;
import com.driver.repository.SpotRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    UserRepository userRepository3;
    @Autowired
    SpotRepository spotRepository3;
    @Autowired
    ReservationRepository reservationRepository3;
    @Autowired
    ParkingLotRepository parkingLotRepository3;
    @Override
    public Reservation reserveSpot(Integer userId, Integer parkingLotId, Integer timeInHours, Integer numberOfWheels) throws Exception {
        // Find the user
        User user = userRepository3.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot make reservation"));

        // Find the parking lot
        ParkingLot parkingLot = parkingLotRepository3.findById(parkingLotId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot make reservation"));

        // Get the available spots in the parking lot
        List<Spot> availableSpots = parkingLot.getSpotList();

        List<Spot> filteredSpots = new ArrayList<>();
        for (Spot spot : availableSpots) {
            SpotType spotType = spot.getSpotType();  // Assuming SpotType has a method to get the number of wheels
            boolean isCompatible = (numberOfWheels == 2 && spotType == SpotType.TWO_WHEELER) ||
                    (numberOfWheels == 4 && spotType == SpotType.FOUR_WHEELER) ||
                    (numberOfWheels != 2 && numberOfWheels != 4 && spotType == SpotType.OTHERS);

            if (isCompatible) {
                filteredSpots.add(spot);
            }
        }

        if (filteredSpots.isEmpty()) {
            throw new IllegalArgumentException("Cannot make reservation");
        }

        // Find the spot with the minimum total price
        Spot minPriceSpot = filteredSpots.stream()
                .min(Comparator.comparingInt(spot -> spot.getPricePerHour() * timeInHours))
                .orElseThrow(() -> new RuntimeException("Cannot make reservation"));


        // Create a reservation
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setSpot(minPriceSpot);
        reservation.setNumberOfHours(timeInHours);

        //update the user's reservation list
        List<Reservation> userReservationList = user.getReservationList();
        userReservationList.add(reservation);
        user.setReservationList(userReservationList);

        userRepository3.save(user);

        // Update spot status
        minPriceSpot.setOccupied(true);
        minPriceSpot.setParkingLot(parkingLot);
        //append one more reservation to the spot
        List<Reservation> allReservations= minPriceSpot.getReservationList();
        allReservations.add(reservation);
        minPriceSpot.setReservationList(allReservations);

        spotRepository3.save(minPriceSpot);

        // Save the reservation to the repository
        return reservationRepository3.save(reservation);
    }

}
