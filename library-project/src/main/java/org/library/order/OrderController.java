package org.library.order;

import lombok.RequiredArgsConstructor;
import org.library.auth.annotations.CurrentUser;
import org.library.order.dto.OrderResponseDto;
import org.library.user.dto.UserResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping( "/api/v1/order" )
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;

  @PostMapping( "/borrow/{bookId}" )
  @ResponseStatus( HttpStatus.OK )
  public void borrowBook ( @CurrentUser UserResponseDto currentUser, @PathVariable Long bookId ) {
    orderService.borrowBook(
      currentUser.getId(),
      bookId
    );
  }

  @PatchMapping( "/return/{orderId}" )
  @ResponseStatus( HttpStatus.OK )
  public void returnBook ( @PathVariable Long orderId ) {
    orderService.returnBook( orderId );
  }

  @PatchMapping( "/cancel/{orderId}" )
  @ResponseStatus( HttpStatus.OK )
  public void cancelOrder ( @PathVariable Long orderId ) {
    orderService.cancelOrder( orderId );
  }

  @GetMapping
  public List<OrderResponseDto> getAllOrders () {
    return orderService.getAllOrders();
  }

  @GetMapping( "/my-orders" )
  public List<OrderResponseDto> getOrdersByUser ( @CurrentUser UserResponseDto currentUser ) {
    return orderService.getOrdersByUserId( currentUser.getId() );
  }

  @DeleteMapping( "/{orderId}" )
  @ResponseStatus( HttpStatus.OK )
  public void deleteOrder ( @PathVariable Long orderId ) {
    orderService.deleteOrder( orderId );
  }
}
