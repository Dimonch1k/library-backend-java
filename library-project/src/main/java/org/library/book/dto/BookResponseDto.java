package org.library.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.library.author.dto.AuthorDto;
import org.library.author.model.Author;
import org.library.order.enums.OrderStatus;


@Data
@AllArgsConstructor
public class BookResponseDto {
  private Long id;
  private String title;
  private String description;
  private String genre;
  private int year;
  private OrderStatus status;
  private AuthorDto author;
}
