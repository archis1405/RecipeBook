package com.example.RecipeBook.service;

import com.example.RecipeBook.dto.RecipeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageQueueService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    public void sendRecipeForProcessing(RecipeMessage message ){
        try{
            rabbitTemplate.convertAndSend(exchange , routingKey , message);
            log.info("Recipe is sent to queue for processing: {}", message.getRecipeId());
        }
        catch (Exception e){
            log.error("Failed to send recipe to queue: {}" , e);
            throw new RuntimeException("Failed to send recipe for processing" , e);
        }
    }
}
