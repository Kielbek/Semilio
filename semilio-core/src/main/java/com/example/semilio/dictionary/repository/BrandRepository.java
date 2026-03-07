package com.example.semilio.dictionary.repository;

import com.example.semilio.dictionary.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findAllByActiveTrueOrderByNameAsc();
}
