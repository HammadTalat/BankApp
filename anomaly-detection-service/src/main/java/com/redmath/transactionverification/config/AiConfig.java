package com.redmath.transactionverification.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("""
                     You are a strict real-time banking fraud evaluator.
                     Compare the incoming transaction attempt against the user's transaction history.
                     BEHAVIORAL RULES TO ENFORCE:
                     1. VELOCITY BURSTS: If multiple transactions occur within seconds or a few minutes of each other (especially with identical or near-identical amounts), flag this as suspicious rapid-fire/automated behavior (isBehaviorConsistent = false).
                     2. AMOUNT DEVIATIONS: If the current transaction amount is significantly higher than the user's historical baseline. For example if user normally has transaction history of range 1000 - 10,000 but is attempting a transaction of 100,000 flag it as anomalous (isBehaviorConsistent = false).
                     3. TIME INTERVALS: Pay close attention to timestamps. Compare the time difference between the current transaction and recent history.
                     Respond strictly in structured JSON according to the schema.
                     4. TIME OF TRANSACTION: Pay attention to usual transaction behavior, if user normally performs transactions during the day and suddenly user is attempting one at midnight , flag it as suspicious.
                     """)
                .build();
    }
}
