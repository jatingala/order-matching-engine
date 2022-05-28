package com.jatin.ome.model;

import static com.jatin.ome.model.Order.OrderStatus.CANCELLED;
import static com.jatin.ome.model.Order.OrderType.LIMIT;
import static com.jatin.ome.model.Order.OrderType.MARKET;
import static com.jatin.ome.model.Order.Side.BUY;
import static com.jatin.ome.model.Order.Side.SELL;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BidAskBookTest {

	private static final String SYMBOL = "SYMBOL";

	private BidAskBook bidAskBook = new BidAskBook();

	@BeforeEach
	public void init() {
		bidAskBook.clearBook();
	}

	// Empty order book, All MARKET orders are CANCELLED
	@Test
	public void matchTest1() {
		Order o = Order.newOrder(BUY, 100l, 99d, LIMIT, SYMBOL);
		bidAskBook.match(o);

		Order o1 = Order.newOrder(BUY, 100l, 99d, MARKET, SYMBOL);
		List<Order> orderStateUpdates = bidAskBook.match(o1);
		Assertions.assertThat(orderStateUpdates.size()).isEqualTo(1);
		Order updatedOrder = orderStateUpdates.get(0);
		Assertions.assertThat(updatedOrder.getOrderStatus()).isEqualTo(CANCELLED);
	}

	// Empty order book, All MARKET orders are CANCELLED
	@Test
	public void matchTest2() {
		Order o = Order.newOrder(SELL, 100l, 99d, LIMIT, SYMBOL);
		bidAskBook.match(o);

		Order o1 = Order.newOrder(SELL, 100l, 99d, MARKET, SYMBOL);
		List<Order> orderStateUpdates = bidAskBook.match(o1);
		Assertions.assertThat(orderStateUpdates.size()).isEqualTo(1);
		Order updatedOrder = orderStateUpdates.get(0);
		Assertions.assertThat(updatedOrder.getOrderStatus()).isEqualTo(CANCELLED);
	}

	@Test
	public void matchTest() {
		Order o1 = Order.newOrder(BUY, 100l, 99d, LIMIT, SYMBOL);
		Order o2 = Order.clone(o1, ob -> ob.createdTime(o1.getCreatedTime() + 1), ob -> ob.qty(50l), ob -> ob.remainingQty(50l));
		Order o3 = Order.clone(o2, ob -> ob.createdTime(o2.getCreatedTime() + 1), ob -> ob.qty(25l), ob -> ob.remainingQty(25l));
		Order o4 = Order.newOrder(BUY, 100l, 98d, LIMIT, SYMBOL);
		Order o5 = Order.clone(o4, ob -> ob.createdTime(o4.getCreatedTime() + 1), ob -> ob.qty(50l), ob -> ob.remainingQty(50l));
		Order o6 = Order.newOrder(BUY, 100l, 97d, LIMIT, SYMBOL);

		Order o7 = Order.newOrder(SELL, 100l, 101d, LIMIT, SYMBOL);
		Order o8 = Order.clone(o7, ob -> ob.createdTime(o7.getCreatedTime() + 1), ob -> ob.qty(50l), ob -> ob.remainingQty(50l));
		Order o9 = Order.clone(o8, ob -> ob.createdTime(o8.getCreatedTime() + 1), ob -> ob.qty(25l), ob -> ob.remainingQty(25l));
		Order o10 = Order.newOrder(SELL, 100l, 102d, LIMIT, SYMBOL);
		Order o11 = Order.clone(o10, ob -> ob.createdTime(o10.getCreatedTime() + 1), ob -> ob.qty(50l), ob -> ob.remainingQty(50l));
		Order o12 = Order.newOrder(SELL, 100l, 103d, LIMIT, SYMBOL);

		List<Order> orders = Arrays.asList(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12);
		orders.forEach(bidAskBook::match);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order marketOrder1 = Order.newOrder(SELL, 200l, 0d, MARKET, SYMBOL);
		List<Order> orderStateUpdates = bidAskBook.match(marketOrder1);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order marketOrder2 = Order.newOrder(BUY, 200l, 0d, MARKET, SYMBOL);
		orderStateUpdates = bidAskBook.match(marketOrder2);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order marketOrder3 = Order.newOrder(SELL, 200l, 0d, MARKET, SYMBOL);
		orderStateUpdates = bidAskBook.match(marketOrder3);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order marketOrder4 = Order.newOrder(BUY, 200l, 0d, MARKET, SYMBOL);
		orderStateUpdates = bidAskBook.match(marketOrder4);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order marketOrder5 = Order.newOrder(SELL, 200l, 0d, MARKET, SYMBOL);
		orderStateUpdates = bidAskBook.match(marketOrder5);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order marketOrder6 = Order.newOrder(BUY, 200l, 0d, MARKET, SYMBOL);
		orderStateUpdates = bidAskBook.match(marketOrder6);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order marketOrder7 = Order.newOrder(SELL, 200l, 0d, MARKET, SYMBOL);
		orderStateUpdates = bidAskBook.match(marketOrder7);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order marketOrder8 = Order.newOrder(BUY, 200l, 0d, MARKET, SYMBOL);
		orderStateUpdates = bidAskBook.match(marketOrder8);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));

		System.out.println("---------------------------LIMIT ORDERS---------------------------");

		orders.forEach(bidAskBook::match);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order limitOrder1 = Order.newOrder(SELL, 200l, 98d, LIMIT, SYMBOL);
		orderStateUpdates = bidAskBook.match(limitOrder1);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order limitOrder2 = Order.newOrder(BUY, 200l, 102d, LIMIT, SYMBOL);
		orderStateUpdates = bidAskBook.match(limitOrder2);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order limitOrder3 = Order.newOrder(SELL, 225l, 98d, LIMIT, SYMBOL);
		orderStateUpdates = bidAskBook.match(limitOrder3);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order limitOrder4 = Order.newOrder(BUY, 225l, 102d, LIMIT, SYMBOL);
		orderStateUpdates = bidAskBook.match(limitOrder4);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));
		
		Order limitOrder5 = Order.newOrder(SELL, 100l, 104d, LIMIT, SYMBOL);
		orderStateUpdates = bidAskBook.match(limitOrder5);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));

		Order limitOrder6 = Order.newOrder(BUY, 100l, 98d, LIMIT, SYMBOL);
		orderStateUpdates = bidAskBook.match(limitOrder6);
		orderStateUpdates.forEach(System.out::println);
		System.out.println(bidAskBook.getBidAskBook(5));
	}

}
