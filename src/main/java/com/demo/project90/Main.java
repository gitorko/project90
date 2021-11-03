package com.demo.project90;

import static com.demo.project90.config.Constant.ITEM_QUEUE;

import java.util.stream.IntStream;

import com.demo.project90.domain.Item;
import com.demo.project90.model.QItem;
import com.demo.project90.repo.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class Main implements CommandLineRunner {

    private final ItemRepository itemRepo;
    private final RabbitTemplate template;
    private final AmqpAdmin admin;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Seeding test data!");
        itemRepo.deleteAll();
        IntStream.range(1, 51).forEach(i -> {
            itemRepo.save(Item.builder()
                    .name("iphone_" + i)
                    .type("iphone11")
                    .price(899.0)
                    .build());
        });
        admin.purgeQueue(ITEM_QUEUE);
        itemRepo.findAll().forEach(i -> {
            template.convertAndSend(ITEM_QUEUE, QItem.builder().itemId(i.getId()).build());
        });
    }

}
