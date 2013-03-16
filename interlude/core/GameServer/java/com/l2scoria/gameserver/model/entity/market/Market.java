package com.l2scoria.gameserver.model.entity.market;

import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.Collection;
import java.util.Map;

import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.templates.L2EtcItem;

public class Market {
	/* Отображение */
	public static final int MAX_LOTS = 50; // Макс. кол-во лотов аукциона
	public static final int MAX_CHAR_LOTS = 12; // Макс. кол-во слотов для чара
	public static final int LOTS_PER_PAGE = 10; // Кол-во лотов на страницу (указывайтеся четное число)
	
	/* Продажа */
	public static final int[] DISALLOWED_ITEMS_FOR_BID = { 5588, 7694  }; // Список запрещенных айтемов для выставления на продажу
	public static final double MARKET_TAX = 0.1; // Налог аукциона (берется с заявленой цены айтема), в процентах
	
	/* Другие настройки */
	public static final boolean SEND_MESSAGE_AFTER_TRADE = true; // Посылать сообщения продавцу и покупателю при покупке / продаже айтема
	public static final boolean ALLOW_AUGMENTATED_ITEMS = true; // Разрешить выставлять на продажу аугментированные айтемы
	public static final boolean ALLOW_ETC_ITEMS_FOR_SELL = false; // Разрешить выставлять на продажу etc. айетмы (свитки и пр.)
	public static final boolean ALLOW_ENCHATED_ITEMS = true; // Разрешить выставлять на продажу заточенные айтемы
	public static final String TRADE_MESSAGE_FORSELLER = "Ваш товар %item% был успешно продан."; // Сообщение продавцу
	public static final String TRADE_MESSAGE_FORBUYER = "Вы успешно купили товар %item%."; // Сообщение покупателю
	
	private int lotsCount = 0;
	
	private static Map<Integer, FastList<Bid>> lots;
	private static Map<String, Integer> prices;
	
	private Market()
	{
		lots = new FastMap<Integer, FastList<Bid>>();
		prices = new FastMap<String, Integer>();
		prices.put("Adena", 57);
		prices.put("CoL", 4037);
	}
	
