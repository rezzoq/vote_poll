package vote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vote.model.Poll;
import vote.model.Vote;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByPoll(Poll poll);
}

