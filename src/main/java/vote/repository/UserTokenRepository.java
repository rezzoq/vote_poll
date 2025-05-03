package vote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vote.model.Poll;
import vote.model.UserToken;

import java.util.List;

public interface UserTokenRepository extends JpaRepository<UserToken, String> {
    List<UserToken> findByPoll(Poll poll);
}
