package pp.ua.library.yummybook.dao;

import pp.ua.library.yummybook.domain.Vote;

public interface VoteDao extends GeneralDAO<Vote>{
    Vote getVote(long bookId, String username);
}
