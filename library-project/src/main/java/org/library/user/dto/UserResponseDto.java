package org.library.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponseDto {
  private UUID id;
  private String email;
  private String role;
}
