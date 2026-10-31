package com.prokey.baccaratio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Represents a player in a baccarat game with capabilities to manage chips and handle game outcomes.
 */
@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    private int chips;

    /**
     * JPA requires a no-arg constructor.
     */
    public Player() {
    }

    /**
     * Constructs a new player with a specified name and initial amount of chips.
     * @param name The name of the player.
     * @param chips The initial number of chips the player has. Must be non-negative.
     */
    public Player(String name, int chips) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty.");
        }
        if (chips < 0) {
            throw new IllegalArgumentException("Initial chips cannot be negative.");
        }
        this.name = name;
        this.chips = chips;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty.");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getChips() {
        return chips;
    }

    public void setChips(int chips) {
        if (chips < 0) {
            throw new IllegalArgumentException("Chips cannot be negative.");
        }
        this.chips = chips;
    }

    /**
     * Increases the player's chip count by the specified amount after a win.
     * @param amount The amount to add to the player's chip count. Must be non-negative.
     */
    public void win(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Win amount cannot be negative.");
        }
        this.chips += amount;
    }

    /**
     * Decreases the player's chip count by the specified amount after a loss.
     * @param amount The amount to subtract from the player's chip count. Must be non-negative.
     * @return true if the player had enough chips to cover the loss, false otherwise.
     */
    public boolean lose(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Loss amount cannot be negative.");
        }
        if (this.chips >= amount) {
            this.chips -= amount;
            return true;
        }
        return false;
    }
}
