package com.flopkart.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flopkart.common.dto.ApiResponse;
import com.flopkart.common.dto.cart.CartDto;
import com.flopkart.common.dto.cart.CartItemDto;
import com.flopkart.common.dto.order.CreateOrderRequest;
import com.flopkart.common.event.PaymentProcessedEvent;
import com.flopkart.common.event.StockReleaseEvent;
import com.flopkart.order.client.CartClient;
import com.flopkart.order.client.ProductClient;
import com.flopkart.order.entity.Order;
import com.flopkart.order.entity.OrderItem;
import com.flopkart.order.entity.OrderStatus;
import com.flopkart.order.event.OrderEventPublisher;
import com.flopkart.order.repository.OrderRepository;
import com.flopkart.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.kafka.listener.auto-startup=false"
})
class OrderServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @MockBean
    private CartClient cartClient;

    @MockBean
    private ProductClient productClient;

    @MockBean
    private OrderEventPublisher orderEventPublisher;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        reset(cartClient, productClient, orderEventPublisher);
    }

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("GET /api/v1/orders/status returns 200 OK and expected JSON message")
    void shouldReturnOrderStatus() throws Exception {
        mockMvc.perform(get("/api/v1/orders/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order Service is operational"))
                .andExpect(jsonPath("$.data.service").value("order-service"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.port").value(8084));
    }

    @Test
    @DisplayName("POST /api/v1/orders creates order in PENDING status and publishes OrderPlacedEvent")
    void shouldCreateOrderSuccessfully() throws Exception {
        // Setup Cart with items
        CartItemDto item1 = new CartItemDto(1L, "Laptop", new BigDecimal("1000.00"), 1, "http://img.com/1", new BigDecimal("1000.00"));
        CartItemDto item2 = new CartItemDto(2L, "Mouse", new BigDecimal("50.00"), 2, "http://img.com/2", new BigDecimal("100.00"));
        CartDto cart = new CartDto("10", List.of(item1, item2), new BigDecimal("1100.00"), 3, LocalDateTime.now());

        when(cartClient.getCart("10")).thenReturn(ApiResponse.success("Cart retrieved", cart));
        when(productClient.reserveStock(any())).thenReturn(ApiResponse.success("Stock reserved", null));

        CreateOrderRequest request = new CreateOrderRequest("123 Tech Lane, Silicon Valley", "buyer@example.com");

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", "10")
                        .header("X-User-Email", "buyer@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order placed successfully"))
                .andExpect(jsonPath("$.data.orderNumber", org.hamcrest.Matchers.startsWith("ORD-")))
                .andExpect(jsonPath("$.data.userId").value(10))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(1100.00))
                .andExpect(jsonPath("$.data.shippingAddress").value("123 Tech Lane, Silicon Valley"))
                .andExpect(jsonPath("$.data.items", hasSize(2)));

        // Verify product reservation was called
        verify(productClient, times(1)).reserveStock(any());

        // Verify Kafka event published
        verify(orderEventPublisher, times(1)).publishOrderPlaced(any());

        // Verify database state
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(10L);
        assertEquals(1, orders.size());
        assertEquals(OrderStatus.PENDING, orders.get(0).getStatus());
    }

    @Test
    @DisplayName("POST /api/v1/orders returns 400 Bad Request when cart is empty")
    void shouldReturn400WhenCartIsEmpty() throws Exception {
        CartDto emptyCart = new CartDto("10", List.of(), BigDecimal.ZERO, 0, LocalDateTime.now());
        when(cartClient.getCart("10")).thenReturn(ApiResponse.success("Empty cart", emptyCart));

        CreateOrderRequest request = new CreateOrderRequest("123 Tech Lane", "buyer@example.com");

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("cart is empty")));

        verify(productClient, never()).reserveStock(any());
        verify(orderEventPublisher, never()).publishOrderPlaced(any());
    }

    @Test
    @DisplayName("POST /api/v1/orders returns 409 Conflict when stock reservation fails")
    void shouldReturn409WhenStockReservationFails() throws Exception {
        CartItemDto item = new CartItemDto(1L, "Laptop", new BigDecimal("1000.00"), 500, "img", new BigDecimal("500000.00"));
        CartDto cart = new CartDto("10", List.of(item), new BigDecimal("500000.00"), 500, LocalDateTime.now());

        when(cartClient.getCart("10")).thenReturn(ApiResponse.success("Cart retrieved", cart));
        when(productClient.reserveStock(any())).thenReturn(ApiResponse.error("Insufficient stock for product Laptop", 409));

        CreateOrderRequest request = new CreateOrderRequest("123 Tech Lane", "buyer@example.com");

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Insufficient stock")));

        assertEquals(0, orderRepository.count());
        verify(orderEventPublisher, never()).publishOrderPlaced(any());
    }

    @Test
    @DisplayName("GET /api/v1/orders returns user order history")
    void shouldReturnUserOrderHistory() throws Exception {
        Order order1 = Order.builder()
                .orderNumber("ORD-1111111111")
                .userId(10L)
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.CONFIRMED)
                .shippingAddress("Addr 1")
                .build();
        order1.addItem(new OrderItem(1L, "Item 1", 1, new BigDecimal("150.00")));

        Order order2 = Order.builder()
                .orderNumber("ORD-2222222222")
                .userId(10L)
                .totalAmount(new BigDecimal("75.00"))
                .status(OrderStatus.PENDING)
                .shippingAddress("Addr 2")
                .build();
        order2.addItem(new OrderItem(2L, "Item 2", 1, new BigDecimal("75.00")));

        orderRepository.save(order1);
        orderRepository.save(order2);

        mockMvc.perform(get("/api/v1/orders")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderNumber} returns single order details")
    void shouldReturnOrderByOrderNumber() throws Exception {
        Order order = Order.builder()
                .orderNumber("ORD-TRACK12345")
                .userId(10L)
                .totalAmount(new BigDecimal("299.99"))
                .status(OrderStatus.CONFIRMED)
                .shippingAddress("Tracking Address")
                .build();
        order.addItem(new OrderItem(5L, "Wireless Earbuds", 1, new BigDecimal("299.99")));
        orderRepository.save(order);

        mockMvc.perform(get("/api/v1/orders/ORD-TRACK12345")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-TRACK12345"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.items", hasSize(1)));
    }

    @Test
    @DisplayName("Saga Listener: Payment SUCCESS transitions order to CONFIRMED and clears cart")
    void shouldConfirmOrderOnPaymentSuccess() {
        Order order = Order.builder()
                .orderNumber("ORD-SAGA-SUCCESS")
                .userId(10L)
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .shippingAddress("Saga Street")
                .build();
        order.addItem(new OrderItem(1L, "Gadget", 1, new BigDecimal("100.00")));
        orderRepository.save(order);

        PaymentProcessedEvent event = new PaymentProcessedEvent(
                "ORD-SAGA-SUCCESS",
                "PAY-9999",
                "SUCCESS",
                null,
                LocalDateTime.now()
        );

        orderService.processPaymentResult(event);

        Order updated = orderRepository.findByOrderNumber("ORD-SAGA-SUCCESS").orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
        verify(cartClient, times(1)).clearCart("10");
        verify(orderEventPublisher, never()).publishStockRelease(any());
    }

    @Test
    @DisplayName("Saga Listener: Payment FAILED transitions order to CANCELLED and publishes StockReleaseEvent")
    void shouldCancelOrderAndReleaseStockOnPaymentFailure() {
        Order order = Order.builder()
                .orderNumber("ORD-SAGA-FAILED")
                .userId(10L)
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .shippingAddress("Saga Street")
                .build();
        order.addItem(new OrderItem(1L, "Gadget", 2, new BigDecimal("50.00")));
        orderRepository.save(order);

        PaymentProcessedEvent event = new PaymentProcessedEvent(
                "ORD-SAGA-FAILED",
                "PAY-8888",
                "FAILED",
                "Insufficient card balance",
                LocalDateTime.now()
        );

        orderService.processPaymentResult(event);

        Order updated = orderRepository.findByOrderNumber("ORD-SAGA-FAILED").orElseThrow();
        assertEquals(OrderStatus.CANCELLED, updated.getStatus());
        verify(cartClient, never()).clearCart(any());

        ArgumentCaptor<StockReleaseEvent> eventCaptor = ArgumentCaptor.forClass(StockReleaseEvent.class);
        verify(orderEventPublisher, times(1)).publishStockRelease(eventCaptor.capture());

        StockReleaseEvent released = eventCaptor.getValue();
        assertEquals("ORD-SAGA-FAILED", released.orderNumber());
        assertEquals(1, released.items().size());
        assertEquals(1L, released.items().get(0).productId());
        assertEquals(2, released.items().get(0).quantity());
    }
}
