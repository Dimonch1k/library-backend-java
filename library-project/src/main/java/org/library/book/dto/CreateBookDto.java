package org.library.book.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateBookDto {
  
  @NotBlank( message = "Title is required" )
  @Size( min = 2, max = 50, message = "Title must be between 2 and 50 characters" )
  private String title;

  @Size( min = 2, max = 100, message = "Description must be between 2 and 100 characters" )
  private String description;

  @NotBlank( message = "Genre is required" )
  @Size( min = 2, max = 50, message = "Genre must be between 2 and 50 characters" )
  private String genre;

  @Min( value = 1800, message = "Year must be greater than 1800" )
  @Max( value = 2025, message = "Year must be less than 2025" )
  private int year;

  @NotBlank( message = "Author ID is required" )
  private String authorId;
}
