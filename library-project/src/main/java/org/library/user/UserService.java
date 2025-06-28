package org.library.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.library.auth.dto.RegisterDto;
import org.library.auth.enums.Role;
import org.library.user.dto.UpdateUserDto;
import org.library.user.dto.UserResponseDto;
import org.library.user.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class UserService {
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  @Transactional
  public UserResponseDto create ( RegisterDto dto ) {
    checkUserDuplicateByEmail( dto.getEmail().trim() );

    String hashedPassword = passwordEncoder.encode( dto.getPassword().trim() );
    dto.setPassword( hashedPassword );

    User user = User
      .builder()
      .id( UUID.randomUUID() )
      .email( dto.getEmail().trim() )
      .password( dto.getPassword() )
      .role( Role.USER )
      .build();

    try {
      return toResponseDto( userRepository.save( user ) );
    } catch ( DataIntegrityViolationException e ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Failed to register user with email: " + dto.getEmail()
      );
    }
  }

  public UserResponseDto getProfile ( UUID id ) {
    User user = userRepository.findById( id ).orElseThrow( () -> new ResponseStatusException(
      NOT_FOUND,
      "Such user doesn't exist"
    ) );
    return toResponseDto( user );
  }

  @Transactional
  public UserResponseDto update ( UUID id, UpdateUserDto dto ) {
    User user = userRepository.findById( id ).orElseThrow( () -> new ResponseStatusException(
      NOT_FOUND,
      "User not found"
    ) );

    if ( dto.getEmail() != null ) {
      user.setEmail( dto.getEmail().trim() );
    }

    if ( dto.getPassword() != null ) {
      String hashedPassword = passwordEncoder.encode( dto.getPassword().trim() );
      user.setPassword( hashedPassword );
    }

    return toResponseDto( userRepository.save( user ) );
  }

  public void checkUserExists ( UUID userId ) {
    if ( !userRepository.existsById( userId ) ) {
      throw new ResponseStatusException(
        NOT_FOUND,
        "User not found with id: " + userId
      );
    }
  }

  public UserResponseDto getById ( UUID id ) {
    User user = userRepository.findById( id ).orElseThrow( () -> new ResponseStatusException(
      NOT_FOUND,
      "User not found with id: " + id
    ) );
    return toResponseDto( user );
  }

  public UserResponseDto getByEmail ( String email ) {
    User user = userRepository.findByEmail( email ).orElseThrow( () -> new ResponseStatusException(
      NOT_FOUND,
      "User not found with email: " + email
    ) );
    return toResponseDto( user );
  }

  public Optional<User> getByEmailWithoutException ( String email ) {
    return userRepository.findByEmail( email );
  }

  public User getByEmailInternal ( String email ) {
    User user = userRepository.findByEmail( email ).orElseThrow( () -> new ResponseStatusException(
      NOT_FOUND,
      "User not found with email: " + email
    ) );
    return user;
  }

  public void checkUserDuplicateByEmail ( String email ) {
    if ( userRepository.findByEmail( email ).isPresent() ) {
      throw new ResponseStatusException(
        CONFLICT,
        "User with the email" + email + " already exists"
      );
    }
  }

  public UserResponseDto toResponseDto ( User user ) {
    return new UserResponseDto(
      user.getId(),
      user.getEmail(),
      user.getRole()
    );
  }
}
