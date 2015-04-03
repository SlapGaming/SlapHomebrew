package me.naithantu.SlapHomebrew.Loggers.AFK;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.naithantu.SlapHomebrew.Loggers.Base.BaseModel;
import me.naithantu.SlapHomebrew.Loggers.Base.BaseTimeModel;
import mkremins.fanciful.FancyMessage;
import nl.stoux.SlapPlayers.SQL.Annotations.Column;
import nl.stoux.SlapPlayers.SQL.Annotations.Table;

@Table("sh_logger_afk")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class AFKModel extends BaseTimeModel {

    @Column("went_afk")
    private long wentAFK;

    @Column("left_afk")
    private long leftAFK;

    @Column
    private String reason;

    public AFKModel(int userID, String reason) {
        super(userID);
        this.wentAFK = System.currentTimeMillis();
        this.reason = reason;
    }

    @Override
    public long getFrom() {
        return wentAFK;
    }

    @Override
    public long getTo() {
        return leftAFK;
    }

}
