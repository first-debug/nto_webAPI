package com.vladislav.nto_webapi.controllers.Api;

import com.vladislav.nto_webapi.Schemes.Response.Event;
import com.vladislav.nto_webapi.Schemes.Response.Message;
import com.vladislav.nto_webapi.Schemes.Response.OrderId;
import com.vladislav.nto_webapi.Schemes.Response.Seats;
import com.vladislav.nto_webapi.Services.SeatService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

//@CrossOrigin
@Log4j2
@RestController
@RequestMapping("/api/events")
@AllArgsConstructor
public class ApiEventsController {

    private final JdbcTemplate jdbcTemplate;
    private final SeatService seatService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    private final StringBuilder occupiedSeats = new StringBuilder();

    @GetMapping()
    public ResponseEntity<List<Event>> getEvents() {
        try {
            List<Event> answer = jdbcTemplate.query("SELECT id, title, description, spaceId, timeToStart FROM events",
             new RowMapper<Event>() {
                @Override
                public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                    return new Event(
                                    resultSet.getInt(1),
                                    resultSet.getString(2),
                                    resultSet.getString(3),
                                    resultSet.getInt(4),
                                    dateFormat.format(resultSet.getDate(5)));
                    
                }
            });
            return new ResponseEntity<>(answer, HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEvent(@PathVariable String eventId) {
        try {
            Event response = jdbcTemplate.queryForObject(
                    "SELECT id, title, description, spaceId, timeToStart FROM events WHERE id = ?",
                    (rs, rowNum) ->
                            new Event(
                                    rs.getInt("id"),
                                    rs.getString("title"),
                                    rs.getString("description"),
                                    rs.getInt("spaceId"),
                                    dateFormat.format(rs.getDate("timeToStart"))
                            ), eventId);
            return response != null ?
                    new ResponseEntity<>(response, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{eventId}/seats")
    public ResponseEntity<Seats> getSeats(@PathVariable String eventId) {
        try {
            Seats seats = jdbcTemplate.queryForObject(
                    "SELECT id, spaceId, seats FROM events WHERE id = ?",
                    (rs, rowNum) ->
                            new Seats(
                                    rs.getInt("id"),
                                    rs.getInt("spaceId"),
                                    seatService.parseSeats(rs.getString("seats"))),
                    eventId);
            return seats != null ?
                    new ResponseEntity<>(seats,
                            HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (EmptyResultDataAccessException e) {
            log.error(e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookSeats(@RequestBody com.vladislav.nto_webapi.Schemes.Requests.Seats request) {
        try {
            String response = jdbcTemplate.queryForObject("SELECT seats FROM events WHERE id = ?", String.class, request.eventId());
            if (response == null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            short[][] dbSeats = seatService.parseSeats(response);
            short[][] requestSeats = request.seats();
            occupiedSeats.delete(0, occupiedSeats.length());

            for (short[] requestSeat : requestSeats)
                if (dbSeats[requestSeat[0]][requestSeat[1]] == 2)
                    occupiedSeats.append('(').append(requestSeat[0]).append(", ").append(requestSeat[1]).append(')');
                else
                    dbSeats[requestSeat[0]][requestSeat[1]] = 2;
            if (!occupiedSeats.isEmpty()) {
                return new ResponseEntity<>(new Message("Место(-а) " + occupiedSeats + " уже занято(-ы)!"), HttpStatus.NOT_FOUND);
            }
            long orderId = Arrays.deepHashCode(requestSeats) + System.currentTimeMillis();
            PreparedStatementCreator preparedStatementCreator = (con) -> {
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO orders VALUES (?, ?, ?)");
                    preparedStatement.setLong(1, orderId);
                    preparedStatement.setInt(2, request.eventId());
                    preparedStatement.setString(3, seatService.parseRequestSeats(requestSeats));
                    return preparedStatement;
            };
            jdbcTemplate.execute(preparedStatementCreator, PreparedStatement::execute);
            jdbcTemplate.update("UPDATE events SET seats=? WHERE id=?", seatService.getSeatsString(dbSeats), request.eventId());
            return new ResponseEntity<>(new OrderId(orderId), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
}
