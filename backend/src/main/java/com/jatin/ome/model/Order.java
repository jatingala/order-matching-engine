package com.jatin.ome.model;

import java.util.UUID;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonCreator;

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
	private final Long updatedTime;
	private final String orderId;

	@JsonCreator
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
		builder.updatedTime(System.nanoTime());
		builder.orderId(UUID.randomUUID().toString());

		return builder.build();
	}

	@SafeVarargs
	public static Order clone(Order o, Consumer<OrderBuilder>... mutations) {
		OrderBuilder builder = Order.builder();
		builder.side(o.getSide());
		builder.qty(o.getQty());
		builder.remainingQty(o.getRemainingQty());
		builder.price(o.getPrice());
		builder.avgFillPrice(o.getAvgFillPrice());
		builder.orderType(o.getOrderType());
		builder.orderStatus(o.getOrderStatus());
		builder.symbol(o.getSymbol());
		builder.createdTime(o.getCreatedTime());
		builder.updatedTime(o.getUpdatedTime());
		builder.orderId(o.getOrderId());

		for (Consumer<OrderBuilder> consumer : mutations) {
			consumer.accept(builder);
		}

		return builder.build();
	}

	public static Order cancelOrder(Order o) {
		OrderStatus status = (o.getQty() - o.getRemainingQty()) == 0 ? OrderStatus.CANCELLED : OrderStatus.PARTIALLY_CANCELLED;
		return clone(o, ob -> ob.orderStatus(status), ob -> ob.updatedTime(System.nanoTime()));
	}

	public static Order fill(Order o, Long qty, Double price) {
		Long remainingQty = qty >= o.getRemainingQty() ? 0 : (o.getRemainingQty() - qty);
		OrderStatus status = qty >= o.getRemainingQty() ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;

		Long qtyFilledSoFar = o.getQty() - o.getRemainingQty();
		Long qtyBeingFilledNow = remainingQty == 0 ? o.getRemainingQty() : qty;
		Double avgFillPrice = ((o.getAvgFillPrice() * qtyFilledSoFar) + (qtyBeingFilledNow * price)) / (qtyFilledSoFar + qtyBeingFilledNow);

		return clone(o, ob -> ob.orderStatus(status), ob -> ob.remainingQty(remainingQty), ob -> ob.avgFillPrice(avgFillPrice), ob -> ob.updatedTime(System.nanoTime()));
	}

}
