package state;

import model.BankAccount;

public class OverdrawnState implements AccountState {

    @Override
    public String getName() {
        return "Sobregirada";
    }

    @Override
    public boolean canReceiveDeposit() {
        return true;
    }

    @Override
    public String deposit(BankAccount account, double amount) {
        account.increaseBalance(amount);

        account.registerTransaction(
                "DEPÓSITO",
                amount,
                "Depósito aplicado a una cuenta sobregirada."
        );

        if (!account.isOverdrawn()) {
            account.changeState(new ActiveState());
            return "Depósito realizado correctamente. La cuenta volvió al estado activo.";
        }

        return "Depósito realizado correctamente. La cuenta sigue sobregirada.";
    }

    @Override
    public String receiveTransfer(BankAccount targetAccount, BankAccount sourceAccount, double amount) {
        targetAccount.increaseBalance(amount);

        targetAccount.registerTransaction(
                "TRANSFERENCIA RECIBIDA",
                amount,
                "Transferencia recibida desde la cuenta " + sourceAccount.getAccountNumber() + "."
        );

        if (!targetAccount.isOverdrawn()) {
            targetAccount.changeState(new ActiveState());
            return "Transferencia recibida correctamente. La cuenta volvió al estado activo.";
        }

        return "Transferencia recibida correctamente. La cuenta sigue sobregirada.";
    }

    @Override
    public String withdraw(BankAccount account, double amount) {
        account.registerTransaction(
                "RETIRO RECHAZADO",
                amount,
                "La cuenta está sobregirada."
        );

        return "No se puede retirar dinero. La cuenta está sobregirada.";
    }

    @Override
    public String transferTo(BankAccount sourceAccount, BankAccount targetAccount, double amount) {
        sourceAccount.registerTransaction(
                "TRANSFERENCIA RECHAZADA",
                amount,
                "La cuenta está sobregirada."
        );

        return "No se puede transferir dinero. La cuenta está sobregirada.";
    }

    @Override
    public String freeze(BankAccount account) {
        account.changeState(new FrozenState());

        account.registerTransaction(
                "BLOQUEO",
                0,
                "La cuenta sobregirada fue bloqueada."
        );

        return "La cuenta fue bloqueada correctamente.";
    }

    @Override
    public String close(BankAccount account) {
        account.registerTransaction(
                "CIERRE RECHAZADO",
                0,
                "La cuenta tiene saldo negativo."
        );

        return "No se puede cerrar la cuenta. Primero debe pagar el sobregiro.";
    }
}