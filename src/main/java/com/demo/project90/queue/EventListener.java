package com.demo.project90.queue;

import static com.demo.project90.config.Constant.ITEM_ADDED_TO_CART_MSG;
import static com.demo.project90.config.Constant.ITEM_ALREADY_IN_CART_MSG;
import static com.demo.project90.config.Constant.ITEM_MISMATCH_MSG;
import static com.demo.project90.config.Constant.ITEM_QUEUE;
import static com.demo.project90.config.Constant.ITEM_SALE_NOT_STARTED_MSG;
import static com.demo.project90.config.Constant.ITEM_SOLD_OUT_MSG;
import static com.demo.project90.config.Constant.ITEM_TYPE;
import static com.demo.project90.config.Constant.TOKEN_QUEUE;

import java.time.LocalDateTime;

import com.demo.project90.domain.Item;
import com.demo.project90.model.QEvent;
import com.demo.project90.model.QItem;
import com.demo.project90.repo.AuditRepository;
import com.demo.project90.repo.ItemRepository;
import com.demo.project90.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventListener {

    private final ItemRepository itemRepo;
    private final AuditRepository auditRepo;
    private final AuditService auditService;
    private final ConnectionFactory connectionFactory;
    private ObjectMapper objectMapper = new ObjectMapper();

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
            //Find the first available item.
            QItem qItem = popAvailableItem();
            if (qItem == null) {
                //sold out.
                auditService.saveAudit(ITEM_SOLD_OUT_MSG, qEvent.getUser(), qEvent.getToken(), -1l, "FAIL");
                return;
            }
            Item item = itemRepo.findByIdAndCartOfIsNull(qItem.getItemId());
            if (item != null) {
                //add to cart of user.
                item.setCartOf(qEvent.getUser());
                item.setAddedOn(LocalDateTime.now());
                itemRepo.save(item);
                auditService.saveAudit(String.format(ITEM_ADDED_TO_CART_MSG, item.getName()), qEvent.getUser(), qEvent.getToken(), item.getId(), "SUCCESS");
            } else {
                auditService.saveAudit(ITEM_MISMATCH_MSG, qEvent.getUser(), qEvent.getToken(), -1l, "FAIL");
            }
        } else {
            //sold out.
            auditService.saveAudit(ITEM_ALREADY_IN_CART_MSG, qEvent.getUser(), qEvent.getToken(), -1l, "FAIL");
        }
    }

    @SneakyThrows
    private QItem popAvailableItem() {
        try (Connection connection = connectionFactory.createConnection()) {
            Channel channel = connection.createChannel(true);
            GetResponse resp = channel.basicGet(ITEM_QUEUE, true);
            if (resp != null) {
                String message = new String(resp.getBody(), "UTF-8");
                return objectMapper.readValue(message, QItem.class);
            }
            return null;
        }
    }

}
