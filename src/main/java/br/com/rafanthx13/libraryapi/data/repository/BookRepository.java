package br.com.rafanthx13.libraryapi.data.repository;

import br.com.rafanthx13.libraryapi.data.entity.Book;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// Extend JPARepository, e receb dois parametor, o tipo da entidade que está mapeanaod e o tipo do ID dessea entidade
public interface BookRepository extends JpaRepository<Book, Long> {

  /*
  QUERY METHODS: O JPA gera em tempo de compilaçâo o metodo que verifica a existencia por query method
  + Ou seja, sem implementaçâo, criamos o método para verificar se existe esse ISBN na tabela
  */

  boolean existsByIsbn(String isbn);

  Optional<Book> findByIsbn( String isbn);

  
}