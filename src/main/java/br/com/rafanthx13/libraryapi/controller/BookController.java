package br.com.rafanthx13.libraryapi.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import br.com.rafanthx13.libraryapi.data.dto.BookDTO;
import br.com.rafanthx13.libraryapi.data.entity.Book;
import br.com.rafanthx13.libraryapi.service.BookService;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

// Customizar Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController // Controlador Rest
@RequiredArgsConstructor // Injeta objtos nos atributos privados. O obejto tem que ser 'FINAL'
@RequestMapping("/api/books") // Base URL
@Api("Book API") // Swagger
@Slf4j // Fornece 'log' para registrar logs da API
public class BookController {

  private final BookService service;
  private final ModelMapper modelMapper;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation("Create a book") // Customizar Swagger
  // '@Valid': Vai validara o DTO de acordo com as coisas de validation que estiverem no DTO
  public BookDTO create(@RequestBody @Valid BookDTO dto){
    log.info(" creating a book for isbn: {} ", dto.getIsbn());
    // converto dto em uma entity Book
    Book entity = modelMapper.map(dto, Book.class);
    entity = service.save(entity);
    // Ao voltar, converto de uma entity apra dtp
    return modelMapper.map(entity, BookDTO.class);
  }

  @GetMapping("{id}")
  @ApiOperation("Get a book details by id")
  public BookDTO get( @PathVariable Long id ){
      log.info(" obtaining details for book id: {} ", id);
      // o map mapeia o resultado, e usamos 'orElseTrhow'
      // para o caso que der erro (no caso, nâo encontrou o livro pois foi vazio)
      // ResponseStatusException: já está disponibilizada no Spring Boot, com status 404
      return service
              .getById(id)
              .map( book -> modelMapper.map(book, BookDTO.class)  )
              .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiOperation("Deletes a book by id")
  // Swagger: customizando o motivo de uma resposta. Muda a 'Reason' no Swagger
  @ApiResponses({
    @ApiResponse(code = 204, message = "Book succesfully deleted")
  })
  public void delete(@PathVariable Long id){
      log.info(" deleting book of id: {} ", id);
      Book book = service
                  .getById(id)
                  .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
      service.delete(book);
  }
  /* put :: /api/books/{id}
    Resgata o livro pelo ID; Insere os novos dados nele; manda para o service atualizar;
    A forma que ficar é como será retornado; Caso der algum erro nesse processo, manda Exception
  */
  @PutMapping("{id}")
  @ApiOperation("Updates a book")
  public BookDTO update( @PathVariable Long id, @RequestBody @Valid BookDTO dto){
      log.info(" updating book of id: {} ", id);
      return service.getById(id).map( book -> {
          book.setAuthor(dto.getAuthor());
          book.setTitle(dto.getTitle());
          book = service.update(book); // update na base
          return modelMapper.map(book, BookDTO.class); // volta DTO:JSON
      }).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
  }

  // GET PAGINADO E FILTRADO

  @GetMapping // get pra raiz. Quando passar por parametros
  /* vai pegar: ?title=%s&author=%s&page=0&size=100 e encaixar em BookDTO e Pageable
      title/author para BooktDTO e page/size para Pageable
  */
  @ApiOperation("Lists books by params")
  public Page<BookDTO> find( BookDTO dto, Pageable pageRequest ){
      Book filter = modelMapper.map(dto, Book.class); // converto para Book
      Page<Book> result = service.find(filter, pageRequest);
      List<BookDTO> list = result.getContent()
              .stream() // serve pra agente fazer operaçôes sobre coleções
              .map(entity -> modelMapper.map(entity, BookDTO.class)) // pra cada elemnto nessa coleçao vai mapear em um DTO
              .collect(Collectors.toList()); // No final vamos ter uma lsita de BookDDTO
      return new PageImpl<BookDTO>( list, pageRequest, result.getTotalElements() ); // conteudo, paginal atual, quantidade tottal de elementos
  }


  
}