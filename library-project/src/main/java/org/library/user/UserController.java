package org.library.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.library.auth.annotations.CurrentUser;
import org.library.user.dto.UpdateUserDto;
import org.library.user.dto.UserResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping( "/api/v1/user/profile" )
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<UserResponseDto> getProfile ( @CurrentUser UserResponseDto currentUser ) {
    return ResponseEntity.ok( userService.getProfile( currentUser.getId() ) );
  }

  @PatchMapping
  public ResponseEntity<UserResponseDto> updateProfile (
    @CurrentUser UserResponseDto currentUser, @RequestBody @Valid UpdateUserDto dto
  ) {
    return ResponseEntity.ok( userService.update(
      currentUser.getId(),
      dto
    ) );
  }
}

