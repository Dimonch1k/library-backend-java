package org.library.order;

import lombok.RequiredArgsConstructor;
import org.library.order.dto.OrderResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping( "/api/v1/order" )
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;

  @PostMapping( "/borrow/{bookId}/user/{userId}" )
  @ResponseStatus( HttpStatus.OK )
  public void borrowBook ( @PathVariable UUID userId, @PathVariable UUID bookId ) {
    orderService.borrowBook(
      userId,
      bookId
    );
  }

  @PatchMapping( "/return/{orderId}" )
  @ResponseStatus( HttpStatus.OK )
  public void returnBook ( @PathVariable UUID orderId ) {
    orderService.returnBook( orderId );
  }

  @PatchMapping( "/cancel/{orderId}" )
  @ResponseStatus( HttpStatus.OK )
  public void cancelOrder ( @PathVariable UUID orderId ) {
    orderService.cancelOrder( orderId );
  }

  @GetMapping
  public List<OrderResponseDto> getAllOrders () {
    return orderService.getAllOrders();
  }

  @GetMapping( "/my-orders/user/{userId}" )
  public List<OrderResponseDto> getOrdersByUser ( @PathVariable UUID userId ) {
    return orderService.getOrdersByUserId( userId );
  }

  @DeleteMapping( "/{orderId}" )
  @ResponseStatus( HttpStatus.OK )
  public void deleteOrder ( @PathVariable UUID orderId ) {
    orderService.deleteOrder( orderId );
  }
}
