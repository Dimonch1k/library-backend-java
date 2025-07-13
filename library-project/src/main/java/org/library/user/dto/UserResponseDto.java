package org.library.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.library.auth.enums.Role;


@Data
@AllArgsConstructor
public class UserResponseDto {
  private Long id;
  private String email;
  private String role;
}
