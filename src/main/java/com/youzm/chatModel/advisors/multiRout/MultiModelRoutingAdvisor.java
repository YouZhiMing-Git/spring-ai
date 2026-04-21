package com.youzm.chatModel.advisors.multiRout;

import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;


@Component
public class MultiModelRoutingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private final String ADVISOR_NAME = "MultiModelRoutingAdvisor";
    // 优先级必须高于执行层
//    private static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;
    private static final int ORDER = Ordered.LOWEST_PRECEDENCE - 10;
    /**
     * 模型注册表：Bean名称 -> ChatModel实例的映射
     */
    Map<String, ChatModel> chatModelRegistry;

    private final ModelRouter modelRouter;

    private static final ThreadLocal<ModelRoute> CURRENT_ROUTE = new ThreadLocal<>();


    public MultiModelRoutingAdvisor(Map<String, ChatModel> chatModelRegistry) {
        this.chatModelRegistry = chatModelRegistry;
        this.modelRouter = new ModelRouter();
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        try {
            //1 获取用户查询
            String userQuery = extractUserQuery(advisedRequest);

            ModelRoute route = modelRouter.route(userQuery);

            String selectedModelName = route.modelName();
            ChatModel selectedModel = chatModelRegistry.get(selectedModelName);
            CURRENT_ROUTE.set(route);
            if (selectedModel == null) {
                return chain.nextAroundCall(advisedRequest);
            }
            ChatResponse chatResponse = selectedModel.call(advisedRequest.toPrompt());
            return AdvisedResponse.builder()
                    .response(chatResponse)
                    .adviseContext(advisedRequest.adviseContext())
                    .build();

        } finally {
            CURRENT_ROUTE.remove();
        }
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        //1 获取用户查询
        String userQuery = extractUserQuery(advisedRequest);

        ModelRoute route = modelRouter.route(userQuery);

        String selectedModelName = route.modelName();
        ChatModel selectedModel = chatModelRegistry.get(selectedModelName);

        if (selectedModel == null) {
            return chain.nextAroundStream(advisedRequest);
        }
        Flux<ChatResponse> responseFlux = selectedModel.stream(advisedRequest.toPrompt());
        return responseFlux.map(response -> AdvisedResponse.builder()
                .response(response)
                .adviseContext(advisedRequest.adviseContext())
                .build());
    }

    @Override
    public String getName() {
        return ADVISOR_NAME;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }


    public String extractUserQuery(AdvisedRequest advisedRequest) {
        List<Message> messages = advisedRequest.toPrompt().getInstructions();
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        return messages.stream()
                .filter(msg -> msg != null && msg.getMessageType() == MessageType.USER)
                .map(Message::getText)
                .filter(text -> text != null && !text.trim().isEmpty())
                .findFirst()
                .orElse("");
    }
}
