package me.cybulski.civ5pbemserver.game;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import me.cybulski.civ5pbemserver.civilization.Civilization;
import me.cybulski.civ5pbemserver.game.exception.CannotModifyGameException;
import me.cybulski.civ5pbemserver.jpa.BaseEntity;
import me.cybulski.civ5pbemserver.user.UserAccount;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Michał Cybulski
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "game_id", "playerNumber" }))
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Slf4j
class Player extends BaseEntity {

    @NotNull
    @ManyToOne(optional = false)
    private Game game;

    /**
     * Indexed from 1.
     */
    private int playerNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Civilization civilization;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PlayerType playerType;

    @ManyToOne
    private UserAccount humanUserAccount;

    void changeToHuman() {
        this.playerType = PlayerType.HUMAN;
    }

    void changeToAi() {
        if (humanUserAccount != null) {
            throw new CannotModifyGameException("Cannot change player with human player joined! Kick player first.");
        }
        this.playerType = PlayerType.AI;
    }

    void close() {
        if (humanUserAccount != null) {
            throw new CannotModifyGameException("Cannot change player with human player joined! Kick player first.");
        }
        this.playerType = PlayerType.CLOSED;
    }

    void joinHuman(UserAccount newPlayer) {
        Assert.state(PlayerType.HUMAN.equals(playerType), "Cannot join - the playerType is not HUMAN: " + playerType);
        this.humanUserAccount = newPlayer;
    }

    void chooseCivilization(Civilization civilization) {
        this.civilization = civilization;
    }

    public void kick() {
        this.humanUserAccount = null;
    }

    public void leave() {
        this.humanUserAccount = null;
    }

    public void die() {
        if (!PlayerType.DEAD.equals(playerType)) {
            log.info("Player {} for game {} just died");
            this.playerType = PlayerType.DEAD;
        }
    }
}
