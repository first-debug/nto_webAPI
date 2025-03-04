package com.vladislav.nto_webapi.Services;

public class SeatService {
    public static String getSeatsString(short[][] seats) {
        StringBuilder builder = new StringBuilder();
        for (short[] row : seats) {
            for (short seat : row)
                builder.append(seat);
            builder.append(" ");
        }
        return builder.toString().strip();
    }
}
