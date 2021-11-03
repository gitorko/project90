package com.demo.project90.queue;

import static com.demo.project90.config.Constant.ITEM_ADDED_TO_CART_MSG;
import static com.demo.project90.config.Constant.ITEM_ALREADY_IN_CART_MSG;
import static com.demo.project90.config.Constant.ITEM_CONCURRENT_EX_MSG;
import static com.demo.project90.config.Constant.ITEM_SALE_NOT_STARTED_MSG;
import static com.demo.project90.config.Constant.ITEM_SOLD_OUT_MSG;
import static com.demo.project90.config.Constant.ITEM_TYPE;
import static com.demo.project90.config.Constant.TOKEN_QUEUE;

import java.time.LocalDateTime;

import com.demo.project90.domain.Item;
import com.demo.project90.model.QEvent;
import com.demo.project90.repo.AuditRepository;
import com.demo.project90.repo.ItemRepository;
import com.demo.project90.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventListener {

    final ItemRepository itemRepo;
    final AuditRepository auditRepo;
    private final AuditService auditService;

    @SneakyThrows
    @RabbitListener(queues = TOKEN_QUEUE)
    public void processRequest(QEvent qEvent) {
        log.info("Received qEvent: {}", qEvent);
        if (!auditService.checkIfSaleStarted()) {
            auditService.saveAudit(ITEM_SALE_NOT_STARTED_MSG, qEvent.getUser(), qEvent.getToken(), -1l, "FAIL");
            return;
        }
        //check if user already has item in cart.
        if (itemRepo.countByCartOfAndType(qEvent.getUser(), ITEM_TYPE) == 0) {
            try {
                //Find the first available item.
                Item item = itemRepo.findFirstByCartOfIsNull();
                if (item == null) {
                    //sold out.
                    auditService.saveAudit(ITEM_SOLD_OUT_MSG, qEvent.getUser(), qEvent.getToken(), -1l, "FAIL");
                } else {
                    //add to cart of user.
                    item.setCartOf(qEvent.getUser());
                    item.setAddedOn(LocalDateTime.now());
                    itemRepo.save(item);
                    auditService.saveAudit(String.format(ITEM_ADDED_TO_CART_MSG, item.getName()), qEvent.getUser(), qEvent.getToken(), item.getId(), "SUCCESS");
                }
            } catch (ObjectOptimisticLockingFailureException ex) {
                //Note: On a single instance one thread event processor this error will not occur.
                //Note: On a multi instance setup this error can occur.
                auditService.saveAudit(ITEM_CONCURRENT_EX_MSG, qEvent.getUser(), qEvent.getToken(), -1l, "FAIL");
                log.error(ITEM_CONCURRENT_EX_MSG, ex);
                //One more attempt to procure item.
                if (qEvent.getAttemptCount() < 1) {
                    qEvent.setAttemptCount(qEvent.getAttemptCount() + 1);
                    processRequest(qEvent);
                }
            }
        } else {
            auditService.saveAudit(ITEM_ALREADY_IN_CART_MSG, qEvent.getUser(), qEvent.getToken(), -1l, "FAIL");
        }
    }


}