	public void addLot(int playerid, int itemObjId, int costItemId, int costItemCount, String tax)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/market/MarketReturnResult.htm");
		L2PcInstance player = L2World.getInstance().getPlayer(playerid);
		L2ItemInstance item = player.getInventory().getItemByObjectId(itemObjId);
		MarketTaxType taxType = null;
		if(tax.equalsIgnoreCase("Seller"))
			taxType = MarketTaxType.SELLER;
		else if(tax.equalsIgnoreCase("Buyer"))
			taxType = MarketTaxType.BUYER;
		if(!checkItemForMarket(item))
		{
			html.replace("%text%", "Извините, этот айтем нельзя выставить на рынок.");
			player.sendPacket(html);
			return;
		}
		if(!prices.containsValue(costItemId))
		{
			html.replace("%text%", "Извините, эта валюта не поддерживается рынком.");
			player.sendPacket(html);
			return;
		}
		if((getBidsCount() +1) > MAX_LOTS)
		{
			html.replace("%text%", "Извините, аукцион переполнен.");
			player.sendPacket(html);
			return;
		}
		if(lots.get(player.getObjectId()) != null && (lots.get(player.getObjectId()).size() +1 > MAX_CHAR_LOTS))
		{
			html.replace("%text%", "Извините, вы превысили макс. количество товаров.");
			player.sendPacket(html);
			return;
		}
		if(taxType == MarketTaxType.SELLER && (player.getInventory().getItemByItemId(costItemId) != null && player.getInventory().getItemByItemId(costItemId).getCount() < (costItemCount * MARKET_TAX)))
		{
			html.replace("%text%", "Извините, у Вас не достаточно средств для оплаты налога рынка.");
			player.sendPacket(html);
			return;
		}
		Bid biditem = new Bid(player, lotsCount++, item, costItemId, costItemCount, taxType);
		if(biditem.getTaxType() == MarketTaxType.SELLER) 
			player.destroyItemByItemId("Market tax", costItemId, (int)(costItemCount * MARKET_TAX), null, false);
		if(lots.get(player.getObjectId()) != null)
			lots.get(player.getObjectId()).add(biditem);
		else
		{
			FastList<Bid> charBidItems = new FastList<Bid>();
			charBidItems.add(biditem);
			lots.put(player.getObjectId(), charBidItems);
		}
		html.replace("%text%", "Товар успешно добавлен на рынок.");
		player.sendPacket(html);
	}
	
	public void deleteLot(int charObjId, int bidId)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(charObjId);
		Bid bid = getBidById(bidId);
		if(bid.getBidder().getObjectId() != player.getObjectId())
			return;
		if(!(lots.get(player.getObjectId()).contains(bid)))
			return;
		lots.get(player.getObjectId()).remove(bid);
		sendResultHtml(player, "Ваш предмет успешно удален с рынка.");
	}
	
	public void buyLot(int buyerId, int bidId)
	{
		Bid bid = getBidById(bidId);
		L2PcInstance seller = L2World.getInstance().getPlayer(bid.getBidder().getObjectId());
		L2PcInstance buyer = L2World.getInstance().getPlayer(buyerId);
		if(seller == null || buyer == null || buyer.getObjectId() == bid.getBidder().getObjectId())
		{
			lots.get(bid.getBidder().getObjectId()).clear();
			return;
		}
		if(seller.getInventory().getItemByItemId(bid.getBidItem().getItemId()) == null)
		{
			if(lots.get(seller.getObjectId()) != null)
				lots.get(seller.getObjectId()).remove(bid);
			return;
		}
		if(buyer.getInventory().getItemByItemId(bid.getCostItemId()) == null || (bid.getTaxType() == MarketTaxType.BUYER && (buyer.getInventory().getItemByItemId(bid.getCostItemId()).getCount() < (bid.getCostItemCount() + (bid.getCostItemCount() * MARKET_TAX)))) || 
				(bid.getTaxType() == MarketTaxType.SELLER && (buyer.getInventory().getItemByItemId(bid.getCostItemId()).getCount() < bid.getCostItemCount())))
		{
			sendResultHtml(buyer, "Извините, у Вас не хватает денег на оплату товара.");
			return;
		}
		L2ItemInstance item = seller.getInventory().getItemByObjectId(bid.getBidItem().getObjectId());
		if(item == null) return;
		double itemcount = (bid.getTaxType() == MarketTaxType.BUYER ? (bid.getCostItemCount() + (bid.getCostItemCount() * MARKET_TAX)) : bid.getCostItemCount());
		buyer.destroyItemByItemId("Market", bid.getCostItemId(), (int)itemcount, buyer, false);
		seller.addItem("Market", bid.getCostItemId(), bid.getCostItemCount(), seller, false);
		seller.transferItem("Market", item.getObjectId(), 1, buyer.getInventory(), seller);
		if(SEND_MESSAGE_AFTER_TRADE)
		{
			seller.sendMessage((TRADE_MESSAGE_FORSELLER.replace("%item%", bid.getBidItem().getItemName() + " +" + bid.getBidItem().getEnchantLevel())));
			buyer.sendMessage((TRADE_MESSAGE_FORBUYER.replace("%item%", bid.getBidItem().getItemName() + " +" + bid.getBidItem().getEnchantLevel())));
		}
		lots.get(bid.getBidder().getObjectId()).remove(bid);
	}
	
	public Bid getBidById(int bidId)
	{
		Collection<FastList<Bid>> collect = lots.values();
		for(FastList<Bid> list: collect)
		{
			for(Bid bid: list)
			{
				if(bid.getBidId() == bidId)
					return bid;
			}
		}
		return null;
	}
	
	public FastList<Bid> getAllBids()
	{
		FastList<Bid> result = new FastList<Bid>();
		Collection<FastList<Bid>> collect = lots.values();
		for(FastList<Bid> list: collect)
		{
			for(Bid bid: list)
			{
				result.add(bid);
			}
		}
		return result;
	}
	
	public int getBidsCount()
	{
		int count = 0;
		Collection<FastList<Bid>> collect = lots.values();
		for(FastList<Bid> list: collect)
		{
			count += list.size();
		}
		return count;
	}
	
	public String getShortItemName(int id)
	{
		for(Map.Entry<String, Integer> entry: prices.entrySet())
		{
			if(entry.getValue() == id)
				return entry.getKey();
		}
		return "";
	}
	
	public int getShortItemId(String name)
	{
		for(Map.Entry<String, Integer> entry: prices.entrySet())
		{
			if(entry.getKey().equalsIgnoreCase(name))
				return entry.getValue();
		}
		return 0;
	}
	
	public String getPriceList()
	{
		String res = "";
		Object[] str = Market.prices.keySet().toArray();
		for(int i = 0;i < str.length;i++)
		{
			res += (String)str[i];
			if(!(i == str.length-1))
			{
				res += ";";
			}
		}
		return res;
	}
	
	public boolean isInArray(int[] arr, int item)
	{
		for(int i: arr)
		{
			if(i == item)
				return true;
		}
		return false;
	}
	
	private void sendResultHtml(L2PcInstance player, String text)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/market/MarketReturnResult.htm");
		html.replace("%text%", text);
		player.sendPacket(html);
	}
	
	public boolean checkItemForMarket(L2ItemInstance item)
	{
		if(isInArray(DISALLOWED_ITEMS_FOR_BID, item.getItemId()) || (item.isAugmented() && !ALLOW_AUGMENTATED_ITEMS) || item.isStackable()
				|| ((item.getItem() instanceof L2EtcItem) && !ALLOW_ETC_ITEMS_FOR_SELL) || (item.getEnchantLevel() > 0 && !ALLOW_ENCHATED_ITEMS))
			return false;
		return true;
	}
	
	public Map<Integer, FastList<Bid>> getLots()
	{
		return lots;
	}
	
	private static Market _instance;
	
	public static Market getInstance()
	{
		if(_instance == null)
			_instance = new Market();
		return _instance;
	}
	
}