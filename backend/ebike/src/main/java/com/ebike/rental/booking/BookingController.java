package com.ebike.rental.booking;

import com.ebike.rental.dto.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @RequestParam Long userId,
            @RequestParam Long bikeId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        try {
            Booking booking = bookingService.createBooking(userId, bikeId, startTime, endTime);
            if (booking != null) {
                BookingDTO dto = new BookingDTO();
                dto.setId(booking.getId());
                dto.setUserId(booking.getUser().getId());
                dto.setBikeId(booking.getBike().getId());
                dto.setStartTime(booking.getStartTime());
                dto.setEndTime(booking.getEndTime());
                dto.setStatus(booking.getStatus().toString());
                dto.setTotalPrice(booking.getTotalPrice());
                dto.setCreatedAt(booking.getCreatedAt());
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>(true, "Booking created successfully", dto));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Invalid user or bike ID"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create booking: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getAllBookings() {
        try {
            List<BookingDTO> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(new ApiResponse<>(true, "Bookings retrieved successfully", bookings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve bookings: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingById(@PathVariable Long id) {
        try {
            Optional<BookingDTO> booking = bookingService.getBookingById(id);
            if (booking.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Booking retrieved successfully", booking.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Booking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve booking: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getBookingsByUserId(@PathVariable Long userId) {
        try {
            List<BookingDTO> bookings = bookingService.getBookingsByUserId(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "User bookings retrieved successfully", bookings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve user bookings: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getUserBookingHistory(@PathVariable Long userId) {
        try {
            List<BookingDTO> bookings = bookingService.getUserBookingHistory(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Booking history retrieved successfully", bookings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve booking history: " + e.getMessage()));
        }
    }

    @GetMapping("/bike/{bikeId}")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getBookingsByBikeId(@PathVariable Long bikeId) {
        try {
            List<BookingDTO> bookings = bookingService.getBookingsByBikeId(bikeId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bike bookings retrieved successfully", bookings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve bike bookings: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getActiveBookings() {
        try {
            List<BookingDTO> bookings = bookingService.getActiveBookings();
            return ResponseEntity.ok(new ApiResponse<>(true, "Active bookings retrieved successfully", bookings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve active bookings: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<String>> confirmBooking(@PathVariable Long id) {
        try {
            if (bookingService.confirmBooking(id)) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Booking confirmed successfully", ""));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Booking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to confirm booking: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<String>> completeBooking(@PathVariable Long id) {
        try {
            if (bookingService.completeBooking(id)) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Booking completed successfully", ""));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Booking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to complete booking: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelBooking(@PathVariable Long id) {
        try {
            if (bookingService.cancelBooking(id)) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Booking cancelled successfully", ""));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Booking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to cancel booking: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBooking(@PathVariable Long id) {
        try {
            if (bookingService.deleteBooking(id)) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Booking deleted successfully", ""));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Booking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to delete booking: " + e.getMessage()));
        }
    }
}
