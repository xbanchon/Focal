package com.xbanchon.imageservice.repository;

import com.xbanchon.imageservice.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {
    List<Image> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Image> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT i FROM Image i WHERE i.status = :status AND i.userId = :userId")
    List<Image> findImagesByStatus(
            @Param("userId") UUID userId,
            @Param("status") Image.ProcessingStatus status
    );
}
