package org.library.author;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.library.author.dto.AuthorResponseDto;
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
public class AuthorController {
  private final AuthorService authorService;

  @PostMapping
  public ResponseEntity<AuthorResponseDto> create ( @RequestBody @Valid CreateAuthorDto dto ) {
    return ResponseEntity.ok( authorService.create( dto ) );
  }

  @GetMapping
  public ResponseEntity<List<AuthorResponseDto>> getAll () {
    return ResponseEntity.ok( authorService.getAll() );
  }

  @GetMapping( "/{id}" )
  public ResponseEntity<AuthorResponseDto> getById ( @PathVariable UUID id ) {
    return ResponseEntity.ok( authorService.getById( id ) );
  }

  @PatchMapping( "/{id}" )
  public ResponseEntity<AuthorResponseDto> update (
    @PathVariable UUID id, @RequestBody @Valid UpdateAuthorDto dto
  ) {
    return ResponseEntity.ok( authorService.update(
      id,
      dto
    ) );
  }

  @DeleteMapping( "/{id}" )
  public ResponseEntity<Void> delete ( @PathVariable UUID id ) {
    authorService.delete( id );
    return ResponseEntity.ok().build();
  }
}
