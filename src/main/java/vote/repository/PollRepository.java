package vote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vote.model.Poll;

public interface PollRepository extends JpaRepository<Poll, Long> {}
