package pp.ua.library.yummybook.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pp.ua.library.yummybook.dao.VoteDao;
import pp.ua.library.yummybook.domain.Vote;
import pp.ua.library.yummybook.spring.repository.VoteRepository;

import java.util.List;

@Service
@Transactional
public class VoteService implements VoteDao {

    @Autowired
    private VoteRepository voteRepository;

    @Override
    public Vote getVote(long bookId, String username) {
        return voteRepository.findByBookIdAndUsername(bookId, username);
    }

    @Override
    public Vote save(Vote vote) {
        return voteRepository.save(vote);
    }

    @Override
    public List<Vote> getAll() {
        return null;
    }

    @Override
    public List<Vote> search(String... searchString) {
        return null;
    }

    @Override
    public Vote get(long id) {
        return null;
    }

    @Override
    public void delete(Vote object) {

    }

    @Override
    public List<Vote> getAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Vote> getAll(int pageNumber, int pageSize, String sortField, Sort.Direction sortDirection) {
        return null;
    }

    @Override
    public Page<Vote> search(int pageNumber, int pageSize, String sortField, Sort.Direction sortDirection, String... searchString) {
        return null;
    }
}