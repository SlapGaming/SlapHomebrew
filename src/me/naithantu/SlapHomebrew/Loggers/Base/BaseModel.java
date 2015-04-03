package me.naithantu.SlapHomebrew.Loggers.Base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.naithantu.SlapHomebrew.Util.Helpers.FancyLine;
import nl.stoux.SlapPlayers.SQL.Annotations.Column;

/**
 * Created by Stoux on 03/03/2015.
 */
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseModel {

    /** Auto increment value */
    @Column(value = "log_id", autoIncrementID = true)
    @Getter private long logID;

    /** The ID of the user for this action */
    @Column("user_id")
    @Getter @Setter
    private int userID;

    public BaseModel(int userID) {
        this.userID = userID;
    }

}
