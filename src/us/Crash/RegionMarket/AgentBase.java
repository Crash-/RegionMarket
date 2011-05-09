package us.Crash.RegionMarket;

import java.io.*;
import java.net.Socket;

import net.minecraft.server.*;

public class AgentBase extends EntityPlayer {

	AgentManager AgentMgr;
	RegionAgent agent;
	
	public AgentBase(MinecraftServer minecraftserver, World world, String s,
			ItemInWorldManager iteminworldmanager, AgentManager mgr, RegionAgent a) {
		super(minecraftserver, world, s, iteminworldmanager);
		AgentMgr = mgr;
		agent = a;
		//AgentNetHandler d;
		//NetworkManager m = new NetworkManager(new AgentSocket(), "AgentMgr", null);
		//this.a = new AgentNetHandler(minecraftserver, m, this);
	}
	
	@Override
	public boolean a(EntityHuman entity){
		
		if(AgentMgr != null)
			AgentMgr.rigthClickCallback(entity, agent);
		
		return super.a(entity);
		
	}
	
}

class AgentSocket extends Socket {
	
    @Override
    public InputStream getInputStream()
    {
        byte[] buf = new byte[1];
        return new ByteArrayInputStream(buf);
    }

    @Override
    public OutputStream getOutputStream()
    {
        return new ByteArrayOutputStream();
    }
	
}

class AgentNetHandler extends NetServerHandler {

    public AgentNetHandler(MinecraftServer minecraftserver, NetworkManager netMgr, EntityPlayer entityplayer) {
        super(minecraftserver, netMgr, entityplayer);
        netMgr.a(this);

    }

    /*@Override
    public CraftPlayer getPlayer() {
        return null;
    }

    @Override
    public void a() {
    }

    @Override
    public void a(String s) {
    }

    @Override
    public void a(Packet10Flying packet10flying) {
    }

    @Override
    public void a(double d0, double d1, double d2, float f, float f1) {
    }

    @Override
    public void a(Packet14BlockDig packet14blockdig) {
    }

    @Override
    public void a(Packet15Place packet15place) {
    }

    @Override
    public void a(String s, Object[] aobject) {
    }

    @Override
    public void a(Packet packet) {
    }

    @Override
    public void b(Packet packet) {
    }

    @Override
    public void a(Packet16BlockItemSwitch packet16blockitemswitch) {
    }

    @Override
    public void a(Packet3Chat packet3chat) {
    }

    @Override
    public void a(Packet18ArmAnimation packet18armanimation) {
    }

    @Override
    public void a(Packet19EntityAction packet19entityaction) {
    }

    @Override
    public void a(Packet255KickDisconnect packet255kickdisconnect) {
    }

    @Override
    public int b() {
        return 0;
    }

    @Override
    public void b(String s) {
    }

    @Override
    public String c() {
        return "";
    }

    @Override
    public void a(Packet7UseEntity packet7useentity) {
    }

    @Override
    public void a(Packet9Respawn packet9respawn) {
    }

    @Override
    public void a(Packet101CloseWindow packet101closewindow) {
    }

    @Override
    public void a(Packet102WindowClick packet102windowclick) {
    }

    @Override
    public void a(Packet106Transaction packet106transaction) {
    }

    @Override
    public void a(Packet130UpdateSign packet130updatesign) {
    }*/
    
}