package com.jatin.ome.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.jatin.ome.model.BidAskBook;
import com.jatin.ome.model.Order;

@Component
public class OrderProcessor {
	private final Map<String, BidAskBook> orderBooksBySymbol = new HashMap<>();

	public void process(Order order) {
		BidAskBook bidAskBook = orderBooksBySymbol.computeIfAbsent(order.getSymbol(), k -> new BidAskBook());
		bidAskBook.match(order);
	}

	public List<List<List<Object>>> getOrderBook(String symbol) {
		BidAskBook bidAskBook = orderBooksBySymbol.computeIfAbsent(symbol, k -> new BidAskBook());
		return bidAskBook.getBidAskBook(5);
	}

}
