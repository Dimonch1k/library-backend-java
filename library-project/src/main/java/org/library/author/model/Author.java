package org.library.author.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table( name = "author" )
public class Author
{
  @Id
  private UUID    id;
  @CreationTimestamp
  @Column( name = "created_at", nullable = false, updatable = false )
  private Instant createdAt;
  @UpdateTimestamp
  @Column( name = "updated_at", nullable = false )
  private Instant updatedAt;
  @Column( name = "first_name", nullable = false )
  private String  firstName;
  @Column( name = "last_name", nullable = false )
  private String  lastName;
  @Column( nullable = false )
  private int     age;
}
