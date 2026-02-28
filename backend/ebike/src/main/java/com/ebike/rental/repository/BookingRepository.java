package com.ebike.rental.repository;

import com.ebike.rental.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByBikeId(Long bikeId);
    List<Booking> findByStatus(Booking.BookingStatus status);
    List<Booking> findByUserIdAndStatus(Long userId, Booking.BookingStatus status);
    List<Booking> findByStartTimeGreaterThanAndEndTimeLessThan(LocalDateTime startTime, LocalDateTime endTime);
    List<Booking> findByBikeIdAndStatus(Long bikeId, Booking.BookingStatus status);
}
