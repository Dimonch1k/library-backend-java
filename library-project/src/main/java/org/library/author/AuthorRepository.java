package org.library.author;

import org.library.author.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
  Optional<Author> findByFirstNameAndLastName (
    String firstName, String lastName
  );
}
