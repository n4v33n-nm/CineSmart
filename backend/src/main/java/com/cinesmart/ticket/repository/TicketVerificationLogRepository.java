package com.cinesmart.ticket.repository;

import com.cinesmart.ticket.entity.TicketVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketVerificationLogRepository extends JpaRepository<TicketVerificationLog, Long> {

    long countByResult(String result);

    long countByResultNot(String result);

    List<TicketVerificationLog> findTop20ByOrderByAttemptedAtDesc();
}

