package state;

import model.BankAccount;

public class FrozenState implements AccountState {

    @Override
    public String getName() {
        return "Bloqueada";
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
                "Depósito realizado en una cuenta bloqueada."
        );

        return "Depósito realizado correctamente. La cuenta sigue bloqueada.";
    }

    @Override
    public String receiveTransfer(BankAccount targetAccount, BankAccount sourceAccount, double amount) {
        targetAccount.increaseBalance(amount);

        targetAccount.registerTransaction(
                "TRANSFERENCIA RECIBIDA",
                amount,
                "Transferencia recibida desde la cuenta " + sourceAccount.getAccountNumber() + "."
        );

        return "Transferencia recibida correctamente. La cuenta sigue bloqueada.";
    }

    @Override
    public String withdraw(BankAccount account, double amount) {
        account.registerTransaction(
                "RETIRO RECHAZADO",
                amount,
                "La cuenta está bloqueada."
        );

        return "No se puede retirar dinero. La cuenta está bloqueada.";
    }

    @Override
    public String transferTo(BankAccount sourceAccount, BankAccount targetAccount, double amount) {
        sourceAccount.registerTransaction(
                "TRANSFERENCIA RECHAZADA",
                amount,
                "La cuenta está bloqueada."
        );

        return "No se puede transferir dinero. La cuenta está bloqueada.";
    }

    @Override
    public String unfreeze(BankAccount account) {
        if (account.isOverdrawn()) {
            account.changeState(new OverdrawnState());
        } else {
            account.changeState(new ActiveState());
        }

        account.registerTransaction(
                "DESBLOQUEO",
                0,
                "La cuenta fue desbloqueada correctamente."
        );

        return "La cuenta fue desbloqueada correctamente.";
    }

    @Override
    public String close(BankAccount account) {
        if (!account.hasZeroBalance()) {
            account.registerTransaction(
                    "CIERRE RECHAZADO",
                    0,
                    "El saldo debe estar en cero."
            );

            return "No se puede cerrar la cuenta bloqueada. El saldo debe estar en cero.";
        }

        account.changeState(new ClosedState());

        account.registerTransaction(
                "CIERRE",
                0,
                "La cuenta bloqueada fue cerrada correctamente."
        );

        return "La cuenta fue cerrada correctamente.";
    }
}