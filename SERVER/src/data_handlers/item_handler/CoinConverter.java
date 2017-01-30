package data_handlers.item_handler;

import utils.ServerGameInfo;

public class CoinConverter
{
  public final int copper;
  public final int silver;
  public final int gold;

  public CoinConverter(int value)
  {
    gold = value / 10000;
    silver = (value - gold * 10000) / 100;
    copper = value - gold * 10000 - silver * 100;
  }

  public Item getGoldItem()
  {
    if (gold > 0) {
      Item it = ServerGameInfo.newItem(34);
      it.setStacked(gold);
      return it;
    }
    return null;
  }

  public Item getSilverItem()
  {
    if (silver > 0) {
      Item it = ServerGameInfo.newItem(35);
      it.setStacked(silver);
      return it;
    }
    return null;
  }

  public Item getCopperItem()
  {
    if (copper > 0) {
      Item it = ServerGameInfo.newItem(36);
      it.setStacked(copper);
      return it;
    }
    return null;
  }
}
