package com.driver.repository;

import com.driver.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface PassengerRepository extends JpaRepository<Passenger,Integer> {

}
