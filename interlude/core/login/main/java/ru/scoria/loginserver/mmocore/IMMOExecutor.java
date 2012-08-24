package ru.scoria.loginserver.mmocore;

public interface IMMOExecutor<T extends MMOConnection<T>>
{
	public void execute(ReceivablePacket<T> packet);
}