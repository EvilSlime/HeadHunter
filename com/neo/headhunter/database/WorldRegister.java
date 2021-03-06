package com.neo.headhunter.database;

import com.neo.headhunter.util.config.Settings;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class WorldRegister {
	private Connection c;
	
	private PreparedStatement addWorld, removeWorld;
	
	public WorldRegister(Connection c) {
		this.c = c;
		
		createTables();
		prepareStatements();
	}
	
	public boolean isValidDropLocation(LivingEntity target) {
		if(Settings.isIgnoreWorlds())
			return true;
		List<UUID> validWorlds = new ArrayList<>();
		try {
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery("select world_id from hunter_world");
			while(rs.next())
				validWorlds.add(UUID.fromString(rs.getString(1)));
		} catch(SQLException e) {
			e.printStackTrace();
		}
		UUID cwid = target.getWorld().getUID();
		for(UUID wid : validWorlds) {
			if(wid.equals(cwid))
				return true;
		}
		return false;
	}
	
	public boolean addWorld(World world) {
		if(world == null)
			return false;
		try {
			addWorld.setString(1, world.getUID().toString());
			addWorld.setString(2, world.getName());
			addWorld.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean removeWorld(World world) {
		if(world == null)
			return false;
		try {
			removeWorld.setString(1, world.getName());
			removeWorld.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private void createTables() {
		try {
			Statement s = c.createStatement();
			s.execute("create table if not exists hunter_world (" +
					          "world_id text, world_name text," +
					          "primary key (world_id))");
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void prepareStatements() {
		try {
			this.addWorld = c.prepareStatement("insert or replace into hunter_world values (?, ?)");
			this.removeWorld = c.prepareStatement("delete from hunter_world where world_name = ?");
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
