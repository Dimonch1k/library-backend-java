package org.library.book;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.library.book.dto.BookResponseDto;
import org.library.book.dto.CreateBookDto;
import org.library.book.dto.UpdateBookDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping( "/api/v1/book" )
@RequiredArgsConstructor
public class BookController {
  private final BookService bookService;

  @PostMapping
  public ResponseEntity<BookResponseDto> create ( @RequestBody @Valid CreateBookDto dto ) {
    return ResponseEntity.ok( bookService.create( dto ) );
  }

  @GetMapping
  public ResponseEntity<List<BookResponseDto>> getAll () {
    return ResponseEntity.ok( bookService.getAll() );
  }

  @GetMapping( "/{id}" )
  public ResponseEntity<BookResponseDto> getById ( @PathVariable Long id ) {
    return ResponseEntity.ok( bookService.getById( id ) );
  }

  @PatchMapping( "/{id}" )
  public ResponseEntity<BookResponseDto> update (
    @PathVariable Long id, @RequestBody @Valid UpdateBookDto dto
  ) {
    return ResponseEntity.ok( bookService.update(
      id,
      dto
    ) );
  }

  @DeleteMapping( "/{id}" )
  public ResponseEntity<Void> delete ( @PathVariable Long id ) {
    bookService.delete( id );
    return ResponseEntity.ok().build();
  }
}
