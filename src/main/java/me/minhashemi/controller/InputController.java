package me.minhashemi.controller;

import me.minhashemi.model.*;
import me.minhashemi.model.block.NetSys;
import me.minhashemi.model.block.NetSysPort;
import me.minhashemi.model.level.LevelData;
import me.minhashemi.view.GameScreen;
import me.minhashemi.view.HUD;
import me.minhashemi.view.wire.Wire;
import me.minhashemi.view.wire.WireManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InputController {
    private final GameScreen gameScreen;
    private final LevelData levelData;
    private final WireManager wireManager;
    private final HUD hud;
    private NetSys selectedNetSys = null;
    private Point dragOffset = null;

    public InputController(GameScreen gameScreen, LevelData levelData, WireManager wireManager, HUD hud) {
        this.gameScreen = gameScreen;
        this.levelData = levelData;
        this.wireManager = wireManager;
        this.hud = hud;
        initializeMouseListeners();
    }

    private void initializeMouseListeners() {
        gameScreen.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Wire nearby = wireManager.findNearbyWire(e.getPoint());
                    if (nearby != null) {
                        nearby.fromPort.setConnected(false);
                        nearby.toPort.setConnected(false);
                        wireManager.removeWire(nearby);
                        Config.remainingWireLength += nearby.getLength();
                        hud.showCallout("🗑️ Wire removed. Refunded: " + (int) nearby.getLength());
                        gameScreen.repaint();
                        return;
                    }
                }

                NetSysPort wireStartPort = wireManager.findNearbyOutputPort(e.getPoint());
                if (wireStartPort != null && !wireStartPort.isConnected()) {
                    Point startPos = wireStartPort.getPosition();
                    Point wireStart = new Point(startPos.x + Config.PORT_SIZE / 2, startPos.y + Config.PORT_SIZE / 2);
                    wireManager.setWireStart(wireStart, wireStartPort);
                    wireManager.setWireEnd(wireStart);
                    wireManager.setDraggingWire(true);
                    gameScreen.repaint();
                    return;
                }

                for (NetSys netsys : levelData.packets) {
                    Rectangle bounds = new Rectangle(netsys.position.x, netsys.position.y, Config.NETSYS_WIDTH, netsys.getHeight());
                    if (bounds.contains(e.getPoint())) {
                        selectedNetSys = netsys;
                        dragOffset = new Point(e.getX() - netsys.position.x, e.getY() - netsys.position.y);
                        return;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (wireManager.isDraggingWire()) {
                    wireManager.setWireEnd(e.getPoint());
                    wireManager.setDraggingWire(false);

                    Point wireStart = wireManager.getWireStart();
                    NetSysPort wireStartPort = wireManager.getWireStartPort();

                    if (wireStart != null && wireStartPort != null) {
                        NetSysPort endPort = wireManager.findNearbyInputPort(e.getPoint());

                        if (endPort != null
                                && !wireStartPort.isConnected()
                                && !endPort.isConnected()
                                && wireStartPort.isInput() != endPort.isInput()
                                && wireStartPort.getType() == endPort.getType()) {

                            NetSysPort from = wireStartPort.isInput() ? endPort : wireStartPort;
                            NetSysPort to = wireStartPort.isInput() ? wireStartPort : endPort;

                            Wire wire = new Wire(from, to);
                            if (wire.getLength() <= Config.remainingWireLength) {
                                wireManager.addWire(wire);
                                from.setConnected(true);
                                to.setConnected(true);
                                Config.remainingWireLength -= wire.getLength();
                                hud.updateHUD(hud.getTemporalProgress(), hud.getPacketLoss(), hud.getCoins() + 1);
                                hud.showCallout("✅ Wire connected. Remaining: " + (int) Config.remainingWireLength);
                            } else {
                                hud.showCallout("❌ Not enough wire! Needed: " + (int) wire.getLength());
                            }
                        } else {
                            hud.showCallout("❌ Ports must be opposite directions and same type.");
                        }
                    }

                    wireManager.setWireStart(null, null);
                    gameScreen.repaint();
                }

                if (selectedNetSys != null) {
                    double newTotalLength = 0;
                    for (Wire wire : wireManager.getWires()) {
                        newTotalLength += wire.getLength();
                    }

                    double delta = newTotalLength - wireManager.getLastTotalWireLength();
                    Config.remainingWireLength -= delta;
                    wireManager.recalculateTotalWireLength();

                    hud.updateHUD(hud.getTemporalProgress(), hud.getPacketLoss(), hud.getCoins());
                    gameScreen.repaint();
                }

                selectedNetSys = null;
                dragOffset = null;
            }
        });

        gameScreen.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (wireManager.isDraggingWire()) {
                    wireManager.setWireEnd(e.getPoint());
                    gameScreen.repaint();
                } else if (selectedNetSys != null && dragOffset != null) {
                    selectedNetSys.setPosition(new Point(e.getX() - dragOffset.x, e.getY() - dragOffset.y));
                    selectedNetSys.initializePorts();
                    gameScreen.repaint();
                }
            }
        });
    }
}