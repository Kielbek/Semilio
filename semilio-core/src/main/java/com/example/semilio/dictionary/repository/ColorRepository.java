package com.example.semilio.dictionary.repository;

import com.example.semilio.dictionary.model.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
    List<Color> findAllByActiveTrueOrderBySortOrderAsc();
}
