package pp.ua.library.yummybook.spring.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import pp.ua.library.yummybook.dao.AuthorDao;
import pp.ua.library.yummybook.dao.BookDao;
import pp.ua.library.yummybook.dao.GenreDao;
import pp.ua.library.yummybook.dao.PublisherDao;
import pp.ua.library.yummybook.domain.Author;
import pp.ua.library.yummybook.domain.Book;
import pp.ua.library.yummybook.domain.Genre;
import pp.ua.library.yummybook.domain.Publisher;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@Log
public class RedirectController {

    @Autowired
    BookDao bookDao;
    @Autowired
    GenreDao genreDao;
    @Autowired
    AuthorDao authorDao;
    @Autowired
    PublisherDao publisherDao;

    @RequestMapping(value = {"", "/booksPage"})
    public String baseUrlRedirect(Model model,
                                  @RequestParam("page") Optional<Integer> page,
                                  @RequestParam("size") Optional<Integer> size,
                                  @RequestParam("genre") Optional<Long> genreId,
                                  @RequestParam("authorOrTitle") Optional<String> authorOrTitle) {
        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(20);
        if (pageSize != 10 && pageSize != 15 && pageSize != 20 && pageSize != 30) pageSize = 20;
        String sortField = "name";
        Sort.Direction sortDirection = Sort.Direction.ASC;

        Page<Book> bookPage;
        if (genreId.isPresent()) {
            bookPage = bookDao.findByGenre(currentPage, pageSize, sortField, sortDirection, genreId.get());
            model.addAttribute("genre", genreId.get());
        } else if (authorOrTitle.isPresent()) {
            bookPage = bookDao.search(currentPage, pageSize, sortField, sortDirection, authorOrTitle.get());
            model.addAttribute("authorOrTitle", authorOrTitle.get());
        } else {
            bookPage = bookDao.getAll(currentPage, pageSize, sortField, sortDirection);
        }
        for (int i = 0; i < bookPage.getContent().size(); i++) {
            byteToBase64Image(bookPage.getContent().get(i));
        }
        List<List<Book>> bookLists = new ArrayList<>();
        for (int j = 0; j < Math.ceil(bookPage.getContent().size() / 5.0); j++) {
            List<Book> bookList = new ArrayList<>();
            for (int i = j * 5; i < (j + 1) * 5; i++) {
                if (bookPage.getContent().size() == i) {
                    break;
                }
                bookList.add(bookPage.getContent().get(i));
            }
            bookLists.add(bookList);
        }
        int totalPages = bookPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        List<Book> topBooks = bookDao.findTopBooks(5);
        for (Book topBook : topBooks) {
            byteToBase64Image(topBook);
        }

        List<Genre> genreList = genreDao.getAll();

        model.addAttribute("bookLists", bookLists);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("size", bookPage.getSize());
        model.addAttribute("number", bookPage.getSize());
        model.addAttribute("topBookList", topBooks);
        model.addAttribute("genreList", genreList);

        return "index";
    }

    @RequestMapping(value = {"/getBook"})
    public String getBook(HttpServletRequest request, @RequestParam("bookId") Optional<Long> bookId) {
        if (bookId.isPresent()) {
            byte[] bookContent = bookDao.getContent(bookId.get());
            if (bookContent != null) {
                request.setAttribute("bookContent", bookContent);
                return "forward:/openBook";
            }
        }
        return "forward:/booksPage";
        //добавить страничку ошибки
    }

    @RequestMapping(value = {"/openBook"}, produces = "application/pdf")
    @ResponseBody
    public byte[] openBook(HttpServletRequest request) {
        return (byte[]) request.getAttribute("bookContent");
    }

    @RequestMapping(value = {"/deleteBook"})
    public String deleteBook(@RequestParam("bookId") Optional<Long> bookId) {
        if(bookId.isPresent()) {
            bookDao.delete(bookDao.get(bookId.get()));
        }
        return "forward:/booksPage";
    }

    @RequestMapping(value = {"/editBook"})
    public String editBook(@RequestParam("bookId") Optional<Long> bookId, Model model) {
        if(bookId.isPresent()) {
            Book book = bookDao.get(bookId.get());
            byteToBase64Image(book);
            model.addAttribute("book", book);
            model.addAttribute("authorList", authorDao.getAll());
            model.addAttribute("genreList", genreDao.getAll());
            model.addAttribute("publisherList", publisherDao.getAll());
            return "editingPage";
        } else {
            return "forward:/booksPage";
            //добавить страничку ошибки
        }
    }

    @RequestMapping(value = {"/saveBook"})
    public String saveBook(@RequestParam("bookId") Optional<Long> bookId,
                           @RequestParam("bookImage") MultipartFile bookImage,
                           @RequestParam("bookName") Optional<String> bookName,
                           @RequestParam("bookAuthor") Optional<Long> bookAuthor,
                           @RequestParam("bookGenre") Optional<Long> bookGenre,
                           @RequestParam("bookPublisher") Optional<Long> bookPublisher,
                           @RequestParam("bookISBN") Optional<String> bookISBN,
                           @RequestParam("bookYear") Optional<Integer> bookYear,
                           @RequestParam("bookPageCount") Optional<Integer> bookPageCount,
                           @RequestParam("bookContent") MultipartFile bookContent,
                           @RequestParam("bookDescr") Optional<String> bookDescr,
                           @RequestParam("bookViewCount") Optional<Long> bookViewCount,
                           @RequestParam("bookTotalRating") Optional<Long> bookTotalRating,
                           @RequestParam("bookTotalVoteCount") Optional<Long> bookTotalVoteCount,
                           @RequestParam("bookAvgRating") Optional<Integer> bookAvgRating) {

        if(bookId.isPresent() && bookName.isPresent() && bookAuthor.isPresent() && bookGenre.isPresent()
                && bookPublisher.isPresent() && bookISBN.isPresent() && bookYear.isPresent() && bookPageCount.isPresent()
                && bookDescr.isPresent() && bookViewCount.isPresent() && bookTotalRating.isPresent()
                && bookTotalVoteCount.isPresent() && bookAvgRating.isPresent()) {
            Genre genre = genreDao.get(bookGenre.get());
            Author author = authorDao.get(bookAuthor.get());
            Publisher publisher = publisherDao.get(bookPublisher.get());
            byte[] image;
            if(!bookImage.isEmpty()){
                try {
                    image = bookImage.getBytes();
                } catch (IOException e) {
                    e.printStackTrace();
                    image = bookDao.get(bookId.get()).getImage();
                }
            } else {
                image = bookDao.get(bookId.get()).getImage();
            }
            Book book = new Book(bookId.get(), bookName.get(), bookPageCount.get(), bookISBN.get(), genre, author, publisher, bookYear.get(), image, bookDescr.get(), bookViewCount.get(), bookTotalRating.get(), bookTotalVoteCount.get(), bookAvgRating.get());
            if(!bookContent.isEmpty()){
                try {
                    book.setContent(bookContent.getBytes());
                } catch (IOException e) {
                    book.setContent(null);
                    e.printStackTrace();
                }
            }
            bookDao.save(book);
        }
        return "forward:/booksPage";
    }

    private Book byteToBase64Image(Book book) {
        book.setImageBase64("data:image/png;base64," + Base64.getEncoder().encodeToString(book.getImage()));
        return book;
    }
}