package com.vladislav.nto_webapi.controllers.Api;

import com.vladislav.nto_webapi.Schemes.Response.Event;
import com.vladislav.nto_webapi.Schemes.Response.Message;
import com.vladislav.nto_webapi.Schemes.Response.OrderId;
import com.vladislav.nto_webapi.Schemes.Response.Seats;
import com.vladislav.nto_webapi.Services.SeatService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

// TODO Обернуть все методы в try-catch
// TODO Реализовать переход на главную страницу после успешного бронирования

//@CrossOrigin
@Log4j2
@RestController
@RequestMapping("/api/events")
public class EventsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    private final StringBuilder occupiedSeats = new StringBuilder();

    @GetMapping()
    public ResponseEntity<List<Event>> getEvents() {
        try {
            List<Event> answer = new ArrayList<>();
            jdbcTemplate.query("SELECT id, title, description, spaceId, timeToStart FROM events", resultSet -> {
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i += 5) {
                    answer.add(
                            new Event(
                                    resultSet.getInt(i),
                                    resultSet.getString(i + 1),
                                    resultSet.getString(i + 2),
                                    resultSet.getInt(i + 3),
                                    dateFormat.format(resultSet.getDate(i + 4)))
                    );
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
                                    parseSeats(rs.getString("seats"))),
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
            if (response == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            short[][] dbSeats = parseSeats(response);
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
                    preparedStatement.setString(3, " ");
                    return preparedStatement;
            };
            jdbcTemplate.execute(preparedStatementCreator, PreparedStatement::execute);
            jdbcTemplate.update("UPDATE events SET seats=? WHERE id=?", SeatService.getSeatsString(dbSeats), request.eventId());
            return new ResponseEntity<>(new OrderId(orderId), HttpStatus.OK);
//            return new ResponseEntity<>(new Message("Места забронированы"), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }


    private static short[][] parseSeats(String seats) {
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
}
