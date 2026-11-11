package ek.osnb.demo.coffeeshop.assistant;

import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
class AssistantController {

    private final ChatClient chatClient;

    AssistantController(ChatClient.Builder builder) {
        this.chatClient=builder
                .build();
    }

    @PostMapping("/chat")
    ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String answer = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
        return new ChatResponse(answer);
    }
}
