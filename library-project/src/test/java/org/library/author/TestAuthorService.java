package org.library.author;

import org.junit.jupiter.api.*;
import org.library.author.dto.AuthorResponseDto;
import org.library.author.dto.CreateAuthorDto;
import org.library.author.dto.UpdateAuthorDto;
import org.library.author.model.Author;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestAuthorService {

  private AuthorService authorService;

  @Mock
  private AuthorRepository authorRepository;

  private AutoCloseable autoCloseable;

  @BeforeEach
  void setUp () {
    autoCloseable = MockitoAnnotations.openMocks( this );
    authorService = new AuthorService( authorRepository );
  }

  @AfterEach
  void tearDown () throws Exception {
    autoCloseable.close();
  }

  @Test
  public void testCreate_Success () {
    CreateAuthorDto createDto = new CreateAuthorDto();
    createDto.setFirstName( "Dmytro" );
    createDto.setLastName( "Leskiv" );
    createDto.setAge( 25 );

    when( authorRepository.findByFirstNameAndLastName(
      "Dmytro",
      "Leskiv"
    ) ).thenReturn( Optional.empty() );

    when( authorRepository.save( any( Author.class ) ) ).thenAnswer( invocation -> {
      Author a = invocation.getArgument( 0 );
      a.setId( 1L );
      return a;
    } );

    AuthorResponseDto created = authorService.create( createDto );

    assertNotNull( created.getId() );
    assertEquals(
      "Dmytro",
      created.getFirstName()
    );
    assertEquals(
      "Leskiv",
      created.getLastName()
    );
    assertEquals(
      25,
      created.getAge()
    );
  }

  @Test
  public void testCreate_Duplicate_ThrowsConflict () {
    CreateAuthorDto dto = new CreateAuthorDto();
    dto.setFirstName( "John" );
    dto.setLastName( "Doe" );
    dto.setAge( 30 );

    when( authorRepository.findByFirstNameAndLastName(
      "John",
      "Doe"
    ) ).thenReturn( Optional.of( new Author() ) );

    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> authorService.create( dto )
    );

    assertEquals(
      409,
      ex.getStatusCode().value()
    );
    assertTrue( ex.getReason().contains( "already exists" ) );
  }

  @Test
  public void testGetAll_ReturnsList () {
    List<Author> authors = List.of(
      buildAuthor(
        1L,
        "A",
        "B",
        20
      ),
      buildAuthor(
        2L,
        "C",
        "D",
        30
      )
    );

    when( authorRepository.findAll() ).thenReturn( authors );

    List<AuthorResponseDto> result = authorService.getAll();
    assertEquals(
      2,
      result.size()
    );
  }

  @Test
  public void testGetById_Success () {
    Long id = new Random().nextLong();
    Author author = buildAuthor(
      id,
      "A",
      "B",
      40
    );

    when( authorRepository.findById( id ) ).thenReturn( Optional.of( author ) );

    AuthorResponseDto result = authorService.getById( id );

    assertEquals(
      "A",
      result.getFirstName()
    );
    assertEquals(
      "B",
      result.getLastName()
    );
  }

  @Test
  public void testGetById_NotFound () {
    Long id = new Random().nextLong();

    when( authorRepository.findById( id ) ).thenReturn( Optional.empty() );

    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> authorService.getById( id )
    );

    assertEquals(
      404,
      ex.getStatusCode().value()
    );
    assertTrue( ex.getReason().contains( "Author not found" ) );
  }

  @Test
  public void testUpdate_Success () {
    Long id = new Random().nextLong();
    Author author = buildAuthor(
      id,
      "Old",
      "Name",
      30
    );

    UpdateAuthorDto dto = new UpdateAuthorDto();
    dto.setFirstName( "New" );
    dto.setAge( 35 );

    when( authorRepository.findById( id ) ).thenReturn( Optional.of( author ) );
    when( authorRepository.save( any( Author.class ) ) ).thenAnswer( i -> i.getArgument( 0 ) );

    AuthorResponseDto updated = authorService.update(
      id,
      dto
    );

    assertEquals(
      "New",
      updated.getFirstName()
    );
    assertEquals(
      "Name",
      updated.getLastName()
    ); // unchanged
    assertEquals(
      35,
      updated.getAge()
    );
  }

  @Test
  public void testUpdate_NotFound () {
    Long id = new Random().nextLong();
    UpdateAuthorDto dto = new UpdateAuthorDto();
    dto.setFirstName( "Test" );

    when( authorRepository.findById( id ) ).thenReturn( Optional.empty() );

    assertThrows(
      ResponseStatusException.class,
      () -> authorService.update(
        id,
        dto
      )
    );
  }

  @Test
  public void testDelete_Success () {
    Long id = new Random().nextLong();
    Author author = buildAuthor(
      id,
      "A",
      "B",
      20
    );

    when( authorRepository.findById( id ) ).thenReturn( Optional.of( author ) );
    doNothing().when( authorRepository ).delete( author );

    assertDoesNotThrow( () -> authorService.delete( id ) );
    verify(
      authorRepository,
      times( 1 )
    ).delete( author );
  }

  @Test
  public void testDelete_NotFound () {
    Long id = new Random().nextLong();

    when( authorRepository.findById( id ) ).thenReturn( Optional.empty() );

    assertThrows(
      ResponseStatusException.class,
      () -> authorService.delete( id )
    );
  }

  @Test
  public void testCreate_DataIntegrityViolation () {
    CreateAuthorDto dto = new CreateAuthorDto();
    dto.setFirstName( "A" );
    dto.setLastName( "B" );
    dto.setAge( 45 );

    when( authorRepository.findByFirstNameAndLastName(
      any(),
      any()
    ) ).thenReturn( Optional.empty() );

    when( authorRepository.save( any() ) ).thenThrow( new DataIntegrityViolationException( "" ) );

    ResponseStatusException ex = assertThrows(
      ResponseStatusException.class,
      () -> authorService.create( dto )
    );

    assertEquals(
      400,
      ex.getStatusCode().value()
    );
    assertEquals(
      "Failed to create author",
      ex.getReason()
    );
  }

  private Author buildAuthor (
    Long id, String firstName, String lastName, int age
  ) {
    return Author
      .builder()
      .id( id )
      .firstName( firstName )
      .lastName( lastName )
      .age( age )
      .build();
  }
}
