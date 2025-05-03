package vote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vote.model.Poll;
import vote.model.UserToken;
import vote.repository.UserTokenRepository;
import vote.service.VotingService;

import java.util.List;

@Controller
public class VotingController {

    @Autowired private VotingService votingService;
    @Autowired private UserTokenRepository tokenRepo;

    @GetMapping("/")
    public String index(Model model) {
        Poll poll = votingService.getActivePoll();
        model.addAttribute("poll", poll);

        if (poll != null) {
            List<String> tokens = tokenRepo.findAll().stream()
                    .filter(t -> !t.isUsed() && t.getPoll().getId().equals(poll.getId()))
                    .map(UserToken::getToken)
                    .toList();
            model.addAttribute("tokens", tokens);
        }
        return "index";
    }

    @PostMapping("/createPoll")
    public String createPoll(@RequestParam("question") String question,
                             @RequestParam("userCount") int userCount) {
        votingService.createPoll(question, userCount);
        return "redirect:/";
    }

    @GetMapping("/vote/{token}")
    public String vote(@PathVariable String token, Model model) {
        if (votingService.isTokenValid(token)) {
            model.addAttribute("token", token);
            // Pass the active poll so the question is visible on the voting page
            model.addAttribute("poll", votingService.getActivePoll());
            return "poll";
        }
        return "redirect:/";
    }

    @PostMapping("/submitVote")
    public String submitVote(@RequestParam("token") String token,
                             @RequestParam("voteYes") boolean voteYes) {
        votingService.vote(token, voteYes);
        return "redirect:/results";
    }

    @GetMapping("/results")
    public String results(Model model) {
        model.addAttribute("poll", votingService.getActivePoll());
        return "results";
    }
}
