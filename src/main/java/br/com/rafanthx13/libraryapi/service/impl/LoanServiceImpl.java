package br.com.rafanthx13.libraryapi.service.impl;

import br.com.rafanthx13.libraryapi.data.dto.LoanFilterDTO;
import br.com.rafanthx13.libraryapi.exception.BusinessException;
import br.com.rafanthx13.libraryapi.data.entity.Book;
import br.com.rafanthx13.libraryapi.data.entity.Loan;
import br.com.rafanthx13.libraryapi.data.repository.LoanRepository;
import br.com.rafanthx13.libraryapi.service.LoanService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LoanServiceImpl implements LoanService {

    private LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    // Antes de slavar empréstimo, verifica se o livro já não está sendo emprestado
    // Busco emprestimo com esse livro que o retorned seja false (Nao foi retornado entâo está emprestado)
    @Override
    public Loan save( Loan loan ) {
        if( repository.existsByBookAndNotReturned( loan.getBook()) ){
            throw new BusinessException("Book already loaned");
        }
        return repository.save(loan);
    }

    // QueryMethod do JPA: busca informaçôes por ID
    @Override
    public Optional<Loan> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return repository.save(loan);
    }

    // Buscar loan filted. Aqui nâo é um query Method, entâo temod que implementar mesmo no repository
    @Override
    public Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageable) {
        return repository.findByBookIsbnOrCustomer( filterDTO.getIsbn(), filterDTO.getCustomer(), pageable );
    }

    @Override
    public Page<Loan> getLoansByBook(Book book, Pageable pageable) {
        return repository.findByBook(book, pageable);
    }

    @Override
    public List<Loan> getAllLateLoans() {
        final Integer loanDays = 4;
        LocalDate threeDaysAgo = LocalDate.now().minusDays(loanDays);
        return repository.findByLoanDateLessThanAndNotReturned(threeDaysAgo);
    }
}