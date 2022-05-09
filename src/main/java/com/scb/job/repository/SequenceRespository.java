package com.scb.job.repository;

import com.scb.job.entity.Sequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SequenceRespository extends JpaRepository<Sequence, String> {

    @Query(value = "SELECT nextval('job_id_sequence');", nativeQuery = true)
    Integer getNextSequence();



}

