package com.demo.project90.service;

import static com.demo.project90.config.Constant.SALE_BEGINS_AFTER;

import java.time.LocalDateTime;

import com.demo.project90.domain.Audit;
import com.demo.project90.repo.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    private final AuditRepository auditRepo;

    public void saveAudit(String message, String username, String token, Long itemId, String type) {
        log.info(message);
        //Note: Audit tables are always insert and no updates should happen.
        auditRepo.save(Audit.builder()
                .username(username)
                .itemId(itemId)
                .message(message)
                .token(token)
                .logDate(LocalDateTime.now())
                .type(type)
                .build());
    }

    public boolean checkIfSaleStarted() {
        if (LocalDateTime.now().isAfter(SALE_BEGINS_AFTER)) {
            return true;
        } else {
            return false;
        }
    }
}
