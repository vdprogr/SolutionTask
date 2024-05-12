package com.vdprog.controller;

import com.vdprog.model.User;
import com.vdprog.service.UserService;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public  ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAll());
    }

    @GetMapping("/{id}")
    public  ResponseEntity<User> getUser(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@Valid @RequestBody User updatedUser, @PathVariable Integer id) {
        return ResponseEntity.ok(userService.updateUser(id, updatedUser));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> patchUser(@RequestBody User updatedUser, @PathVariable Integer id) {
        return ResponseEntity.ok(userService.patchUser(id, updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByBirthDateRange(
            @RequestParam @Valid LocalDate from,
            @RequestParam @Valid LocalDate to) {
        return ResponseEntity.ok(userService.searchUsersByBirthDateRange(from, to));
    }
}