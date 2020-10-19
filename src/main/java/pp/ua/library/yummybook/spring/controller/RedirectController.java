package pp.ua.library.yummybook.spring.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pp.ua.library.yummybook.dao.BookDao;
import pp.ua.library.yummybook.dao.GenreDao;
import pp.ua.library.yummybook.domain.Book;
import pp.ua.library.yummybook.domain.Genre;

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

    @RequestMapping(value = {"", "/booksPage"})
    public String baseUrlRedirect(Model model,
                                  @RequestParam("page") Optional<Integer> page,
                                  @RequestParam("size") Optional<Integer> size,
                                  @RequestParam("sort") Optional<String> sort,
                                  @RequestParam("direction") Optional<Sort.Direction> direction,
                                  @RequestParam("genre") Optional<Long> genreId) {
        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(20);;
        if(pageSize != 10 && pageSize != 15 && pageSize != 20 && pageSize != 30){
            pageSize = 20;
        }
        String sortField = sort.orElse("name");
        Sort.Direction sortDirection = direction.orElse(Sort.Direction.ASC);

        Page<Book> bookPage;
        if(genreId.isPresent()) {
            bookPage = bookDao.findByGenre(currentPage, pageSize, sortField, sortDirection, genreId.get());
            model.addAttribute("genre", genreId.get());
        } else {
            bookPage = bookDao.getAll(currentPage, pageSize, sortField, sortDirection);
        }
        for (int i = 0; i < bookPage.getContent().size(); i++) {
            bookPage.getContent().get(i).setImageBase64("data:image/png;base64," + Base64.getEncoder().encodeToString(bookPage.getContent().get(i).getImage()));
        }

        List<List<Book>> bookLists = new ArrayList<>();
        for (int j = 0; j < Math.ceil(bookPage.getContent().size()/5.0); j++) {
            List<Book> bookList = new ArrayList<>();
            for (int i = j * 5; i < (j + 1) * 5; i++) {
                if(bookPage.getContent().size() == i){
                    break;
                }
                bookList.add(bookPage.getContent().get(i));
            }
            bookLists.add(bookList);
        }
        model.addAttribute("bookLists", bookLists);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("size", bookPage.getSize());
        model.addAttribute("number", bookPage.getSize());

        List<Book> topBooks = bookDao.findTopBooks(5);
        for (Book topBook: topBooks) {
            topBook.setImageBase64("data:image/png;base64," + Base64.getEncoder().encodeToString(topBook.getImage()));
        }
        model.addAttribute("topBookList", topBooks);

        List<Genre> genreList = genreDao.getAll();
        model.addAttribute("genreList", genreList);

        int totalPages = bookPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return "index";
    }
}