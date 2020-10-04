package com.llira.springbootbackendapirest.controller;

import com.llira.springbootbackendapirest.entity.Customer;
import com.llira.springbootbackendapirest.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/api/v1")
public class CustomerRestController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/customers")
    public ResponseEntity<?> index() {
        List<Customer> customers;
        Map<String, Object> map = new HashMap<>();
        try {
            customers = customerService.findAll();
        } catch (DataAccessException e) {
            map.put("message", "Se produjo un error al cargar los clientes.");
            map.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    /**
     *
     * @param page
     * @param limit
     * @param orderBy
     * @return ResponseEntity
     */
    @GetMapping("/customers/page/{page}")
    public ResponseEntity<?> index(@PathVariable Integer page,
                                   @RequestParam(name = "limit", defaultValue = "5") Integer limit,
                                   @RequestParam(name = "orderBy", defaultValue = "id") String orderBy) {
        Page<Customer> customers;
        Map<String, Object> map = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, limit, Sort.by(orderBy).descending());
            customers = customerService.findAll(pageable);
        } catch (DataAccessException e) {
            map.put("message", "Se produjo un error al cargar los clientes.");
            map.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<?> show(@PathVariable Long id) {
        Customer customer;
        Map<String, Object> map = new HashMap<>();
        try {
            customer = customerService.findById(id);
        } catch (NoSuchElementException e) {
            map.put("message", "Se produjo un error al obtener el cliente.");
            map.put("error", e.getMessage());
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DataAccessException e) {
            map.put("message", "Se produjo un error al obtener el cliente.");
            map.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (customer == null) {
            map.put("message", "No se encontró el cliente con id: ".concat(id.toString()));
            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    @PostMapping("/customers")
    public ResponseEntity<?> create(@Valid @RequestBody Customer customer, BindingResult result) {
        Customer c;
        Map<String, Object> map = new HashMap<>();
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(error -> "El campo [" + error.getField().toUpperCase() + "] " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            map.put("errors", errors);
            return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
        }
        try {
            c = customerService.save(customer);
        } catch (DataAccessException e) {
            map.put("message", "Se produjo un error al insertar el cliente.");
            map.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        map.put("message", "El cliente se creó con éxito.");
        map.put("customer", c);
        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Customer customer, BindingResult result, @PathVariable Long id) {
        Customer c = customerService.findById(id);
        Customer c1;
        Map<String, Object> map = new HashMap<>();
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(error -> "El campo [" + error.getField().toUpperCase() + "] " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            map.put("errors", errors);
            return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
        }
        if (c == null) {
            map.put("message", "No se puede actualizar el cliente con id: ".concat(id.toString()));
            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }
        try {
            c.setFullName(customer.getFullName());
            System.out.println(customer.getFullName());
            c.setLastName(customer.getLastName());
            c.setEmail(customer.getEmail());
            c.setCreatedAt(customer.getCreatedAt());
            c1 = customerService.save(c);
        } catch (DataAccessException e) {
            map.put("message", "Se produjo un error al actualizar el cliente.");
            map.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        map.put("message", "El cliente se actualizó con éxito.");
        map.put("customer", c1);
        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Map<String, Object> map = new HashMap<>();
        try {
            customerService.delete(id);
        } catch (DataAccessException e) {
            map.put("message", "Se produjo un error al eliminar el cliente.");
            map.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        map.put("message", "El cliente se eliminó con éxito.");
        return new ResponseEntity<>(map, HttpStatus.OK);
    }
}
