package com.vladislav.nto_webapi.controllers.Api;

import com.vladislav.nto_webapi.Schemes.Requests.OrderInfo;
import com.vladislav.nto_webapi.Schemes.Response.Message;
import com.vladislav.nto_webapi.Services.SeatService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;

@Log4j2
@RestController
@RequestMapping("/api/order")
@AllArgsConstructor
public class ApiOrderController {

    private final JdbcTemplate jdbcTemplate;
    private final SeatService seatService;

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable long orderId) {
        try {
            OrderInfo orderInfo = jdbcTemplate.queryForObject(
                    "SELECT e.title, o.id FROM events as e, orders as o WHERE e.id=o.eventid AND o.id=?",
                    (rs, rowNum) ->
                            new OrderInfo(rs.getString(1), rs.getInt(2)),
                    orderId
            );
            return new ResponseEntity<>(orderInfo, HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(new Message("ID заказа не найден"), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/check/{orderId}")
    public ResponseEntity<?> check(@PathVariable("orderId") String orderId) {
        try {
            Order order = new Order();
            jdbcTemplate.query("SELECT eventId, seats FROM orders WHERE id = ?", resultSet -> {
                order.setEventId(resultSet.getInt("eventId"));
                order.setSeats(seatService.parseOrderSeats(resultSet.getString("seats")));
            }, orderId);
            short[][] eventSeats = seatService.parseSeats(
                    jdbcTemplate.queryForObject("SELECT seats FROM events WHERE id=?",
                    String.class,
                    order.eventId)
            );
            if (eventSeats == null)
                throw new Exception("Field 'seats' is empty!");
            for (short seatNum : order.seats) {
                int row = seatNum / eventSeats[0].length, col = seatNum - row * eventSeats[0].length;
                if (eventSeats[row][col] == 3)
                    throw new Exception("This order already has been checked!");
                eventSeats[row][col] = 3;
            }

            PreparedStatementCreator creator = (connection) -> {
                PreparedStatement statement = connection.prepareStatement("UPDATE events SET seats=? WHERE id=?");
                statement.setString(1, seatService.getSeatsString(eventSeats));
                statement.setInt(2, order.eventId);
                return statement;
            };
            jdbcTemplate.execute(creator, PreparedStatement::execute);

        } catch (Exception ex) {
            log.warn(ex);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Order
    {
        Integer eventId;
        short[] seats;
    }

}
