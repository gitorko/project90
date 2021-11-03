package com.demo.project90;

import java.util.stream.IntStream;

import com.demo.project90.domain.Item;
import com.demo.project90.repo.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class Main implements CommandLineRunner {

    @Autowired
    ItemRepository itemRepo;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Seeding test data!");
        itemRepo.deleteAll();
        IntStream.range(1, 101).forEach(i -> {
            itemRepo.save(Item.builder()
                    .name("iphone_" + i)
                    .type("iphone11")
                    .price(899.0)
                    .build());
        });
    }

}
