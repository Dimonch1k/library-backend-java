package org.library.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.library.user.dto.UserResponseDto;

@Data
@AllArgsConstructor
public class AuthResponseDto {
  private UserResponseDto user;
  private String accessToken;
}
