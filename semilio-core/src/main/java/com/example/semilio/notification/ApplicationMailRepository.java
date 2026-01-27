package com.example.semilio.notification;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationMailRepository extends CrudRepository<ApplicationMail, Long> {
}
