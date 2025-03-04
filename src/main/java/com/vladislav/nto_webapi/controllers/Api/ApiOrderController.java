package com.vladislav.nto_webapi.controllers.Api;

import com.vladislav.nto_webapi.Schemes.Requests.OrderInfo;
import com.vladislav.nto_webapi.Schemes.Response.Message;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/order")
public class ApiOrderController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable long orderId) {
        log.info("orderId: {}", orderId);
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
            jdbcTemplate.queryForObject("SELECT id FROM orders WHERE id = ?", Integer.class, orderId);
        } catch (EmptyResultDataAccessException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
