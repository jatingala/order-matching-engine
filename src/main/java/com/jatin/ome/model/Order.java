package com.jatin.ome.model;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Order {

	public static enum Side {
		BUY, SELL
	};

	public static enum OrderType {
		LIMIT, MARKET
	};

	public static enum OrderStatus {
		OPEN, FILLED, PARTIALLY_FILLED, CANCELLED, PARTIALLY_CANCELLED
	};

	private final Side side;
	private final Long qty;
	private final Long remainingQty;
	private final Double price;
	private final Double avgFillPrice;
	private final OrderType orderType;
	private final OrderStatus orderStatus;
	private final String symbol;
	private final Long createdTime;

	public static Order newOrder(Side side, Long qty, Double price, OrderType orderType, String symbol) {
		OrderBuilder builder = Order.builder();
		builder.side(side);
		builder.qty(qty);
		builder.remainingQty(qty);
		builder.price(price);
		builder.avgFillPrice(0d);
		builder.orderType(orderType);
		builder.orderStatus(OrderStatus.OPEN);
		builder.symbol(symbol);
		builder.createdTime(System.nanoTime());

		return builder.build();
	}

	@SafeVarargs
	public static Order clone(Order o, Consumer<OrderBuilder>... mutations) {
		OrderBuilder builder = Order.builder();
		builder.side(o.side);
		builder.qty(o.qty);
		builder.remainingQty(o.remainingQty);
		builder.price(o.price);
		builder.avgFillPrice(o.avgFillPrice);
		builder.orderType(o.orderType);
		builder.orderStatus(o.orderStatus);
		builder.symbol(o.symbol);
		builder.createdTime(o.createdTime);

		for (Consumer<OrderBuilder> consumer : mutations) {
			consumer.accept(builder);
		}

		return builder.build();
	}

	public static Order cancelOrder(Order o) {
		OrderStatus status = (o.getQty() - o.getRemainingQty()) == 0 ? OrderStatus.CANCELLED : OrderStatus.PARTIALLY_CANCELLED;
		return clone(o, ob -> ob.orderStatus(status));
	}

	public static Order fill(Order o, Long qty, Double price) {
		Long remainingQty = qty >= o.getRemainingQty() ? 0 : (o.getRemainingQty() - qty);
		OrderStatus status = qty >= o.getRemainingQty() ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
		return clone(o, ob -> ob.orderStatus(status), ob -> ob.remainingQty(remainingQty));
	}

}
