package com.demo.project90.queue;

import static com.demo.project90.config.Constant.ITEM_ADDED_TO_CART_MSG;
import static com.demo.project90.config.Constant.ITEM_ALREADY_IN_CART_MSG;
import static com.demo.project90.config.Constant.ITEM_CONCURRENT_EX_MSG;
import static com.demo.project90.config.Constant.ITEM_SALE_NOT_STARTED_MSG;
import static com.demo.project90.config.Constant.ITEM_SOLD_OUT_MSG;
import static com.demo.project90.config.Constant.ITEM_TYPE;
import static com.demo.project90.config.Constant.SALE_BEGINS_AFTER;
import static com.demo.project90.config.Constant.TOKEN_QUEUE;

import java.time.LocalDateTime;

import com.demo.project90.domain.Audit;
import com.demo.project90.domain.Item;
import com.demo.project90.model.QEvent;
import com.demo.project90.repo.AuditRepository;
import com.demo.project90.repo.ItemRepository;
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

    @SneakyThrows
    @RabbitListener(queues = TOKEN_QUEUE)
    public void processRequest(QEvent qEvent) {
        log.info("Received qEvent: {}", qEvent);
        if (!checkIfSaleStarted()) {
            saveAudit(ITEM_SALE_NOT_STARTED_MSG, qEvent.getUser(), qEvent.getToken(), -1l, "FAIL");
            return;
        }
        //check if user already has item in cart.
        if (itemRepo.countByCartOfAndType(qEvent.getUser(), ITEM_TYPE) == 0) {
            try {
                //Find the first available item.
                Item item = itemRepo.findFirstByCartOfIsNull();
                if (item == null) {
                    //sold out.
                    saveAudit(ITEM_SOLD_OUT_MSG, qEvent.getUser(), qEvent.getToken(), -1l, "FAIL");
                } else {
                    //add to cart of user.
                    item.setCartOf(qEvent.getUser());
                    item.setAddedOn(LocalDateTime.now());
                    itemRepo.save(item);
                    saveAudit(String.format(ITEM_ADDED_TO_CART_MSG, item.getName()), qEvent.getUser(), qEvent.getToken(), item.getId(), "SUCCESS");
                }
            } catch (ObjectOptimisticLockingFailureException ex) {
                //Note: On a single instance one thread event processor this error will not occur.
                //Note: On a multi instance setup this error can occur.
                saveAudit(ITEM_CONCURRENT_EX_MSG, qEvent.getUser(), qEvent.getToken(), -1l, "FAIL");
                log.error(ITEM_CONCURRENT_EX_MSG, ex);
                //One more attempt to procure item.
                if (qEvent.getAttemptCount() < 1) {
                    qEvent.setAttemptCount(qEvent.getAttemptCount() + 1);
                    processRequest(qEvent);
                }
            }
        } else {
            saveAudit(ITEM_ALREADY_IN_CART_MSG, qEvent.getUser(), qEvent.getToken(), -1l, "FAIL");
        }
    }

    private void saveAudit(String message, String username, String token, Long itemId, String type) {
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

    public static boolean checkIfSaleStarted() {
        if (LocalDateTime.now().isAfter(SALE_BEGINS_AFTER)) {
            return true;
        } else {
            return false;
        }
    }
}
