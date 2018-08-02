package smp.edgecraft.uhc.core.teams;

import java.util.List;

public enum UHCTeam
{
    BLUE,
    RED,
    YELLOW,
    GREEN,
    PINK;

    private List<UHCPlayer> players;

    public List<UHCPlayer> getPlayers() {
        return this.players;
    }

    public void setPlayers(List<UHCPlayer> players) {
        this.players = players;
    }
}
