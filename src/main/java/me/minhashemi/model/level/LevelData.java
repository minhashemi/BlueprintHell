package me.minhashemi.model.level;

import me.minhashemi.model.Metadata;
import me.minhashemi.model.block.NetSys;

import java.util.List;

public class LevelData {
    public int level_id;
    public String level_name;
    public List<NetSys> packets;
    public Metadata metadata;
}
