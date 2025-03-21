package com.example.javaprofinal.repository;

import com.example.javaprofinal.entity.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LimitRepository extends JpaRepository<Limit, Long> {

    @Modifying
    @Query("UPDATE Limit lmt SET lmt.dailyLimit = :dailyLimit")
    void resetLimits(@Param("dailyLimit") Integer dailyLimit);

}
