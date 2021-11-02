package com.demo.project90.config;

import java.time.LocalDateTime;

public final class Constant {
    public static final String TOKEN_QUEUE = "sale-queue";

    public static final LocalDateTime SALE_BEGINS_AFTER = LocalDateTime.parse("2021-11-01T00:00:00.000");

    public static final String ITEM_TYPE = "iphone11";
    public static final String ITEM_ALREADY_IN_CART_MSG = "Item already in cart, only 1 item can be purchased!";
    public static final String ITEM_ADDED_TO_CART_MSG = "Item: %s added to cart!";
    public static final String ITEM_SOLD_OUT_MSG = "Item sold out!";
    public static final String ITEM_CONCURRENT_EX_MSG = "Concurrent modification of item";
    public static final String ITEM_SALE_NOT_STARTED_MSG = "Sale not yet started";
}
