package com.demo.project90.controller;

import static com.demo.project90.config.Constant.ITEM_QUEUE;
import static com.demo.project90.config.Constant.ITEM_SALE_NOT_STARTED_MSG;
import static com.demo.project90.config.Constant.TOKEN_QUEUE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.demo.project90.domain.Audit;
import com.demo.project90.domain.Item;
import com.demo.project90.model.QEvent;
import com.demo.project90.model.QItem;
import com.demo.project90.repo.AuditRepository;
import com.demo.project90.repo.ItemRepository;
import com.demo.project90.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final RabbitTemplate template;
    private final ItemRepository itemRepo;
    private final AuditRepository auditRepo;
    private final AuditService auditService;
    private final ConnectionFactory connectionFactory;

    @GetMapping(value = "/api/user")
    public String getUser() {
        return UUID.randomUUID().toString().substring(0, 7);
    }

    @GetMapping(value = "/api/items/count")
    public long getFreeItemCount() {
        return itemRepo.countAllByCartOfIsNull();
    }

    @GetMapping(value = "/api/cart/items/{username}")
    public Iterable<Item> getCartItems(@PathVariable String username) {
        return itemRepo.findAllByCartOf(username);
    }

    @GetMapping(value = "/api/cart/{username}")
    public QEvent addCartItem(@PathVariable String username) {
        Instant start = Instant.now();
        log.info("username: {}", username);
        String token = UUID.randomUUID().toString();
        QEvent qEvent = QEvent.builder()
                .user(username)
                .token(token)
                .attemptCount(0)
                .build();
        if (!auditService.checkIfSaleStarted()) {
            auditService.saveAudit(ITEM_SALE_NOT_STARTED_MSG, qEvent.getUser(), qEvent.getToken(), -1l, "FAIL");
            Instant finish = Instant.now();
            log.info("Request rejected in: {} ms", username, Duration.between(start, finish).toMillis());
            return qEvent;
        } else {
            template.convertAndSend(TOKEN_QUEUE, qEvent);
            Instant finish = Instant.now();
            log.info("Add to cart for {} took: {} ms", username, Duration.between(start, finish).toMillis());
            return qEvent;
        }
    }

    @DeleteMapping(value = "/api/cart/{username}/{id}")
    public boolean deleteCartItem(@PathVariable String username, @PathVariable Long id) {
        itemRepo.findById(id).ifPresent(e -> {
            //only user who owns the cart can delete
            if (e.getCartOf().equals(username)) {
                e.setCartOf(null);
                e.setAddedOn(null);
                itemRepo.save(e);
                pushAvailableItem(QItem.builder().itemId(id).build());
            }
        });

        return true;
    }

    @GetMapping(value = "/api/audit/{token}")
    public Audit getTokenMessage(@PathVariable String token) {
        if (auditRepo.findByToken(token).isPresent()) {
            return auditRepo.findByToken(token).get();
        } else {
            throw new ResponseStatusException(NOT_FOUND, "token not found!");
        }
    }

    private void pushAvailableItem(QItem qItem) {
        template.convertAndSend(ITEM_QUEUE, qItem);
    }
}
