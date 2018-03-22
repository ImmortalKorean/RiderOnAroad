package com.project.sangyeop.road_rideronaroad;

/**
 * Created by leesa on 2017-12-04.
 */

public class ChattingRoomlist_Array {

    int roomId;
    String roomTitle = null;
    int roomlistId;
    int friend_index;
    String last_message;
    String last_time;

    public ChattingRoomlist_Array(int roomlistId, int roomId, int friendindex, String friendName, String last_message, String last_time) {

        this.roomlistId = roomlistId;
        this.roomId = roomId;
        this.friend_index = friendindex;
        this.roomTitle = friendName;
        this.last_message = last_message;
        this.last_time = last_time;

    }

}
