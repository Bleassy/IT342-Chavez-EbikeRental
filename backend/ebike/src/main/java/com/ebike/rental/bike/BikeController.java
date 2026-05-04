package com.ebike.rental.bike;

import com.ebike.rental.dto.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/bikes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BikeController {

    @Autowired
    private BikeService bikeService;

    @PostMapping
    public ResponseEntity<ApiResponse<BikeDTO>> createBike(@RequestBody Bike bike) {
        try {
            Bike createdBike = bikeService.createBike(bike);
            // Convert to DTO to avoid circular references with bookings
            Optional<BikeDTO> bikeDTO = bikeService.getBikeById(createdBike.getId());
            if (bikeDTO.isPresent()) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>(true, "Bike created successfully", bikeDTO.get()));
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Bike created successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create bike: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BikeDTO>>> getAllBikes() {
        try {
            List<BikeDTO> bikes = bikeService.getAllBikes();
            return ResponseEntity.ok(new ApiResponse<>(true, "Bikes retrieved successfully", bikes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve bikes: " + e.getMessage()));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<BikeDTO>>> getAvailableBikes() {
        try {
            List<BikeDTO> bikes = bikeService.getAvailableBikes();
            return ResponseEntity.ok(new ApiResponse<>(true, "Available bikes retrieved", bikes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve available bikes: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BikeDTO>> getBikeById(@PathVariable Long id) {
        try {
            Optional<BikeDTO> bike = bikeService.getBikeById(id);
            if (bike.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Bike retrieved successfully", bike.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Bike not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve bike: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{bikeCode}")
    public ResponseEntity<ApiResponse<BikeDTO>> getBikeByCode(@PathVariable String bikeCode) {
        try {
            Optional<BikeDTO> bike = bikeService.getBikeByCode(bikeCode);
            if (bike.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Bike retrieved successfully", bike.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Bike not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve bike: " + e.getMessage()));
        }
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<ApiResponse<List<BikeDTO>>> getBikesByLocation(@PathVariable String location) {
        try {
            List<BikeDTO> bikes = bikeService.getBikesByLocation(location);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bikes retrieved successfully", bikes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve bikes: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BikeDTO>> updateBike(@PathVariable Long id, @RequestBody Bike bikeDetails) {
        try {
            Bike updatedBike = bikeService.updateBike(id, bikeDetails);
            if (updatedBike != null) {
                // Convert to DTO to avoid circular references with bookings
                Optional<BikeDTO> bikeDTO = bikeService.getBikeById(id);
                if (bikeDTO.isPresent()) {
                    return ResponseEntity.ok(new ApiResponse<>(true, "Bike updated successfully", bikeDTO.get()));
                }
                // Fallback if DTO conversion fails
                return ResponseEntity.ok(new ApiResponse<>(true, "Bike updated successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Bike not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to update bike: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBike(@PathVariable Long id) {
        try {
            if (bikeService.deleteBike(id)) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Bike deleted successfully", ""));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Bike not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to delete bike: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateBikeStatus(@PathVariable Long id, @RequestParam Bike.BikeStatus status) {
        try {
            if (bikeService.updateBikeStatus(id, status)) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Bike status updated successfully", ""));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Bike not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to update bike status: " + e.getMessage()));
        }
    }
}
