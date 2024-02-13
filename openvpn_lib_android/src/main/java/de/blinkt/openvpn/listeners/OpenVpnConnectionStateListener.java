package de.blinkt.openvpn.listeners;

public interface OpenVpnConnectionStateListener extends OpenVpnListener {
    @Override
    void onEvent(String state);
}


