package org.library.author;

import lombok.RequiredArgsConstructor;
import org.library.author.model.Author;
import org.library.author.dto.CreateAuthorDto;
import org.library.author.dto.UpdateAuthorDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping( "/api/v1/author" )
@RequiredArgsConstructor
public class AuthorController
{
  private final AuthorService authorService;

  @PostMapping
  public ResponseEntity<Author> create(
    @RequestBody
    CreateAuthorDto dto )
  {
    return ResponseEntity.ok( authorService.create( dto ) );
  }

  @GetMapping
  public ResponseEntity<List<Author>> getAll() {
    return ResponseEntity.ok( authorService.getAll() );
  }

  @GetMapping( "/{id}" )
  public ResponseEntity<Author> getById(
    @PathVariable
    UUID id )
  {
    return ResponseEntity.ok( authorService.getById( id ) );
  }

  @PatchMapping( "/{id}" )
  public ResponseEntity<Author> update(
    @PathVariable
    UUID id,
    @RequestBody
    UpdateAuthorDto dto )
  {
    return ResponseEntity.ok( authorService.update(
      id,
      dto
    ) );
  }

  @DeleteMapping( "/{id}" )
  public ResponseEntity<Void> delete(
    @PathVariable
    UUID id )
  {
    authorService.delete( id );
    return ResponseEntity.ok().build();
  }
}
