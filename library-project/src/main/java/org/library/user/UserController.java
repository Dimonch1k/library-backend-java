package org.library.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.library.user.dto.UpdateUserDto;
import org.library.user.dto.UserResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping( "/api/v1/user/profile" )
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping( "/{id}" )
  public ResponseEntity<UserResponseDto> getProfile ( @PathVariable UUID id ) {
    return ResponseEntity.ok( userService.getProfile( id ) );
  }

  @PatchMapping( "/{id}" )
  public ResponseEntity<UserResponseDto> updateProfile (
    @PathVariable UUID id, @RequestBody @Valid UpdateUserDto dto
  ) {

    return ResponseEntity.ok( userService.update(
      id,
      dto
    ) );
  }
}
