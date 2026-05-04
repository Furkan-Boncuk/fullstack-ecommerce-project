package com.furkan.ecommerce.location.domain;

import com.furkan.ecommerce.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "countries")
@SQLRestriction("active = true")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Country extends BaseEntity {
    @Column(nullable = false, unique = true, length = 2)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String displayName;

    @Column(nullable = false)
    private boolean active;
}
