package org.library.author;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.library.author.dto.AuthorResponseDto;
import org.library.author.dto.CreateAuthorDto;
import org.library.author.dto.UpdateAuthorDto;
import org.library.author.model.Author;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class AuthorService {
  private final AuthorRepository authorRepository;

  @Transactional
  public AuthorResponseDto create ( CreateAuthorDto dto ) {
    checkAuthorDuplicate(
      dto.getFirstName().trim(),
      dto.getLastName().trim()
    );

    Author author = Author
      .builder()
      .id( UUID.randomUUID() )
      .firstName( dto.getFirstName().trim() )
      .lastName( dto.getLastName().trim() )
      .age( dto.getAge() )
      .build();

    try {
      return toResponseDto( authorRepository.save( author ) );
    } catch ( DataIntegrityViolationException e ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Failed to create author"
      );
    }
  }

  public List<AuthorResponseDto> getAll () {
    return authorRepository.findAll().stream().map( this::toResponseDto ).toList();
  }

  public AuthorResponseDto getById ( UUID id ) {
    Author author = authorRepository.findById( id ).orElseThrow( () -> new ResponseStatusException(
      NOT_FOUND,
      "Author not found with id: " + id
    ) );

    return toResponseDto( author );
  }

  @Transactional
  public AuthorResponseDto update ( UUID id, UpdateAuthorDto dto ) {
    Author author = getByIdInternal( id );

    if ( dto.getFirstName() != null ) {
      author.setFirstName( dto.getFirstName().trim() );
    }

    if ( dto.getLastName() != null ) {
      author.setLastName( dto.getLastName().trim() );
    }

    if ( dto.getAge() != null ) {
      author.setAge( dto.getAge() );
    }

    try {
      return toResponseDto( authorRepository.save( author ) );
    } catch ( DataIntegrityViolationException e ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Failed to update author"
      );
    }
  }

  @Transactional
  public void delete ( UUID id ) {
    Author author = getByIdInternal( id );

    try {
      authorRepository.delete( author );
    } catch ( Exception e ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Failed to delete author"
      );
    }
  }

  private Author getByIdInternal ( UUID id ) {
    return authorRepository.findById( id ).orElseThrow( () -> new ResponseStatusException(
      NOT_FOUND,
      "Author not found with id: " + id
    ) );
  }

  private void checkAuthorDuplicate ( String firstName, String lastName ) {
    Optional<Author> existing = authorRepository.findByFirstNameAndLastName(
      firstName,
      lastName
    );

    if ( existing.isPresent() ) {
      throw new ResponseStatusException(
        CONFLICT,
        "Author with the same name already exists"
      );
    }
  }

  private AuthorResponseDto toResponseDto ( Author author ) {
    return new AuthorResponseDto(
      author.getId(),
      author.getFirstName(),
      author.getLastName(),
      author.getAge()
    );
  }
}
