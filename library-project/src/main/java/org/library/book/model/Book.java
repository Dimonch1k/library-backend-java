package org.library.book.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.library.author.model.Author;
import org.library.order.enums.OrderStatus;

import java.time.Instant;

@Entity
@Table( name = "BOOK" )
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
  @Id
  @GeneratedValue( strategy = GenerationType.IDENTITY )
  private Long id;

  @CreationTimestamp
  @Column( name = "CREATED_AT", nullable = false, updatable = false )
  private Instant createdAt;

  @UpdateTimestamp
  @Column( name = "UPDATED_AT", nullable = false )
  private Instant updatedAt;

  @Column( name = "TITLE", nullable = false, unique = true )
  private String title;

  @Column( name = "DESCRIPTION" )
  private String description;

  @Column( name = "GENRE", nullable = false )
  private String genre;

  @Column( name = "YEAR", nullable = false )
  private int year;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false, columnDefinition = "BOOK_STATUS")
  private OrderStatus status;

  @ManyToOne( fetch = FetchType.EAGER )
  @JoinColumn( name = "AUTHOR_ID" )
  private Author author;
}
