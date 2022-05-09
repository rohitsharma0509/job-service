package com.scb.job.entity;



import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Table(name = "job_id_sequence")
@Entity
public class Sequence {

    @Id
    private String id;

    private long seq;

}