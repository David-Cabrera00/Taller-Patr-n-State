package app;

import service.BankService;
import web.WebServer;

public class Main {
    public static void main(String[] args) {
        BankService bankService = new BankService();
        WebServer webServer = new WebServer(8080, bankService);
        webServer.start();
    }
}