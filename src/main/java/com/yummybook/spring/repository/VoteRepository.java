package com.yummybook.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.yummybook.domain.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Vote findByBookIdAndUsername(long bookId, String username);
}
