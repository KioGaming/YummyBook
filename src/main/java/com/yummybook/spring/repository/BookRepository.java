package com.yummybook.spring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.yummybook.domain.Book;

import java.util.List;


@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByNameContainingIgnoreCaseOrAuthorFioContainingIgnoreCaseOrderByName(String name, String fio, Pageable pageable);

    @Query("select new com.yummybook.domain.Book(b.id, b.name, b.numberOfPages, b.isbn, b.genre, b.author, b.publisher, b.publishYear, b.image, b.descr, b.numberOfViews, b.totalRating, b.totalVoteCount, b.avgRating) from Book b")
    Page<Book> findAllWithoutContent(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("update Book b set b.content=:content where b.id =:id")
    void updateContent(@Param("content") byte[] content, @Param("id") long id);

    @Query("select new com.yummybook.domain.Book(b.id, b.image) from Book b")
    List<Book> findTopBooks(Pageable pageable);

    @Query("select b.content FROM Book b where b.id = :id")
    byte[] getContent(@Param("id") long id);

    @Query("select new com.yummybook.domain.Book(b.id, b.name, b.numberOfPages, b.isbn, b.genre, b.author, b.publisher, b.publishYear, b.image, b.descr, b.numberOfViews, b.totalRating, b.totalVoteCount, b.avgRating) from Book b where b.genre.id=:genreId")
    Page<Book> findByGenre(@Param("genreId") long genreId, Pageable pageable);

    @Modifying
    @Query("update Book b set b.numberOfViews=:viewCount where b.id =:id")
    void updateViewCount(@Param("viewCount") long viewCount, @Param("id") long id);

    @Modifying
    @Query("update Book b set b.totalVoteCount=:totalVoteCount, b.totalRating=:totalRating, b.avgRating=:avgRating where b.id =:id")
    void updateRating(@Param("totalRating") long totalRating, @Param("totalVoteCount") long totalVoteCount,  @Param("avgRating") int avgRating, @Param("id") long id);
}
