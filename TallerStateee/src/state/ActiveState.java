package state;

import model.BankAccount;

public class ActiveState implements AccountState {

    @Override
    public String getName() {
        return "Activa";
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
                "Depósito realizado correctamente."
        );

        return "Depósito realizado correctamente.";
    }

    @Override
    public String receiveTransfer(BankAccount targetAccount, BankAccount sourceAccount, double amount) {
        targetAccount.increaseBalance(amount);

        targetAccount.registerTransaction(
                "TRANSFERENCIA RECIBIDA",
                amount,
                "Transferencia recibida desde la cuenta " + sourceAccount.getAccountNumber() + "."
        );

        return "Transferencia recibida correctamente.";
    }

    @Override
    public String withdraw(BankAccount account, double amount) {
        if (!account.hasAvailableFunds(amount)) {
            account.registerTransaction(
                    "RETIRO RECHAZADO",
                    amount,
                    "Fondos insuficientes."
            );

            return "Fondos insuficientes. El retiro supera el saldo disponible y el límite de sobregiro.";
        }

        account.decreaseBalance(amount);

        account.registerTransaction(
                "RETIRO",
                amount,
                "Retiro realizado correctamente."
        );

        if (account.isOverdrawn()) {
            account.changeState(new OverdrawnState());
            return "Retiro realizado correctamente. La cuenta quedó sobregirada.";
        }

        return "Retiro realizado correctamente.";
    }

    @Override
    public String transferTo(BankAccount sourceAccount, BankAccount targetAccount, double amount) {
        if (sourceAccount == targetAccount) {
            sourceAccount.registerTransaction(
                    "TRANSFERENCIA RECHAZADA",
                    amount,
                    "La cuenta origen y destino son iguales."
            );

            return "No se puede transferir dinero a la misma cuenta.";
        }

        if (!targetAccount.canReceiveDeposit()) {
            sourceAccount.registerTransaction(
                    "TRANSFERENCIA RECHAZADA",
                    amount,
                    "La cuenta destino no puede recibir dinero."
            );

            return "No se puede transferir. La cuenta destino no puede recibir dinero.";
        }

        if (!sourceAccount.hasAvailableFunds(amount)) {
            sourceAccount.registerTransaction(
                    "TRANSFERENCIA RECHAZADA",
                    amount,
                    "Fondos insuficientes."
            );

            return "Fondos insuficientes para realizar la transferencia.";
        }

        sourceAccount.decreaseBalance(amount);

        sourceAccount.registerTransaction(
                "TRANSFERENCIA ENVIADA",
                amount,
                "Transferencia enviada a la cuenta " + targetAccount.getAccountNumber() + "."
        );

        targetAccount.receiveTransferFrom(sourceAccount, amount);

        if (sourceAccount.isOverdrawn()) {
            sourceAccount.changeState(new OverdrawnState());
            return "Transferencia realizada correctamente. La cuenta origen quedó sobregirada.";
        }

        return "Transferencia realizada correctamente.";
    }

    @Override
    public String freeze(BankAccount account) {
        account.changeState(new FrozenState());

        account.registerTransaction(
                "BLOQUEO",
                0,
                "La cuenta fue bloqueada correctamente."
        );

        return "La cuenta fue bloqueada correctamente.";
    }

    @Override
    public String close(BankAccount account) {
        if (!account.hasZeroBalance()) {
            account.registerTransaction(
                    "CIERRE RECHAZADO",
                    0,
                    "El saldo debe estar en cero."
            );

            return "No se puede cerrar la cuenta. El saldo debe estar en cero.";
        }

        account.changeState(new ClosedState());

        account.registerTransaction(
                "CIERRE",
                0,
                "La cuenta fue cerrada correctamente."
        );

        return "La cuenta fue cerrada correctamente.";
    }
}