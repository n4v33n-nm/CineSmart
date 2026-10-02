package com.cinesmart.cinema.dto;

import com.cinesmart.cinema.entity.Cinema;

public class CinemaDTO {

    private Long id;
    private String name;
    private String city;
    private String address;
    private String contactNumber;

    public CinemaDTO() {
    }

    public CinemaDTO(Long id, String name, String city, String address, String contactNumber) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    public static CinemaDTO fromEntity(Cinema cinema) {
        if (cinema == null) return null;
        return new CinemaDTO(
                cinema.getId(),
                cinema.getName(),
                cinema.getCity(),
                cinema.getAddress(),
                cinema.getContactNumber()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
