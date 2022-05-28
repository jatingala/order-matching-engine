package com.jatin.ome.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jatin.ome.model.Order;
import com.jatin.ome.processor.OrderProcessor;

@RestController
public class OrderController {

	private final OrderProcessor orderProcessor;

	@Autowired
	public OrderController(OrderProcessor orderProcessor) {
		this.orderProcessor = orderProcessor;
	}

	@PostMapping("/order")
	public void submitOrder(@RequestBody Order order) {
		orderProcessor.process(order);
	}

	@GetMapping("/orderbook/${symbol}")
	public List<List<List<Object>>> getOrderBook(@PathVariable("symbol") String symbol) {
		return orderProcessor.getOrderBook(symbol);
	}

}
