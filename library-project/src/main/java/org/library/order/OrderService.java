package org.library.order;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.library.book.BookService;
import org.library.book.model.Book;
import org.library.order.dto.OrderResponseDto;
import org.library.order.enums.OrderStatus;
import org.library.order.model.Order;
import org.library.user.UserService;
import org.library.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class OrderService {
  private final OrderRepository orderRepository;
  private final BookService bookService;
  private final UserService userService;

  @Transactional
  public void borrowBook ( Long userId, Long bookId ) {
    userService.checkUserExists( userId );
    bookService.checkBookExists( bookId );

    boolean bookIsTaken = orderRepository.existsByBookIdAndStatus(
      bookId,
      OrderStatus.ACTIVE
    );

    if ( bookIsTaken ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "This book is currently unavailable"
      );
    }

    Order order = Order
      .builder()
      .name( "order-" + System.currentTimeMillis() + "-" +
             (int) ( Math.random() * 1000 ) )
      .borrowDate( Instant.now() )
      .status( OrderStatus.ACTIVE )
      .user( User.builder().id( userId ).build() )
      .book( Book.builder().id( bookId ).build() )
      .build();

    bookService.updateStatus(
      bookId,
      OrderStatus.ACTIVE
    );

    orderRepository.save( order );
  }

  public List<OrderResponseDto> getAllOrders() {
    return orderRepository
      .findAllByOrderByCreatedAtDesc()
      .stream()
      .map(this::toResponseDto)
      .toList();
  }

  public List<OrderResponseDto> getOrdersByUserId(Long userId) {
    userService.checkUserExists(userId);

    return orderRepository
      .findByUserIdOrderByCreatedAtDesc(userId)
      .stream()
      .map(this::toResponseDto)
      .toList();
  }

  @Transactional
  public void returnBook ( Long orderId ) {
    Order order = checkOrderExists( orderId );
    if ( order.getStatus() != OrderStatus.ACTIVE ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Only active orders can be returned"
      );
    }

    bookService.updateStatus(
      order.getBook().getId(),
      OrderStatus.RETURNED
    );

    order.setStatus( OrderStatus.RETURNED );
    order.setReturnDate( Instant.now() );
    orderRepository.save( order );
  }

  @Transactional
  public void cancelOrder ( Long orderId ) {
    Order order = checkOrderExists( orderId );
    if ( order.getStatus() != OrderStatus.ACTIVE ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Only active orders can be cancelled"
      );
    }

    bookService.updateStatus(
      order.getBook().getId(),
      OrderStatus.CANCELLED
    );

    order.setStatus( OrderStatus.CANCELLED );
    orderRepository.save( order );
  }

  @Transactional
  public void deleteOrder ( Long orderId ) {
    Order order = checkOrderExists( orderId );

    bookService.updateStatus(
      order.getBook().getId(),
      null
    );

    orderRepository.deleteById( orderId );
  }

  public Order checkOrderExists ( Long orderId ) {
    return orderRepository
      .findById( orderId )
      .orElseThrow( () -> new ResponseStatusException(
        NOT_FOUND,
        "Order not found"
      ) );
  }

  private OrderResponseDto toResponseDto ( Order order ) {
    return new OrderResponseDto(
      order.getId(),
      order.getName(),
      order.getBorrowDate(),
      order.getReturnDate(),
      order.getStatus(),
      userService.getById( order.getUser().getId() ),
      bookService.getById( order.getBook().getId() )
    );
  }
}