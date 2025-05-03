package vote.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class UserToken {
    @Id
    private String token;

    private boolean used;

    @ManyToOne
    private Poll poll;

    public UserToken(String token) {
        this.token = token;
        this.used = false;
    }

    public UserToken() {}
}

