package com.ebike.rental.bike;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BikeService {

    @Autowired
    private BikeRepository bikeRepository;

    public Bike createBike(Bike bike) {
        return bikeRepository.save(bike);
    }

    public Optional<BikeDTO> getBikeById(Long id) {
        return bikeRepository.findById(id).map(this::convertToDTO);
    }

    public Optional<BikeDTO> getBikeByCode(String bikeCode) {
        return bikeRepository.findByBikeCode(bikeCode).map(this::convertToDTO);
    }

    public List<BikeDTO> getAllBikes() {
        return bikeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BikeDTO> getAvailableBikes() {
        return bikeRepository.findByStatus(Bike.BikeStatus.AVAILABLE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BikeDTO> getBikesByLocation(String location) {
        return bikeRepository.findByLocation(location).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BikeDTO> getBikesByType(Bike.BikeType type) {
        return bikeRepository.findByType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BikeDTO> getBikesByCondition(Bike.BikeCondition condition) {
        return bikeRepository.findByCondition(condition).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Bike updateBike(Long id, Bike bikeDetails) {
        return bikeRepository.findById(id).map(bike -> {
            if (bikeDetails.getModel() != null) bike.setModel(bikeDetails.getModel());
            if (bikeDetails.getBrand() != null) bike.setBrand(bikeDetails.getBrand());
            if (bikeDetails.getColor() != null) bike.setColor(bikeDetails.getColor());
            if (bikeDetails.getDescription() != null) bike.setDescription(bikeDetails.getDescription());
            if (bikeDetails.getPricePerHour() != null) bike.setPricePerHour(bikeDetails.getPricePerHour());
            if (bikeDetails.getPricePerDay() != null) bike.setPricePerDay(bikeDetails.getPricePerDay());
            if (bikeDetails.getStatus() != null) bike.setStatus(bikeDetails.getStatus());
            if (bikeDetails.getBatteryLevel() != null) bike.setBatteryLevel(bikeDetails.getBatteryLevel());
            if (bikeDetails.getLocation() != null) bike.setLocation(bikeDetails.getLocation());
            if (bikeDetails.getCondition() != null) bike.setCondition(bikeDetails.getCondition());
            if (bikeDetails.getImageUrl() != null) bike.setImageUrl(bikeDetails.getImageUrl());
            return bikeRepository.save(bike);
        }).orElse(null);
    }

    public boolean deleteBike(Long id) {
        if (bikeRepository.existsById(id)) {
            bikeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean updateBikeStatus(Long id, Bike.BikeStatus status) {
        return bikeRepository.findById(id).map(bike -> {
            bike.setStatus(status);
            bikeRepository.save(bike);
            return true;
        }).orElse(false);
    }

    private BikeDTO convertToDTO(Bike bike) {
        BikeDTO dto = new BikeDTO();
        dto.setId(bike.getId());
        dto.setBikeCode(bike.getBikeCode());
        dto.setModel(bike.getModel());
        dto.setBrand(bike.getBrand());
        dto.setColor(bike.getColor());
        dto.setYear(bike.getYear());
        dto.setType(bike.getType().toString());
        dto.setPricePerHour(bike.getPricePerHour());
        dto.setPricePerDay(bike.getPricePerDay());
        dto.setStatus(bike.getStatus().toString());
        dto.setDescription(bike.getDescription());
        dto.setImageUrl(bike.getImageUrl());
        dto.setCondition(bike.getCondition().toString());
        dto.setBatteryLevel(bike.getBatteryLevel());
        dto.setLocation(bike.getLocation());
        dto.setCreatedAt(bike.getCreatedAt());
        return dto;
    }
}
