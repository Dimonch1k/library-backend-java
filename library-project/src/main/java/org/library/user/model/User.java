package org.library.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.library.auth.enums.Role;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table( name = "\"user\"" )
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  private UUID id;

  @CreationTimestamp
  @Column( name = "created_at", nullable = false, updatable = false )
  private Instant createdAt;

  @UpdateTimestamp
  @Column( name = "updated_at", nullable = false )
  private Instant updatedAt;

  @Column( nullable = false, unique = true )
  private String email;

  @Column( nullable = false )
  private String password;

  @Column( nullable = false )
  private Role role;
}
