package org.library.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.library.user.dto.UpdateUserDto;
import org.library.user.dto.UserResponseDto;
import org.library.user.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class UserService {
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

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

  private UserResponseDto toResponseDto ( User user ) {
    return new UserResponseDto(
      user.getId(),
      user.getEmail(),
      user.getRole()
    );
  }
}
