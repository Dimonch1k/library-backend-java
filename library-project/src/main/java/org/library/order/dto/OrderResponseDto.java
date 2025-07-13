package org.library.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.library.book.dto.BookResponseDto;
import org.library.user.dto.UserResponseDto;
import org.library.order.enums.OrderStatus;

import java.time.Instant;

@Data
@AllArgsConstructor
public class OrderResponseDto {
  private Long id;
  private String name;
  private Instant borrowDate;
  private Instant returnDate;
  private OrderStatus status;
  private UserResponseDto user;
  private BookResponseDto book;
}
