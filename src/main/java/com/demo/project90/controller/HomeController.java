package com.demo.project90.controller;

import java.util.UUID;

import com.demo.project90.domain.Ticket;
import com.demo.project90.repo.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private static final Integer EXPIRY_TTL_SECS = 30;

    @Autowired
    TicketRepository ticketRepo;

    @GetMapping(value = "/api/user")
    public String getUser() {
        return UUID.randomUUID().toString().substring(0, 7);
    }

    @GetMapping(value = "/api/tickets")
    public Iterable<Ticket> getTickets() {
        return ticketRepo.findAllByOrderByIdAsc();
    }

}
