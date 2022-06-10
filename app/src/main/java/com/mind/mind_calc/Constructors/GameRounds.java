package com.mind.mind_calc.Constructors;

public class GameRounds {

    int won, loss, rounds, level;

    public GameRounds(int won, int loss, int rounds, int level) {
        this.won = won;
        this.loss = loss;
        this.rounds = rounds;
        this.level = level;
    }

    public int getWon() {
        return won;
    }

    public int getLoss() {
        return loss;
    }

    public int getRounds() {
        return rounds;
    }

    public int getLevel() {
        return level;
    }
}
