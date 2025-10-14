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
import com.example.demo.controllers.exception.ActiveLoanExistsException;
import com.example.demo.controllers.exception.BookNotFoundException;
import com.example.demo.controllers.exception.InvalidLoanStatusException;
import com.example.demo.controllers.exception.LoanNotFoundException;
import com.example.demo.controllers.exception.UserNotFoundException;
import com.example.demo.controllers.response.LoanResponse;
import com.example.demo.controllers.response.LoanSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;


    @Transactional
    public LoanResponse createLoan(LoanRequest request) {

        // Buscar el Libro por id, error si no se encuentra
        Book book = bookRepository.findById(request.getBookId()).orElseThrow(() -> new BookNotFoundException("Libro no encontrado"));

        // Verifica si el libro ya está prestado, si lo está, no se puede crear otro prestamo hasta que sea entregado
        if (!book.getAvailable()) {
            // Buscar el préstamo activo (no entregado) en el libro
            Book.LoanSummary activeLoan = book.getLoans().stream()
                    .filter(l -> "Prestado".equals(l.getStatus()) || "Vencido".equals(l.getStatus()))
                    .findFirst()
                    .orElse(null);

            LoanSummaryResponse response = mapLoanSummaryToResponse(activeLoan);

            throw new ActiveLoanExistsException("El libro no está disponible para préstamo", response);
        }

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
                .book(createBookSummary(book))
                .user(createUserSummary(user))
                .build();

        // Guardar el préstamo
        Loan savedLoan = loanRepository.save(loan);
        log.info("Préstamo creado con ID: {}", savedLoan.getId());

        // Limpiar la caché de libros relacionada
        cacheManager.getCache("books").clear();
        cacheManager.getCache("booksById").evict(book.getId());
        cacheManager.getCache("BooksBySearch").clear();

        // crear o actualizar el libro con la información del prestamo
        // Al crear un nuevo préstamo, el libro ya no está disponible
        book.setAvailable(false);
        updateBookWithNewLoan(book, savedLoan);

        // Crear o actualizar el usuario con la información del prestamo
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
                .averageRating(book.getAverageRating())
                .build();
    }



    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }


    private void updateBookWithNewLoan(Book book, Loan loan) {

        Book.LoanSummary loanSummary = Book.LoanSummary.builder()
                .id(loan.getId())
                .loanDate(loan.getLoanDate())
                .expectedReturnDate(loan.getExpectedReturnDate())
                .status(loan.getStatus())
                .returnDate(loan.getReturnDate()) // Asigna la fecha de devolución
                .user(Book.LoanSummary.UserInfo.builder()
                        .id(loan.getUser().getUserId())
                        .cardNum(loan.getUser().getCardNum())
                        .fullName(loan.getUser().getFullName())
                        .build()
                ).build();

        if(book.getLoans() == null){
            book.setLoans(new ArrayList<>());
        }

        book.getLoans().add(loanSummary);
        bookRepository.save(book);

    }
    private void updateUserWithNewLoan(Users user, Loan loan) {

        Users.LoanSummary loanSummary = Users.LoanSummary.builder()
                .id(loan.getId())
                .loanDate(loan.getLoanDate())
                .expectedReturnDate(loan.getExpectedReturnDate())
                .status(loan.getStatus())
                .book(Users.LoanSummary.BookInfo.builder()
                        .id(loan.getBook().getBookId())
                        .title(loan.getBook().getTitle())
                        .coverImageUrl(loan.getBook().getCoverImageUrl())
                        .build()
                ).build();

        if(user.getLoans() == null){
            user.setLoans(new ArrayList<>());
        }

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

    /**
     * Mapea un Book.LoanSummary a LoanSummaryResponse.
     * Solo llena el campo user (para BookResponse). El campo book queda null.
     * Se usa solo para sacar la info del prestamo activo de cierto libro.
     */
    private static LoanSummaryResponse mapLoanSummaryToResponse(Book.LoanSummary loanSummary) {
        if (loanSummary == null) return null;
        return LoanSummaryResponse.builder()
                .id(loanSummary.getId())
                .loanDate(loanSummary.getLoanDate())
                .expectedReturnDate(loanSummary.getExpectedReturnDate())
                .returnDate(loanSummary.getReturnDate())
                .status(loanSummary.getStatus())
                .user(loanSummary.getUser() != null ? LoanSummaryResponse.UserInfoResponse.builder()
                        .id(loanSummary.getUser().getId())
                        .fullName(loanSummary.getUser().getFullName())
                        .cardNum(loanSummary.getUser().getCardNum())
                        .build() : null)
                .book(null)
                .build();
    }

    @Transactional
    public LoanResponse markAsReturned(String id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("Préstamo no encontrado"));

        if ("Prestado".equals(loan.getStatus()) || "Vencido".equals(loan.getStatus())) {
            loan.setStatus("Entregado");
            loan.setReturnDate(LocalDate.now());
            loanRepository.save(loan);
        } else {
            throw new InvalidLoanStatusException("Solo se puede actualizar de 'Prestado' o 'Vencido' a 'Entregado'.");
        }

        // extrayendo el libro
        Book book = bookRepository.findById(loan.getBook().getBookId()).orElseThrow(() -> new BookNotFoundException("Libro no encontrado"));
        // actualizando el estado del prestamo en el libro
        book.getLoans().stream()
                .filter(loanSummary -> loanSummary.getId().equals(loan.getId()))
                .forEach(loanSummary -> {
                    loanSummary.setStatus("Entregado");
                    loanSummary.setReturnDate(loan.getReturnDate());
                });
        // Al marcar el préstamo como entregado, el libro vuelve a estar disponible
        book.setAvailable(true);
        bookRepository.save(book);

        // Limpiar la caché de libros relacionada
        cacheManager.getCache("books").clear();
        cacheManager.getCache("booksById").evict(book.getId());
        cacheManager.getCache("BooksBySearch").clear();

        // extrayendo el usuario
        Users user = userRepository.findById(loan.getUser().getUserId()).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        // actualizando el estado y la fecha de devolución en el préstamo del usuario
        user.getLoans().stream()
                .filter(loanSummary -> loanSummary.getId().equals(loan.getId()))
                .forEach(loanSummary -> {
                    loanSummary.setStatus("Entregado");
                    loanSummary.setReturnDate(loan.getReturnDate());
                });
        userRepository.save(user);

        return mapToLoanResponse(loan);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "loansById")
    public LoanResponse getLoanById(String id) {
        return loanRepository.findById(id)
                .map(this::mapToLoanResponse)
                .orElseThrow(() -> new LoanNotFoundException("Préstamo no encontrado con ID: " + id));
    }

    @Transactional
    @CacheEvict(value = "loansById", key = "#id")
    public void deleteLoan(String id) {
        // Buscar el préstamo
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("No se puede eliminar. Préstamo no encontrado con ID: " + id));

        // Eliminar la referencia del préstamo en el usuario
        userRepository.findById(loan.getUser().getUserId()).ifPresent(user -> {
            if (user.getLoans() != null) {
                user.getLoans().removeIf(summary -> summary.getId().equals(id));
                userRepository.save(user);
                log.info("Préstamo '{}' eliminado del usuario '{}'", id, user.getFullName());
            }
        });

        // Eliminar la referencia del préstamo en el libro
        bookRepository.findById(loan.getBook().getBookId()).ifPresent(book -> {
            if (book.getLoans() != null) {
                book.getLoans().removeIf(summary -> summary.getId().equals(id));
                bookRepository.save(book);
                log.info("Préstamo '{}' eliminado del libro '{}'", id, book.getTitle());
            }
        });

        // Eliminar el préstamo de la base de datos
        loanRepository.delete(loan);
        log.info("Préstamo eliminado con ID: {}", id);
    }


}
