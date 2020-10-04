package com.llira.springbootbackendapirest.repository;

import com.llira.springbootbackendapirest.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
