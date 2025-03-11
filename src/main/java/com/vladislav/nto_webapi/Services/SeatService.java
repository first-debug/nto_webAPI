package com.vladislav.nto_webapi.Services;


public class SeatService {
    public static String getSeatsString(short[][] seats) {
        if (seats == null)
            return null;
        StringBuilder builder = new StringBuilder();
        for (short[] row : seats) {
            for (short seat : row)
                builder.append(seat);
            builder.append(" ");
        }
        return builder.toString().strip();
    }

    public static short[][] parseSeats(String seats) {
        if (seats == null)
            return null;
        String[] rows = seats.split(" ");
        char[] nums = seats.toCharArray();
        int maxRowLen = 0;
        for (String row : rows)
            if (row.length()> maxRowLen)
                maxRowLen = row.length();
        short[][] result = new short[rows.length][maxRowLen];
        for (int i = 0, j = 0, k = 0; i < nums.length; i++) {
            short num = (short)(nums[i] - 48);
            if (num == -16) {
                j++;
                k = 0;
            } else {
                result[j][k++] = num;
            }
        }
        return result;
    }

    public static String parseRequestSeats(short[][] requestSeats) {
        StringBuilder builder = new StringBuilder();
        for (short[] seat : requestSeats) {
            builder.append(seat[2]).append(" ");
        }
        return builder.toString().strip();
    }

    public static short[] parseOrderSeats(String seats) {
        String[] rows = seats.split(" ");
        short[] result = new short[rows.length];
        for (int i = 0; i < rows.length; i++)
            result[i] = Short.parseShort(rows[i]);
        return result;
    }
}
