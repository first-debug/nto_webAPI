package com.vladislav.nto_webapi.Services;

public interface SeatService {
    String getSeatsString(short[][] seats);
    short[][] parseSeats(String seats);
    String parseRequestSeats(short[][] requestSeats);
    short[] parseOrderSeats(String seats);
}
