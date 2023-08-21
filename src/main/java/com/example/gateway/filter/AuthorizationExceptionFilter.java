package com.example.gateway.filter;


import com.example.gateway.dto.ResponseMessageDto;
import com.example.gateway.jwt.exception.TokenValidException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthorizationExceptionFilter implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ResponseMessageDto responseBody = new ResponseMessageDto(message, status.value());

        DataBufferFactory bufferFactory = response.bufferFactory();
        DataBuffer buffer = null;
        try {
            buffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(responseBody));
        } catch (JsonProcessingException e) {
            log.error("Error while processing JSON response body", e);
            return writeErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof TokenValidException) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized");
        } else if (ex instanceof IllegalStateException) {
            return writeErrorResponse(exchange, HttpStatus.BAD_REQUEST, "Bad Request");
        } else {
            return writeErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }
}
