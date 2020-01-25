package be.limero.dashboard;

public interface PubMsgHandler {
    public void onPubMsg(String topic,Object object);
}