package me.minhashemi.view;

import me.minhashemi.controller.audio.player;
import me.minhashemi.model.Impact;
import me.minhashemi.model.level.LevelData;
import me.minhashemi.model.MovingPacket;
import me.minhashemi.model.block.NetSys;
import me.minhashemi.model.block.NetSysPort;
import me.minhashemi.model.block.PortType;
import me.minhashemi.view.wire.WireManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PacketManager {
    private final LevelData levelData;
    private final WireManager wireManager;
    private final HUD hud;
    private final List<MovingPacket> movingPackets;
    private final List<Impact> impacts;
    private int lostPackets;
    private final int totalPackets;
    private boolean impactIsDisabled;
    private boolean waveIsDisabled;

    public PacketManager(LevelData levelData, WireManager wireManager, HUD hud, List<MovingPacket> movingPackets, int totalPackets) {
        this.levelData = levelData;
        this.wireManager = wireManager;
        this.hud = hud;
        this.movingPackets = movingPackets;
        this.impacts = new ArrayList<>();
        this.lostPackets = 0;
        this.totalPackets = totalPackets;
        this.impactIsDisabled = false;
        this.waveIsDisabled = false;
    }

    public void updateAndRenderPackets(Graphics2D g2) {
        Iterator<MovingPacket> iterator = movingPackets.iterator();
        List<MovingPacket> toAdd = new ArrayList<>();

        while (iterator.hasNext()) {
            MovingPacket packet = iterator.next();
            packet.update();
            findImpact(packet);

            if (packet.isLost()) {
                iterator.remove();
                lostPackets++;
                hud.updateHUD(hud.getTemporalProgress(), lostPackets, hud.getCoins());
            } else if (packet.isArrived()) {
                iterator.remove();
                packet.getWire().setHasPacket(false);

                NetSysPort toPort = packet.getWire().getToPort();
                NetSys targetSys = toPort.getParent();
                targetSys.markReceived();
                targetSys.tryToForwardPacket(wireManager, toAdd, packet.getType());

                int coinsEarned = packet.getType() == PortType.SQUARE ? 1 : 2;
                hud.updateHUD(hud.getTemporalProgress(), lostPackets, hud.getCoins() + coinsEarned);
            } else {
                packet.draw(g2);
            }
        }

        manageImpacts();
        movingPackets.addAll(toAdd);
    }

    private void findImpact(MovingPacket packet1) {
        if (impactIsDisabled) return;

        for (MovingPacket packet2 : movingPackets) {
            if (packet2 == packet1 || packet1.getWire() == null || packet2.getWire() == null) {
                continue;
            }

            Area area1 = new Area(packet1.getPath());
            Area area2 = new Area(packet2.getPath());
            area1.intersect(area2);

            boolean firstImpact = true;
            for (Impact i : impacts) {
                if (i.contains(packet1, packet2)) {
                    firstImpact = false;
                    break;
                }
            }

            if (!area1.isEmpty() && firstImpact) {
                Rectangle2D boundsArea1 = area1.getBounds2D();
                Point point = new Point((int) boundsArea1.getX(), (int) boundsArea1.getY());
                Impact impact = new Impact(packet1, packet2, point);
                impacts.add(impact);
                player.playEffect("collide");
            }
        }
    }

    private void manageImpacts() {
        for (Impact impact : impacts) {
            if (impact.isDisabled()) continue;

            for (MovingPacket packet : movingPackets) {
                if (impact.packet1 == packet || impact.packet2 == packet) {
                    packet.increaseNoise(30f);
                } else if (!waveIsDisabled) {
                    packet.applyImpact(impact.point);
                }
            }
        }
        impacts.clear();
    }

    public void disableImpactForSeconds(int seconds) {
        impactIsDisabled = true;
        new Timer(seconds * 1000, e -> impactIsDisabled = false).start();
    }

    public void disableWaveForSeconds(int seconds) {
        waveIsDisabled = true;
        new Timer(seconds * 1000, e -> waveIsDisabled = false).start();
    }

    public boolean isImpactDisabled() {
        return impactIsDisabled;
    }

    public boolean isWaveDisabled() {
        return waveIsDisabled;
    }

    public void resetAllNoise() {
        for (MovingPacket packet : movingPackets) {
            packet.setNoise(0);
        }
    }

    public int getLostPackets() {
        return lostPackets;
    }

    public boolean allBlocksGreen() {
        for (NetSys netsys : levelData.packets) {
            if (!netsys.hasReceivedPacket()) return false;
        }
        return true;
    }

    public List<MovingPacket> getMovingPackets() {
        return movingPackets;
    }

    public int getTotalPackets() {
        return totalPackets;
    }
}