package com.deokhugam.backend.controller;

import com.deokhugam.backend.dto.cursor.CursorPageResponsePowerUserDto;
import com.deokhugam.backend.dto.dashboard.PowerUserQuery;
import com.deokhugam.backend.dto.user.UserDto;
import com.deokhugam.backend.dto.user.UserLoginRequest;
import com.deokhugam.backend.dto.user.UserRegisterRequest;
import com.deokhugam.backend.dto.user.UserUpdateRequest;
import com.deokhugam.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegisterRequest request) {
        UserDto user = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody UserLoginRequest request) {
        UserDto user = userService.login(request);

        return ResponseEntity.status(HttpStatus.OK)
                .header("Deokhugam-Request-User-ID", user.id().toString())
                .body(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> findByUserId(@PathVariable UUID userId) {
        UserDto user = userService.find(userId);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> update(
            @PathVariable UUID userId,
            @RequestBody String nicknameRaw
    ) {
        // JSON 문자열로 올 경우 따옴표 제거
        String nickname = nicknameRaw.replace("\"", "").trim();

        UserUpdateRequest request = new UserUpdateRequest(nickname);
        UserDto user = userService.update(userId, request);

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    public void softDelete(@PathVariable UUID userId) {
        userService.softDelete(userId);
    }

    @DeleteMapping("/{userId}/hard")
    public void hardDelete(@PathVariable UUID userId) {
        userService.hardDelete(userId);
    }

    @GetMapping("/power")
    public ResponseEntity<CursorPageResponsePowerUserDto> findByPowerUser(PowerUserQuery query) {
        var powerUSer = userService.findByPowerUser(query);
        return ResponseEntity.status(HttpStatus.OK).body(powerUSer);
    }
}
