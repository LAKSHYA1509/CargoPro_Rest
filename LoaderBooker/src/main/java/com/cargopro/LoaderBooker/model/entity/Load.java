package com.cargopro.LoaderBooker.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.cargopro.LoaderBooker.model.enums.LoadStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "loads")
@Data
public class Load {

    private LoadStatus loadStatus;
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String loadingPoint;

    @Column(nullable = false)
    private String unloadingPoint;

    @Column(nullable = false)
    private LocalDateTime loadingDate;

    @Column(nullable = false)
    private LocalDateTime unloadingDate;

    @Column(nullable = false)
    private String productType;

    @Column(nullable = false)
    private String truckType;

    @Column(nullable = false)
    private int noOfTrucks;

    @Column(nullable = false)
    private double weight;

    private String comment;

    @Column(nullable = false)
    private String shipperId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime datePosted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoadStatus status;

    @OneToMany(mappedBy = "load", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<Booking> bookings;

    @PrePersist
    protected void onCreate() {
        this.datePosted = LocalDateTime.now();
        if (this.status == null) {
            this.status = LoadStatus.POSTED;
        }
    }

    

}