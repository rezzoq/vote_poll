package vote.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vote.model.Poll;
import vote.model.UserToken;
import vote.model.Vote;
import vote.repository.PollRepository;
import vote.repository.UserTokenRepository;
import vote.repository.VoteRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VotingService {
    @Autowired
    private PollRepository pollRepo;
    @Autowired
    private UserTokenRepository tokenRepo;
    @Autowired
    private VoteRepository voteRepo;

    private Poll currentPoll;

    public Poll createPoll(String question, int userCount) {
        pollRepo.findAll().forEach(p -> {
            p.setActive(false);
            pollRepo.save(p);
        });

        Poll poll = new Poll();
        poll.setQuestion(question);
        poll.setActive(true);
        pollRepo.save(poll);

        generateTokens(userCount, poll);
        return poll;
    }


    private void generateTokens(int count, Poll poll) {
        for (int i = 0; i < count; i++) {
            String token = UUID.randomUUID().toString();
            UserToken ut = new UserToken(token);
            ut.setPoll(poll);
            tokenRepo.save(ut);
        }
    }


    public boolean isTokenValid(String token) {
        return tokenRepo.findById(token)
                .map(t -> !t.isUsed())
                .orElse(false);
    }

    public void vote(String token, boolean voteYes) {
        UserToken userToken = tokenRepo.findById(token).orElse(null);
        if (userToken != null && !userToken.isUsed()) {
            Poll poll = userToken.getPoll();

            Vote vote = new Vote();
            vote.setToken(token);
            vote.setVoteYes(voteYes);
            voteRepo.save(vote);

            if (voteYes) poll.setYesCount(poll.getYesCount() + 1);
            else poll.setNoCount(poll.getNoCount() + 1);

            pollRepo.save(poll);

            userToken.setUsed(true);
            tokenRepo.save(userToken);
        }
    }


    public Poll getPoll() {
        return currentPoll;
    }

    public List<String> getGeneratedTokens() {
        return tokenRepo.findByPoll(currentPoll)
                .stream()
                .map(UserToken::getToken)
                .collect(Collectors.toList());
    }

    public Poll getActivePoll() {
        return pollRepo.findAll().stream()
                .filter(Poll::isActive)
                .findFirst()
                .orElse(null);
    }

}
