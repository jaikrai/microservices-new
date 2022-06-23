package com.program4fun.orderService.controller;

import com.program4fun.orderService.dto.OrderRequest;
import com.program4fun.orderService.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @TimeLimiter(name = "inventory")
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallBackMethod")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest orderRequest){
       return CompletableFuture.supplyAsync(()-> orderService.placeOrder(orderRequest));
//        return CompletableFuture.supplyAsync(()->"Order placed successfully");
    }

    public CompletableFuture<String> fallBackMethod(OrderRequest orderRequest, RuntimeException runtimeException){
        return CompletableFuture.supplyAsync(() ->"Oops something went wrong, please order after some time!");
    }
}
