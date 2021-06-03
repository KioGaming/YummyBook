package com.yummybook.dao;

import com.yummybook.domain.Vote;

public interface VoteDao extends GeneralDAO<Vote>{
    Vote getVote(long bookId, String username);
}
