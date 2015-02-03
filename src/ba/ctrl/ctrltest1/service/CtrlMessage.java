package ba.ctrl.ctrltest1.service;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Structure of Client CTRL Message
 * Packing to JSON and unpacking from JSON.
 */
public class CtrlMessage {
    // misc
    private boolean isExtracted = false;

    // Header fields
    private boolean isAck = false;
    private boolean isSystemMessage = false;
    private boolean isSync = false;
    private boolean isProcessed = false;
    private boolean isOutOfSync = false;
    private boolean isNotification = false;
    private boolean isBackoff = false;
    // BaseID list
    private ArrayList<String> baseIds = new ArrayList<String>();
    // TXsender
    private int TXsender = 0;
    // Data
    private Object data = null;

    public CtrlMessage() {
    }

    // Unpack message from JSON String
    public CtrlMessage(String message) {
        try {
            JSONObject msg = new JSONObject(message);

            // Header
            isSync = isAck = isSystemMessage = isProcessed = isOutOfSync = isNotification = isBackoff = false;
            if (msg.has("header")) {
                JSONObject header = msg.getJSONObject("header");
                if (header.has("sync") && !header.isNull("sync") && header.getBoolean("sync")) {
                    isSync = true;
                }
                if (header.has("ack") && !header.isNull("ack") && header.getBoolean("ack")) {
                    isAck = true;
                }
                if (header.has("system_message") && !header.isNull("system_message") && header.getBoolean("system_message")) {
                    isSystemMessage = true;
                }
                if (header.has("processed") && !header.isNull("processed") && header.getBoolean("processed")) {
                    isProcessed = true;
                }
                if (header.has("out_of_sync") && !header.isNull("out_of_sync") && header.getBoolean("out_of_sync")) {
                    isOutOfSync = true;
                }
                if (header.has("notification") && !header.isNull("notification") && header.getBoolean("notification")) {
                    isNotification = true;
                }
                if (header.has("backoff") && !header.isNull("backoff") && header.getBoolean("backoff")) {
                    isBackoff = true;
                }
            }

            // BaseIDs
            baseIds.clear();
            if (msg.has("baseid") && !msg.isNull("baseid") && (msg.get("baseid") instanceof JSONArray)) {
                JSONArray jBaseIds = msg.getJSONArray("baseid");
                for (int i = 0; i < jBaseIds.length(); i++) {
                    baseIds.add(jBaseIds.getString(i));
                }
            }

            // TXsender
            if (msg.has("TXsender")) {
                TXsender = msg.getInt("TXsender");
            }

            // Data, might come as Object or as String
            if (msg.has("data") && !msg.isNull("data") && (msg.get("data") instanceof String)) {
                data = msg.getString("data");
            }
            else {
                data = msg.get("data");
            }

            isExtracted = true;
        }
        catch (JSONException e) {
            e.printStackTrace();

            isExtracted = false;
        }
    }

    /**
     * Pack message and return as JSON String.
     * 
     * @return
     */
    public String buildMessage() {
        JSONObject jHeader = new JSONObject();
        try {
            if (isAck)
                jHeader.put("ack", isAck);
            if (isSystemMessage)
                jHeader.put("system_message", isSystemMessage);
            if (isSync)
                jHeader.put("sync", isSync);
            if (isProcessed)
                jHeader.put("processed", isProcessed);
            if (isOutOfSync)
                jHeader.put("out_of_sync", isOutOfSync);
            if (isNotification)
                jHeader.put("notification", isNotification);
            if (isBackoff)
                jHeader.put("backoff", isBackoff);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject msg = new JSONObject();
        try {
            msg.put("header", jHeader);
            if (baseIds.size() > 0) {
                JSONArray b = new JSONArray(baseIds);
                msg.put("baseid", b);
            }
            msg.put("TXsender", TXsender);
            msg.put("data", data);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return msg.toString();
    }

    public void addBaseId(String baseId) {
        baseIds.add(baseId);
    }

    public void setBaseIds(ArrayList<String> baseIds) {
        this.baseIds = baseIds;
    }

    public ArrayList<String> getBaseIds() {
        return baseIds;
    }

    public boolean getIsAck() {
        return isAck;
    }

    public void setIsAck(boolean isAck) {
        this.isAck = isAck;
    }

    public boolean getIsSystemMessage() {
        return isSystemMessage;
    }

    public void setIsSystemMessage(boolean isSystemMessage) {
        this.isSystemMessage = isSystemMessage;
    }

    public boolean getIsSync() {
        return isSync;
    }

    public void setIsSync(boolean isSync) {
        this.isSync = isSync;
    }

    public boolean getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    public boolean getIsOutOfSync() {
        return isOutOfSync;
    }

    public void setIsOutOfSync(boolean isOutOfSync) {
        this.isOutOfSync = isOutOfSync;
    }

    public boolean getIsNotification() {
        return isNotification;
    }

    public void setIsNotification(boolean isNotification) {
        this.isNotification = isNotification;
    }

    public int getTXsender() {
        return TXsender;
    }

    public void setTXsender(int tXsender) {
        TXsender = tXsender;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean getIsBackoff() {
        return isBackoff;
    }

    public void setIsBackoff(boolean isBackoff) {
        this.isBackoff = isBackoff;
    }

    public boolean getIsExtracted() {
        return isExtracted;
    }

}