package ba.ctrl.ctrltest1.service;

public interface BaseEventReceiverCallbacks {
    public void baseNewDataArrival(String baseId, String data);

    public void baseNewConnectionStatus(String baseId, boolean connected);
}
