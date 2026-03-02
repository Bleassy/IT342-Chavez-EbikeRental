package com.ebike.rental.service;

import com.ebike.rental.entity.Booking;
import com.ebike.rental.entity.Bike;
import com.ebike.rental.entity.User;
import com.ebike.rental.dto.BookingDTO;
import com.ebike.rental.repository.BookingRepository;
import com.ebike.rental.repository.BikeRepository;
import com.ebike.rental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BikeRepository bikeRepository;

    @Autowired
    private UserRepository userRepository;

    public Booking createBooking(Long userId, Long bikeId, LocalDateTime startTime, LocalDateTime endTime) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Bike> bike = bikeRepository.findById(bikeId);

        if (user.isEmpty() || bike.isEmpty()) {
            return null;
        }

        Booking booking = new Booking();
        booking.setUser(user.get());
        booking.setBike(bike.get());
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setTotalPrice(calculatePrice(bike.get(), startTime, endTime));

        // Update bike status
        bike.get().setStatus(Bike.BikeStatus.RENTED);
        bikeRepository.save(bike.get());

        return bookingRepository.save(booking);
    }

    public Optional<BookingDTO> getBookingById(Long id) {
        return bookingRepository.findById(id).map(this::convertToDTO);
    }

    public List<BookingDTO> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getBookingsByBikeId(Long bikeId) {
        return bookingRepository.findByBikeId(bikeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getUserBookingHistory(Long userId) {
        return bookingRepository.findByUserIdAndStatus(userId, Booking.BookingStatus.COMPLETED).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getActiveBookings() {
        return bookingRepository.findByStatus(Booking.BookingStatus.CONFIRMED).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean confirmBooking(Long bookingId) {
        return bookingRepository.findById(bookingId).map(booking -> {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            return true;
        }).orElse(false);
    }

    public boolean completeBooking(Long bookingId) {
        return bookingRepository.findById(bookingId).map(booking -> {
            booking.setStatus(Booking.BookingStatus.COMPLETED);
            booking.getBike().setStatus(Bike.BikeStatus.AVAILABLE);
            bikeRepository.save(booking.getBike());
            bookingRepository.save(booking);
            return true;
        }).orElse(false);
    }

    public boolean cancelBooking(Long bookingId) {
        return bookingRepository.findById(bookingId).map(booking -> {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            booking.getBike().setStatus(Bike.BikeStatus.AVAILABLE);
            bikeRepository.save(booking.getBike());
            bookingRepository.save(booking);
            return true;
        }).orElse(false);
    }

    public boolean deleteBooking(Long bookingId) {
        if (bookingRepository.existsById(bookingId)) {
            bookingRepository.deleteById(bookingId);
            return true;
        }
        return false;
    }

    // Private helper method to calculate price
    private BigDecimal calculatePrice(Bike bike, LocalDateTime startTime, LocalDateTime endTime) {
        long hours = ChronoUnit.HOURS.between(startTime, endTime);
        long days = hours / 24;
        long remainingHours = hours % 24;

        BigDecimal dailyPrice = bike.getPricePerDay().multiply(BigDecimal.valueOf(days));
        BigDecimal hourlyPrice = bike.getPricePerHour().multiply(BigDecimal.valueOf(remainingHours));

        return dailyPrice.add(hourlyPrice);
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUser().getId());
        dto.setUserName(booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
        dto.setUserEmail(booking.getUser().getEmail());
        dto.setBikeId(booking.getBike().getId());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setStatus(booking.getStatus().toString());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setNotes(booking.getNotes());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }
}
