package org.library.order.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.library.book.model.Book;
import org.library.user.model.User;
import org.library.order.enums.OrderStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table( name = "\"order\"" )
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
  @Id
  private UUID id;

  @CreationTimestamp
  @Column( name = "created_at", nullable = false, updatable = false )
  private Instant createdAt;

  @UpdateTimestamp
  @Column( name = "updated_at", nullable = false )
  private Instant updatedAt;

  private String name;

  @Column( name = "borrow_date", nullable = false )
  private Instant borrowDate;

  @Column( name = "return_date" )
  private Instant returnDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, columnDefinition = "order_status")
  private OrderStatus status;

  @ManyToOne
  @JoinColumn( name = "user_id" )
  private User user;

  @ManyToOne
  @JoinColumn( name = "book_id" )
  private Book book;
}
