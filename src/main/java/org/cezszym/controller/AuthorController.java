package org.cezszym.controller;

import org.cezszym.entity.Author;
import org.cezszym.entity.User;
import org.cezszym.enums.Role;
import org.cezszym.jwt.Identity;
import org.cezszym.repository.AuthorRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class AuthorController {

    private final AuthorRepository authorRepository;
    private final Identity identity;

    public AuthorController(AuthorRepository authorRepository, Identity identity) {
        this.authorRepository = authorRepository;
        this.identity = identity;
    }

    private Role GetUserRole() {
        User user = identity.getCurrent();
        return user.getRole();
    }

    @GetMapping("/authors")
    ResponseEntity<CollectionModel<Author>>  all() {
        ArrayList<Author> authors = new ArrayList<>(authorRepository.findAll());
        return ResponseEntity.ok(CollectionModel.of(authors));
    }

    @PostMapping("/authors")
    ResponseEntity<EntityModel<Author>> newAuthor(@RequestBody Author newAuthor) {
        if (!GetUserRole().equals(Role.ADMIN)) return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        Author author = authorRepository.save(newAuthor);
        return ResponseEntity.ok(EntityModel.of(author));

    }


    @GetMapping("/authors/{id}")
    ResponseEntity<EntityModel<Author>> getAuthor(@PathVariable int id) {

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));
        return ResponseEntity.ok(EntityModel.of(author));
    }

    @PutMapping("/authors/{id}")
    ResponseEntity<EntityModel<Author>> replaceEmployee(@RequestBody Author newAuthor, @PathVariable int id) {
        if (!GetUserRole().equals(Role.ADMIN)) return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        Author selectedAuthor = authorRepository.findById(id)
                .map(author -> {
                    author.setName(newAuthor.getName());
                    author.setSurname(newAuthor.getSurname());
                    author.setGender(newAuthor.getGender());
                    author.setNationality(newAuthor.getNationality());
                    author.setYearOfBirth(newAuthor.getYearOfBirth());
                    return authorRepository.save(newAuthor);
                })
                .orElseGet(() -> {
                    newAuthor.setId(id);
                    return authorRepository.save(newAuthor);
                });
        return ResponseEntity.ok(EntityModel.of(selectedAuthor));
    }

    @DeleteMapping("/authors/{id}")
    ResponseEntity deleteAuthor(@PathVariable int id) {
        if (!GetUserRole().equals(Role.ADMIN)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        authorRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    class AuthorNotFoundException extends RuntimeException {

        AuthorNotFoundException(int id) {
            super("No author with an id: " + id);
        }
    }

}
