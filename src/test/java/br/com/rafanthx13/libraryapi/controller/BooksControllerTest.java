package br.com.rafanthx13.libraryapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import br.com.rafanthx13.libraryapi.data.dto.BookDTO;
import br.com.rafanthx13.libraryapi.data.entity.Book;
import br.com.rafanthx13.libraryapi.service.BookService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @RunWith(SpringRunner.class) // JUnit 4
@ExtendWith(SpringExtension.class) // Para JUnit5
@ActiveProfiles("test") // que tipo de contexto de test estamos faendo
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc // configurar para fazer as requisições
public class BooksControllerTest {

  static String BOOK_API = "/api/books";

  @Autowired // gera e injeta o objeto automaticamente
  MockMvc mvc;

  @MockBean // Mock Especializado do Spring
  BookService service;

  @Test
  @DisplayName("Deve criar um livro com sucesso")
  public void createBookTest() throws Exception{

    // Representa o JSON a ser enviado, um dto
    BookDTO dto = createNewBook();

    // Representa o objeto Mockado que vai ser retornardo quando o 'service' executar 'save'
    // Ou seja um retonro par aum método que será posteriormente mockado
    Book savedBook = Book.builder().id(10l).author("Artur").title("As aventuras").isbn("001").build();

    // Mockando Serviço: simulando o retorno de dados do BD (savedBook)
    // Estou simulando o método Save do Controller de Book. Quando executado vai retornar SavedBook
    BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

    // ObjectMapper recebe um objeto e tranforma em JSON. Estou preparando uma reqsuição real
    String json = new ObjectMapper().writeValueAsString(dto);

    // Montando Requisição HTTP: post em 'BOOK_API' aceitando JSON
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
      .post(BOOK_API)
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .content(json)
    ;

    // Importar métodos diretos assim fica mais legivel o 'status' e 'jsonPath'
    //    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath/status;
    // Executo esse request e VERIFICO o que voltou: status e o conteudo do json de retorno
    mvc
      .perform(request) // executo chamada HTTP
      .andExpect( status().isCreated() )
      .andExpect( jsonPath("id").value(10l) )
      .andExpect( jsonPath("title").value(dto.getTitle()) )
      .andExpect( jsonPath("author").value(dto.getAuthor()) )
      .andExpect( jsonPath("isbn").value(dto.getIsbn()) )
    ;
  }

  // Insiro um livro e depois eu obtenho suas informações
  @Test
  @DisplayName("Deve obter informacoes de um livro.")
  public void getBookDetailsTest() throws Exception{
      // cenario (given):
      Long id = 1l;

      Book book = Book.builder()
                  .id(id)
                  .title(createNewBook().getTitle())
                  .author(createNewBook().getAuthor())
                  .isbn(createNewBook().getIsbn())
                  .build();
      // Mock de funcionaldiade: quando 'BookController' executar .getByID(), vai retornar isso
      BDDMockito.given( service.getById(id) ).willReturn(Optional.of(book));

      // execucao (when): executo um get buscando por um ID, buscando JSON
      MockHttpServletRequestBuilder request = MockMvcRequestBuilders
              .get(BOOK_API.concat("/" + id))
              .accept(MediaType.APPLICATION_JSON);

      // verificação (then):
      mvc
          .perform(request)
          .andExpect(status().isOk())
          .andExpect( jsonPath("id").value(id) )
          .andExpect( jsonPath("title").value(createNewBook().getTitle()) )
          .andExpect( jsonPath("author").value(createNewBook().getAuthor()) )
          .andExpect( jsonPath("isbn").value(createNewBook().getIsbn()) )
      ;
  }

  @Test
  @DisplayName("Deve retornar resource not found quando o livro procurado não existir")
  public void bookNotFoundTest() throws Exception {

      // Quando ele buscar pelo ID, para qualquer ID, vai retornar VAZIO, pois queremos ver o caso em que retorna nada e asism dá erro
      BDDMockito.given( service.getById(Mockito.anyLong()) ).willReturn( Optional.empty() );

      MockHttpServletRequestBuilder request = MockMvcRequestBuilders
              .get(BOOK_API.concat("/" + 1))
              .accept(MediaType.APPLICATION_JSON);

      mvc
          .perform(request)
          .andExpect(status().isNotFound());
  }



