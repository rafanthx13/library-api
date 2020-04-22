package br.com.rafanthx13.libraryapi.service;

import br.com.rafanthx13.libraryapi.data.dto.LoanFilterDTO;
import br.com.rafanthx13.libraryapi.exception.BusinessException;
import br.com.rafanthx13.libraryapi.data.entity.Book;
import br.com.rafanthx13.libraryapi.data.entity.Loan;
import br.com.rafanthx13.libraryapi.data.repository.LoanRepository;
import br.com.rafanthx13.libraryapi.service.impl.LoanServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService service;

    @MockBean // Vai criar instância Mock dessa interface
    LoanRepository repository;

    @BeforeEach // Antes de cada test, criar a implementação  do service
    public void setUp(){
        this.service = new LoanServiceImpl(repository);
    }

    // POST : Inserir Empréstimo

    @Test
    @DisplayName("Deve salvar um empréstimo")
    public void saveLoanTest(){
        // Crio o livro
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";
        // Crio instância do Loan
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
        // Instância do Loan anterior já salva no banco (PARA O MOCK DE Retorno)
        Loan savedLoan = Loan.builder()
                .id(1l)
                .loanDate(LocalDate.now())
                .customer(customer)
                .book(book).build();


        when( repository.existsByBookAndNotReturned(book) ).thenReturn(false);
        // Quando salvar o savingLoan, vai retornar o savedLoan simulando o retorno do repository
        when( repository.save(savingLoan) ).thenReturn( savedLoan ); 

        Loan loan = service.save(savingLoan); // vai executa os métodos mockados linhas atrás

        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar um empréstimo com livro já emprestado")
    public void loanedBookSaveTest(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                        .book(book)
                        .customer(customer)
                        .loanDate(LocalDate.now())
                        .build();

        // Vai retornar true, querendo dizer que já está emprestado
        when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable exception = catchThrowable(() -> service.save(savingLoan));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        // Garanto que ele nâo executou o save, pois nesse caso, como já está emprestado, não deve salvar
        verify(repository, never()).save(savingLoan);

    }

    // PATH : Devolver um Livro

    @Test
    @DisplayName("Deve atualizar um empréstimo.")
    public void updateLoanTest(){
        Loan loan = createLoan();
        loan.setId(1l);
        loan.setReturned(true);

        when( repository.save(loan) ).thenReturn( loan );

        Loan updatedLoan = service.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();
        verify(repository).save(loan);
    }

    // GET

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo ID")
    public void getLoanDetaisTest(){
        //cenário
        Long id = 1l;

        Loan loan = createLoan();
        loan.setId(id);

        // Faço o Mock do findById do repository
        Mockito.when( repository.findById(id) )
               .thenReturn( Optional.of(loan) );

        //execucao : quando fizer getById do service vai retornar o loan mcokado ateriormente
        Optional<Loan> result = service.getById(id);

        //verificacao
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify( repository ).findById(id);

    }

    // GET FILTED   

    @Test
    @DisplayName("Deve filtrar empréstimos pelas propriedades")
    public void findLoanTest(){
        // Recebendo DTO e convertendo em Entidade
        /* OBS!!!! Lembre-se, não vamos usar um Example no Book, pois era uma única entidade
            Mas como agora é mais complexo, vamos ter que fazer e uma forma difenrete, vamos ter
            que criar um DTO Especifico par aintercepitar os parametos e fazer a filtragem
        */
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();
        Loan loan = createLoan();
        loan.setId(1l);
        // Criando a página
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> lista = Arrays.asList(loan);
        Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, lista.size());
        // Mockar a busca paginada
        when( repository.findByBookIsbnOrCustomer( Mockito.anyString(), Mockito.anyString(), 
            Mockito.any(PageRequest.class))
        ).thenReturn(page);
        // Execução
        Page<Loan> result = service.find( loanFilterDTO, pageRequest );
        //verificacoes
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    // Cria um livro e um empréstimo, só falta id
    public static Loan createLoan(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        return Loan.builder()
                        .book(book)
                        .customer(customer)
                        .loanDate(LocalDate.now())
                        .build();
    }
}