package state;

public class ClosedState implements AccountState {

    @Override
    public String getName() {
        return "Cerrada";
    }

    @Override
    public boolean canReceiveDeposit() {
        return false;
    }
}