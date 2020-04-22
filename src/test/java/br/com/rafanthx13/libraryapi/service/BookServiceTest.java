package br.com.rafanthx13.libraryapi.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.rafanthx13.libraryapi.data.entity.Book;
import br.com.rafanthx13.libraryapi.data.repository.BookRepository;
import br.com.rafanthx13.libraryapi.exception.BusinessException;
import br.com.rafanthx13.libraryapi.service.impl.BookServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThrows;
// import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")

public class BookServiceTest {

  BookService service;

  @MockBean
  BookRepository repository;

  @BeforeEach
  public void setUp(){
    this.service = new BookServiceImpl(repository);
  }

  @Test
  @DisplayName("Deve salvar um livro")
  public void saveBookTest() {
      //cenario
      Book book = createValidBook(); // Instnacia valida
      // vai dar falsa a existencia de ISBN, se for true, nao vai progredir (estamos respoetiando a regra de negocio)
      Mockito.when(repository.existsByIsbn(Mockito.anyString()) ).thenReturn(false);
      // Mock do salvamento de livro
      Mockito.when(repository.save(book)).thenReturn(
              Book.builder().id(1l)
                      .isbn("123")
                      .author("Fulano")
                      .title("As aventuras").build()
      );

      //execucao
      Book savedBook = service.save(book);

      //verificacao
      assertThat(savedBook.getId()).isNotNull();
      assertThat(savedBook.getIsbn()).isEqualTo("123");
      assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
      assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
  }

  @Test
  @DisplayName("Deve lançar erro de negocio ao tentar salvar um livro com isbn duplicado")
  public void shouldNotSaveABookWithDuplicatedISBN(){

      // cenario
      // Faço um Mock dizendo que ao verificar o ISBN retornara como true nesse testse
      Book book = createValidBook();
      Mockito.when( repository.existsByIsbn(Mockito.anyString()) ).thenReturn(true);

      // execucao
      // Espero que ao fazer o save de isbn duplicado, dê erro e solte uma exceção
      Throwable exception = Assertions.catchThrowable(() -> service.save(book));

      // verificacoes
      assertThat(exception)
              .isInstanceOf(BusinessException.class)
              .hasMessage("Isbn já cadastrado.");

      // Testo que o método repository.save não foi chamado. 
      // Nâo deve ser chamado pois existiByIsbn bloqueia pois é true e nâo proseggue
      Mockito.verify(repository, Mockito.never()).save(book);

  }

  // getByID

  @Test
  @DisplayName("Deve obter um livro por Id")
  public void getByIdTest(){
      // Crio o livro
      Long id = 1l;
      Book book = createValidBook();
      book.setId(id);
      // Mock: quando chmar o findByID vai me retornar esse book
      Mockito.when( repository.findById(id) ).thenReturn( Optional.of(book) );

      //execucao
      Optional<Book> foundBook = service.getById(id);

      //verificacoes
      assertThat( foundBook.isPresent() ).isTrue();
      assertThat( foundBook.get().getId()).isEqualTo(id);
      assertThat( foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
      assertThat( foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
      assertThat( foundBook.get().getTitle()).isEqualTo(book.getTitle());
  }

  @Test
  @DisplayName("Deve retornar vazio ao obter um livro por Id quando ele não existe na base.")
  public void bookNotFoundByIdTest(){
      Long id = 1l;
      when( repository.findById(id) ).thenReturn(Optional.empty());

      //execucao
      Optional<Book> book = service.getById(id);

      //verificacoes
      // isPressent é um metodo da classe Optional: "É uma classe que permite que a variável seja nula e verificável"
      //  Dessa forma, podemos consultar com '.isPresent' senao devirmaos verificar tudo como null
      assertThat( book.isPresent() ).isFalse();

  }

  // DELETE

  @Test
  @DisplayName("Deve deletar um livro.")
  public void deleteBookTest(){
      Book book = Book.builder().id(1l).build();

      //execucao
      org.junit.jupiter.api.Assertions.assertDoesNotThrow( () -> service.delete(book) );

      //verificacoes
      Mockito.verify(repository, Mockito.times(1)).delete(book);
  }

  @Test
  @DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente.")
  public void deleteInvalidBookTest(){
      Book book = new Book();

      org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

      Mockito.verify( repository, Mockito.never() ).delete(book);
  }

  // UPDATE

  @Test
  @DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente.")
  public void updateInvalidBookTest(){
      Book book = new Book();
      // Como nâo inseriu o livro antes, ao buscar vai nao vai achar, entao espera-se que dê erro
      org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

      Mockito.verify( repository, Mockito.never() ).save(book);
  }

  @Test
  @DisplayName("Deve atualizar um livro.")
  public void updateBookTest(){
      //cenário
      long id = 1l;

      //livro a atualizar
      Book updatingBook = Book.builder().id(id).build();

      //simulacao
      Book updatedBook = createValidBook();
      updatedBook.setId(id);
      when(repository.save(updatingBook)).thenReturn(updatedBook);

      //exeucao
      Book book = service.update(updatingBook);

      //verificacoes
      assertThat(book.getId()).isEqualTo(updatedBook.getId());
      assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
      assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
      assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());

  }

  // GET FILTED

  @Test
  @DisplayName("Deve filtrar livros pelas propriedades")
  public void findBookTest(){
      //cenario
      Book book = createValidBook();

      PageRequest pageRequest = PageRequest.of(0, 10);

      List<Book> lista = Arrays.asList(book);
      Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);
      when( repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
              .thenReturn(page);

      //execucao
      Page<Book> result = service.find(book, pageRequest);


      //verificacoes
      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent()).isEqualTo(lista);
      assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
      assertThat(result.getPageable().getPageSize()).isEqualTo(10);
  }

  // GET Book by ISBN

  @Test
  @DisplayName("deve obter um livro pelo isbn")
  public void getBookByIsbnTest(){
      String isbn = "1230";
      when( repository.findByIsbn(isbn) )
            .thenReturn( Optional.of(Book.builder().id(1l).isbn(isbn).build()) );

      Optional<Book> book = service.getBookByIsbn(isbn);

      assertThat(book.isPresent()).isTrue();
      assertThat(book.get().getId()).isEqualTo(1l);
      assertThat(book.get().getIsbn()).isEqualTo(isbn);

      Mockito.verify(repository, Mockito.times(1)).findByIsbn(isbn); // verifica se chamaou uma vez esse método
  }

  // metodo privado

  private Book createValidBook() {
      return Book.builder().isbn("123").author("Fulano").title("As aventuras").build();
  }

}