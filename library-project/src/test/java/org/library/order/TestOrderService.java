package org.library.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.author.dto.AuthorDto;
import org.library.book.BookService;
import org.library.book.dto.BookResponseDto;
import org.library.book.model.Book;
import org.library.order.dto.OrderResponseDto;
import org.library.order.enums.OrderStatus;
import org.library.order.model.Order;
import org.library.user.UserService;
import org.library.user.dto.UserResponseDto;
import org.library.user.model.User;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

public class TestOrderService {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private BookService bookService;

  @Mock
  private UserService userService;

  @InjectMocks
  private OrderService orderService;

  private AutoCloseable closeable;

  @BeforeEach
  void setUp () {
    closeable = MockitoAnnotations.openMocks( this );
  }

  @AfterEach
  void tearDown () throws Exception {
    closeable.close();
  }

  @Test
  void borrowBook_ShouldSucceed_WhenValidUserAndBookAndBookAvailable () {
    // Given
    Long userId = 1L;
    Long bookId = 1L;

    doNothing().when( userService ).checkUserExists( userId );
    doNothing().when( bookService ).checkBookExists( bookId );
    when( orderRepository.existsByBookIdAndStatus(
      bookId,
      OrderStatus.ACTIVE
    ) ).thenReturn( false );
    when( orderRepository.save( any( Order.class ) ) ).thenReturn( new Order() );

    // When
    orderService.borrowBook(
      userId,
      bookId
    );

    // Then
    verify( userService ).checkUserExists( userId );
    verify( bookService ).checkBookExists( bookId );
    verify( orderRepository ).existsByBookIdAndStatus(
      bookId,
      OrderStatus.ACTIVE
    );
    verify( bookService ).updateStatus(
      bookId,
      OrderStatus.ACTIVE
    );
    verify( orderRepository ).save( any( Order.class ) );
  }

  @Test
  void borrowBook_ShouldThrowException_WhenBookIsAlreadyTaken () {
    // Given
    Long userId = 1L;
    Long bookId = 1L;

    doNothing().when( userService ).checkUserExists( userId );
    doNothing().when( bookService ).checkBookExists( bookId );
    when( orderRepository.existsByBookIdAndStatus(
      bookId,
      OrderStatus.ACTIVE
    ) ).thenReturn( true );

    // When & Then
    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> orderService.borrowBook(
        userId,
        bookId
      )
    );

