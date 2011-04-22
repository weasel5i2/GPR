package net.weasel.GPR;

// import org.bukkit.plugin.PluginManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Radar extends JavaPlugin
{
	public final String pluginName = "GPR";
	public final String pluginVersion = "1.0";
	
	public String mapResult = "";

	@Override
	public void onDisable() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEnable() 
	{
        // PluginManager manager = getServer().getPluginManager();
		logOutput( "Ground Penetrating Radar is enabled." );
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) 
    {
		if( sender instanceof Player )
		{
	    	Player player = (Player)sender;
	    	String pCommand = command.getName().toLowerCase();
	    	
	    	if( pCommand.equals( "gpr" ) || pCommand.equals("scan") )
	    	{
	    		if( args.length == 1 )
	    		{
	    			String arg = arrayToString( args, " " );
	    			int aInt = Integer.parseInt(arg);
	    			
	    			if( aInt < 3 )
	    			{
	    				player.sendMessage( "You might as well just use a shovel and dig." );
	    				return true;
	    			}
	    			
	    			doRadarScan( player, aInt, 15 );
	    			return true;
	    		}
	    	}
		}

    	return false;
    }
	
	public void logOutput( String text ) 
	{
		System.out.println( "[" + pluginName + "] " + text );
	}

	public static String arrayToString(String[] a, String separator) 
    {
        String result = "";
        
        if (a.length > 0) 
        {
            result = a[0];    // start with the first element
            for (int i=1; i<a.length; i++) {
                result = result + separator + a[i];
            }
        }
        return result;
    }
	
	public void doRadarScan( Player who, Integer depth, Integer radius )
	{
		Location pL = who.getLocation();
		boolean foundAir = false;
		double foundAt = 0;
		
		double pX = pL.getX();
		double pY = who.getLocation().getBlockY() - 1;
		double pZ = pL.getZ();
		double minX = pX - 3;
		double maxX = pX + 4;
		double minZ = pZ - radius;
		double maxZ = pZ + radius;
		double X = 0, Z = 0, Y = 0;
		Block targetBlock = null;
		double tbY = 0;
		double maxDepth = 0;
        String fChar = "";
        double facing = 0;
		
        who.sendMessage( ChatColor.BLUE + "GPR: " + ChatColor.WHITE + "scanning.. " 
        + ChatColor.GRAY + "0+ " 
        + ChatColor.DARK_AQUA + "10+ " 
        + ChatColor.DARK_BLUE + "20+ " 
        + ChatColor.DARK_GREEN + "30+ " 
        + ChatColor.GOLD + "40+ " 
        + ChatColor.DARK_RED + "50+ " );
        
        who.sendMessage( ChatColor.DARK_GRAY + "===============================" );
        
		mapResult = "" + ChatColor.GRAY;

		for( X = minX; X <= maxX; X++ )
		{
			for( Z = minZ; Z <= maxZ; Z++ )
			{
				foundAt = -1;
				foundAir = false;
				
				targetBlock = who.getWorld().getBlockAt( (int)Math.round(X), (int)Math.round(pY), (int)Math.round(Z) );

		    	tbY = targetBlock.getY();
				
				if( depth > pY ) depth = (int) pY;

				maxDepth = tbY - depth;
								
				for( Y = tbY; Y >= maxDepth; Y-- )
				{
					targetBlock = who.getWorld().getBlockAt( (int)Math.round(X), (int)Math.round(Y), (int)Math.round(Z) );
					
					if( targetBlock.getTypeId() == 0 && foundAt == -1 ) 
					{
						if( targetBlock != getTopBlock(who.getWorld(),targetBlock.getX(),targetBlock.getZ() ) )
							foundAir = true;

						foundAt = Y;
					}
				}
				
				if( Math.round(X) == Math.round(pX) && Math.round(Z) == Math.round(pZ) )
				{
					facing = Math.round( Math.abs( who.getLocation().getYaw() ) );
					
					while( facing > 360 ) facing -= 360;
					while( facing < 0 ) facing += 360;
						
						 if( facing < 90 )
						fChar = ">";
					else if( facing < 180 )
						fChar = "V";
					else if( facing < 270 )
						fChar = "<";
					else
						fChar = "^";
					
					mapResult += ChatColor.AQUA + fChar + ChatColor.GRAY;
				}
				else
				{
					if( foundAir == true )
						mapResult += getDepthChar( Math.abs(tbY-foundAt) );
					else
						mapResult += ChatColor.GRAY + " . ";
				}
			}
			who.sendMessage( mapResult );
			mapResult = "" + ChatColor.GRAY;
		}
	}

	public String getDepthChar( double depth )
	{
		String retVal = "";
		String values = "1234567890";
		String cChar = "";
		int pos = (int)depth % 10;
		
		if( depth > 50 ) cChar += ChatColor.DARK_RED;
		if( depth > 40 && depth < 50 ) cChar += ChatColor.GOLD;
		else if( depth > 30 && depth < 40 ) cChar += ChatColor.DARK_GREEN;
		else if( depth > 20 && depth < 30 ) cChar += ChatColor.DARK_BLUE;
		else if( depth > 10 && depth < 20 ) cChar += ChatColor.DARK_AQUA;
		else cChar += ChatColor.GRAY;
		
		if( (int)depth > values.length() ) depth = values.length() - 2;
		
		retVal = cChar + values.substring( pos, pos+1 ) + ChatColor.GRAY;
		
		if( depth > 0 )
			return retVal;
		else
			return( " . " );
	}

    public static Block getTopBlock( World world, double X, double Z )
    {
    	Block retVal = null;
    	double Y = 127;
    	boolean exitLoop = false;
    	Block currentBlock = null;
    	
    	while( Y > 0 && exitLoop == false )
    	{
    		try
    		{
    			currentBlock = world.getBlockAt((int)X,(int)Y,(int)Z);
    		}
    		catch( Exception e )
    		{
    			retVal = null;
    			exitLoop = true;
    			break;
    		}
    		
    		if( currentBlock.getTypeId() != 0 && currentBlock.getTypeId() != 17 && currentBlock.getTypeId() != 18 )
    		{
    			retVal = currentBlock;
    			exitLoop = true;
    		}
    		else
    		{
    			Y--;
    		}
    	}
    	
    	return retVal.getRelative( org.bukkit.block.BlockFace.UP );
    }
}
