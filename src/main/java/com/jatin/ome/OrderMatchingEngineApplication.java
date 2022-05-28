package com.jatin.ome;

import java.util.concurrent.TimeUnit;

import com.jatin.ome.model.Order;
import com.jatin.ome.processor.OrderProcessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderMatchingEngineApplication {

	public static void main(String[] args) throws InterruptedException {
		OrderProcessor processor = new OrderProcessor();
		for (int i = 0; i < 100; i++) {
			Order order = Order.newOrder(getSide(), getQty(), getPrice(), getOrderType(), "SYMBOL");
			log.info("Process order " + order.toString());
			processor.process(order);

			TimeUnit.SECONDS.sleep(5);
		}
	}

	private static Order.Side getSide() {
		return Math.random() > 0.5 ? Order.Side.BUY : Order.Side.SELL;
	}

	private static Order.OrderType getOrderType() {
		return Math.random() > 0.5 ? Order.OrderType.LIMIT : Order.OrderType.MARKET;
	}

	private static Double getPrice() {
		return 1d + (long) (Math.random() * 10);
	}

	private static Long getQty() {
		return 1l + (long) (Math.random() * 10);
	}

}
