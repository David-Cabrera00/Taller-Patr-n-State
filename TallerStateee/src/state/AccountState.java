package state;

import model.BankAccount;

public interface AccountState {
    String getName();

    default boolean canReceiveDeposit() {
        return false;
    }

    default String deposit(BankAccount account, double amount) {
        account.registerTransaction(
                "DEPÓSITO RECHAZADO",
                amount,
                "La cuenta está en estado " + getName() + "."
        );

        return "No se puede realizar el depósito. La cuenta está en estado " + getName() + ".";
    }

    default String receiveTransfer(BankAccount targetAccount, BankAccount sourceAccount, double amount) {
        targetAccount.registerTransaction(
                "TRANSFERENCIA RECIBIDA RECHAZADA",
                amount,
                "La cuenta está en estado " + getName() + "."
        );

        return "No se puede recibir la transferencia. La cuenta destino está en estado " + getName() + ".";
    }

    default String withdraw(BankAccount account, double amount) {
        account.registerTransaction(
                "RETIRO RECHAZADO",
                amount,
                "La cuenta está en estado " + getName() + "."
        );

        return "No se puede realizar el retiro. La cuenta está en estado " + getName() + ".";
    }

    default String transferTo(BankAccount sourceAccount, BankAccount targetAccount, double amount) {
        sourceAccount.registerTransaction(
                "TRANSFERENCIA RECHAZADA",
                amount,
                "La cuenta está en estado " + getName() + "."
        );

        return "No se puede realizar la transferencia. La cuenta está en estado " + getName() + ".";
    }

    default String freeze(BankAccount account) {
        account.registerTransaction(
                "BLOQUEO RECHAZADO",
                0,
                "La cuenta está en estado " + getName() + "."
        );

        return "No se puede bloquear la cuenta. Estado actual: " + getName() + ".";
    }

    default String unfreeze(BankAccount account) {
        account.registerTransaction(
                "DESBLOQUEO RECHAZADO",
                0,
                "La cuenta está en estado " + getName() + "."
        );

        return "No se puede desbloquear la cuenta. Estado actual: " + getName() + ".";
    }

    default String close(BankAccount account) {
        account.registerTransaction(
                "CIERRE RECHAZADO",
                0,
                "La cuenta está en estado " + getName() + "."
        );

        return "No se puede cerrar la cuenta. Estado actual: " + getName() + ".";
    }
}