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
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"})
@RestController
@RequestMapping("/api/v1")
public class CustomerRestController {

    @Autowired
    private CustomerService customerService;

    /**
     * Método para obtener una lista sin paginación
     *
     * @return {@link ResponseEntity}
     */
    @GetMapping("/customers")
    public ResponseEntity<?> index() {
        List<Customer> customers;
        Map<String, Object> map = new HashMap<>();
        try {
            customers = customerService.findAll();
        } catch (DataAccessException e) {
            map.put("message", "Se produjo un error al cargar los clientes.");
            map.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    /**
     * Método para obtener una lista con paginación
     *
     * @param page    página actual
     * @param limit   elementos por página
     * @param orderBy campo en específico para ordenar
     * @param shape   ordenamiento ascendente o descendente
     * @return {@link ResponseEntity}
     */
    @GetMapping("/customers/paginated")
    public ResponseEntity<?> index(@RequestParam(name = "page", defaultValue = "0") Integer page,
                                   @RequestParam(name = "limit", defaultValue = "5") Integer limit,
                                   @RequestParam(name = "orderBy", defaultValue = "id") String orderBy,
                                   @RequestParam(name = "shape", defaultValue = "desc") String shape) {
        Page<Customer> customers;
        Map<String, Object> map = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, limit, Sort.Direction.DESC, orderBy);
            if (shape.equalsIgnoreCase("asc"))
                pageable = PageRequest.of(page, limit, Sort.Direction.ASC, orderBy);
            System.out.println("Pageable --> " + pageable);
            customers = customerService.findAll(pageable);
        } catch (DataAccessException e) {
            map.put("message", "Se produjo un error al cargar los clientes.");
            map.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    /**
     * Método para obtener un registro en específico
     *
     * @param id identificador único del registro
     * @return {@link ResponseEntity}
     */
    @GetMapping("/customers/{id}")
    public ResponseEntity<?> show(@PathVariable Long id) {
        Customer customer;
        Map<String, Object> map = new HashMap<>();
        try {
            customer = customerService.findById(id);
        } catch (DataAccessException e) {
            map.put("message", "Se produjo un error al obtener el cliente.");
            map.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
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
        Map<String, Object> map = new HashMap<>();
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(e -> "El campo [" + e.getField().toUpperCase() + "] " + e.getDefaultMessage())
                    .collect(Collectors.toList());
            map.put("errors", errors);
            return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
        }
        try {
            customerService.save(customer);
        } catch (DataAccessException e) {
            map.put("message", "Se produjo un error al insertar el cliente.");
            map.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        map.put("message", "El cliente se creó con éxito.");
        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Customer customer, BindingResult result, @PathVariable Long id) {
        Customer c = customerService.findById(id);
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
            c.setLastName(customer.getLastName());
            c.setEmail(customer.getEmail());
            c.setCreatedAt(customer.getCreatedAt());
            c.setActive(customer.getActive());
            customerService.save(c);
        } catch (DataAccessException e) {
            map.put("message", "Se produjo un error al actualizar el cliente.");
            map.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        map.put("message", "El cliente se actualizó con éxito.");
        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Map<String, Object> map = new HashMap<>();
        try {
            customerService.delete(id);
        } catch (DataAccessException e) {
            map.put("message", "Se produjo un error al eliminar el cliente.");
            map.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        map.put("message", "El cliente se eliminó con éxito.");
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @GetMapping("/customers/filter")
    public List<Customer> getData(@RequestParam(required = false, name = "fullName") String fullname,
                                  @RequestParam(required = false, name = "lastName") String lastname,
                                  @RequestParam(required = false, name = "email") String correo,
                                  @RequestParam(required = false, name = "active") Boolean activo,
                                  @RequestParam(required = false, name = "id") Integer key) {
        HashMap<String, Object> params = new HashMap<>();
        if (fullname != null)
            params.put("fullName", fullname);
        if (lastname != null)
            params.put("lastName", lastname);
        if (correo != null)
            params.put("email", correo);
        if (activo != null)
            params.put("active", activo);

        return customerService.getData(params);
    }
}
