package br.com.rafanthx13.libraryapi.controller;

import br.com.rafanthx13.libraryapi.data.dto.BookDTO;
import br.com.rafanthx13.libraryapi.data.dto.LoanDTO;
import br.com.rafanthx13.libraryapi.data.dto.LoanFilterDTO;
import br.com.rafanthx13.libraryapi.data.dto.ReturnedLoanDTO;
import br.com.rafanthx13.libraryapi.exception.BusinessException;
import br.com.rafanthx13.libraryapi.data.entity.Book;
import br.com.rafanthx13.libraryapi.data.entity.Loan;
import br.com.rafanthx13.libraryapi.service.BookService;
import br.com.rafanthx13.libraryapi.service.LoanService;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor // Com esas notaçâo: LoanService, BookService e ModelMapper já vao ser criados e injetados
// @Api("Book API") // Swagger
public class LoanController {

	private final LoanService service;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    // POST :: body

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto) {
    	// Mapear de LoanDTO para Loan. Para fazer isso, é preciso buscar no Banco o 'Book' de acordo com O ID passado
    	// em 'dto.getIsbn()'. Se nâo conseguir resgatar ou der algum erro, , joga uma exceção
        Book book = bookService
                .getBookByIsbn(dto.getIsbn())
                .orElseThrow(() ->
                		// No caso de nao achar o livro pelo ISBN
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));
        // Com o livro em mão, construa a entidade 'Loan'
        Loan entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();
        // Por fim, vou salvar esse Loan no banco e retorno o id do posts feito
        entity = service.save(entity);
        return entity.getId(); // Estamos retornando só o ID
    }

    // PATH

    // Busca pelo Loan e se achar, vai atualizar de acordo com o dto
    @PatchMapping("{id}") // O Body é um ReturnedLoanDTO
    public void returnBook( @PathVariable Long id, @RequestBody ReturnedLoanDTO dto) {
        Loan loan = service.getById(id)
        				   .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.getReturned());
        service.update(loan);
    }

    // GET FILTED

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest) {
    	// cada entity é um Loan que tem um Book dentro
    	// O que temos que fazer é converter das entidades para os DTO
        Page<Loan> result = service.find(dto, pageRequest); 
        List<LoanDTO> loans = result.getContent().stream()
                .map(entity -> { 
                    Book book = entity.getBook();
                    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(loans, pageRequest, result.getTotalElements());
    }

}