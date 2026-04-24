package service;

import model.BankAccount;

public class BankService {
    private BankAccount currentAccount;
    private final BankAccount targetAccount;
    private int accountCounter;

    public BankService() {
        this.accountCounter = 1;

        this.targetAccount = new BankAccount(
                "ACC-999",
                "Cuenta destino de prueba",
                100000,
                0
        );
    }

    public String createAccount(String ownerName, double initialBalance, double overdraftLimit) {
        String accountNumber = String.format("ACC-%03d", accountCounter++);

        currentAccount = new BankAccount(
                accountNumber,
                ownerName,
                initialBalance,
                overdraftLimit
        );

        return "Cuenta creada correctamente.";
    }

    public String deposit(double amount) {
        return getRequiredAccount().deposit(amount);
    }

    public String withdraw(double amount) {
        return getRequiredAccount().withdraw(amount);
    }

    public String transfer(double amount) {
        return getRequiredAccount().transferTo(targetAccount, amount);
    }

    public String freeze() {
        return getRequiredAccount().freeze();
    }

    public String unfreeze() {
        return getRequiredAccount().unfreeze();
    }

    public String close() {
        return getRequiredAccount().close();
    }

    public BankAccount getCurrentAccount() {
        return currentAccount;
    }

    public BankAccount getTargetAccount() {
        return targetAccount;
    }

    private BankAccount getRequiredAccount() {
        if (currentAccount == null) {
            throw new IllegalStateException("Primero debe crear una cuenta.");
        }

        return currentAccount;
    }
}