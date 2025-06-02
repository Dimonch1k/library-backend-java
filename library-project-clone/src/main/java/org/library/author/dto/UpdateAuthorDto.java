package org.library.author.dto;

import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class UpdateAuthorDto {

  @Size(min = 3, max = 20, message = "First name must be between 3 and 20 characters")
  private String firstName;

  @Size(min = 3, max = 20, message = "Last name must be between 3 and 20 characters")
  private String lastName;

  @Min(value = 18, message = "Age must be greater than 18")
  @Max(value = 100, message = "Age must be less than 100")
  private Integer age;
}
