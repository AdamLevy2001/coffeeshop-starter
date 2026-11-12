package ek.osnb.demo.coffeeshop.assistant;

import ek.osnb.demo.coffeeshop.order.OrderApi;
import ek.osnb.demo.coffeeshop.order.OrderSummaryInfo;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class OrderTools {

    private final OrderApi orderApi;

    OrderTools(OrderApi orderApi) {
        this.orderApi=orderApi;
    }

    @Tool(description = "Show customer orders with own login")
    List<OrderSummaryInfo> myOrders(ToolContext toolContext) {
        Object object = toolContext.getContext().get("customerId");
        Long id = (Long) object;
        return orderApi.findOrders(id);
    }
}
