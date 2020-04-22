package br.com.rafanthx13.libraryapi.service;

import br.com.rafanthx13.libraryapi.data.entity.Book;
import br.com.rafanthx13.libraryapi.data.entity.Loan;
import br.com.rafanthx13.libraryapi.data.dto.LoanFilterDTO;
import br.com.rafanthx13.libraryapi.controller.BookController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoanService {

    Loan save( Loan loan );

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageable);

    Page<Loan> getLoansByBook( Book book, Pageable pageable);

    List<Loan> getAllLateLoans();

}