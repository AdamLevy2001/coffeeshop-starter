package ek.osnb.demo.coffeeshop.assistant;

import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
class AssistantController {

    private final ChatClient chatClient;

    AssistantController(ChatClient.Builder builder, ChatMemory chatMemory, CatalogTools catalogTools) {
        this.chatClient=builder
                .defaultTools(catalogTools)
                .defaultSystem("You are an assistant for a coffeeshop, you answer is short, you speak the same languange as the customer and if you dont know the question just say dont know, instead of invent products, prices and orders and also always use tools")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @PostMapping("/chat")
    ChatResponse chat(@Valid @RequestBody ChatRequest request, Authentication auth) {
        String answer = chatClient.prompt()
                .user(request.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, auth.getName()))
                .call()
                .content();
        return new ChatResponse(answer);
    }
}
