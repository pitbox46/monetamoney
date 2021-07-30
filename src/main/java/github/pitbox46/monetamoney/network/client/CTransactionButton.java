package github.pitbox46.monetamoney.network.client;

public class CTransactionButton {
    public final int amount;
    public final Button button;

    public CTransactionButton(int amount, Button button) {
        this.amount = amount;
        this.button = button;
    }

    public enum Button {
        DEPOSIT,
        WITHDRAW,
        PURCHASE,
        REMOVE,
        LIST_ITEM,
    }
}
