package com.program4fun.orderService.service;

import com.program4fun.orderService.dto.InventoryResponse;
import com.program4fun.orderService.dto.OrderLineItemsDto;
import com.program4fun.orderService.dto.OrderRequest;
import com.program4fun.orderService.model.Order;
import com.program4fun.orderService.model.OrderLineItems;;
import com.program4fun.orderService.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final WebClient.Builder webClientBuilder;
    private final OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems  =  orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
            order.setOrderLineListItems(orderLineItems);

            // include this skucodes as request paramater
           List<String> skuCodes =  order.getOrderLineListItems().stream()
                    .map(OrderLineItems::getSkuCode)
                    .toList();
            // call Inventory service and place order if the product is in
            // stock
            // http://localhost:8082/api/inventory?skuCode=iPhone-13&skuCodde=iPhone-13-red
            InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block(); // block will change Asyhc call into syhnc call since (bodyToMono is ashync call
        Boolean allProductsInStocks =  Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);
            if(allProductsInStocks){
                orderRepository.save(order);
            } else {
                throw new IllegalArgumentException("Product is not in stock, please try again later");
            }

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
