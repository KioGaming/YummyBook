package com.yummybook.spring.controller;

import com.yummybook.dao.AuthorDao;
import com.yummybook.dao.GenreDao;
import com.yummybook.dao.PublisherDao;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import com.yummybook.domain.Author;
import com.yummybook.domain.Book;
import com.yummybook.domain.Genre;
import com.yummybook.domain.Publisher;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Controller
@Log
public class CatalogController {

    @Autowired
    GenreDao genreDao;
    @Autowired
    AuthorDao authorDao;
    @Autowired
    PublisherDao publisherDao;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/author"})
    public String author(Model model,
                         @RequestParam("page") Optional<Integer> currentPageOptional) {
        int currentPage = currentPageOptional.orElse(0);
        int pageSize = 20;
        String sortField = "fio";
        Sort.Direction sortDirection = Sort.Direction.ASC;

        Page<Author> page = authorDao.getAll(currentPage, pageSize, sortField, sortDirection);

        Optional<List<Integer>> numbersOfPage = Utils.getNumbersOfPage(page.getTotalPages(), page.getNumber());
        if (numbersOfPage.isPresent()) {
            model.addAttribute("pageNumbers", numbersOfPage.get());
        } else {
            log.log(Level.ALL, "Unable to show authors due to an error with pagination");
            return "error";
        }

        model.addAttribute("page", page);
        model.addAttribute("genres", genreDao.getAll());
        model.addAttribute("authors", authorDao.getAll());
        model.addAttribute("publishers", publisherDao.getAll());
        model.addAttribute("newBook", new Book());
        model.addAttribute("newAuthor", new Author());
        model.addAttribute("ZoneIdDefault", ZoneId.systemDefault());
        return "authors";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/saveAuthor"})
    public RedirectView saveAuthor(@ModelAttribute Author author,
                                   @RequestParam("birthdayParam") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> authorBirthdayOptional) {
        if (authorBirthdayOptional.isPresent()) {
            author.setBirthday(java.sql.Timestamp.valueOf(authorBirthdayOptional.get()));
            authorDao.save(author);
            return new RedirectView("/author");
        }
        log.log(Level.ALL, "Unable to save author because authorBirthdayOptional param isn't valid");
        return new RedirectView("/error");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/deleteAuthor"})
    public RedirectView deleteAuthor(@RequestParam("authorId") Optional<Long> authorId) {
        if (authorId.isPresent()) {
            Author author = authorDao.get(authorId.get());
            if (author != null && author.getBooks().size() == 0) {
                authorDao.delete(authorDao.get(authorId.get()));
                return new RedirectView("/author");
            }
            log.log(Level.ALL, "Unable to delete author with id " + authorId.get() + " because there is no such author in database or this author is used in a book");
            return new RedirectView("/error");
        }
        log.log(Level.ALL, "Unable to delete author because authorId param isn't valid");
        return new RedirectView("/error");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/genre"})
    public String genre(Model model,
                        @RequestParam("page") Optional<Integer> currentPageOptional) {
        int currentPage = currentPageOptional.orElse(0);
        int pageSize = 20;
        String sortField = "name";
        Sort.Direction sortDirection = Sort.Direction.ASC;

        Page<Genre> page = genreDao.getAll(currentPage, pageSize, sortField, sortDirection);

        Optional<List<Integer>> numbersOfPage = Utils.getNumbersOfPage(page.getTotalPages(), page.getNumber());
        if (numbersOfPage.isPresent()) {
            model.addAttribute("pageNumbers", numbersOfPage.get());
        } else {
            log.log(Level.ALL, "Unable to show genres due to an error with pagination");
            return "error";
        }

        model.addAttribute("page", page);
        model.addAttribute("genres", genreDao.getAll());
        model.addAttribute("authors", authorDao.getAll());
        model.addAttribute("publishers", publisherDao.getAll());
        model.addAttribute("newBook", new Book());
        model.addAttribute("newGenre", new Genre());
        return "genres";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/saveGenre"})
    public RedirectView saveGenre(@ModelAttribute Genre genre) {
        genreDao.save(genre);
        return new RedirectView("/genre");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/deleteGenre"})
    public RedirectView deleteGenre(@RequestParam("genreId") Optional<Long> genreId) {
        if (genreId.isPresent()) {
            Genre genre = genreDao.get(genreId.get());
            if (genre != null && genre.getBooks().size() == 0) {
                genreDao.delete(genreDao.get(genreId.get()));
                return new RedirectView("/genre");
            }
            log.log(Level.ALL, "Unable to delete genre with id " + genreId.get() + " because there is no such genre in database or this genre is used in a book");
            return new RedirectView("/error");
        }
        log.log(Level.ALL, "Unable to delete genre because genreId param isn't valid");
        return new RedirectView("/error");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/publisher"})
    public String publisher(Model model,
                            @RequestParam("page") Optional<Integer> currentPageOptional) {
        int currentPage = currentPageOptional.orElse(0);
        int pageSize = 20;
        String sortField = "name";
        Sort.Direction sortDirection = Sort.Direction.ASC;

        Page<Publisher> page = publisherDao.getAll(currentPage, pageSize, sortField, sortDirection);

        Optional<List<Integer>> numbersOfPage = Utils.getNumbersOfPage(page.getTotalPages(), page.getNumber());
        if (numbersOfPage.isPresent()) {
            model.addAttribute("pageNumbers", numbersOfPage.get());
        } else {
            log.log(Level.ALL, "Unable to show publishers due to an error with pagination");
            return "error";
        }

        model.addAttribute("page", page);
        model.addAttribute("genres", genreDao.getAll());
        model.addAttribute("authors", authorDao.getAll());
        model.addAttribute("publishers", publisherDao.getAll());
        model.addAttribute("newBook", new Book());
        model.addAttribute("newPublisher", new Publisher());
        return "publishers";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/savePublisher"})
    public RedirectView savePublisher(@ModelAttribute Publisher publisher) {
        publisherDao.save(publisher);
        return new RedirectView("/publisher");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/deletePublisher"})
    public RedirectView deletePublisher(@RequestParam("publisherId") Optional<Long> publisherId) {
        if (publisherId.isPresent()) {
            Publisher publisher = publisherDao.get(publisherId.get());
            if (publisher != null && publisher.getBooks().size() == 0) {
                publisherDao.delete(publisherDao.get(publisherId.get()));
                return new RedirectView("/publisher");
            }
            log.log(Level.ALL, "Unable to delete publisher with id " + publisherId.get() + " because there is no such publisher in database or this publisher is used in a book");
            return new RedirectView("/error");
        }
        log.log(Level.ALL, "Unable to delete publisher because publisherId param isn't valid");
        return new RedirectView("/error");
    }
}