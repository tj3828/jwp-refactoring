package kitchenpos.domain;

import kitchenpos.dto.OrderLineItemRequest;
import kitchenpos.dto.OrderRequest;
import kitchenpos.exception.MenuNotFoundException;
import kitchenpos.exception.OrderTableNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderFactory {
    private final MenuRepository menuRepository;
    private final OrderTableRepository orderTableRepository;

    public OrderFactory(MenuRepository menuRepository, OrderTableRepository orderTableRepository) {
        this.menuRepository = menuRepository;
        this.orderTableRepository = orderTableRepository;
    }

    public Order create(final OrderRequest orderRequest) {
        validateOrderLineItems(orderRequest);

        final OrderTable orderTable = orderTableRepository.findById(orderRequest.getOrderTableId())
                .orElseThrow(() -> new OrderTableNotFoundException(orderRequest.getOrderTableId()));

        Order order = orderTable.createOrder();
        addOrderLineItems(orderRequest.getOrderLineItems(), order);

        return order;
    }

    private void addOrderLineItems(final List<OrderLineItemRequest> orderLineItems, final Order order) {
        for (final OrderLineItemRequest orderLineItem : orderLineItems) {
            Menu menu = menuRepository.findById(orderLineItem.getMenuId())
                    .orElseThrow(() -> new MenuNotFoundException(orderLineItem.getMenuId()));
            order.addOrderLineItem(menu, orderLineItem.getQuantity());
        }
    }

    private void validateOrderLineItems(final OrderRequest orderRequest) {
        if (orderRequest.isEmptyOrderLineItems()) {
            throw new IllegalArgumentException("요청한 주문 항목이 없습니다");
        }
    }
}
