package br.com.rafanthx13.libraryapi.service.impl;

// import java.util.Optional;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

// import java.util.Collections;
// import java.util.List;
import java.util.Optional;

import br.com.rafanthx13.libraryapi.data.entity.Book;
import br.com.rafanthx13.libraryapi.data.repository.BookRepository;
import br.com.rafanthx13.libraryapi.exception.BusinessException;
// import br.com.rafanthx13.libraryapi.exception.BusinessException;
import br.com.rafanthx13.libraryapi.service.BookService;

@Service
public class BookServiceImpl implements BookService {
  
  private BookRepository repository;

  public BookServiceImpl(BookRepository repository) {
    this.repository = repository;
  }

  @Override // Indica que sobrescreveu metodo da interface
  public Book save(Book book) {
      if( repository.existsByIsbn(book.getIsbn()) ){
          throw new BusinessException("Isbn já cadastrado.");
      }
      return repository.save(book);
  }

  @Override
  public void delete(Book book) {
      if(book == null || book.getId() == null){
          throw new IllegalArgumentException("Book id cant be null.");
      }
      this.repository.delete(book);
  }

  // get simples sem paginaçõa
  @Override
  public Optional<Book> getById(Long id){
    return this.repository.findById(id);
  }
  

    @Override
    public Page<Book> find( Book filter, Pageable pageRequest ) {
        // VOu criar um Example de acordo com o filter, que será o critério apra buscar os livros filtrados
        Example<Book> example = Example.of(filter,
                ExampleMatcher // vai permitir fazer as configuraçôes
                        .matching()
                        .withIgnoreCase() // para as cosia do tipo string, encaixar tanto apra maisuculo quanto minusculo
                        .withIgnoreNullValues() // se tiver passado algo null, vai ignorar
                        .withStringMatcher( ExampleMatcher.StringMatcher.CONTAINING ) 
                        // quando for comprar uma string, vai comparar com o seuginte critério: bastar ter um pedaço da palavra
        ) ;
        return repository.findAll(example, pageRequest);
    }

  @Override
  public Optional<Book> getBookByIsbn(String isbn) {
      return repository.findByIsbn(isbn); 
      // Query Method. Nâo precisa definir em Repository pois essa Notaçâo é própria da JPA
  }

  @Override
  public Book update(Book book) {
      if(book == null || book.getId() == null){
          throw new IllegalArgumentException("Book id cant be null.");
      }
      return this.repository.save(book); // é o meso que o de criar
  }

}