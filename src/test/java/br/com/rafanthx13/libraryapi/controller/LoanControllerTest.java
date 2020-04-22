package br.com.rafanthx13.libraryapi.controller;

import br.com.rafanthx13.libraryapi.data.dto.LoanDTO;
import br.com.rafanthx13.libraryapi.data.dto.LoanFilterDTO;
import br.com.rafanthx13.libraryapi.data.dto.ReturnedLoanDTO;
import br.com.rafanthx13.libraryapi.exception.BusinessException;
import br.com.rafanthx13.libraryapi.data.entity.Book;
import br.com.rafanthx13.libraryapi.data.entity.Loan;
import br.com.rafanthx13.libraryapi.service.BookService;
import br.com.rafanthx13.libraryapi.service.LoanService;
import br.com.rafanthx13.libraryapi.service.LoanServiceTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static br.com.rafanthx13.libraryapi.controller.BooksControllerTest.BOOK_API;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // status()

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(controllers = LoanController.class) // Usar somente esse controller
public class LoanControllerTest {

	static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    // POST TESTS

    @Test
    @DisplayName("Deve realizar um emprestimo")
    public void createLoanTest() throws Exception {
    	// Criação do JSON de Loan
        LoanDTO dto = LoanDTO.builder().isbn("123").email("customer@email.com").customer("Fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);
        // Para fazer um empréstimo é necessário o livro já está na base
        // Famoz o Mock dizendo que já tem o livro lá, e vai retornar o 'book' abaixo. É um Optional pois pode nao existir
        Book book = Book.builder().id(1l).isbn("123").build();
        BDDMockito.given( bookService.getBookByIsbn("123") ).willReturn( Optional.of(book) );

        Loan loan = Loan.builder().id(1l).customer("Fulano").book(book).loanDate(LocalDate.now()).build();
        BDDMockito.given( loanService.save(Mockito.any(Loan.class)) ).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform( request )
                .andExpect( status().isCreated() )
                .andExpect( content().string("1") ) 
        		// espero que o retorno tenha o id do emprestimo fetio. 
        		// PERCEBA, NÂO É UM JSON, É SÓ UM VALOR RETORNADO
            ;

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro inexistente.")
    public void invalidIsbnCreateLoanTest() throws  Exception{

        LoanDTO dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given( bookService.getBookByIsbn("123") ).willReturn( Optional.empty() );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform( request )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", Matchers.hasSize(1)) ) // Uma única mensagem de erro
                .andExpect( jsonPath("errors[0]").value("Book not found for passed isbn")) 
        ;
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro já emprestado.")
    public void loanedBookErrorOnCreateLoanTest() throws  Exception{

        LoanDTO dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        // A diferença do anterior é que, o getBookByIsbn agora vai retonar o livro, e assim ao fazer o save vai dar erro
        Book book = Book.builder().id(1l).isbn("123").build();
        BDDMockito.given( bookService.getBookByIsbn("123") ).willReturn( Optional.of(book) );
        // Quando fizer o save,  vai dar exceção de que já está emprestado (isso é RN então usamos BusinessError)
        // Lembre-se: isso está mockado. Nâo depedene de LoanService implementar isso, oq eu deixa as cosias separadas
        BDDMockito.given( loanService.save(Mockito.any(Loan.class)) )
                .willThrow( new BusinessException("Book already loaned") );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform( request )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", Matchers.hasSize(1)) )
                .andExpect( jsonPath("errors[0]").value("Book already loaned"))
        ;
    }

    // PATH

    @Test
    @DisplayName("Deve devolver um livro")
    public void returnBookTest() throws Exception{
        //cenário { returned: true }
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        Loan loan = Loan.builder().id(1l).build();
        BDDMockito.given( loanService.getById(Mockito.anyLong()) )
                .willReturn(Optional.of(loan));

        String json = new ObjectMapper().writeValueAsString(dto);

        mvc.perform(
            patch(LOAN_API.concat("/1"))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
        ).andExpect( status().isOk() );

        Mockito.verify(loanService, Mockito.times(1)).update(loan);

    }

    @Test
    @DisplayName("Deve retornar error quando tentar devolver um livro inexistente.")
    public void returnInexistentBookTest() throws Exception{
        //cenário
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        mvc.perform(
                patch(LOAN_API.concat("/1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect( status().isNotFound() );

    }

    // GET FILTED

    @Test
    @DisplayName("Deve filtrar empréstimos")
    public void findLoansTest() throws Exception{
        //cenário
        Long id = 1l;
        Loan loan = LoanServiceTest.createLoan();
        loan.setId(id);
        Book book = Book.builder().id(1l).isbn("321").build();
        loan.setBook(book);

        //  PageRequest.of :: Página 1, cada página tem 10 itens, só voltou um item
        BDDMockito.given( loanService.find( Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)) )
              	  	.willReturn( new PageImpl<Loan>( Arrays.asList(loan), PageRequest.of(0,10), 1 ) );

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10",
                book.getIsbn(), loan.getCustomer());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc
            .perform( request )
            .andExpect( status().isOk() )
            .andExpect( jsonPath("content", Matchers.hasSize(1)))
            .andExpect( jsonPath("totalElements").value(1) )
            .andExpect( jsonPath("pageable.pageSize").value(10) )
            .andExpect( jsonPath("pageable.pageNumber").value(0))
        ;
    }




}