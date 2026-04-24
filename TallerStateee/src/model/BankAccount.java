package model;

import state.AccountState;
import state.ActiveState;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BankAccount {
    private final String accountNumber;
    private final String ownerName;
    private final double overdraftLimit;
    private double balance;
    private AccountState currentState;
    private final List<Transaction> transactions;

    public BankAccount(String accountNumber, String ownerName, double initialBalance, double overdraftLimit) {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del titular es obligatorio.");
        }

        if (initialBalance < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo.");
        }

        if (overdraftLimit < 0) {
            throw new IllegalArgumentException("El límite de sobregiro no puede ser negativo.");
        }

        this.accountNumber = accountNumber;
        this.ownerName = ownerName.trim();
        this.balance = initialBalance;
        this.overdraftLimit = overdraftLimit;
        this.currentState = new ActiveState();
        this.transactions = new ArrayList<>();

        registerTransaction("APERTURA", initialBalance, "Cuenta creada correctamente.");
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getBalance() {
        return balance;
    }

    public String getStateName() {
        return currentState.getName();
    }

    public String deposit(double amount) {
        validateAmount(amount);
        return currentState.deposit(this, amount);
    }

    public String withdraw(double amount) {
        validateAmount(amount);
        return currentState.withdraw(this, amount);
    }

    public String transferTo(BankAccount targetAccount, double amount) {
        validateAmount(amount);

        if (targetAccount == null) {
            throw new IllegalArgumentException("La cuenta destino no puede ser nula.");
        }

        return currentState.transferTo(this, targetAccount, amount);
    }

    public String receiveTransferFrom(BankAccount sourceAccount, double amount) {
        validateAmount(amount);

        if (sourceAccount == null) {
            throw new IllegalArgumentException("La cuenta origen no puede ser nula.");
        }

        return currentState.receiveTransfer(this, sourceAccount, amount);
    }

    public String freeze() {
        return currentState.freeze(this);
    }

    public String unfreeze() {
        return currentState.unfreeze(this);
    }

    public String close() {
        return currentState.close(this);
    }

    public void changeState(AccountState newState) {
        String previousState = currentState.getName();
        currentState = newState;

        registerTransaction(
                "CAMBIO DE ESTADO",
                0,
                previousState + " -> " + newState.getName()
        );
    }

    public boolean canReceiveDeposit() {
        return currentState.canReceiveDeposit();
    }

    public boolean hasAvailableFunds(double amount) {
        return amount <= balance + overdraftLimit;
    }

    public boolean isOverdrawn() {
        return balance < 0;
    }

    public boolean hasZeroBalance() {
        return Math.abs(balance) < 0.01;
    }

    public void increaseBalance(double amount) {
        balance += amount;
    }

    public void decreaseBalance(double amount) {
        balance -= amount;
    }

    public void registerTransaction(String type, double amount, String description) {
        transactions.add(new Transaction(type, amount, description));
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public String getFormattedBalance() {
        return formatMoney(balance);
    }

    public String getFormattedOverdraftLimit() {
        return formatMoney(overdraftLimit);
    }

    private String formatMoney(double value) {
        NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        return moneyFormatter.format(value);
    }

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("El valor debe ser mayor que cero.");
        }
    }
}