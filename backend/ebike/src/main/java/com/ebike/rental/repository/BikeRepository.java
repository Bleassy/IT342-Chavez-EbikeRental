package com.ebike.rental.repository;

import com.ebike.rental.entity.Bike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface BikeRepository extends JpaRepository<Bike, Long> {
    Optional<Bike> findByBikeCode(String bikeCode);
    List<Bike> findByStatus(Bike.BikeStatus status);
    List<Bike> findByLocation(String location);
    List<Bike> findByType(Bike.BikeType type);
    List<Bike> findByCondition(Bike.BikeCondition condition);
}
