package pp.ua.library.yummybook.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pp.ua.library.yummybook.domain.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Vote findByBookIdAndUsername(long bookId, String username);
    //@Query("select new pp.ua.library.yummybook.domain.Vote(v.id, v.value, v.bookId, v.username) FROM Vote v where v.bookId =: bookId AND v.username =: username")
    //Vote getVote(@Param("bookId") long bookId, @Param("username") String username);
}
