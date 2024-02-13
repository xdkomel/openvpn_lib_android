package de.blinkt.openvpn.listeners;

public interface OpenVpnConnectionNetstatListener extends OpenVpnListener {
    @Override
    void onEvent(String byteIn, String byteOut);
}
