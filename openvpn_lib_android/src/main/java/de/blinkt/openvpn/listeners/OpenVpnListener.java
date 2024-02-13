package de.blinkt.openvpn.listeners;

public interface OpenVpnListener {

    default void onEvent(String message) {}

    default void onEvent(String arg1, String arg2) {}
}
