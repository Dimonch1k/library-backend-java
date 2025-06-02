package org.library.author;

import lombok.RequiredArgsConstructor;
import org.library.author.dto.CreateAuthorDto;
import org.library.author.dto.UpdateAuthorDto;
import org.library.author.model.Author;
import org.library.author.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorService
{
  private final AuthorRepository authorRepository;

  public Author create( CreateAuthorDto dto ) {
    Author author = Author.builder()
                          .id( UUID.randomUUID().toString() )
                          .firstName( dto.getFirstName().trim() )
                          .lastName( dto.getLastName().trim() )
                          .age( dto.getAge() )
                          .build();

    return authorRepository.save( author );
  }

  public List<Author> getAll() {
    return authorRepository.findAll();
  }

  public Author getById( String id ) {
    return authorRepository.findById( id )
                           .orElseThrow( () -> new RuntimeException(
                             "Author not found with id: " + id ) );
  }

  public Author update( String id, UpdateAuthorDto dto ) {
    Author author = getById( id );

    if ( dto.getFirstName() != null ) {
      author.setFirstName( dto.getFirstName().trim() );
    }

    if ( dto.getLastName() != null ) {
      author.setLastName( dto.getLastName().trim() );
    }

    if ( dto.getAge() != null ) {
      author.setAge( dto.getAge() );
    }

    return authorRepository.save( author );
  }

  public void delete( String id ) {
    Author author = getById( id );
    authorRepository.delete( author );
  }
}
