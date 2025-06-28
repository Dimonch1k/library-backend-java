package org.library.order;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.library.book.BookService;
import org.library.book.dto.BookResponseDto;
import org.library.book.model.Book;
import org.library.order.dto.OrderResponseDto;
import org.library.order.enums.OrderStatus;
import org.library.order.model.Order;
import org.library.user.UserService;
import org.library.user.dto.UserResponseDto;
import org.library.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class OrderService {
  private final OrderRepository orderRepository;
  private final BookService bookService;
  private final UserService userService;

  @Transactional
  public void borrowBook(UUID userId, UUID bookId) {
    userService.checkUserExists(userId);

    bookService.checkBookExists(bookId);

    boolean bookIsTaken = orderRepository.existsByBookIdAndStatus(
      bookId,
      OrderStatus.ACTIVE
    );

    if (bookIsTaken) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "This book is currently unavailable"
      );
    }

    Order order = Order
      .builder()
      .id(UUID.randomUUID())
      .name("order-" + System.currentTimeMillis())
      .borrowDate(Instant.now())
      .status(OrderStatus.ACTIVE)
      .user(User.builder().id(userId).build())
      .book(Book.builder().id(bookId).build())
      .build();

    orderRepository.save(order);
  }

  public List<OrderResponseDto> getAllOrders() {
    return orderRepository.findAll().stream().map(this::toResponseDto).toList();
  }

  public List<OrderResponseDto> getOrdersByUserId(UUID userId) {
    userService.checkUserExists(userId);

    return orderRepository.findByUserId(userId).stream().map(this::toResponseDto).toList();
  }

  @Transactional
  public void returnBook(UUID orderId) {
    Order order = checkOrderExists(orderId);
    if (order.getStatus() != OrderStatus.ACTIVE) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Only active orders can be returned"
      );
    }

    order.setStatus(OrderStatus.RETURNED);
    order.setReturnDate(Instant.now());
    orderRepository.save(order);
  }

  @Transactional
  public void cancelOrder(UUID orderId) {
    Order order = checkOrderExists(orderId);
    if (order.getStatus() != OrderStatus.ACTIVE) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Only active orders can be cancelled"
      );
    }

    order.setStatus(OrderStatus.CANCELLED);
    orderRepository.save(order);
  }

  @Transactional
  public void deleteOrder(UUID orderId) {
    checkOrderExists(orderId);
    orderRepository.deleteById(orderId);
  }

  public Order checkOrderExists(UUID orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(
      NOT_FOUND,
      "Order not found"
    ));
  }

  private OrderResponseDto toResponseDto(Order order) {
    UserResponseDto userDto = userService.getById(order.getUser().getId());
    BookResponseDto bookDto = bookService.getById(order.getBook().getId());

    return new OrderResponseDto(
      order.getId(),
      order.getName(),
      order.getBorrowDate(),
      order.getReturnDate(),
      order.getStatus(),
      userDto,
      bookDto
    );
  }
}