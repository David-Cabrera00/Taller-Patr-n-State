package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import model.BankAccount;
import model.Transaction;
import service.BankService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServer {
    private final int port;
    private final BankService bankService;

    public WebServer(int port, BankService bankService) {
        this.port = port;
        this.bankService = bankService;
    }

    public void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/api/account", this::handleAccountApi);
            server.createContext("/", this::handleStaticFile);

            server.setExecutor(null);
            server.start();

            System.out.println("Servidor iniciado correctamente.");
            System.out.println("Abra el navegador en: http://localhost:" + port);
        } catch (IOException exception) {
            System.out.println("No se pudo iniciar el servidor: " + exception.getMessage());
        }
    }

    private void handleAccountApi(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method) && "/api/account".equals(path)) {
                sendJson(exchange, 200, buildAccountJson("Consulta realizada correctamente."));
                return;
            }

            if (!"POST".equals(method)) {
                sendJson(exchange, 405, buildErrorJson("Método no permitido."));
                return;
            }

            String body = readRequestBody(exchange);
            String message;

            switch (path) {
                case "/api/account/create":
                    String ownerName = getStringValue(body, "ownerName");
                    double initialBalance = getDoubleValue(body, "initialBalance");
                    double overdraftLimit = getDoubleValue(body, "overdraftLimit");
                    message = bankService.createAccount(ownerName, initialBalance, overdraftLimit);
                    break;

                case "/api/account/deposit":
                    message = bankService.deposit(getDoubleValue(body, "amount"));
                    break;

                case "/api/account/withdraw":
                    message = bankService.withdraw(getDoubleValue(body, "amount"));
                    break;

                case "/api/account/transfer":
                    message = bankService.transfer(getDoubleValue(body, "amount"));
                    break;

                case "/api/account/freeze":
                    message = bankService.freeze();
                    break;

                case "/api/account/unfreeze":
                    message = bankService.unfreeze();
                    break;

                case "/api/account/close":
                    message = bankService.close();
                    break;

                default:
                    sendJson(exchange, 404, buildErrorJson("Ruta no encontrada."));
                    return;
            }

            sendJson(exchange, 200, buildAccountJson(message));
        } catch (RuntimeException exception) {
            sendJson(exchange, 400, buildErrorJson(exception.getMessage()));
        }
    }

    private void handleStaticFile(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();

        if ("/".equals(requestPath)) {
            requestPath = "/index.html";
        }

        Path basePath = Path.of("public").toAbsolutePath().normalize();
        Path filePath = basePath.resolve(requestPath.substring(1)).normalize();

        if (!filePath.startsWith(basePath) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendText(exchange, 404, "Archivo no encontrado.", "text/plain");
            return;
        }

        String contentType = getContentType(filePath);
        byte[] fileBytes = Files.readAllBytes(filePath);

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, fileBytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(fileBytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private String getStringValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Falta el campo requerido: " + key);
        }

        return matcher.group(1).trim();
    }

    private double getDoubleValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+(\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Falta el campo requerido: " + key);
        }

        return Double.parseDouble(matcher.group(1));
    }

    private String buildAccountJson(String message) {
        BankAccount account = bankService.getCurrentAccount();
        BankAccount targetAccount = bankService.getTargetAccount();

        StringBuilder builder = new StringBuilder();

        builder.append("{");
        builder.append("\"success\":true,");
        builder.append("\"message\":\"").append(escapeJson(message)).append("\",");

        if (account == null) {
            builder.append("\"account\":null,");
        } else {
            builder.append("\"account\":{");
            builder.append("\"accountNumber\":\"").append(escapeJson(account.getAccountNumber())).append("\",");
            builder.append("\"ownerName\":\"").append(escapeJson(account.getOwnerName())).append("\",");
            builder.append("\"balance\":\"").append(escapeJson(account.getFormattedBalance())).append("\",");
            builder.append("\"overdraftLimit\":\"").append(escapeJson(account.getFormattedOverdraftLimit())).append("\",");
            builder.append("\"stateName\":\"").append(escapeJson(account.getStateName())).append("\",");
            builder.append("\"transactions\":[");

            for (int i = 0; i < account.getTransactions().size(); i++) {
                Transaction transaction = account.getTransactions().get(i);
                builder.append("\"").append(escapeJson(transaction.toDisplayText())).append("\"");

                if (i < account.getTransactions().size() - 1) {
                    builder.append(",");
                }
            }

            builder.append("]");
            builder.append("},");
        }

        builder.append("\"targetAccount\":{");
        builder.append("\"accountNumber\":\"").append(escapeJson(targetAccount.getAccountNumber())).append("\",");
        builder.append("\"ownerName\":\"").append(escapeJson(targetAccount.getOwnerName())).append("\",");
        builder.append("\"balance\":\"").append(escapeJson(targetAccount.getFormattedBalance())).append("\"");
        builder.append("}");

        builder.append("}");

        return builder.toString();
    }

    private String buildErrorJson(String message) {
        return "{"
                + "\"success\":false,"
                + "\"message\":\"" + escapeJson(message) + "\""
                + "}";
    }

    private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        sendText(exchange, statusCode, json, "application/json");
    }

    private void sendText(HttpExchange exchange, int statusCode, String text, String contentType) throws IOException {
        byte[] responseBytes = text.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    private String getContentType(Path filePath) {
        String fileName = filePath.toString().toLowerCase();

        if (fileName.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }

        if (fileName.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }

        if (fileName.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }

        return "application/octet-stream";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}