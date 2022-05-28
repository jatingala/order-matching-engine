package com.jatin.ome.model;

import static com.jatin.ome.model.Order.OrderStatus.CANCELLED;
import static com.jatin.ome.model.Order.OrderStatus.PARTIALLY_CANCELLED;
import static com.jatin.ome.model.Order.OrderStatus.PARTIALLY_FILLED;
import static com.jatin.ome.model.Order.OrderStatus.FILLED;
import static com.jatin.ome.model.Order.OrderType.LIMIT;
import static com.jatin.ome.model.Order.OrderType.MARKET;
import static com.jatin.ome.model.Order.Side.BUY;
import static com.jatin.ome.model.Order.Side.SELL;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class OrderTest {

	private static final String SYMBOL = "SYMBOL";

	@Test
	public void cancelOrderTest() {
		Order order1 = Order.newOrder(BUY, 100l, 200d, MARKET, SYMBOL);
		Order cancelledOrder1 = Order.cancelOrder(order1);
		Assertions.assertThat(cancelledOrder1.getOrderStatus()).isEqualTo(CANCELLED);

		Order order2 = Order.fill(Order.newOrder(SELL, 100l, 200d, LIMIT, SYMBOL), 50l, 200d);
		Order cancelledOrder2 = Order.cancelOrder(order2);
		Assertions.assertThat(cancelledOrder2.getOrderStatus()).isEqualTo(PARTIALLY_CANCELLED);
	}

	@Test
	public void fillTest() {
		Order order = Order.newOrder(BUY, 100l, 200d, MARKET, SYMBOL);

		Order filledOrder1 = Order.fill(order, 100l, 200d);
		Assertions.assertThat(filledOrder1.getOrderStatus()).isEqualTo(FILLED);
		Assertions.assertThat(filledOrder1.getRemainingQty()).isEqualTo(0l);
		Assertions.assertThat(filledOrder1.getAvgFillPrice()).isEqualTo(200d);

		Order filledOrder2 = Order.fill(order, 25l, 200d);
		Assertions.assertThat(filledOrder2.getOrderStatus()).isEqualTo(PARTIALLY_FILLED);
		Assertions.assertThat(filledOrder2.getRemainingQty()).isEqualTo(75l);
		Assertions.assertThat(filledOrder2.getAvgFillPrice()).isEqualTo(200d);

		Order filledOrder3 = Order.fill(filledOrder2, 25l, 210d);
		Assertions.assertThat(filledOrder3.getOrderStatus()).isEqualTo(PARTIALLY_FILLED);
		Assertions.assertThat(filledOrder3.getRemainingQty()).isEqualTo(50l);
		Assertions.assertThat(filledOrder3.getAvgFillPrice()).isEqualTo(205d);

		Order filledOrder4 = Order.fill(filledOrder3, 100l, 215d);
		Assertions.assertThat(filledOrder4.getOrderStatus()).isEqualTo(FILLED);
		Assertions.assertThat(filledOrder4.getRemainingQty()).isEqualTo(0l);
		Assertions.assertThat(filledOrder4.getAvgFillPrice()).isEqualTo(210d);

		Order filledOrder5 = Order.fill(order, 200l, 225d);
		Assertions.assertThat(filledOrder5.getOrderStatus()).isEqualTo(FILLED);
		Assertions.assertThat(filledOrder5.getRemainingQty()).isEqualTo(0l);
		Assertions.assertThat(filledOrder5.getAvgFillPrice()).isEqualTo(225d);
	}

}