  @Test
  @DisplayName("Deve deletar um livro")
  public void deleteBookTest() throws Exception {
      // ao fazer o getByID do service, vai retornar True como se tive-se o livro lá
      BDDMockito.given( service.getById(anyLong()) )
                .willReturn( Optional.of(Book.builder().id(1l).build()) );

      MockHttpServletRequestBuilder request = MockMvcRequestBuilders
              .delete(BOOK_API.concat("/" + 1));

      // verificao: status 203: no contetn pode deletou corretamennte mas nao volta nada
      mvc.perform( request )
          .andExpect( status().isNoContent() );
  }

  @Test
  @DisplayName("Deve retornar resource not found quando não encontrar o livro para deletar")
  public void deleteInexistentBookTest() throws Exception {

      BDDMockito.given( service.getById(anyLong()) ).willReturn(Optional.empty());

      MockHttpServletRequestBuilder request = MockMvcRequestBuilders
              .delete(BOOK_API.concat("/" + 1));

      mvc.perform( request )
              .andExpect( status().isNotFound() );
  }

  // test update

  @Test
  @DisplayName("Deve atualizar um livro")
  public void updateBookTest() throws Exception {
      Long id = 1l;
      String json = new ObjectMapper().writeValueAsString(createNewBook());

      // O livro como era antes de atualizar
      Book updatingBook = Book.builder().id(1l).title("some title").author("some author").isbn("321").build();
      BDDMockito.given( service.getById(id) )
                .willReturn( Optional.of(updatingBook) );

      // O livro depois de atualizar
      Book updatedBook = Book.builder().id(id).author("Artur").title("As aventuras").isbn("321").build();
      BDDMockito.given( service.update(updatingBook) )
                .willReturn(updatedBook);

      // request : put in /api/books/{id} com BODY
      MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);


      mvc.perform( request )
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id) )
                .andExpect( jsonPath("title").value(createNewBook().getTitle()) )
                .andExpect( jsonPath("author").value(createNewBook().getAuthor()) )
                .andExpect( jsonPath("isbn").value("321") );
  }

  @Test
  @DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente")
  public void updateInexistentBookTest() throws Exception {

      String json = new ObjectMapper().writeValueAsString(createNewBook());
      BDDMockito.given( service.getById(Mockito.anyLong()) )
              .willReturn( Optional.empty() );

      MockHttpServletRequestBuilder request = MockMvcRequestBuilders
              .put(BOOK_API.concat("/" + 1))
              .content(json)
              .accept(MediaType.APPLICATION_JSON)
              .contentType(MediaType.APPLICATION_JSON);

      mvc.perform( request )
              .andExpect( status().isNotFound() );
  }

  // get FIltred e Paginada

  @Test
  @DisplayName("Deve filtrar livros")
  public void findBooksTest() throws Exception{

      Long id = 1l;

      Book book = Book.builder()
                  .id(id)
                  .title(createNewBook().getTitle())
                  .author(createNewBook().getAuthor())
                  .isbn(createNewBook().getIsbn())
                  .build();

      // Para a busca filtrada e paginada, eu passo para oservice.fin, um livro e um Pageable
      // Pagealbe serve para buscas paginadas: qual numero da pagina e quantos registros trazer
      // Nao importa o que agente vai mandar, pois está mockado
      // O retorno também será paginado PageImpl já é do spring
      // PageRequest são as informações da pagianiçao: page:0, 100 elemetnos, total de parametros para busca: 1
      BDDMockito.given( service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)) )
              .willReturn( new PageImpl<Book>( Arrays.asList(book), PageRequest.of(0,100), 1 )   );

      String queryString = String.format("?title=%s&author=%s&page=0&size=100",
              book.getTitle(), book.getAuthor());

      MockHttpServletRequestBuilder request = MockMvcRequestBuilders
              .get(BOOK_API.concat(queryString))
              .accept(MediaType.APPLICATION_JSON);

      // Quando retornanmos um page, há algumas propriedades
      mvc
          .perform( request )
          .andExpect( status().isOk() ) // 
          .andExpect( jsonPath("content", Matchers.hasSize(1))) // so to retornando uma lista com um livro
          .andExpect( jsonPath("totalElements").value(1) ) // so espero 1 elemento no total
          .andExpect( jsonPath("pageable.pageSize").value(100) ) // trazer até 100 elemenos
          .andExpect( jsonPath("pageable.pageNumber").value(0)) // Numero da página
          ;
  }




  private BookDTO createNewBook() {
    return BookDTO.builder().author("Artur").title("As aventuras").isbn("001").build();
  }

  
}