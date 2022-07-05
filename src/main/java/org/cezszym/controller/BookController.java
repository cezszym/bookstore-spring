package org.cezszym.controller;

import org.cezszym.dto.BookDTO;
import org.cezszym.entity.Author;
import org.cezszym.entity.Book;
import org.cezszym.entity.Category;
import org.cezszym.entity.User;
import org.cezszym.enums.Role;
import org.cezszym.jwt.Identity;
import org.cezszym.repository.AuthorRepository;
import org.cezszym.repository.BookRepository;
import org.cezszym.repository.CategoryRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class BookController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final Identity identity;

    public BookController(BookRepository bookRepository, AuthorRepository authorRepository, CategoryRepository shelfRepository, Identity identity) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = shelfRepository;
        this.identity = identity;
    }

    private Role GetUserRole() {
        User user = identity.getCurrent();
        return user.getRole();
    }

    @GetMapping("/books")
    ResponseEntity<CollectionModel<Book>>  all() {
        ArrayList<Book> books = new ArrayList<>(bookRepository.findAll());
        return ResponseEntity.ok(CollectionModel.of(books));
    }

    @PostMapping("/books")
    ResponseEntity<EntityModel<Book>> newBook(@RequestBody BookDTO newBook) {

        if (!GetUserRole().equals(Role.ADMIN)) return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        Author author = authorRepository.getById(newBook.getAuthor_id());
        if (author == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        Category category = categoryRepository.getById(newBook.getCategory_id());
        if (author == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);


        Book book = bookRepository.save(Book.builder()
                .title(newBook.getTitle())
                .author(author)
                .authorFullname(author.getName() + " " + author.getSurname())
                .category(category)
                .categoryName(category.getName())
                .description(newBook.getDescription())
                .price(newBook.getPrice())
                .year(newBook.getYear())
                .build());
        return ResponseEntity.ok(EntityModel.of(book));

    }


    @GetMapping("/books/{id}")
    ResponseEntity<EntityModel<Book>> getBook(@PathVariable int id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        return ResponseEntity.ok(EntityModel.of(book));
    }

    @PutMapping("/books/{id}")
    ResponseEntity<EntityModel<Book>> replaceEmployee(@RequestBody Book newBook, @PathVariable int id) {

        if (!GetUserRole().equals(Role.ADMIN)) return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        Book selectedBook = bookRepository.findById(id)
                .map(book -> {
                    book.setTitle(newBook.getTitle());
                    book.setDescription(newBook.getDescription());
                    book.setPrice(newBook.getPrice());
                    book.setYear(newBook.getYear());
                    return bookRepository.save(newBook);
                })
                .orElseGet(() -> {
                    newBook.setId(id);
                    return bookRepository.save(newBook);
                });
        return ResponseEntity.ok(EntityModel.of(selectedBook));
    }

    @DeleteMapping("/books/{id}")
    ResponseEntity deleteEmployee(@PathVariable int id) {
        if (!GetUserRole().equals(Role.ADMIN)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        bookRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    class BookNotFoundException extends RuntimeException {

        BookNotFoundException(int id) {
            super("No book with an id: " + id);
        }
    }

    class AuthorNotExistingException extends RuntimeException {

        AuthorNotExistingException() {
            super("Author with this id doesn't exist");
        }
    }

}
