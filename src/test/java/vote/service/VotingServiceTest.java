package vote.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vote.model.Poll;
import vote.model.UserToken;
import vote.model.Vote;
import vote.repository.PollRepository;
import vote.repository.UserTokenRepository;
import vote.repository.VoteRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotingServiceTest {

    @Mock private PollRepository pollRepo;
    @Mock private UserTokenRepository tokenRepo;
    @Mock private VoteRepository voteRepo;

    @InjectMocks
    private VotingService votingService;

    private Poll activePoll;
    private UserToken unusedToken;

    @BeforeEach
    void setUp() {
        activePoll = new Poll();
        activePoll.setQuestion("Should we adopt remote work?");
        activePoll.setActive(true);

        unusedToken = new UserToken("test-token-uuid");
        unusedToken.setPoll(activePoll);
        unusedToken.setUsed(false);
    }

    // ── createPoll ────────────────────────────────────────────────────────────

    @Test
    void createPoll_deactivatesExistingPolls() {
        Poll oldPoll = new Poll();
        oldPoll.setActive(true);
        when(pollRepo.findAll()).thenReturn(List.of(oldPoll));
        when(pollRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tokenRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        votingService.createPoll("New question", 3);

        assertFalse(oldPoll.isActive(), "Previous poll should be deactivated");
    }

    @Test
    void createPoll_generatesCorrectNumberOfTokens() {
        when(pollRepo.findAll()).thenReturn(List.of());
        when(pollRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tokenRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        votingService.createPoll("Question?", 5);

        // 1 poll save + 5 token saves
        verify(tokenRepo, times(5)).save(any(UserToken.class));
    }

    // ── isTokenValid ──────────────────────────────────────────────────────────

    @Test
    void isTokenValid_returnsTrueForUnusedToken() {
        when(tokenRepo.findById("valid-token")).thenReturn(Optional.of(unusedToken));
        assertTrue(votingService.isTokenValid("valid-token"));
    }

    @Test
    void isTokenValid_returnsFalseForUsedToken() {
        unusedToken.setUsed(true);
        when(tokenRepo.findById("used-token")).thenReturn(Optional.of(unusedToken));
        assertFalse(votingService.isTokenValid("used-token"));
    }

    @Test
    void isTokenValid_returnsFalseForUnknownToken() {
        when(tokenRepo.findById("ghost-token")).thenReturn(Optional.empty());
        assertFalse(votingService.isTokenValid("ghost-token"));
    }

    // ── vote ──────────────────────────────────────────────────────────────────

    @Test
    void vote_yes_incrementsYesCount() {
        when(tokenRepo.findById("test-token-uuid")).thenReturn(Optional.of(unusedToken));
        when(voteRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(pollRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tokenRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        votingService.vote("test-token-uuid", true);

        assertEquals(1, activePoll.getYesCount());
        assertEquals(0, activePoll.getNoCount());
    }

    @Test
    void vote_no_incrementsNoCount() {
        when(tokenRepo.findById("test-token-uuid")).thenReturn(Optional.of(unusedToken));
        when(voteRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(pollRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tokenRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        votingService.vote("test-token-uuid", false);

        assertEquals(0, activePoll.getYesCount());
        assertEquals(1, activePoll.getNoCount());
    }

    @Test
    void vote_marksTokenAsUsed() {
        when(tokenRepo.findById("test-token-uuid")).thenReturn(Optional.of(unusedToken));
        when(voteRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(pollRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tokenRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        votingService.vote("test-token-uuid", true);

        assertTrue(unusedToken.isUsed(), "Token should be marked as used after voting");
    }

    @Test
    void vote_doesNothingForUsedToken() {
        unusedToken.setUsed(true);
        when(tokenRepo.findById("test-token-uuid")).thenReturn(Optional.of(unusedToken));

        votingService.vote("test-token-uuid", true);

        // No vote should be saved
        verify(voteRepo, never()).save(any(Vote.class));
        assertEquals(0, activePoll.getYesCount());
    }
}
