package com.l2scoria.gameserver.util;

import org.apache.log4j.Logger;

import java.util.ArrayList;

public class Messages
{
  private static final Logger _log = Logger.getLogger(Messages.class
    .getName());

  private int _index = 0;
  private String _message;
  private Integer _messageId;
  private boolean _isStoreType;
  private ArrayList<String> _args;

  public Messages(Integer unicId, String lang)
  {
    _message = Localization.getInstance().getString(lang, unicId);
    if (_message == null) {
      _log.warn("CustomMessage: message with ID \"" + unicId +
        "\" not found!");
      _message = "";
    }
  }

  public Messages(Integer unicId, boolean isStoreType) {
    _messageId = unicId;
    _isStoreType = isStoreType;
  }

  public void add(Object l) {
    if (_isStoreType) {
      getStoredArgs().add(String.valueOf(l));
    } else {
      _message = _message.replace(String.format("{%d}", new Object[] { Integer.valueOf(_index) }), 
        String.valueOf(l));
      _index += 1;
    }
  }

  public String toString()
  {
    if (_isStoreType)
      return toString("en");
    return _message;
  }

  public String toString(String lang) {
    if (!_isStoreType)
      return "";
    _message = Localization.getInstance().getString(lang, _messageId);
    if (_message == null) {
      _log.warn("CustomMessage: message with ID \"" + _messageId +
        "\" not found!");
      return "";
    }
    for (String arg : getStoredArgs()) {
      _message = _message.replace(String.format("{%d}", new Object[] { Integer.valueOf(_index) }), arg);
      _index += 1;
    }
    _index = 0;
    return _message;
  }

  private ArrayList<String> getStoredArgs() {
    if (_args == null)
      _args = new ArrayList<String>();
    return _args;
  }
}