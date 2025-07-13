package org.library.book;

import org.junit.jupiter.api.*;
import org.library.author.AuthorService;
import org.library.author.dto.AuthorDto;
import org.library.author.model.Author;
import org.library.book.dto.BookResponseDto;
import org.library.book.dto.CreateBookDto;
import org.library.book.dto.UpdateBookDto;
import org.library.book.model.Book;
import org.library.order.enums.OrderStatus;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
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
    // Arrange
    CreateBookDto dto = new CreateBookDto();
    dto.setTitle( "Test Title" );
    dto.setGenre( "Fantasy" );
    dto.setYear( 2020 );
    dto.setDescription( "Test Description" );
    Long authorId = 1L;
    dto.setAuthorId( authorId );

    Author author = Author
      .builder()
      .id( authorId )
      .firstName( "John" )
      .lastName( "Doe" )
      .age( 30 )
      .build();

    Book savedBook = Book
      .builder()
      .id( 1L )
      .title( "Test Title" )
      .description( "Test Description" )
      .genre( "Fantasy" )
      .year( 2020 )
      .author( author )
      .status( null )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    AuthorDto authorDto = new AuthorDto(
      authorId,
      "John",
      "Doe",
      30
    );

    when( bookRepository.findByTitle( "Test Title" ) ).thenReturn( Optional.empty() );
    when( authorService.getFullById( authorId ) ).thenReturn( author );
    when( bookRepository.save( any( Book.class ) ) ).thenReturn( savedBook );
    when( authorService.toAuthorDto( author ) ).thenReturn( authorDto );

    // Act
    BookResponseDto response = bookService.create( dto );

    // Assert
    assertEquals(
      "Test Title",
      response.getTitle()
    );
    assertEquals(
      "Test Description",
      response.getDescription()
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
      response.getAuthor().getId()
    );
    assertEquals(
      "John",
      response.getAuthor().getFirstName()
    );
    assertEquals(
      "Doe",
      response.getAuthor().getLastName()
    );

    verify( bookRepository ).findByTitle( "Test Title" );
    verify( authorService ).getFullById( authorId );
    verify( bookRepository ).save( any( Book.class ) );
  }

  @Test
  public void testCreate_Duplicate_ThrowsConflict () {
    // Arrange
    CreateBookDto dto = new CreateBookDto();
    dto.setTitle( "Duplicate Title" );

    when( bookRepository.findByTitle( "Duplicate Title" ) ).thenReturn( Optional.of( new Book() ) );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> bookService.create( dto )
    );

    assertEquals(
      409,
      ex.getStatusCode().value()
    );
    assertTrue( ex.getReason().contains( "already exists" ) );

    verify( bookRepository ).findByTitle( "Duplicate Title" );
    verify(
      authorService,
      never()
    ).getFullById( any() );
    verify(
      bookRepository,
      never()
    ).save( any() );
  }

  @Test
  public void testCreate_DataIntegrityViolation () {
    // Arrange
    CreateBookDto dto = new CreateBookDto();
    dto.setTitle( "Title" );
    dto.setGenre( "Genre" );
    dto.setYear( 2022 );
    Long authorId = 1L;
    dto.setAuthorId( authorId );

    Author author = Author
      .builder()
      .id( authorId )
      .firstName( "John" )
      .lastName( "Doe" )
      .age( 30 )
      .build();

    when( bookRepository.findByTitle( "Title" ) ).thenReturn( Optional.empty() );
    when( authorService.getFullById( authorId ) ).thenReturn( author );
    when( bookRepository.save( any( Book.class ) ) ).thenThrow( new DataIntegrityViolationException( "" ) );

    // Act & Assert
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

  @Test
  public void testGetAll_ReturnsList () {
    // Arrange
    Long authorId = 1L;
    Author author = Author
      .builder()
      .id( authorId )
      .firstName( "John" )
      .lastName( "Doe" )
      .age( 30 )
      .build();

    List<Book> books = List.of(
      Book
        .builder()
        .id( 1L )
        .title( "Book A" )
        .genre( "Genre 1" )
        .year( 2020 )
        .author( author )
        .createdAt( Instant.now() )
        .updatedAt( Instant.now() )
        .build(),
      Book
        .builder()
        .id( 2L )
        .title( "Book B" )
        .genre( "Genre 2" )
        .year( 2021 )
        .author( author )
        .createdAt( Instant.now() )
        .updatedAt( Instant.now() )
        .build()
    );

    AuthorDto authorDto = new AuthorDto(
      authorId,
      "John",
      "Doe",
      30
    );

    when( bookRepository.findAll() ).thenReturn( books );
    when( authorService.toAuthorDto( author ) ).thenReturn( authorDto );

    // Act
    List<BookResponseDto> result = bookService.getAll();

    // Assert
    assertEquals(
      2,
      result.size()
    );
    assertEquals(
      "Book A",
      result.get( 0 ).getTitle()
    );
    assertEquals(
      "Book B",
      result.get( 1 ).getTitle()
    );

    verify( bookRepository ).findAll();
    verify(
      authorService,
      times( 2 )
    ).toAuthorDto( author );
  }

  @Test
  public void testGetById_Success () {
    // Arrange
    Long id = 1L;
    Long authorId = 1L;
    Author author = Author
      .builder()
      .id( authorId )
      .firstName( "John" )
      .lastName( "Doe" )
      .age( 30 )
      .build();

    Book book = Book
      .builder()
      .id( id )
      .title( "Book Title" )
      .genre( "Genre" )
      .year( 2023 )
      .author( author )

      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    AuthorDto authorDto = new AuthorDto(
      authorId,
      "John",
      "Doe",
      30
    );

    when( bookRepository.findById( id ) ).thenReturn( Optional.of( book ) );
    when( authorService.toAuthorDto( author ) ).thenReturn( authorDto );

    // Act
    BookResponseDto response = bookService.getById( id );

    // Assert
    assertEquals(
      "Book Title",
      response.getTitle()
    );
    assertEquals(
      "Genre",
      response.getGenre()
    );
    assertEquals(
      2023,
      response.getYear()
    );
    assertEquals(
      authorId,
      response.getAuthor().getId()
    );

    verify( bookRepository ).findById( id );
    verify( authorService ).toAuthorDto( author );
  }

  @Test
  public void testGetById_NotFound () {
    // Arrange
    Long id = 1L;

    when( bookRepository.findById( id ) ).thenReturn( Optional.empty() );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> bookService.getById( id )
    );

    assertEquals(
      404,
      ex.getStatusCode().value()
    );
    assertTrue( ex.getReason().contains( "Book not found with id: " + id ) );

    verify( bookRepository ).findById( id );
  }

  @Test
  public void testUpdate_Success () {
    // Arrange
    Long id = 1L;
    Long authorId = 1L;
    Author author = Author
      .builder()
      .id( authorId )
      .firstName( "John" )
      .lastName( "Doe" )
      .age( 30 )
      .build();

    Book existingBook = Book
      .builder()
      .id( id )
      .title( "Old Title" )
      .genre( "Old Genre" )
      .year( 2010 )
      .author( author )

      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    UpdateBookDto dto = new UpdateBookDto();
    dto.setTitle( "New Title" );
    dto.setGenre( "New Genre" );

    AuthorDto authorDto = new AuthorDto(
      authorId,
      "John",
      "Doe",
      30
    );

    when( bookRepository.findById( id ) ).thenReturn( Optional.of( existingBook ) );
    when( bookRepository.save( any( Book.class ) ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    when( authorService.toAuthorDto( author ) ).thenReturn( authorDto );

    // Act
    BookResponseDto updated = bookService.update(
      id,
      dto
    );

    // Assert
    assertEquals(
      "New Title",
      updated.getTitle()
    );
    assertEquals(
      "New Genre",
      updated.getGenre()
    );
    assertEquals(
      2010,
      updated.getYear()
    ); // Year should remain unchanged

    verify( bookRepository ).findById( id );
    verify( bookRepository ).save( existingBook );
    verify( authorService ).toAuthorDto( author );
  }

  @Test
  public void testUpdate_WithNewAuthor () {
    // Arrange
    Long id = 1L;
    Long oldAuthorId = 1L;
    Long newAuthorId = 2L;

    Author oldAuthor = Author
      .builder()
      .id( oldAuthorId )
      .firstName( "John" )
      .lastName( "Doe" )
      .age( 30 )
      .build();

    Author newAuthor = Author
      .builder()
      .id( newAuthorId )
      .firstName( "Jane" )
      .lastName( "Smith" )
      .age( 25 )
      .build();

    Book existingBook = Book
      .builder()
      .id( id )
      .title( "Title" )
      .genre( "Genre" )
      .year( 2010 )
      .author( oldAuthor )

      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    UpdateBookDto dto = new UpdateBookDto();
    dto.setAuthorId( newAuthorId );

    AuthorDto newAuthorDto = new AuthorDto(
      newAuthorId,
      "Jane",
      "Smith",
      25
    );

    when( bookRepository.findById( id ) ).thenReturn( Optional.of( existingBook ) );
    when( authorService.getFullById( newAuthorId ) ).thenReturn( newAuthor );
    when( bookRepository.save( any( Book.class ) ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    when( authorService.toAuthorDto( newAuthor ) ).thenReturn( newAuthorDto );

    // Act
    BookResponseDto updated = bookService.update(
      id,
      dto
    );

    // Assert
    assertEquals(
      newAuthorId,
      updated.getAuthor().getId()
    );
    assertEquals(
      "Jane",
      updated.getAuthor().getFirstName()
    );

    verify( bookRepository ).findById( id );
    verify( authorService ).getFullById( newAuthorId );
    verify( bookRepository ).save( existingBook );
    verify( authorService ).toAuthorDto( newAuthor );
  }

  @Test
  public void testUpdate_NotFound () {
    // Arrange
    Long id = 1L;
    UpdateBookDto dto = new UpdateBookDto();
    dto.setTitle( "Updated" );

    when( bookRepository.findById( id ) ).thenReturn( Optional.empty() );

    // Act & Assert
    assertThrows(
      ResponseStatusException.class,
      () -> bookService.update(
        id,
        dto
      )
    );

    verify( bookRepository ).findById( id );
    verify(
      bookRepository,
      never()
    ).save( any() );
  }

  @Test
  public void testUpdate_DataIntegrityViolation () {
    // Arrange
    Long id = 1L;
    Book existingBook = Book
      .builder()
      .id( id )
      .title( "Title" )
      .genre( "Genre" )
      .year( 2010 )

      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    UpdateBookDto dto = new UpdateBookDto();
    dto.setTitle( "New Title" );

    when( bookRepository.findById( id ) ).thenReturn( Optional.of( existingBook ) );
    when( bookRepository.save( any( Book.class ) ) ).thenThrow( new DataIntegrityViolationException( "" ) );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> bookService.update(
        id,
        dto
      )
    );

    assertEquals(
      400,
      ex.getStatusCode().value()
    );
    assertEquals(
      "Failed to update book",
      ex.getReason()
    );
  }

  @Test
  public void testDelete_Success () {
    // Arrange
    Long id = 1L;
    Book book = Book
      .builder()
      .id( id )
      .title( "Delete Me" )
      .genre( "Genre" )
      .year( 2020 )

      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    when( bookRepository.findById( id ) ).thenReturn( Optional.of( book ) );
    doNothing().when( bookRepository ).delete( book );

    // Act & Assert
    assertDoesNotThrow( () -> bookService.delete( id ) );

    verify( bookRepository ).findById( id );
    verify( bookRepository ).delete( book );
  }

  @Test
  public void testDelete_NotFound () {
    // Arrange
    Long id = 1L;

    when( bookRepository.findById( id ) ).thenReturn( Optional.empty() );

    // Act & Assert
    assertThrows(
      ResponseStatusException.class,
      () -> bookService.delete( id )
    );

    verify( bookRepository ).findById( id );
    verify(
      bookRepository,
      never()
    ).delete( any() );
  }

  @Test
  public void testDelete_Exception () {
    // Arrange
    Long id = 1L;
    Book book = Book
      .builder()
      .id( id )
      .title( "Delete Me" )
      .genre( "Genre" )
      .year( 2020 )

      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    when( bookRepository.findById( id ) ).thenReturn( Optional.of( book ) );
    doThrow( new RuntimeException( "Database error" ) )
      .when( bookRepository )
      .delete( book );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> bookService.delete( id )
    );

    assertEquals(
      400,
      ex.getStatusCode().value()
    );
    assertEquals(
      "Failed to delete book",
      ex.getReason()
    );
  }

  @Test
  public void testUpdateStatus_Success () {
    // Arrange
    Long id = 1L;
    Book book = Book
      .builder()
      .id( id )
      .title( "Book Title" )
      .genre( "Genre" )
      .year( 2020 )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    when( bookRepository.findById( id ) ).thenReturn( Optional.of( book ) );
    when( bookRepository.save( any( Book.class ) ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );

    // Act
    assertDoesNotThrow( () -> bookService.updateStatus(
      id,
      null
    ) );

    // Assert
    verify( bookRepository ).findById( id );
    verify( bookRepository ).save( book );
    assertEquals(
      null,
      book.getStatus()
    );
  }

  @Test
  public void testUpdateStatus_NotFound () {
    // Arrange
    Long id = 1L;

    when( bookRepository.findById( id ) ).thenReturn( Optional.empty() );

    // Act & Assert
    assertThrows(
      ResponseStatusException.class,
      () -> bookService.updateStatus(
        id,
        null
      )
    );

    verify( bookRepository ).findById( id );
    verify(
      bookRepository,
      never()
    ).save( any() );
  }

  @Test
  public void testUpdateStatus_DataIntegrityViolation () {
    // Arrange
    Long id = 1L;
    Book book = Book
      .builder()
      .id( id )
      .title( "Book Title" )
      .genre( "Genre" )
      .year( 2020 )
      .createdAt( Instant.now() )
      .updatedAt( Instant.now() )
      .build();

    when( bookRepository.findById( id ) ).thenReturn( Optional.of( book ) );
    when( bookRepository.save( any( Book.class ) ) ).thenThrow( new DataIntegrityViolationException( "" ) );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> bookService.updateStatus(
        id,
        null
      )
    );

    assertEquals(
      400,
      ex.getStatusCode().value()
    );
    assertEquals(
      "Failed to update book status",
      ex.getReason()
    );
  }

  @Test
  public void testCheckBookExists_Success () {
    // Arrange
    Long id = 1L;

    when( bookRepository.existsById( id ) ).thenReturn( true );

    // Act & Assert
    assertDoesNotThrow( () -> bookService.checkBookExists( id ) );

    verify( bookRepository ).existsById( id );
  }

  @Test
  public void testCheckBookExists_NotFound () {
    // Arrange
    Long id = 1L;

    when( bookRepository.existsById( id ) ).thenReturn( false );

    // Act & Assert
    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> bookService.checkBookExists( id )
    );

    assertEquals(
      404,
      ex.getStatusCode().value()
    );
    assertTrue( ex.getReason().contains( "Book not found with id: " + id ) );

    verify( bookRepository ).existsById( id );
  }
}