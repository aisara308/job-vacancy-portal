package com.jobportal.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vacancies")
public class Vacancies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vacancyId;

    private Integer employerId;
    private String title;
    private BigDecimal salaryFrom;
    private BigDecimal salaryTo;
    private String experience;
    private String location;
    private String adres;
    private String schedule;
    private String paymentTime;
    private Boolean remote;
    private Boolean forStudents;
    private String requirements;
    private LocalDateTime createdAt;
    private String status;

    public void setVacancyId(Long vacancyId) {
        this.vacancyId = vacancyId;
    }

    public void setEmployerId(Integer employerId) {
        this.employerId = employerId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSalaryFrom(BigDecimal salaryFrom) {
        this.salaryFrom = salaryFrom;
    }

    public void setSalaryTo(BigDecimal salaryTo) {
        this.salaryTo = salaryTo;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setAdres(String adres) {
        this.adres = adres;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }

    public void setRemote(Boolean remote) {
        this.remote = remote;
    }

    public void setForStudents(Boolean forStudents) {
        this.forStudents = forStudents;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getVacancyId() {
        return vacancyId;
    }

    public Integer getEmployerId() {
        return employerId;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getSalaryFrom() {
        return salaryFrom;
    }

    public BigDecimal getSalaryTo() {
        return salaryTo;
    }

    public String getExperience() {
        return experience;
    }

    public String getLocation() {
        return location;
    }

    public String getAdres() {
        return adres;
    }

    public String getSchedule() {
        return schedule;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public Boolean getRemote() {
        return remote;
    }

    public Boolean getForStudents() {
        return forStudents;
    }

    public String getRequirements() {
        return requirements;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }
}
