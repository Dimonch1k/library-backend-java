package org.library.book.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table( name = "book" )
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
  @Id
  private UUID id;

  @CreationTimestamp
  @Column( name = "created_at", nullable = false, updatable = false )
  private Instant createdAt;

  @UpdateTimestamp
  @Column( name = "updated_at", nullable = false )
  private Instant updatedAt;

  @Column( nullable = false, unique = true )
  private String title;

  @Column
  private String description;

  @Column( nullable = false )
  private String genre;

  @Column( nullable = false )
  private int year;

  @Column( name = "author_id" )
  private UUID authorId;
}
