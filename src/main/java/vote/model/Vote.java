package vote.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean voteYes;

    private String token;

    @ManyToOne
    private Poll poll;
}

