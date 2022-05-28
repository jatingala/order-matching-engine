package com.jatin.ome.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

public class BidAskBook {

	private static final Comparator<Double> BID_PRICE_COMPARATOR = (d1, d2) -> d2.compareTo(d1);
	private static final Comparator<Double> ASK_PRICE_COMPARATOR = (d1, d2) -> d1.compareTo(d2);
	private static final Function<Double, NavigableMap<Long, Order>> MAPPING_FUNCTION = k -> new TreeMap<>();

	private final NavigableMap<Double, NavigableMap<Long, Order>> bids = new TreeMap<>(BID_PRICE_COMPARATOR); // buyers
	private final NavigableMap<Double, NavigableMap<Long, Order>> asks = new TreeMap<>(ASK_PRICE_COMPARATOR); // sellers

	public void clearBook() {
		bids.clear();
		asks.clear();
	}

	public List<Order> match(Order order) {
		List<Order> orderStateUpdates = new ArrayList<>();
		orderStateUpdates.addAll(marketOrder(order));
		orderStateUpdates.addAll(limitBuyOrder(order));
		orderStateUpdates.addAll(limitSellOrder(order));
		return orderStateUpdates;
	}

	private List<Order> marketOrder(Order order) {
		List<Order> orderStateUpdates = new ArrayList<>();

		if (order.getOrderType() == Order.OrderType.MARKET) {
			while (order.getRemainingQty() > 0) {
				Entry<Double, NavigableMap<Long, Order>> firstEntry = (order.getSide() == Order.Side.BUY) ? asks.firstEntry() : bids.firstEntry();

				if (firstEntry == null) {
					orderStateUpdates.add(Order.cancelOrder(order));
					break;
				} else {
					Entry<Long, Order> firstEntryFirstOrderEntry = firstEntry.getValue().firstEntry();

					Order orderBookOrder = firstEntryFirstOrderEntry.getValue();
					Long remainingQty = orderBookOrder.getRemainingQty();

					orderBookOrder = Order.fill(orderBookOrder, order.getRemainingQty(), orderBookOrder.getPrice());
					order = Order.fill(order, remainingQty, orderBookOrder.getPrice());

					orderStateUpdates.add(orderBookOrder);
					orderStateUpdates.add(order);

					if (orderBookOrder.getRemainingQty() == 0) {
						firstEntry.getValue().remove(firstEntryFirstOrderEntry.getKey());
					} else {
						firstEntry.getValue().put(firstEntryFirstOrderEntry.getKey(), orderBookOrder);
					}

					if (order.getSide() == Order.Side.BUY) {
						cleanEmptyAsks();
					} else {
						cleanEmptyBids();
					}
				}
			}
		}

		return orderStateUpdates;
	}

	private List<Order> limitBuyOrder(Order order) {
		List<Order> orderStateUpdates = new ArrayList<>();

		if (order.getOrderType() == Order.OrderType.LIMIT && order.getSide() == Order.Side.BUY) {
			while (order.getRemainingQty() > 0) {
				Entry<Double, NavigableMap<Long, Order>> firstEntry = asks.firstEntry();

				if (firstEntry != null && order.getPrice() >= firstEntry.getKey()) {
					Entry<Long, Order> firstEntryFirstOrderEntry = firstEntry.getValue().firstEntry();
					Order orderBookOrder = firstEntryFirstOrderEntry.getValue();
					Long remainingQty = orderBookOrder.getRemainingQty();

					orderBookOrder = Order.fill(orderBookOrder, order.getRemainingQty(), order.getPrice());
					order = Order.fill(order, remainingQty, orderBookOrder.getPrice());

					orderStateUpdates.add(orderBookOrder);
					orderStateUpdates.add(order);

					if (orderBookOrder.getRemainingQty() == 0) {
						firstEntry.getValue().remove(firstEntryFirstOrderEntry.getKey());
					} else {
						firstEntry.getValue().put(firstEntryFirstOrderEntry.getKey(), orderBookOrder);
					}

					cleanEmptyAsks();
				} else {
					bids.computeIfAbsent(order.getPrice(), MAPPING_FUNCTION).put(order.getCreatedTime(), order);
					break;
				}
			}
		}

		return orderStateUpdates;
	}

	private List<Order> limitSellOrder(Order order) {
		List<Order> orderStateUpdates = new ArrayList<>();

		if (order.getOrderType() == Order.OrderType.LIMIT && order.getSide() == Order.Side.SELL) {
			while (order.getRemainingQty() > 0) {
				Entry<Double, NavigableMap<Long, Order>> firstEntry = bids.firstEntry();

				if (firstEntry != null && order.getPrice() <= firstEntry.getKey()) {
					Entry<Long, Order> firstEntryFirstOrderEntry = firstEntry.getValue().firstEntry();
					Order orderBookOrder = firstEntryFirstOrderEntry.getValue();
					Long remainingQty = orderBookOrder.getRemainingQty();

					orderBookOrder = Order.fill(orderBookOrder, order.getRemainingQty(), order.getPrice());
					order = Order.fill(order, remainingQty, orderBookOrder.getPrice());

					orderStateUpdates.add(orderBookOrder);
					orderStateUpdates.add(order);

					if (orderBookOrder.getRemainingQty() == 0) {
						firstEntry.getValue().remove(firstEntryFirstOrderEntry.getKey());
					} else {
						firstEntry.getValue().put(firstEntryFirstOrderEntry.getKey(), orderBookOrder);
					}

					cleanEmptyBids();
				} else {
					asks.computeIfAbsent(order.getPrice(), MAPPING_FUNCTION).put(order.getCreatedTime(), order);
					break;
				}
			}
		}

		return orderStateUpdates;
	}

	// -----------------------------------------------------------------------------------------------------------

	private void cleanEmptyAsks() {
		cleanEmpty(asks);
	}

	private void cleanEmptyBids() {
		cleanEmpty(bids);
	}

	private void cleanEmpty(NavigableMap<Double, NavigableMap<Long, Order>> map) {
		Entry<Double, NavigableMap<Long, Order>> firstEntry = map.firstEntry();
		while (firstEntry != null && firstEntry.getValue().size() == 0) {
			map.remove(firstEntry.getKey());
			firstEntry = map.firstEntry();
		}
	}

	// -----------------------------------------------------------------------------------------------------------

	public List<List<List<Object>>> getBidAskBook(int depth) {
		List<List<Object>> _bids = aggregate(bids);
		List<List<Object>> _asks = aggregate(asks);

		_bids = _bids.subList(0, Math.min(_bids.size(), depth));
		_asks = _asks.subList(0, Math.min(_asks.size(), depth));

		return Arrays.asList(_bids, _asks);
	}

	private List<List<Object>> aggregate(NavigableMap<Double, NavigableMap<Long, Order>> map) {
		List<List<Object>> list = new ArrayList<>();
		for (Entry<Double, NavigableMap<Long, Order>> entry : map.entrySet()) {
			List<Object> objects = new ArrayList<>(2);
			objects.add(entry.getKey());
			objects.add(sumOfQuantities(entry.getValue()));
			list.add(objects);
		}
		return list;
	}

	private Long sumOfQuantities(NavigableMap<Long, Order> map) {
		Long sum = 0l;
		for (Order o : map.values()) {
			sum += o.getRemainingQty();
		}
		return sum;
	}

}
