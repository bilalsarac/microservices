package com.ecommerce.microservices.order.service;

import com.ecommerce.microservices.order.client.InventoryClient;
import com.ecommerce.microservices.order.dto.OrderRequest;
import com.ecommerce.microservices.order.event.OrderPlacedEvent;
import com.ecommerce.microservices.order.model.Order;
import com.ecommerce.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String,Object> kafkaTemplate;



    public void placeOrder(OrderRequest orderRequest) {
        var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());
        System.out.println("PRODUCT IS IN THE STOCK ? "+isProductInStock);
        if(isProductInStock){
            Order order = Order.builder()
                    .orderNumber(UUID.randomUUID().toString())
                    .price(orderRequest.price())
                    .skuCode(orderRequest.skuCode())
                    .quantity(orderRequest.quantity())
                    .build();
            orderRepository.save(order);


            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
            orderPlacedEvent.setOrderNumber(order.getOrderNumber());
            orderPlacedEvent.setEmail("testuser@outlook.com" );
            orderPlacedEvent.setFirstName(orderRequest.userDetails() != null ? orderRequest.userDetails().firstName(): "test" );
            orderPlacedEvent.setLastName(orderRequest.userDetails() != null ? orderRequest.userDetails().lastName(): "user" );

            log.info("Notify user");
            kafkaTemplate.send("order-placed", orderPlacedEvent);
            log.info("User notified");

        }else{

            throw new RuntimeException("Product with SkuCode"+ orderRequest.skuCode() + " is not in the stock."+ orderRequest);
        }


    }
}
