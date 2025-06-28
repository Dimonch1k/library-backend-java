package org.library.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.library.auth.enums.Role;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponseDto {
  private UUID id;
  private String email;
  private Role role;
}
