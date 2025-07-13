package org.library.book.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateBookDto {
  @Size( min = 3, max = 20, message = "Title must be between 3 and 20 characters" )
  private String title;

  @Size( min = 3, max = 100, message = "Description must be between 3 and 100 characters" )
  private String description;

  @Size( min = 3, max = 20, message = "Genre must be between 3 and 20 characters" )
  private String genre;

  @Min( value = 1800, message = "Year must be greater than 1800" )
  @Max( value = 2025, message = "Year must be less than 2025" )
  private Integer year;

  private Long authorId;
}
