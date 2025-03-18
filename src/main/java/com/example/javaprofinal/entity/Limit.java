package com.example.javaprofinal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "limits")
public class Limit {

    @Id
    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "daily_limit")
    private Integer dailyLimit;

}
