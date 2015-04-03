package me.naithantu.SlapHomebrew.Loggers.Base;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Created by Stoux on 03/03/2015.
 */
@NoArgsConstructor
public abstract class BaseTimeModel extends BaseModel {

    public BaseTimeModel(int userID) {
        super(userID);
    }

    /**
     * Get the from time
     * @return time
     */
    public abstract long getFrom();

    /**
     * Get the to time
     * @return time
     */
    public abstract long getTo();

}
