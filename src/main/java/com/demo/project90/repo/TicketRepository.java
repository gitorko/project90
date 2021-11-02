package com.demo.project90.repo;

import com.demo.project90.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Iterable<Ticket> findAllByOrderByIdAsc();

    Iterable<Ticket> findAllByLockExpiryIsNotNull();

    Ticket findByEntryTokenIs(String entryToken);

}
