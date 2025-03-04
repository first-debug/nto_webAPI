package com.vladislav.nto_webapi.Schemes.Response;

public record Seats(int eventId, int spaceId, short[][] seats) {
}
