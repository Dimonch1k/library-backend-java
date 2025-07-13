package org.library.book;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.library.author.AuthorService;
import org.library.author.dto.AuthorDto;
import org.library.author.model.Author;
import org.library.book.dto.BookResponseDto;
import org.library.book.dto.CreateBookDto;
import org.library.book.dto.UpdateBookDto;
import org.library.book.model.Book;
import org.library.order.enums.OrderStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class BookService {
  private final BookRepository bookRepository;
  private final AuthorService authorService;

  @Transactional
  public BookResponseDto create ( CreateBookDto dto ) {
    checkBookDuplicate( dto.getTitle().trim() );

    Author author = authorService.getFullById( dto.getAuthorId() );

    Book book = Book
      .builder()
      .title( dto.getTitle().trim() )
      .description(
        dto.getDescription() != null ? dto.getDescription().trim() : null )
      .genre( dto.getGenre().trim() )
      .year( dto.getYear() )
      .author( author )
      .status( null )
      .build();

    try {
      return toResponseDto( bookRepository.save( book ) );
    } catch ( DataIntegrityViolationException e ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Failed to create book",
        e
      );
    }
  }

  @Transactional
  public List<BookResponseDto> getAll () {
    return bookRepository
      .findAll()
      .stream()
      .map( this::toResponseDto )
      .toList();
  }

  @Transactional
  public BookResponseDto getById ( Long id ) {
    Book book = bookRepository
      .findById( id )
      .orElseThrow( () -> new ResponseStatusException(
        NOT_FOUND,
        "Book not found with id: " + id
      ) );
    return toResponseDto( book );
  }

  @Transactional
  public BookResponseDto update ( Long id, UpdateBookDto dto ) {
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
      Author author = authorService.getFullById( dto.getAuthorId() );
      book.setAuthor( author );
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
  public void delete ( Long id ) {
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

  @Transactional
  public void updateStatus(Long id, OrderStatus bookStatus) {
    Book book = getByIdInternal(id);
    book.setStatus(bookStatus);

    try {
      bookRepository.save(book);
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Failed to update book status"
      );
    }
  }


  private Book getByIdInternal ( Long id ) {
    return bookRepository
      .findById( id )
      .orElseThrow( () -> new ResponseStatusException(
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

  public void checkBookExists ( Long bookId ) {
    if ( !bookRepository.existsById( bookId ) ) {
      throw new ResponseStatusException(
        NOT_FOUND,
        "Book not found with id: " + bookId
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
      book.getStatus(),
      authorService.toAuthorDto( book.getAuthor() )
    );
  }
}