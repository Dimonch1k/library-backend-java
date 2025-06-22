package org.library.book;

import org.junit.jupiter.api.*;
import org.library.author.AuthorService;
import org.library.book.dto.BookResponseDto;
import org.library.book.dto.CreateBookDto;
import org.library.book.dto.UpdateBookDto;
import org.library.book.model.Book;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestBookService {

  private BookService bookService;

  @Mock
  private BookRepository bookRepository;

  @Mock
  private AuthorService authorService;

  private AutoCloseable autoCloseable;

  @BeforeEach
  void setUp () {
    autoCloseable = MockitoAnnotations.openMocks( this );
    bookService = new BookService(
      bookRepository,
      authorService
    );
  }

  @AfterEach
  void tearDown () throws Exception {
    autoCloseable.close();
  }

  @Test
  public void testCreate_Success () {
    CreateBookDto dto = new CreateBookDto();
    dto.setTitle( "Test Title" );
    dto.setGenre( "Fantasy" );
    dto.setYear( 2020 );
    UUID authorId = UUID.randomUUID();
    dto.setAuthorId( authorId.toString() );

    when( bookRepository.findByTitle( "Test Title" ) ).thenReturn( Optional.empty() );
    when( authorService.getById( authorId ) ).thenReturn( null ); // Author is checked but not returned
    when( bookRepository.save( any( Book.class ) ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );

    BookResponseDto response = bookService.create( dto );

    assertEquals(
      "Test Title",
      response.getTitle()
    );
    assertEquals(
      "Fantasy",
      response.getGenre()
    );
    assertEquals(
      2020,
      response.getYear()
    );
    assertEquals(
      authorId,
      response.getAuthorId()
    );
  }

  @Test
  public void testCreate_Duplicate_ThrowsConflict () {
    CreateBookDto dto = new CreateBookDto();
    dto.setTitle( "Duplicate Title" );

    when( bookRepository.findByTitle( "Duplicate Title" ) ).thenReturn( Optional.of( new Book() ) );

    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> {
        bookService.create( dto );
      }
    );

    assertEquals(
      409,
      ex.getStatusCode().value()
    );
    assertTrue( ex.getReason().contains( "already exists" ) );
  }

  @Test
  public void testGetAll_ReturnsList () {
    UUID authorId = UUID.randomUUID();
    List<Book> books = List.of(
      Book
        .builder()
        .id( UUID.randomUUID() )
        .title( "A" )
        .genre( "G1" )
        .year( 2020 )
        .authorId( authorId )
        .build(),
      Book
        .builder()
        .id( UUID.randomUUID() )
        .title( "B" )
        .genre( "G2" )
        .year( 2021 )
        .authorId( authorId )
        .build()
    );

    when( bookRepository.findAll() ).thenReturn( books );

    List<BookResponseDto> result = bookService.getAll();
    assertEquals(
      2,
      result.size()
    );
  }

  @Test
  public void testGetById_Success () {
    UUID id = UUID.randomUUID();
    Book book = Book
      .builder()
      .id( id )
      .title( "Book Title" )
      .genre( "Genre" )
      .year( 2023 )
      .authorId( UUID.randomUUID() )
      .build();

    when( bookRepository.findById( id ) ).thenReturn( Optional.of( book ) );

    BookResponseDto response = bookService.getById( id );

    assertEquals(
      "Book Title",
      response.getTitle()
    );
  }

  @Test
  public void testGetById_NotFound () {
    UUID id = UUID.randomUUID();

    when( bookRepository.findById( id ) ).thenReturn( Optional.empty() );

    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> {
        bookService.getById( id );
      }
    );

    assertEquals(
      404,
      ex.getStatusCode().value()
    );
  }

  @Test
  public void testUpdate_Success () {
    UUID id = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    Book existingBook = Book
      .builder()
      .id( id )
      .title( "Old Title" )
      .genre( "Old Genre" )
      .year( 2010 )
      .authorId( authorId )
      .build();

    UpdateBookDto dto = new UpdateBookDto();
    dto.setTitle( "New Title" );

    when( bookRepository.findById( id ) ).thenReturn( Optional.of( existingBook ) );
    when( bookRepository.save( any( Book.class ) ) ).thenAnswer( i -> i.getArgument( 0 ) );

    BookResponseDto updated = bookService.update(
      id,
      dto
    );

    assertEquals(
      "New Title",
      updated.getTitle()
    );
  }

  @Test
  public void testUpdate_NotFound () {
    UUID id = UUID.randomUUID();
    UpdateBookDto dto = new UpdateBookDto();
    dto.setTitle( "Updated" );

    when( bookRepository.findById( id ) ).thenReturn( Optional.empty() );

    assertThrows(
      ResponseStatusException.class,
      () -> bookService.update(
        id,
        dto
      )
    );
  }

  @Test
  public void testDelete_Success () {
    UUID id = UUID.randomUUID();
    Book book = Book.builder().id( id ).title( "Delete Me" ).build();

    when( bookRepository.findById( id ) ).thenReturn( Optional.of( book ) );
    doNothing().when( bookRepository ).delete( book );

    assertDoesNotThrow( () -> bookService.delete( id ) );
  }

  @Test
  public void testDelete_NotFound () {
    UUID id = UUID.randomUUID();

    when( bookRepository.findById( id ) ).thenReturn( Optional.empty() );

    assertThrows(
      ResponseStatusException.class,
      () -> bookService.delete( id )
    );
  }

  @Test
  public void testCreate_DataIntegrityViolation () {
    CreateBookDto dto = new CreateBookDto();
    dto.setTitle( "Title" );
    dto.setGenre( "Genre" );
    dto.setYear( 2022 );
    UUID authorId = UUID.randomUUID();
    dto.setAuthorId( authorId.toString() );

    when( bookRepository.findByTitle( "Title" ) ).thenReturn( Optional.empty() );
    when( authorService.getById( authorId ) ).thenReturn( null );
    when( bookRepository.save( any() ) ).thenThrow( new DataIntegrityViolationException( "" ) );

    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> bookService.create( dto )
    );

    assertEquals(
      400,
      ex.getStatusCode().value()
    );
    assertEquals(
      "Failed to create book",
      ex.getReason()
    );
  }
}
