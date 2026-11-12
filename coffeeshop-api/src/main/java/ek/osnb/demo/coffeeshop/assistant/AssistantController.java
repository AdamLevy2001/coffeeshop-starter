package ek.osnb.demo.coffeeshop.assistant;

import ek.osnb.demo.coffeeshop.customer.CustomerApi;
import ek.osnb.demo.coffeeshop.customer.CustomerInfo;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/assistant")
class AssistantController {

    private final ChatClient chatClient;
    private final OrderTools orderTools;
    private  final CustomerApi customerApi;
    private final StaffTools staffTools;

    AssistantController(ChatClient.Builder builder, ChatMemory chatMemory, CatalogTools catalogTools,
                        OrderTools orderTools, CustomerApi customerApi, StaffTools staffTools) {
        this.chatClient=builder
                .defaultTools(catalogTools)
                .defaultSystem("You are an assistant for a coffeeshop, you answer is short, you speak the same languange as the customer and if you dont know the question just say dont know, instead of invent products, prices and orders and also always use tools")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.orderTools=orderTools;
        this.customerApi=customerApi;
        this.staffTools=staffTools;
    }

    @PostMapping("/chat")
    ChatResponse chat(@Valid @RequestBody ChatRequest request, Authentication auth) {
        Optional<CustomerInfo> find = customerApi.findCustomerByEmail(auth.getName());
        ChatClient.ChatClientRequestSpec call = chatClient.prompt()
                .user(request.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, auth.getName()));
        if (find.isPresent()) {
            call.tools(orderTools).toolContext(Map.of("customerId", find.get().id()));
        }
        boolean isStaff = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .anyMatch(p -> p.equals("ROLE_STAFF"));
        if (isStaff) {
            call.tools(staffTools);
        }
           String answer = call
                   .call()
                   .content();
        return new ChatResponse(answer);
    }
}
