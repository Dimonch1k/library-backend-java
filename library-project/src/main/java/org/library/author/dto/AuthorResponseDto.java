package org.library.author.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthorResponseDto {
  private UUID id;
  private String firstName;
  private String lastName;
  private Integer age;
}