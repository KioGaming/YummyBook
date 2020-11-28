package pp.ua.library.yummybook.spring.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pp.ua.library.yummybook.dao.AuthorDao;
import pp.ua.library.yummybook.dao.GenreDao;
import pp.ua.library.yummybook.dao.PublisherDao;
import pp.ua.library.yummybook.domain.Author;
import pp.ua.library.yummybook.domain.Genre;
import pp.ua.library.yummybook.domain.Publisher;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    @RequestMapping(value = {"/catalogs"})
    public String getCatalog(@RequestParam("catalog") Optional<String> catalog,
                             @RequestParam("page") Optional<Integer> page,
                             @RequestParam("size") Optional<Integer> size,
                             Model model) {
        if (catalog.isPresent()) {
            int currentPage = page.orElse(1) - 1;
            int pageSize = size.orElse(20);
            if (pageSize != 10 && pageSize != 15 && pageSize != 20 && pageSize != 30) pageSize = 20;
            Sort.Direction sortDirection = Sort.Direction.ASC;

            model.addAttribute("selectedCatalog", catalog.get());
            if(catalog.get().equals("authors")){
                Page<Author> list = authorDao.getAll(currentPage, pageSize, "fio", sortDirection);
                model.addAttribute("authors", list);
                buildModelForCatalogs(model, list.getTotalPages(), list.getSize(), list);
                return "authorsCatalogPage";
            } else if(catalog.get().equals("genres")){
                Page<Genre> list = genreDao.getAll(currentPage, pageSize, "name", sortDirection);
                model.addAttribute("genres", list);
                buildModelForCatalogs(model, list.getTotalPages(), list.getSize(), list);
                return "genresCatalogPage";
            } else if(catalog.get().equals("publishers")){
                Page<Publisher> list = publisherDao.getAll(currentPage, pageSize, "name", sortDirection);
                model.addAttribute("publishers", list);
                buildModelForCatalogs(model, list.getTotalPages(), list.getSize(), list);
                return "publishersCatalogPage";
            }
        }
        return "forward:/booksPage";
    }

    private void buildModelForCatalogs(Model model, int totalPages, int size2, Page list) {
        model.addAttribute("totalPages", totalPages);
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("size", size2);
        model.addAttribute("number", size2);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/deleteAuthor"})
    public String deleteAuthor(@RequestParam("authorId") Optional<Long> authorId) {
        if(authorId.isPresent()) {
            Author author = authorDao.get(authorId.get());
            if(author.getBooks().size() == 0) {
                authorDao.delete(authorDao.get(authorId.get()));
            }
        }
        return "redirect:/catalogs?catalog=authors";
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/deleteGenre"})
    public String deleteGenre(@RequestParam("genreId") Optional<Long> genreId) {
        if(genreId.isPresent()) {
            Genre genre = genreDao.get(genreId.get());
            if(genre.getBooks().size() == 0) {
                genreDao.delete(genreDao.get(genreId.get()));
            }
        }
        return "redirect:/catalogs?catalog=genres";
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/deletePublisher"})
    public String deletePublisher(@RequestParam("publisherId") Optional<Long> publisherId) {
        if(publisherId.isPresent()) {
            Publisher publisher = publisherDao.get(publisherId.get());
            if(publisher.getBooks().size() == 0) {
                publisherDao.delete(publisherDao.get(publisherId.get()));
            }
        }
        return "redirect:/catalogs?catalog=publishers";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/editAuthor"})
    public String editAuthor(@RequestParam("authorId") Optional<Long> authorId,
                             Model model) {
        if (authorId.isPresent()){
            Author author = authorDao.get(authorId.get());
            model.addAttribute("author", author);
            model.addAttribute("date", author.getBirthday().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        return "catalogs/editAuthor";
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/editGenre"})
    public String editGenre(@RequestParam("genreId") Optional<Long> genreId,
                            Model model) {
        genreId.ifPresent(aLong -> model.addAttribute("genre", genreDao.get(aLong)));
        return "catalogs/editGenre";
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/editPublisher"})
    public String editPublisher(@RequestParam("publisherId") Optional<Long> publisherId,
                                Model model) {
        publisherId.ifPresent(aLong -> model.addAttribute("publisher", publisherDao.get(aLong)));
        return "catalogs/editPublisher";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/saveAuthor"})
    public String saveAuthor(@RequestParam("authorId") Optional<Long> authorId,
                             @RequestParam("authorFio") Optional<String> authorFio,
                             @RequestParam("authorBirthday") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                     Optional<LocalDateTime> authorBirthday) {
        if(authorFio.isPresent() && authorBirthday.isPresent()){
            Author author;
            if(authorId.isPresent()){
                author = authorDao.get(authorId.get());
            } else {
                author = new Author();
            }
            author.setFio(authorFio.get());
            author.setBirthday(java.sql.Timestamp.valueOf(authorBirthday.get()));
            authorDao.save(author);
        }
        return "redirect:/catalogs?catalog=authors";
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/saveGenre"})
    public String saveGenre(@RequestParam("genreId") Optional<Long> genreId,
                            @RequestParam("genreName") Optional<String> name) {
        if(name.isPresent()){
            Genre genre;
            if(genreId.isPresent()){
                genre = genreDao.get(genreId.get());
            } else {
                genre = new Genre();
            }
            genre.setName(name.get());
            genreDao.save(genre);
        }
        return "redirect:/catalogs?catalog=genres";
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = {"/savePublisher"})
    public String savePublisher(@RequestParam("publisherId") Optional<Long> publisherId,
                                @RequestParam("publisherName") Optional<String> name) {
        if(name.isPresent()){
            Publisher publisher;
            if(publisherId.isPresent()){
                publisher = publisherDao.get(publisherId.get());
            } else {
                publisher = new Publisher();
            }
            publisher.setName(name.get());
            publisherDao.save(publisher);
        }
        return "redirect:/catalogs?catalog=publishers";
    }
}
