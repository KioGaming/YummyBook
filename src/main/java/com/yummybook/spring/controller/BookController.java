package com.yummybook.spring.controller;

import com.yummybook.dao.*;
import com.yummybook.domain.Book;
import com.yummybook.domain.Genre;
import com.yummybook.domain.Vote;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Controller
@Log
public class BookController {

    @Autowired
    BookDao bookDao;
    @Autowired
    GenreDao genreDao;
    @Autowired
    AuthorDao authorDao;
    @Autowired
    PublisherDao publisherDao;
    @Autowired
    VoteDao voteDao;

    @RequestMapping(value = {"", "/", "/book"})
    public String main(Model model,
                       @RequestParam("page") Optional<Integer> currentPageOptional,
                       @RequestParam("genre") Optional<Long> genreId,
                       @RequestParam("authorOrTitle") Optional<String> authorOrTitle) {
        int currentPage = currentPageOptional.orElse(0);
        int pageSize = 20;
        String sortField = "name";
        Sort.Direction sortDirection = Sort.Direction.ASC;

        Page<Book> bookPage;
        if (genreId.isPresent()) {
            bookPage = bookDao.findByGenre(currentPage, pageSize, sortField, sortDirection, genreId.get());
            Genre genre = genreDao.get(genreId.get());
            if (genre != null) {
                model.addAttribute("genre", genre.getName());
                model.addAttribute("foundBooks", bookPage.getTotalElements());
            } else {
                log.log(Level.ALL, "Unable to find books by genre because there is no such genre in database");
                return "error";
            }
        } else if (authorOrTitle.isPresent() && !authorOrTitle.get().isEmpty()) {
            bookPage = bookDao.search(currentPage, pageSize, sortField, sortDirection, authorOrTitle.get());
            model.addAttribute("authorOrTitle", authorOrTitle.get());
            model.addAttribute("foundBooks", bookPage.getTotalElements());
        } else {
            bookPage = bookDao.getAll(currentPage, pageSize, sortField, sortDirection);
        }

        if (bookPage.getNumberOfElements() != 0) {
            Optional<List<Integer>> numbersOfPage = Utils.getNumbersOfPage(bookPage.getTotalPages(), bookPage.getNumber());
            if (numbersOfPage.isPresent()) {
                model.addAttribute("pageNumbers", numbersOfPage.get());
            } else {
                model.addAttribute("noPages", true);
            }
        }

        model.addAttribute("page", bookPage);
        model.addAttribute("topFiveBooks", bookDao.findTopBooks(5));
        model.addAttribute("genres", genreDao.getAll());
        model.addAttribute("authors", authorDao.getAll());
        model.addAttribute("publishers", publisherDao.getAll());
        model.addAttribute("newBook", new Book());
        return "index";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/saveBook"})
    public RedirectView saveBook(@ModelAttribute Book book,
                                 @RequestParam("imageParam") Optional<MultipartFile> image,
                                 @RequestParam("contentParam") Optional<MultipartFile> content,
                                 @RequestParam("authorParam") Optional<Long> authorId,
                                 @RequestParam("genreParam") Optional<Long> genreId,
                                 @RequestParam("publisherParam") Optional<Long> publisherId) {
        if (image.isPresent() && content.isPresent() && authorId.isPresent() && genreId.isPresent() && publisherId.isPresent()) {
            book.setGenre(genreDao.get(genreId.get()));
            book.setAuthor(authorDao.get(authorId.get()));
            book.setPublisher(publisherDao.get(publisherId.get()));
            try {
                Book temp = bookDao.get(book.getId());
                if (temp != null && image.get().isEmpty()) {
                    book.setImage(temp.getImage());
                } else {
                    book.setImage(image.get().getBytes());
                }
                if (temp != null && content.get().isEmpty()) {
                    book.setContent(temp.getContent());
                } else {
                    book.setContent(content.get().getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            bookDao.save(book);
            return new RedirectView("/book");
        }
        log.log(Level.ALL, "Unable to save book because image, content, authorId, genreId, publisherId params aren't valid");
        return new RedirectView("/error");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/deleteBook"})
    public RedirectView deleteBook(@RequestParam("bookId") Optional<Long> bookId) {
        if (bookId.isPresent()) {
            Book book = bookDao.get(bookId.get());
            if (book != null) {
                bookDao.delete(book);
                return new RedirectView("/book");
            }
            log.log(Level.ALL, "Unable to delete book with id " + bookId.get() + " because there is no such book in database");
            return new RedirectView("/error");
        }
        log.log(Level.ALL, "Unable to delete book because bookId param isn't valid");
        return new RedirectView("/error");
    }

    @RequestMapping(value = {"/openBook"})
    public String openBook(HttpServletRequest request, @RequestParam("bookId") Optional<Long> bookId) {
        if (bookId.isPresent()) {
            byte[] bookContent = bookDao.getContent(bookId.get());
            if (bookContent != null) {
                bookDao.updateNumberOfViews(bookDao.get(bookId.get()).getNumberOfViews() + 1, bookId.get());
                request.setAttribute("bookContent", bookContent);
                return "forward:/getBook";
            }
            log.log(Level.ALL, "Unable to open book with id " + bookId.get() + " because book hasn't content");
            return "error";
        }
        log.log(Level.ALL, "Unable to open book because bookId param isn't valid");
        return "error";
    }

    @RequestMapping(value = {"/getBook"}, produces = "application/pdf")
    @ResponseBody
    public byte[] getBook(HttpServletRequest request) {
        return (byte[]) request.getAttribute("bookContent");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @RequestMapping(value = {"/voting"})
    @ResponseStatus(value = HttpStatus.OK)
    public void voting(@RequestParam("bookId") Optional<Long> bookId,
                       @RequestParam("vote") Optional<Long> vote) {
        if (bookId.isPresent() && vote.isPresent() && vote.get() > 0 && vote.get() < 6) {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Vote userVote = voteDao.getVote(bookId.get(), username);
            Book book = bookDao.get(bookId.get());
            if (book != null) {
                long voteValue = vote.get();
                long lastVoteValue = 0;
                long voteCount = book.getTotalVoteCount();

                if (userVote == null) {
                    voteDao.save(new Vote((int) voteValue, bookId.get(), username));
                    voteCount++;
                } else {
                    lastVoteValue = userVote.getValue();
                    voteDao.save(new Vote(userVote.getId(), (int) voteValue, bookId.get(), username));
                }

                long newRating = book.getTotalRating() + voteValue - lastVoteValue;
                int newAvgRating = (int) Utils.calcAverageRating(newRating, voteCount);

                bookDao.updateRating(newRating, voteCount, newAvgRating, bookId.get());
            } else {
                log.log(Level.ALL, "Unable to add vote for book with id " + bookId.get() + " because bookId isn't valid");
            }
        } else {
            log.log(Level.ALL, "Unable to add vote for book because bookId or vote params aren't valid");
        }
    }
}