package br.com.rafanthx13.libraryapi.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.rafanthx13.libraryapi.data.entity.Book;

public interface BookService {

  Book save(Book any);

  Optional<Book> getById(Long id);

  void delete(Book book);

  Book update(Book book);

  Page<Book> find( Book filter, Pageable pageRequest );

  Optional<Book> getBookByIsbn(String isbn);
  
}