    assertEquals(
      BAD_REQUEST,
      exception.getStatusCode()
    );
    assertEquals(
      "This book is currently unavailable",
      exception.getReason()
    );
    verify(
      orderRepository,
      never()
    ).save( any( Order.class ) );
    verify(
      bookService,
      never()
    ).updateStatus(
      anyLong(),
      any( OrderStatus.class )
    );
  }

  @Test
  void borrowBook_ShouldThrowException_WhenUserDoesNotExist () {
    // Given
    Long userId = 1L;
    Long bookId = 1L;

    doThrow( new ResponseStatusException(
      NOT_FOUND,
      "User not found"
    ) ).when( userService ).checkUserExists( userId );

    // When & Then
    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> orderService.borrowBook(
        userId,
        bookId
      )
    );

    assertEquals(
      NOT_FOUND,
      exception.getStatusCode()
    );
    assertEquals(
      "User not found",
      exception.getReason()
    );
    verify(
      bookService,
      never()
    ).checkBookExists( anyLong() );
    verify(
      orderRepository,
      never()
    ).save( any( Order.class ) );
  }

  @Test
  void borrowBook_ShouldThrowException_WhenBookDoesNotExist () {
    // Given
    Long userId = 1L;
    Long bookId = 1L;

    doNothing().when( userService ).checkUserExists( userId );
    doThrow( new ResponseStatusException(
      NOT_FOUND,
      "Book not found"
    ) ).when( bookService ).checkBookExists( bookId );

    // When & Then
    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> orderService.borrowBook(
        userId,
        bookId
      )
    );

    assertEquals(
      NOT_FOUND,
      exception.getStatusCode()
    );
    assertEquals(
      "Book not found",
      exception.getReason()
    );
    verify(
      orderRepository,
      never()
    ).save( any( Order.class ) );
  }

  @Test
  void getAllOrders_ShouldReturnAllOrdersOrderedByCreatedAtDesc () {
    // Given
    Order order1 = createTestOrder(
      1L,
      "order-1",
      OrderStatus.ACTIVE
    );
    Order order2 = createTestOrder(
      2L,
      "order-2",
      OrderStatus.RETURNED
    );
    List<Order> orders = List.of(
      order1,
      order2
    );

    when( orderRepository.findAllByOrderByCreatedAtDesc() ).thenReturn( orders );
    when( userService.getById( 1L ) ).thenReturn( createTestUserResponseDto( 1L ) );
    when( bookService.getById( 1L ) ).thenReturn( createTestBookResponseDto( 1L ) );

    // When
    List<OrderResponseDto> result = orderService.getAllOrders();

    // Then
    assertNotNull( result );
    assertEquals(
      2,
      result.size()
    );
    verify( orderRepository ).findAllByOrderByCreatedAtDesc();
  }

  @Test
  void getOrdersByUserId_ShouldReturnUserOrdersOrderedByCreatedAtDesc () {
    // Given
    Long userId = 1L;
    Order order1 = createTestOrder(
      1L,
      "order-1",
      OrderStatus.ACTIVE
    );
    Order order2 = createTestOrder(
      2L,
      "order-2",
      OrderStatus.RETURNED
    );
    List<Order> orders = List.of(
      order1,
      order2
    );

    doNothing().when( userService ).checkUserExists( userId );
    when( orderRepository.findByUserIdOrderByCreatedAtDesc( userId ) ).thenReturn( orders );
    when( userService.getById( 1L ) ).thenReturn( createTestUserResponseDto( 1L ) );
    when( bookService.getById( 1L ) ).thenReturn( createTestBookResponseDto( 1L ) );

    // When
    List<OrderResponseDto> result = orderService.getOrdersByUserId( userId );

    // Then
    assertNotNull( result );
    assertEquals(
      2,
      result.size()
    );
    verify( userService ).checkUserExists( userId );
    verify( orderRepository ).findByUserIdOrderByCreatedAtDesc( userId );
  }

  @Test
  void getOrdersByUserId_ShouldThrowException_WhenUserDoesNotExist () {
    // Given
    Long userId = 1L;

    doThrow( new ResponseStatusException(
      NOT_FOUND,
      "User not found"
    ) ).when( userService ).checkUserExists( userId );

    // When & Then
    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> orderService.getOrdersByUserId( userId )
    );

    assertEquals(
      NOT_FOUND,
      exception.getStatusCode()
    );
    assertEquals(
      "User not found",
      exception.getReason()
    );
    verify(
      orderRepository,
      never()
    ).findByUserIdOrderByCreatedAtDesc( anyLong() );
  }

  @Test
  void returnBook_ShouldSucceed_WhenOrderIsActive () {
    // Given
    Long orderId = 1L;
    Order order = createTestOrder(
      orderId,
      "order-1",
      OrderStatus.ACTIVE
    );

    when( orderRepository.findById( orderId ) ).thenReturn( Optional.of( order ) );
    when( orderRepository.save( any( Order.class ) ) ).thenReturn( order );

    // When
    orderService.returnBook( orderId );

    // Then
    verify( bookService ).updateStatus(
      order.getBook().getId(),
      OrderStatus.RETURNED
    );
    verify( orderRepository ).save( order );
    assertEquals(
      OrderStatus.RETURNED,
      order.getStatus()
    );
    assertNotNull( order.getReturnDate() );
  }

  @Test
  void returnBook_ShouldThrowException_WhenOrderIsNotActive () {
    // Given
    Long orderId = 1L;
    Order order = createTestOrder(
      orderId,
      "order-1",
      OrderStatus.RETURNED
    );

    when( orderRepository.findById( orderId ) ).thenReturn( Optional.of( order ) );

    // When & Then
    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> orderService.returnBook( orderId )
    );

    assertEquals(
      BAD_REQUEST,
      exception.getStatusCode()
    );
    assertEquals(
      "Only active orders can be returned",
      exception.getReason()
    );
    verify(
      bookService,
      never()
    ).updateStatus(
      anyLong(),
      any( OrderStatus.class )
    );
    verify(
      orderRepository,
      never()
    ).save( any( Order.class ) );
  }

  @Test
  void returnBook_ShouldThrowException_WhenOrderDoesNotExist () {
    // Given
    Long orderId = 1L;

    when( orderRepository.findById( orderId ) ).thenReturn( Optional.empty() );

    // When & Then
    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> orderService.returnBook( orderId )
    );

    assertEquals(
      NOT_FOUND,
      exception.getStatusCode()
    );
    assertEquals(
      "Order not found",
      exception.getReason()
    );
  }

  @Test
  void cancelOrder_ShouldSucceed_WhenOrderIsActive () {
    // Given
    Long orderId = 1L;
    Order order = createTestOrder(
      orderId,
      "order-1",
      OrderStatus.ACTIVE
    );

    when( orderRepository.findById( orderId ) ).thenReturn( Optional.of( order ) );
    when( orderRepository.save( any( Order.class ) ) ).thenReturn( order );

    // When
    orderService.cancelOrder( orderId );

    // Then
    verify( bookService ).updateStatus(
      order.getBook().getId(),
      OrderStatus.CANCELLED
    );
    verify( orderRepository ).save( order );
    assertEquals(
      OrderStatus.CANCELLED,
      order.getStatus()
    );
  }

  @Test
  void cancelOrder_ShouldThrowException_WhenOrderIsNotActive () {
    // Given
    Long orderId = 1L;
    Order order = createTestOrder(
      orderId,
      "order-1",
      OrderStatus.RETURNED
    );

    when( orderRepository.findById( orderId ) ).thenReturn( Optional.of( order ) );

    // When & Then
    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> orderService.cancelOrder( orderId )
    );

    assertEquals(
      BAD_REQUEST,
      exception.getStatusCode()
    );
    assertEquals(
      "Only active orders can be cancelled",
      exception.getReason()
    );
    verify(
      bookService,
      never()
    ).updateStatus(
      anyLong(),
      any( OrderStatus.class )
    );
    verify(
      orderRepository,
      never()
    ).save( any( Order.class ) );
  }

  @Test
  void cancelOrder_ShouldThrowException_WhenOrderDoesNotExist () {
    // Given
    Long orderId = 1L;

    when( orderRepository.findById( orderId ) ).thenReturn( Optional.empty() );

    // When & Then
    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> orderService.cancelOrder( orderId )
    );

    assertEquals(
      NOT_FOUND,
      exception.getStatusCode()
    );
    assertEquals(
      "Order not found",
      exception.getReason()
    );
  }

  @Test
  void deleteOrder_ShouldSucceed_WhenOrderExists () {
    // Given
    Long orderId = 1L;
    Order order = createTestOrder(
      orderId,
      "order-1",
      OrderStatus.ACTIVE
    );

    when( orderRepository.findById( orderId ) ).thenReturn( Optional.of( order ) );
    doNothing().when( orderRepository ).deleteById( orderId );

    // When
    orderService.deleteOrder( orderId );

    // Then
    verify( bookService ).updateStatus(
      order.getBook().getId(),
      null
    );
    verify( orderRepository ).deleteById( orderId );
  }

  @Test
  void deleteOrder_ShouldThrowException_WhenOrderDoesNotExist () {
    // Given
    Long orderId = 1L;

    when( orderRepository.findById( orderId ) ).thenReturn( Optional.empty() );

    // When & Then
    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> orderService.deleteOrder( orderId )
    );

    assertEquals(
      NOT_FOUND,
      exception.getStatusCode()
    );
    assertEquals(
      "Order not found",
      exception.getReason()
    );
    verify(
      orderRepository,
      never()
    ).deleteById( anyLong() );
  }

  @Test
  void checkOrderExists_ShouldReturnOrder_WhenOrderExists () {
    // Given
    Long orderId = 1L;
    Order order = createTestOrder(
      orderId,
      "order-1",
      OrderStatus.ACTIVE
    );

    when( orderRepository.findById( orderId ) ).thenReturn( Optional.of( order ) );

    // When
    Order result = orderService.checkOrderExists( orderId );

    // Then
    assertNotNull( result );
    assertEquals(
      orderId,
      result.getId()
    );
    verify( orderRepository ).findById( orderId );
  }

  @Test
  void checkOrderExists_ShouldThrowException_WhenOrderDoesNotExist () {
    // Given
    Long orderId = 1L;

    when( orderRepository.findById( orderId ) ).thenReturn( Optional.empty() );

    // When & Then
    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> orderService.checkOrderExists( orderId )
    );

    assertEquals(
      NOT_FOUND,
      exception.getStatusCode()
    );
    assertEquals(
      "Order not found",
      exception.getReason()
    );
  }

  // Helper methods for creating test data
  private Order createTestOrder ( Long id, String name, OrderStatus status ) {
    return Order
      .builder()
      .id( id )
      .name( name )
      .borrowDate( Instant.now() )
      .status( status )
      .user( User.builder().id( 1L ).build() )
      .book( Book.builder().id( 1L ).build() )
      .build();
  }

  private UserResponseDto createTestUserResponseDto ( Long id ) {
    return new UserResponseDto(
      id,
      "test@example.com",
      "USER"
    );
  }

  private BookResponseDto createTestBookResponseDto ( Long id ) {
    AuthorDto authorDto = new AuthorDto(
      1L,
      "Test",
      "Author",
      35
    );
    return new BookResponseDto(
      id,
      "Test Book",
      "Test Description",
      "Fiction",
      2023,
      OrderStatus.ACTIVE,
      authorDto
    );
  }
}