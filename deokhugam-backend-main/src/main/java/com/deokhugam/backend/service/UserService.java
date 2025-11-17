package com.deokhugam.backend.service;

import com.deokhugam.backend.dto.cursor.CursorPageResponsePowerUserDto;
import com.deokhugam.backend.dto.dashboard.PowerUserQuery;
import com.deokhugam.backend.dto.user.UserDto;
import com.deokhugam.backend.dto.user.UserLoginRequest;
import com.deokhugam.backend.dto.user.UserRegisterRequest;
import com.deokhugam.backend.dto.user.UserUpdateRequest;
import com.deokhugam.backend.entity.User;
import com.deokhugam.backend.exception.user.InvalidCredentialsException;
import com.deokhugam.backend.exception.user.UserAlreadyExistsException;
import com.deokhugam.backend.exception.user.UserNotFoundException;
import com.deokhugam.backend.mapper.UserMapper;
import com.deokhugam.backend.repository.UserRepository;
import com.deokhugam.backend.repository.query.PowerUserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PowerUserQueryRepository powerUserQueryRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto create(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw UserAlreadyExistsException.withEmail(request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(request.password())
                .nickname(request.nickname())
                .build();

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserDto find(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (user.getDeletedAt() != null) {
            throw UserNotFoundException.withMessage("사용자를 찾을 수 없습니다");
        }

        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public CursorPageResponsePowerUserDto findByPowerUser(PowerUserQuery query) {
         return powerUserQueryRepository.findByPowerUserIdWithCursor(query);
    }

    @Transactional
    public UserDto update(UUID userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (user.getDeletedAt() != null) {
            throw UserNotFoundException.withMessage("사용자를 찾을 수 없습니다");
        }

        user.update(request);
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public void softDelete(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.softDelete();
    }

    @Transactional
    public void hardDelete(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        userRepository.delete(user);
    }

    @Transactional
    public UserDto login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(UserNotFoundException::new);
        if (!user.getPassword().equals(request.password())) {
            throw InvalidCredentialsException.wrongPassword();
        }
        return userMapper.toDto(user);
    }
}
