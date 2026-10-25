package com.example.ecommerce;

import java.net.InetSocketAddress;
import java.net.Socket;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 execution condition that checks if a real PostgreSQL server is reachable.
 * Allows live database integration tests to run when PostgreSQL is available,
 * and skips cleanly when offline per AGENTS.md guidelines.
 */
public class PostgresAvailableCondition implements ExecutionCondition {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5433;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        String host = System.getenv("POSTGRES_TEST_HOST");
        if (host == null || host.isBlank()) {
            host = System.getProperty("POSTGRES_TEST_HOST", DEFAULT_HOST);
        }

        String portStr = System.getenv("POSTGRES_TEST_PORT");
        if (portStr == null || portStr.isBlank()) {
            portStr = System.getProperty("POSTGRES_TEST_PORT", String.valueOf(DEFAULT_PORT));
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            port = DEFAULT_PORT;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1000);
            return ConditionEvaluationResult.enabled("PostgreSQL is reachable at " + host + ":" + port);
        } catch (Exception e) {
            return ConditionEvaluationResult.disabled("PostgreSQL is not reachable at " + host + ":" + port
                    + " (" + e.getMessage() + "). Skipping live PostgreSQL tests per AGENTS.md.");
        }
    }
}
