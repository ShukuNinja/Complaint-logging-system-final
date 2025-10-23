package com.complaint.system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "DEPARTMENTS")
public class Department {
    @Id
    @Column(name = "DEPT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dept_seq")
    @SequenceGenerator(name = "dept_seq", sequenceName = "dept_seq", allocationSize = 1)
    private Long deptId;

    @Column(name = "DEPT_NAME", nullable = false, unique = true, length = 100)
    private String deptName;

    public Department() {}

    public Department(String deptName) {
        this.deptName = deptName;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    @Override
    public String toString() {
        return deptName;
    }
}