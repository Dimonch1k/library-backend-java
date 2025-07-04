package org.library.author.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAuthorDto {

  @NotBlank( message = "First name is required" )
  @Size( min = 2, max = 50, message = "First name must be between 2 and 50 characters" )
  private String firstName;

  @NotBlank( message = "Last name is required" )
  @Size( min = 2, max = 50, message = "Last name must be between 2 and 50 characters" )
  private String lastName;

  @NotNull( message = "Age must be provided" )
  @Min( value = 18, message = "Age must be greater than or equal to 18" )
  @Max( value = 100, message = "Age must be less than or equal to 100" )
  private Integer age;
}
