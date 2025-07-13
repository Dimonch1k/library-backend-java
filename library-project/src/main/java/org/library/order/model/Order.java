package org.library.order.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.library.book.model.Book;
import org.library.user.model.User;
import org.library.order.enums.OrderStatus;

import java.time.Instant;

@Entity
@Table( name = "\"ORDER\"" )
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreationTimestamp
  @Column( name = "CREATED_AT", nullable = false, updatable = false )
  private Instant createdAt;

  @UpdateTimestamp
  @Column( name = "UPDATED_AT", nullable = false )
  private Instant updatedAt;

  private String name;

  @Column( name = "BORROW_DATE", nullable = false )
  private Instant borrowDate;

  @Column( name = "RETURN_DATE" )
  private Instant returnDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false, columnDefinition = "ORDER_STATUS")
  private OrderStatus status;

  @ManyToOne
  @JoinColumn(name = "USER_ID")
  private User user;

  @ManyToOne
  @JoinColumn(name = "BOOK_ID")
  private Book book;
}
