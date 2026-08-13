package com.cartonerp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_records")
public class SalaryRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 60) private String employeeName;
    @Column(length = 60) private String position;
    private Double attendanceShifts = 0.0;
    private Double baseSalary = 0.0;
    private Double overtimeSalary = 0.0;
    private Double allowance = 0.0;
    private Double socialSecuritySubsidy = 0.0;
    private Double payableSalary = 0.0;
    private Double socialSecurityWithheld = 0.0;
    private Double netSalary = 0.0;
    private LocalDate paymentDate;
    @Column(columnDefinition = "TEXT") private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public String getPosition() { return position; }
    public Double getAttendanceShifts() { return attendanceShifts; }
    public Double getBaseSalary() { return baseSalary; }
    public Double getOvertimeSalary() { return overtimeSalary; }
    public Double getAllowance() { return allowance; }
    public Double getSocialSecuritySubsidy() { return socialSecuritySubsidy; }
    public Double getPayableSalary() { return payableSalary; }
    public Double getSocialSecurityWithheld() { return socialSecurityWithheld; }
    public Double getNetSalary() { return netSalary; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public void setPosition(String position) { this.position = position; }
    public void setAttendanceShifts(Double attendanceShifts) { this.attendanceShifts = attendanceShifts; }
    public void setBaseSalary(Double baseSalary) { this.baseSalary = baseSalary; }
    public void setOvertimeSalary(Double overtimeSalary) { this.overtimeSalary = overtimeSalary; }
    public void setAllowance(Double allowance) { this.allowance = allowance; }
    public void setSocialSecuritySubsidy(Double socialSecuritySubsidy) { this.socialSecuritySubsidy = socialSecuritySubsidy; }
    public void setPayableSalary(Double payableSalary) { this.payableSalary = payableSalary; }
    public void setSocialSecurityWithheld(Double socialSecurityWithheld) { this.socialSecurityWithheld = socialSecurityWithheld; }
    public void setNetSalary(Double netSalary) { this.netSalary = netSalary; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
