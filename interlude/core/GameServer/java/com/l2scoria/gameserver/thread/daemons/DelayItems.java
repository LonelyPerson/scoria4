package com.l2scoria.gameserver.thread.daemons;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.util.L2Utils;
import com.l2scoria.util.database.L2DatabaseFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

/**
 *
 * @author zenn
 */
public class DelayItems implements Runnable {
        Logger _log = Logger.getLogger(DelayItems.class.getName());
        private static DelayItems _instance;
    
        public static DelayItems getInstance()
        {
		if(_instance == null)
		{
			_instance = new DelayItems();
		}

		return _instance;
        }
        private DelayItems()
	{
		_log.info("Delayed Items Deamon is runned...");
	}
        
        @Override
	public void run()
	{
            Connection con = null;
            try {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement query = con.prepareStatement("SELECT * FROM delay_item");
                ResultSet result = query.executeQuery();
                while(result.next()) 
                {
                    int transId = result.getInt("id");
                    String char_name = result.getString("char_name");
                    int item_id = result.getInt("item_id");
                    int item_count = result.getInt("count");
                    L2PcInstance character = L2Utils.loadPlayer(char_name);
                    
                    if(character != null) {
                        PreparedStatement remove = con.prepareStatement("DELETE FROM delay_item WHERE id = ?");
                        remove.setInt(1, transId);
                        remove.execute();
                        remove.close();
                        if(Config.DEBUG) {
                            _log.info("Taking delayed items to: "+char_name+". Transaction id: "+transId);
                        }
                        character.addItem("DelayItem", item_id, item_count, null, true);
                    }
                }
                result.close();
                query.close();
                con.close();
                
            } catch(Exception e) {
                //
            }
            
        }
    
}
