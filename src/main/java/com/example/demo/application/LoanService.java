package com.example.demo.application;

import com.example.demo.controllers.domain.Model.BookSummary;
import com.example.demo.controllers.domain.Model.UserSummary;
import com.example.demo.controllers.domain.entity.Book;
import com.example.demo.controllers.domain.entity.Loan;
import com.example.demo.controllers.domain.entity.Users;
import com.example.demo.controllers.domain.repository.BookRepository;
import com.example.demo.controllers.domain.repository.LoanRepository;
import com.example.demo.controllers.domain.repository.UserRepository;
import com.example.demo.controllers.dto.LoanRequest;
import com.example.demo.controllers.exception.BookNotFoundException;
import com.example.demo.controllers.exception.UserNotFoundException;
import com.example.demo.controllers.response.LoanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;


    @Transactional
    public LoanResponse createLoan(LoanRequest request) {

        // Buscar el Libro por id, error si no se encuentra
        Book book = bookRepository.findById(request.getBookId()).orElseThrow(() -> new BookNotFoundException("Libro no encontrado"));

        // Buscar el Usuario por id, error si no se encuentra
        Users user = userRepository.findById(request.getUserId()).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // especifico que se envie un loanDate
        LocalDate loanDate = request.getLoanDate() != null ? request.getLoanDate() : LocalDate.now();
        request.setLoanDate(loanDate);

        // especifico que se envie un expectedReturnDate
        LocalDate expectedReturnDate = request.getExpectedReturnDate() != null ? request.getExpectedReturnDate() : loanDate.plusDays(30);
        request.setExpectedReturnDate(expectedReturnDate);

        // Mapear LoanRequest a la entidad Loan
        Loan loan = Loan.builder()
                .status("Prestado")
                .loanDate(request.getLoanDate())
                .expectedReturnDate(request.getExpectedReturnDate())
                .book(new BookSummary(book.getId(), book.getTitle(), book.getCoverImageUrl(), book.getAverageRating()))
                .user(new UserSummary(user.getId(), user.getFullName(), user.getCardNum(), user.getEmail()))
                .build();

        // Guardar el préstamo
        Loan savedLoan = loanRepository.save(loan);
        log.info("Préstamo creado con ID: {}", savedLoan.getId());

        // ingreso el BookSummary al Loan
        BookSummary bookSummary = createBookSummary(book);
        savedLoan.setBook(bookSummary);

        // Ingreso el UserSummary al Loan
        UserSummary userSummary = createUserSummary(user);
        savedLoan.setUser(userSummary);


        // crear o actualizar el libro con la información del prestamo
        updateBookWithNewLoan(book, savedLoan);

        updateUserWithNewLoan(user, savedLoan);


        //Convertir a DTO y retornar
        return mapToLoanResponse(savedLoan);
    }

    private UserSummary createUserSummary(Users user) {
        return UserSummary.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .cardNum(user.getCardNum())
                .email(user.getEmail())
                .build();
    }

    private BookSummary createBookSummary(Book book) {
        return BookSummary.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .coverImageUrl(book.getCoverImageUrl())
                .build();
    }



    public Optional<Loan> updateLoan(String id, Loan loan) {
        return loanRepository.findById(id).map(existing -> {
            loan.setId(id);
            return loanRepository.save(loan);
        });
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }


    private void updateBookWithNewLoan(Book book, Loan loan) {

        Book.LoanSummary loanSummary = Book.LoanSummary.builder()
                .LoanId(loan.getId())
                .loanDate(loan.getLoanDate())
                .expectedReturnDate(loan.getExpectedReturnDate())
                .status(loan.getStatus())
                .user(Book.LoanSummary.UserInfo.builder()
                        .id(loan.getUser().getUserId())
                        .cardNum(loan.getUser().getCardNum())
                        .fullName(loan.getUser().getFullName())
                        .build()
                ).build();
        book.getLoans().add(loanSummary);
        bookRepository.save(book);

    }
    private void updateUserWithNewLoan(Users user, Loan loan) {

        Users.LoanSummary loanSummary = Users.LoanSummary.builder()
                .LoanId(loan.getId())
                .loanDate(loan.getLoanDate())
                .expectedReturnDate(loan.getExpectedReturnDate())
                .status(loan.getStatus())
                .book(Users.LoanSummary.BookInfo.builder()
                        .id(loan.getBook().getBookId())
                        .title(loan.getBook().getTitle())
                        .coverImageUrl(loan.getBook().getCoverImageUrl())
                        .build()
                ).build();

        user.getLoans().add(loanSummary);
        userRepository.save(user);

    }

    private LoanResponse mapToLoanResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .status(loan.getStatus())
                .loanDate(loan.getLoanDate())
                .expectedReturnDate(loan.getExpectedReturnDate())
                .returnDate(loan.getReturnDate())
                .book(LoanResponse.BookInfoResponse.builder()
                        .id(loan.getBook().getBookId())
                        .title(loan.getBook().getTitle())
                        .coverImageUrl(loan.getBook().getCoverImageUrl())
                        .build()
                )
                .user(LoanResponse.UserInfoResponse.builder()
                        .id(loan.getUser().getUserId())
                        .fullName(loan.getUser().getFullName())
                        .cardNum(loan.getUser().getCardNum())
                        .build()
                )
                .build();
    }

}