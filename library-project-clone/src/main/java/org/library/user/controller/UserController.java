package org.library.user.controller;

import lombok.RequiredArgsConstructor;
import org.library.user.dto.UserDto;
import org.library.user.model.User;
import org.library.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping( "/api/v1/user/profile" )
@RequiredArgsConstructor
public class UserController
{
  private final UserService userService;

  @GetMapping
  public ResponseEntity<?> profile(
    @AuthenticationPrincipal
    User user )
  {
    if ( user == null ) {
      return ResponseEntity.status( 401 ).body( "Unauthorized" );
    }
    return ResponseEntity.ok( userService.getById( user.getId() ) );
  }

  @PatchMapping
  public ResponseEntity<?> updateProfile(
    @AuthenticationPrincipal
    User user,
    @RequestBody
    UserDto dto )
  {
    if ( user == null ) {
      return ResponseEntity.status( 401 ).body( "Unauthorized" );
    }
    User updatedUser = userService.update(
      user.getId(),
      dto
    );
    return ResponseEntity.ok( updatedUser );
  }
}