package com.jatin.ome.processor;

import java.util.HashMap;
import java.util.Map;

import com.jatin.ome.model.BidAskBook;
import com.jatin.ome.model.Order;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderProcessor {
	Map<String, BidAskBook> orderBooksBySymbol = new HashMap<>();

	public void process(Order order) {
		BidAskBook bidAskBook = orderBooksBySymbol.computeIfAbsent(order.getSymbol(), k -> new BidAskBook());
		bidAskBook.match(order);

		log.info(bidAskBook.getBidAskBook(5).toString());
	}

}
