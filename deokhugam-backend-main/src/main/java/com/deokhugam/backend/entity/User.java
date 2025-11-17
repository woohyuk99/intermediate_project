package com.deokhugam.backend.entity;

import com.deokhugam.backend.dto.user.UserUpdateRequest;
import com.deokhugam.backend.entity.base.BaseSoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class User extends BaseSoftDeletableEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20, nullable = false)
    private String nickname;

    @Column(length = 20, nullable = false)
    private String password;

    public void update(UserUpdateRequest request) {
        if (!Objects.equals(request.nickname(), this.nickname)) {
            this.nickname = request.nickname();
        }
    }
}
