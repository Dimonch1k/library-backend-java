package org.library.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class BookResponseDto {
  private UUID id;
  private String title;
  private String description;
  private String genre;
  private int year;
  private UUID authorId;
}
