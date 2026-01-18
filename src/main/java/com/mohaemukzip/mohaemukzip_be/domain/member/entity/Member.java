package com.mohaemukzip.mohaemukzip_be.domain.member.entity;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.enums.LoginType;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.enums.Role;
import com.mohaemukzip.mohaemukzip_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(
        name = "members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UQ_LOGIN_ID",
                        columnNames = {"login_id"}
                ),
                @UniqueConstraint(
                        name = "UQ_PROVIDER_OAUTH_ID",
                        columnNames = {"login_type", "oauth_id"}
                )
        }
)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname")
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "inactive_date")
    private LocalDate inactiveDate;

    @Column(name = "login_id", unique = true)
    private String loginId;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    @Column(name = "oauth_id", unique = true)
    private Long oauthId;

    @Column(name = "score")
    private Integer score;

    public void deactivate() {
        if (this.inactiveDate == null) {
            this.inactiveDate = LocalDate.now();
        }
    }

    public void reactivate() {
        this.inactiveDate = null;
    }

    public boolean isInactive() { return this.inactiveDate != null; }
}
