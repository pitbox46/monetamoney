package github.pitbox46.monetamoney.network;

import github.pitbox46.monetamoney.data.Team;

public class SOpenTeamsPage {
    public final Team team;
    public final Type type;

    public SOpenTeamsPage(Team team, Type type) {
        this.team = team;
        this.type = type;
    }

    public enum Type {
        INDIFFERENT,
        INSAME,
        INNONE
    }
}
