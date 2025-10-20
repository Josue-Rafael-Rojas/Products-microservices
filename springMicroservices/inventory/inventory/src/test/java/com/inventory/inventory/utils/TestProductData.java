package com.inventory.inventory.utils;

import java.util.List;
import java.util.UUID;

public class TestProductData {
    public static final UUID PRODUCT_1 = UUID.fromString("213f7602-35c6-4271-a7bf-bfe77746e02f");
    public static final UUID PRODUCT_2 = UUID.fromString("49b6503f-89cc-46b2-8d84-793e41580679");
    public static final UUID PRODUCT_3 = UUID.fromString("4050d780-488d-45fa-8d56-90575931f67e");
    public static final UUID PRODUCT_4 = UUID.fromString("327852c8-6535-47a2-a375-9820d5a7bc57");
    public static final UUID PRODUCT_5 = UUID.fromString("5e7a909b-8880-4984-b2c9-d23f6c08a769");
    public static final UUID PRODUCT_6 = UUID.fromString("a51193df-8f79-4317-ad0c-c415278b62a2");
    public static final UUID PRODUCT_7 = UUID.fromString("1634e855-69bf-4c7b-a759-c3589d31aa35");
    public static final UUID PRODUCT_8 = UUID.fromString("a189bfdf-7eef-4daa-8b38-d3089cc5b94d");
    public static final UUID PRODUCT_9 = UUID.fromString("191a1b1e-4a26-4ae9-b47e-89bef636c7b4");
    public static final UUID PRODUCT_10 = UUID.fromString("14433dde-ef97-4270-93c7-12c8e26f498b");
    public static final UUID PRODUCT_11 = UUID.fromString("58bddad3-336f-474b-91fb-765b23976d9a");
    public static final UUID PRODUCT_12 = UUID.fromString("87790ed6-ccb7-41a2-8700-d52e7b67658e");
    public static final UUID PRODUCT_13 = UUID.fromString("d0d15c50-1814-4c09-9df4-7c3bbdb6b1b1");
    public static final UUID PRODUCT_14 = UUID.fromString("d172fdfa-e8ca-4502-b474-1b09acf1e420");
    public static final UUID PRODUCT_15 = UUID.fromString("d4dbf60e-1681-4bf6-92a7-717f5586ca12");
    public static final UUID PRODUCT_16 = UUID.fromString("44a046dd-69b4-4f3e-9809-a980e4ad06c3");
    public static final UUID PRODUCT_17 = UUID.fromString("7acfe66b-699b-46af-bd35-d76d4436d0e5");
    public static final UUID PRODUCT_18 = UUID.fromString("61d12b4a-86eb-4027-879c-7075dd78f877");

    public static final List<UUID> ALL_PRODUCTS = List.of(
        PRODUCT_1, PRODUCT_2, PRODUCT_3, PRODUCT_4, PRODUCT_5, PRODUCT_6,
        PRODUCT_7, PRODUCT_8, PRODUCT_9, PRODUCT_10, PRODUCT_11, PRODUCT_12,
        PRODUCT_13, PRODUCT_14, PRODUCT_15, PRODUCT_16, PRODUCT_17, PRODUCT_18
    );

    public static UUID getRandomExistingProduct() {
        return ALL_PRODUCTS.get((int) (Math.random() * ALL_PRODUCTS.size()));
    }
}


