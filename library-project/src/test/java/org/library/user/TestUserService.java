package org.library.user;

import org.junit.jupiter.api.*;
import org.library.auth.dto.RegisterDto;
import org.library.auth.enums.Role;
import org.library.user.dto.UpdateUserDto;
import org.library.user.dto.UserResponseDto;
import org.library.user.model.User;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestUserService {

  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  private AutoCloseable autoCloseable;

  @BeforeEach
  void setUp () {
    autoCloseable = MockitoAnnotations.openMocks( this );
    userService = new UserService(
      passwordEncoder,
      userRepository
    );
  }

  @AfterEach
  void tearDown () throws Exception {
    autoCloseable.close();
  }

  @Test
  public void testCreate_Success () {
    // Arrange
    RegisterDto dto = new RegisterDto();
    dto.setEmail( "test@example.com" );
    dto.setPassword( "password123" );

    User savedUser = User
      .builder()
      .id( 1L )
      .email( "test@example.com" )
      .password( "hashedPassword" )
      .role( Role.USER.toString() )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    when( userRepository.findByEmail( "test@example.com" ) ).thenReturn( Optional.empty() );
    when( passwordEncoder.encode( "password123" ) ).thenReturn( "hashedPassword" );
    when( userRepository.save( any( User.class ) ) ).thenReturn( savedUser );

    // Act
    UserResponseDto response = userService.create( dto );

    // Assert
    assertEquals(
      1L,
      response.getId()
    );
    assertEquals(
      "test@example.com",
      response.getEmail()
    );
    assertEquals(
      Role.USER.toString(),
      response.getRole()
    );

    verify( userRepository ).findByEmail( "test@example.com" );
    verify( passwordEncoder ).encode( "password123" );
    verify( userRepository ).save( any( User.class ) );
  }

  @Test
  public void testCreate_DuplicateEmail_ThrowsConflict () {
    // Arrange
    RegisterDto dto = new RegisterDto();
    dto.setEmail( "duplicate@example.com" );
    dto.setPassword( "password123" );

    when( userRepository.findByEmail( "duplicate@example.com" ) ).thenReturn( Optional.of( new User() ) );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> userService.create( dto )
    );

    assertEquals(
      409,
      ex.getStatusCode().value()
    );
    assertTrue( ex.getReason().contains( "already exists" ) );

    verify( userRepository ).findByEmail( "duplicate@example.com" );
    verify(
      passwordEncoder,
      never()
    ).encode( any() );
    verify(
      userRepository,
      never()
    ).save( any() );
  }

  @Test
  public void testCreate_DataIntegrityViolation () {
    // Arrange
    RegisterDto dto = new RegisterDto();
    dto.setEmail( "test@example.com" );
    dto.setPassword( "password123" );

    when( userRepository.findByEmail( "test@example.com" ) ).thenReturn( Optional.empty() );
    when( passwordEncoder.encode( "password123" ) ).thenReturn( "hashedPassword" );
    when( userRepository.save( any( User.class ) ) ).thenThrow( new DataIntegrityViolationException( "" ) );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> userService.create( dto )
    );

    assertEquals(
      400,
      ex.getStatusCode().value()
    );
    assertTrue( ex
                  .getReason()
                  .contains( "Failed to register user with email" ) );
  }

  @Test
  public void testGetProfile_Success () {
    // Arrange
    Long userId = 1L;
    User user = User
      .builder()
      .id( userId )
      .email( "test@example.com" )
      .password( "hashedPassword" )
      .role( Role.USER.toString() )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    when( userRepository.findById( userId ) ).thenReturn( Optional.of( user ) );

    // Act
    UserResponseDto response = userService.getProfile( userId );

    // Assert
    assertEquals(
      userId,
      response.getId()
    );
    assertEquals(
      "test@example.com",
      response.getEmail()
    );
    assertEquals(
      Role.USER.toString(),
      response.getRole()
    );

    verify( userRepository ).findById( userId );
  }

  @Test
  public void testGetProfile_NotFound () {
    // Arrange
    Long userId = 1L;

    when( userRepository.findById( userId ) ).thenReturn( Optional.empty() );

    // Act & Assert
    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> userService.getProfile( userId )
    );

    assertEquals(
      404,
      exception.getStatusCode().value()
    );
    assertTrue( exception.getReason().contains( "Such user doesn't exist" ) );

    verify( userRepository ).findById( userId );
  }

  @Test
  public void testUpdate_Success () {
    // Arrange
    Long userId = 1L;
    User user = User
      .builder()
      .id( userId )
      .email( "old@example.com" )
      .password( "oldHashedPassword" )
      .role( Role.USER.toString() )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    UpdateUserDto dto = new UpdateUserDto();
    dto.setEmail( "new@example.com" );
    dto.setPassword( "newPassword" );

    when( userRepository.findById( userId ) ).thenReturn( Optional.of( user ) );
    when( passwordEncoder.encode( "newPassword" ) ).thenReturn( "newHashedPassword" );
    when( userRepository.save( any( User.class ) ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );

    // Act
    UserResponseDto result = userService.update(
      userId,
      dto
    );

    // Assert
    assertEquals(
      "new@example.com",
      user.getEmail()
    );
    assertEquals(
      "newHashedPassword",
      user.getPassword()
    );
    assertEquals(
      userId,
      result.getId()
    );
    assertEquals(
      "new@example.com",
      result.getEmail()
    );

    verify( userRepository ).findById( userId );
    verify( passwordEncoder ).encode( "newPassword" );
    verify( userRepository ).save( user );
  }

  @Test
  public void testUpdate_EmailOnly () {
    // Arrange
    Long userId = 1L;
    User user = User
      .builder()
      .id( userId )
      .email( "old@example.com" )
      .password( "oldHashedPassword" )
      .role( Role.USER.toString() )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    UpdateUserDto dto = new UpdateUserDto();
    dto.setEmail( "new@example.com" );
    // No password update

    when( userRepository.findById( userId ) ).thenReturn( Optional.of( user ) );
    when( userRepository.save( any( User.class ) ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );

    // Act
    UserResponseDto result = userService.update(
      userId,
      dto
    );

    // Assert
    assertEquals(
      "new@example.com",
      user.getEmail()
    );
    assertEquals(
      "oldHashedPassword",
      user.getPassword()
    ); // Password unchanged
    assertEquals(
      userId,
      result.getId()
    );

    verify( userRepository ).findById( userId );
    verify(
      passwordEncoder,
      never()
    ).encode( any() );
    verify( userRepository ).save( user );
  }

  @Test
  public void testUpdate_PasswordOnly () {
    // Arrange
    Long userId = 1L;
    User user = User
      .builder()
      .id( userId )
      .email( "test@example.com" )
      .password( "oldHashedPassword" )
      .role( Role.USER.toString() )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    UpdateUserDto dto = new UpdateUserDto();
    dto.setPassword( "newPassword" );
    // No email update

    when( userRepository.findById( userId ) ).thenReturn( Optional.of( user ) );
    when( passwordEncoder.encode( "newPassword" ) ).thenReturn( "newHashedPassword" );
    when( userRepository.save( any( User.class ) ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );

    // Act
    UserResponseDto result = userService.update(
      userId,
      dto
    );

    // Assert
    assertEquals(
      "test@example.com",
      user.getEmail()
    ); // Email unchanged
    assertEquals(
      "newHashedPassword",
      user.getPassword()
    );
    assertEquals(
      userId,
      result.getId()
    );

    verify( userRepository ).findById( userId );
    verify( passwordEncoder ).encode( "newPassword" );
    verify( userRepository ).save( user );
  }

  @Test
  public void testUpdate_NotFound () {
    // Arrange
    Long userId = 1L;
    UpdateUserDto dto = new UpdateUserDto();
    dto.setEmail( "email@example.com" );

    when( userRepository.findById( userId ) ).thenReturn( Optional.empty() );

    // Act & Assert
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

    verify( userRepository ).findById( userId );
    verify(
      userRepository,
      never()
    ).save( any() );
  }

  @Test
  public void testCheckUserExists_Success () {
    // Arrange
    Long userId = 1L;

    when( userRepository.existsById( userId ) ).thenReturn( true );

    // Act & Assert
    assertDoesNotThrow( () -> userService.checkUserExists( userId ) );

    verify( userRepository ).existsById( userId );
  }

  @Test
  public void testCheckUserExists_NotFound () {
    // Arrange
    Long userId = 1L;

    when( userRepository.existsById( userId ) ).thenReturn( false );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> userService.checkUserExists( userId )
    );

    assertEquals(
      404,
      ex.getStatusCode().value()
    );
    assertTrue( ex
                  .getReason()
                  .contains( "User not found with id: " + userId ) );

    verify( userRepository ).existsById( userId );
  }

  @Test
  public void testGetById_Success () {
    // Arrange
    Long userId = 1L;
    User user = User
      .builder()
      .id( userId )
      .email( "test@example.com" )
      .password( "hashedPassword" )
      .role( Role.USER.toString() )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    when( userRepository.findById( userId ) ).thenReturn( Optional.of( user ) );

    // Act
    UserResponseDto response = userService.getById( userId );

    // Assert
    assertEquals(
      userId,
      response.getId()
    );
    assertEquals(
      "test@example.com",
      response.getEmail()
    );
    assertEquals(
      Role.USER.toString(),
      response.getRole()
    );

    verify( userRepository ).findById( userId );
  }

  @Test
  public void testGetById_NotFound () {
    // Arrange
    Long userId = 1L;

    when( userRepository.findById( userId ) ).thenReturn( Optional.empty() );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> userService.getById( userId )
    );

    assertEquals(
      404,
      ex.getStatusCode().value()
    );
    assertTrue( ex
                  .getReason()
                  .contains( "User not found with id: " + userId ) );

    verify( userRepository ).findById( userId );
  }

  @Test
  public void testGetByEmail_Success () {
    // Arrange
    String email = "test@example.com";
    User user = User
      .builder()
      .id( 1L )
      .email( email )
      .password( "hashedPassword" )
      .role( Role.USER.toString() )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    when( userRepository.findByEmail( email ) ).thenReturn( Optional.of( user ) );

    // Act
    UserResponseDto response = userService.getByEmail( email );

    // Assert
    assertEquals(
      1L,
      response.getId()
    );
    assertEquals(
      email,
      response.getEmail()
    );
    assertEquals(
      Role.USER.toString(),
      response.getRole()
    );

    verify( userRepository ).findByEmail( email );
  }

  @Test
  public void testGetByEmail_NotFound () {
    // Arrange
    String email = "nonexistent@example.com";

    when( userRepository.findByEmail( email ) ).thenReturn( Optional.empty() );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> userService.getByEmail( email )
    );

    assertEquals(
      404,
      ex.getStatusCode().value()
    );
    assertTrue( ex
                  .getReason()
                  .contains( "User not found with email: " + email ) );

    verify( userRepository ).findByEmail( email );
  }

  @Test
  public void testGetByEmailWithoutException_Success () {
    // Arrange
    String email = "test@example.com";
    User user = User
      .builder()
      .id( 1L )
      .email( email )
      .password( "hashedPassword" )
      .role( Role.USER.toString() )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    when( userRepository.findByEmail( email ) ).thenReturn( Optional.of( user ) );

    // Act
    Optional<User> result = userService.getByEmailWithoutException( email );

    // Assert
    assertTrue( result.isPresent() );
    assertEquals(
      email,
      result.get().getEmail()
    );

    verify( userRepository ).findByEmail( email );
  }

  @Test
  public void testGetByEmailWithoutException_NotFound () {
    // Arrange
    String email = "nonexistent@example.com";

    when( userRepository.findByEmail( email ) ).thenReturn( Optional.empty() );

    // Act
    Optional<User> result = userService.getByEmailWithoutException( email );

    // Assert
    assertFalse( result.isPresent() );

    verify( userRepository ).findByEmail( email );
  }

  @Test
  public void testGetByEmailInternal_Success () {
    // Arrange
    String email = "test@example.com";
    User user = User
      .builder()
      .id( 1L )
      .email( email )
      .password( "hashedPassword" )
      .role( Role.USER.toString() )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    when( userRepository.findByEmail( email ) ).thenReturn( Optional.of( user ) );

    // Act
    User result = userService.getByEmailInternal( email );

    // Assert
    assertEquals(
      email,
      result.getEmail()
    );
    assertEquals(
      1L,
      result.getId()
    );

    verify( userRepository ).findByEmail( email );
  }

  @Test
  public void testGetByEmailInternal_NotFound () {
    // Arrange
    String email = "nonexistent@example.com";

    when( userRepository.findByEmail( email ) ).thenReturn( Optional.empty() );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> userService.getByEmailInternal( email )
    );

    assertEquals(
      404,
      ex.getStatusCode().value()
    );
    assertTrue( ex
                  .getReason()
                  .contains( "User not found with email: " + email ) );

    verify( userRepository ).findByEmail( email );
  }

  @Test
  public void testCheckUserDuplicateByEmail_NoDuplicate () {
    // Arrange
    String email = "unique@example.com";

    when( userRepository.findByEmail( email ) ).thenReturn( Optional.empty() );

    // Act & Assert
    assertDoesNotThrow( () -> userService.checkUserDuplicateByEmail( email ) );

    verify( userRepository ).findByEmail( email );
  }

  @Test
  public void testCheckUserDuplicateByEmail_DuplicateExists () {
    // Arrange
    String email = "duplicate@example.com";
    User existingUser = User
      .builder()
      .id( 1L )
      .email( email )
      .password( "hashedPassword" )
      .role( Role.USER.toString() )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    when( userRepository.findByEmail( email ) ).thenReturn( Optional.of( existingUser ) );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> userService.checkUserDuplicateByEmail( email )
    );

    assertEquals(
      409,
      ex.getStatusCode().value()
    );
    assertTrue( ex
                  .getReason()
                  .contains(
                    "User with the email " + email + " already exists" ) );

    verify( userRepository ).findByEmail( email );
  }

  @Test
  public void testToResponseDto () {
    // Arrange
    User user = User
      .builder()
      .id( 1L )
      .email( "test@example.com" )
      .password( "hashedPassword" )
      .role( Role.USER.toString() )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    // Act
    UserResponseDto result = userService.toResponseDto( user );

    // Assert
    assertEquals(
      1L,
      result.getId()
    );
    assertEquals(
      "test@example.com",
      result.getEmail()
    );
    assertEquals(
      Role.USER.toString(),
      result.getRole()
    );
  }
}