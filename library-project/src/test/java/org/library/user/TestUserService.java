package org.library.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.user.dto.UpdateUserDto;
import org.library.user.dto.UserResponseDto;
import org.library.user.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestUserService {

  private UserService userService;
  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp () {
    userRepository = mock( UserRepository.class );
    passwordEncoder = mock( PasswordEncoder.class );
    userService = new UserService(
      passwordEncoder,
      userRepository
    );
  }

  @Test
  public void testGetProfile_Success () {
    UUID userId = UUID.randomUUID();
    User user = User.builder().id( userId ).email( "test@example.com" ).role( "USER" ).build();

    when( userRepository.findById( userId ) ).thenReturn( Optional.of( user ) );

    UserResponseDto response = userService.getProfile( userId );

    assertEquals(
      userId,
      response.getId()
    );
    assertEquals(
      "test@example.com",
      response.getEmail()
    );
    assertEquals(
      "USER",
      response.getRole()
    );
  }

  @Test
  public void testGetProfile_NotFound () {
    UUID userId = UUID.randomUUID();

    when( userRepository.findById( userId ) ).thenReturn( Optional.empty() );

    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> userService.getProfile( userId )
    );

    assertEquals(
      404,
      exception.getStatusCode().value()
    );
    assertTrue( exception.getReason().contains( "Such user doesn't exist" ) );
  }

  @Test
  public void testUpdate_Success () {
    UUID userId = UUID.randomUUID();
    User user = User
      .builder()
      .id( userId )
      .email( "old@example.com" )
      .password( "oldPass" )
      .role( "USER" )
      .build();

    UpdateUserDto dto = new UpdateUserDto();
    dto.setEmail( "new@example.com" );
    dto.setPassword( "newPassword" );

    when( userRepository.findById( userId ) ).thenReturn( Optional.of( user ) );
    when( passwordEncoder.encode( "newPassword" ) ).thenReturn( "hashedPassword" );
    when( userRepository.save( any( User.class ) ) ).thenAnswer( i -> i.getArgument( 0 ) );

    UserResponseDto result = userService.update(
      userId,
      dto
    );

    assertEquals(
      "new@example.com",
      user.getEmail()
    );
    assertEquals(
      "hashedPassword",
      user.getPassword()
    );
    assertEquals(
      userId,
      result.getId()
    );
  }

  @Test
  public void testUpdate_NotFound () {
    UUID userId = UUID.randomUUID();
    UpdateUserDto dto = new UpdateUserDto();
    dto.setEmail( "email@example.com" );

    when( userRepository.findById( userId ) ).thenReturn( Optional.empty() );

    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> userService.update(
        userId,
        dto
      )
    );

    assertEquals(
      404,
      exception.getStatusCode().value()
    );
    assertTrue( exception.getReason().contains( "User not found" ) );
  }
}
