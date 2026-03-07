package com.example.semilio.dictionary.repository;

import com.example.semilio.dictionary.model.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {
    List<Size> findAllByActiveTrueOrderBySortOrderAsc();
}
