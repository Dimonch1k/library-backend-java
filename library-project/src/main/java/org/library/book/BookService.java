package org.library.book;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.library.author.AuthorService;
import org.library.book.dto.BookResponseDto;
import org.library.book.dto.CreateBookDto;
import org.library.book.dto.UpdateBookDto;
import org.library.book.model.Book;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class BookService {
  private final BookRepository bookRepository;
  private final AuthorService authorService;

  @Transactional
  public BookResponseDto create ( CreateBookDto dto ) {
    checkBookDuplicate( dto.getTitle().trim() );

    authorService.getById( UUID.fromString( dto.getAuthorId() ) );

    Book book = Book
      .builder()
      .id( UUID.randomUUID() )
      .title( dto.getTitle().trim() )
      .description( dto.getDescription() != null
                    ? dto.getDescription().trim()
                    : null )
      .genre( dto.getGenre().trim() )
      .year( dto.getYear() )
      .authorId( UUID.fromString( dto.getAuthorId() ) )
      .build();

    try {
      return toResponseDto( bookRepository.save( book ) );
    } catch ( DataIntegrityViolationException e ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Failed to create book"
      );
    }
  }

  public List<BookResponseDto> getAll () {
    return bookRepository.findAll().stream().map( this::toResponseDto ).toList();
  }

  public BookResponseDto getById ( UUID id ) {
    Book book = bookRepository.findById( id ).orElseThrow( () -> new ResponseStatusException(
      NOT_FOUND,
      "Book not found with id: " + id
    ) );
    return toResponseDto( book );
  }

  @Transactional
  public BookResponseDto update ( UUID id, UpdateBookDto dto ) {
    Book book = getByIdInternal( id );

    if ( dto.getTitle() != null ) {
      book.setTitle( dto.getTitle().trim() );
    }
    if ( dto.getDescription() != null ) {
      book.setDescription( dto.getDescription().trim() );
    }
    if ( dto.getGenre() != null ) {
      book.setGenre( dto.getGenre().trim() );
    }
    if ( dto.getYear() != null ) {
      book.setYear( dto.getYear() );
    }
    if ( dto.getAuthorId() != null ) {
      authorService.getById( UUID.fromString( dto.getAuthorId() ) );
      book.setAuthorId( UUID.fromString( dto.getAuthorId() ) );
    }

    try {
      return toResponseDto( bookRepository.save( book ) );
    } catch ( DataIntegrityViolationException e ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Failed to update book"
      );
    }
  }

  @Transactional
  public void delete ( UUID id ) {
    Book book = getByIdInternal( id );
    try {
      bookRepository.delete( book );
    } catch ( Exception e ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Failed to delete book"
      );
    }
  }

  private Book getByIdInternal ( UUID id ) {
    return bookRepository.findById( id ).orElseThrow( () -> new ResponseStatusException(
      NOT_FOUND,
      "Book not found with id: " + id
    ) );
  }

  private void checkBookDuplicate ( String title ) {
    if ( bookRepository.findByTitle( title ).isPresent() ) {
      throw new ResponseStatusException(
        CONFLICT,
        "Book with the same title already exists"
      );
    }
  }

  private BookResponseDto toResponseDto ( Book book ) {
    return new BookResponseDto(
      book.getId(),
      book.getTitle(),
      book.getDescription(),
      book.getGenre(),
      book.getYear(),
      book.getAuthorId()
    );
  }
}
