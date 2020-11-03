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

    @RequestMapping(value = {"/addBook"})
    public String addBook(Model model) {
        model.addAttribute("authorList", authorDao.getAll());
        model.addAttribute("genreList", genreDao.getAll());
        model.addAttribute("publisherList", publisherDao.getAll());
        model.addAttribute("noImage","data:image/png;base64,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");
        return "addingPage";
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
            byte[] image = Base64.getDecoder().decode("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");
            if(!bookImage.isEmpty()){
                try {
                    image = bookImage.getBytes();
                } catch (IOException e) {
                    e.printStackTrace();
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