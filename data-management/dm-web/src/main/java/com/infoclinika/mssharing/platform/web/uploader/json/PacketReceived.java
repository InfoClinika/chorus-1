package com.infoclinika.mssharing.platform.web.uploader.json;

/**
 * @author Pavel Kaplin
 */
public class PacketReceived {
    private int packet;
    private String result;

    public PacketReceived(int packet) {
        this.packet = packet;
        this.result = "success";
    }

    public PacketReceived(int packet, String result) {
        this.packet = packet;
        this.result = result;
    }

    public String getAction() {
        return "new_packet";
    }

    public String getResult() {
        return result;
    }

    public int getPacket() {
        return packet;
    }
}
