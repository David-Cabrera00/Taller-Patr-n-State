package model;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Transaction {
    private final LocalDateTime dateTime;
    private final String type;
    private final double amount;
    private final String description;

    public Transaction(String type, double amount, String description) {
        this.dateTime = LocalDateTime.now();
        this.type = type;
        this.amount = amount;
        this.description = description;
    }

    public String toDisplayText() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        return dateTime.format(dateFormatter)
                + " | "
                + type
                + " | "
                + moneyFormatter.format(amount)
                + " | "
                + description;
    }
}