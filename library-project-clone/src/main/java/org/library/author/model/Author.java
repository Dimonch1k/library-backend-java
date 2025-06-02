package org.library.author.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table( name = "author" )
public class Author
{
  @Id
  @GeneratedValue( strategy = GenerationType.UUID )
  private String id;

  @Column(name = "first_name", nullable = false )
  private String firstName;

  @Column(name = "last_name", nullable = false )
  private String lastName;

  @Column( nullable = false )
  private int    age;
}
