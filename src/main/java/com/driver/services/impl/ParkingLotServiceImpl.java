package com.driver.services.impl;

import com.driver.model.ParkingLot;
import com.driver.model.Spot;
import com.driver.model.SpotType;
import com.driver.repository.ParkingLotRepository;
import com.driver.repository.SpotRepository;
import com.driver.services.ParkingLotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ParkingLotServiceImpl implements ParkingLotService {
    @Autowired
    ParkingLotRepository parkingLotRepository1;
    @Autowired
    SpotRepository spotRepository1;
    @Override
    public ParkingLot addParkingLot(String name, String address) {
        ParkingLot newParkingLot = new ParkingLot();
        newParkingLot.setName(name);
        newParkingLot.setAddress(name);
        return parkingLotRepository1.save(newParkingLot);
    }

    @Override
    public Spot addSpot(int parkingLotId, Integer numberOfWheels, Integer pricePerHour) {
        return parkingLotRepository1.findById(parkingLotId)
                .map(parkingLot -> {
                    Spot newSpot = new Spot();
                    newSpot.setParkingLot(parkingLot);
                    newSpot.setPricePerHour(pricePerHour);

                    if (numberOfWheels == 2) newSpot.setSpotType(SpotType.TWO_WHEELER);
                    else if (numberOfWheels == 4) newSpot.setSpotType(SpotType.FOUR_WHEELER);
                    else newSpot.setSpotType(SpotType.OTHERS);

                    //add this spot to the parking lot as well
                    List<Spot> parkingLotSpots = parkingLot.getSpotList();
                    parkingLotSpots.add(newSpot);
                    parkingLot.setSpotList(parkingLotSpots);

                    parkingLotRepository1.save(parkingLot);

                    return spotRepository1.save(newSpot);
                })
                .orElseThrow(() -> new IllegalArgumentException("Parking lot not found"));
    }

    @Override
    public void deleteSpot(int spotId) {
         spotRepository1.deleteById(spotId);
    }

    @Override
    public Spot updateSpot(int parkingLotId, int spotId, int pricePerHour) {
         Spot updatedSpot = spotRepository1.findById(spotId)
                 .orElseThrow(() -> new IllegalArgumentException("Spot Not Found"));

         ParkingLot parkingLot = parkingLotRepository1.findById(parkingLotId)
                 .orElseThrow(() -> new IllegalArgumentException("ParkingLot not found"));

         ParkingLot oldParkingLot = parkingLotRepository1.findById(updatedSpot.getParkingLot().getId())
                 .orElseThrow(() -> new IllegalArgumentException("Old Parking Lot not found"));

         //setting the spots list on the old parking lot
         List<Spot> oldSpots = oldParkingLot.getSpotList();
         oldSpots.remove(updatedSpot);
         oldParkingLot.setSpotList(oldSpots);
         parkingLotRepository1.save(oldParkingLot);


        //updating the spot
        updatedSpot.setParkingLot(parkingLot);
        updatedSpot.setPricePerHour(pricePerHour);


        //adding new spot to new parking lot
         List<Spot> newSpots = parkingLot.getSpotList();
         newSpots.add(updatedSpot);
         parkingLot.setSpotList(newSpots);
         parkingLotRepository1.save(parkingLot);


         return spotRepository1.save(updatedSpot);
    }

    @Override
    public void deleteParkingLot(int parkingLotId) {
          parkingLotRepository1.deleteById(parkingLotId);
    }
}
