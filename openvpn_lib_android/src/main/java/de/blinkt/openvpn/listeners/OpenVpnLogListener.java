package de.blinkt.openvpn.listeners;

public interface OpenVpnLogListener extends OpenVpnListener {
    @Override
    void onEvent(String message);

}
