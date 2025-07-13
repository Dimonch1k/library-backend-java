package org.library.author.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.library.book.dto.BookResponseDto;
import org.library.book.model.Book;

import java.time.Instant;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table( name = "AUTHOR" )
public class Author {
  @Id
  @GeneratedValue( strategy = GenerationType.IDENTITY )
  private Long id;

  @CreationTimestamp
  @Column( name = "CREATED_AT", nullable = false, updatable = false )
  private Instant createdAt;

  @UpdateTimestamp
  @Column( name = "UPDATED_AT", nullable = false )
  private Instant updatedAt;

  @Column( name = "FIRST_NAME", nullable = false )
  private String firstName;

  @Column( name = "LAST_NAME", nullable = false )
  private String lastName;

  @Column( name = "AGE", nullable = false )
  private int age;

  @OneToMany( mappedBy = "author", fetch = FetchType.LAZY )
  private Set<Book> books;
}
