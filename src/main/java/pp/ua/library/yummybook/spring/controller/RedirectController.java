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
import pp.ua.library.yummybook.domain.Book;

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

    @RequestMapping(value = {"", "/index"})
    public String baseUrlRedirect(Model model,
                                  @RequestParam("page") Optional<Integer> page,
                                  @RequestParam("size") Optional<Integer> size,
                                  @RequestParam("sort") Optional<String> sort,
                                  @RequestParam("direction") Optional<Sort.Direction> direction) {
        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(20);
        String sortField = sort.orElse("name");
        Sort.Direction sortDirection = direction.orElse(Sort.Direction.ASC);

        Page<Book> bookPage = bookDao.getAll(currentPage, pageSize, sortField, sortDirection);
        for (int i = 0; i < bookPage.getContent().size(); i++) {
            bookPage.getContent().get(i).setImageBase64("data:image/png;base64," + Base64.getEncoder().encodeToString(bookPage.getContent().get(i).getImage()));
        }
        model.addAttribute("bookPage", bookPage);

        int totalPages = bookPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return "books/books-template";
    }
}