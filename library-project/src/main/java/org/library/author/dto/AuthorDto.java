package org.library.author.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorDto {
  private Long id;
  private String firstName;
  private String lastName;
  private Integer age;
}