package com.jatin.ome.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	public void match(Order order) {
		marketOrder(order);
		limitBuyOrder(order);
		limitSellOrder(order);
	}

	private void marketOrder(Order order) {
		if (order.getOrderType() == Order.OrderType.MARKET) {
			while (order.getRemainingQty() > 0) {
				Entry<Double, NavigableMap<Long, Order>> firstEntry = (order.getSide() == Order.Side.BUY) ? asks.firstEntry() : bids.firstEntry();

				if (firstEntry == null) {
					order = Order.cancelOrder(order);
					log.info(order.toString());
					break;
				} else {
					Entry<Long, Order> firstEntryFirstOrderEntry = firstEntry.getValue().firstEntry();

					Order orderBookOrder = firstEntryFirstOrderEntry.getValue();
					Long remainingQty = orderBookOrder.getRemainingQty();

					orderBookOrder = Order.fill(orderBookOrder, order.getRemainingQty(), order.getPrice());
					order = Order.fill(order, remainingQty, orderBookOrder.getPrice());
					log.info(orderBookOrder.toString());
					log.info(order.toString());

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
	}

	private void limitBuyOrder(Order order) {
		if (order.getOrderType() == Order.OrderType.LIMIT && order.getSide() == Order.Side.BUY) {
			while (order.getRemainingQty() > 0) {
				Entry<Double, NavigableMap<Long, Order>> firstEntry = asks.firstEntry();

				if (firstEntry != null && order.getPrice() >= firstEntry.getKey()) {
					Entry<Long, Order> firstEntryFirstOrderEntry = firstEntry.getValue().firstEntry();
					Order orderBookOrder = firstEntryFirstOrderEntry.getValue();
					Long remainingQty = orderBookOrder.getRemainingQty();

					orderBookOrder = Order.fill(orderBookOrder, order.getRemainingQty(), order.getPrice());
					order = Order.fill(order, remainingQty, orderBookOrder.getPrice());
					log.info(orderBookOrder.toString());
					log.info(order.toString());

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
				} else {
					bids.computeIfAbsent(order.getPrice(), MAPPING_FUNCTION).put(order.getCreatedTime(), order);
					break;
				}
			}
		}
	}

	private void limitSellOrder(Order order) {
		if (order.getOrderType() == Order.OrderType.LIMIT && order.getSide() == Order.Side.SELL) {
			while (order.getRemainingQty() > 0) {
				Entry<Double, NavigableMap<Long, Order>> firstEntry = bids.firstEntry();

				if (firstEntry != null && order.getPrice() <= firstEntry.getKey()) {
					Entry<Long, Order> firstEntryFirstOrderEntry = firstEntry.getValue().firstEntry();
					Order orderBookOrder = firstEntryFirstOrderEntry.getValue();
					Long remainingQty = orderBookOrder.getRemainingQty();

					orderBookOrder = Order.fill(orderBookOrder, order.getRemainingQty(), order.getPrice());
					order = Order.fill(order, remainingQty, orderBookOrder.getPrice());
					log.info(orderBookOrder.toString());
					log.info(order.toString());

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
				} else {
					asks.computeIfAbsent(order.getPrice(), MAPPING_FUNCTION).put(order.getCreatedTime(), order);
					break;
				}
			}
		}
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
