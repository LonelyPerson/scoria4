package com.l2scoria.gameserver.geodata.loader;

import com.l2scoria.gameserver.geodata.GeoEngine;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

public class OffGeoLoader extends AbstractGeoLoader
{

	private static final Pattern PATTERN = Pattern.compile("[\\d]{2}_[\\d]{2}_conv.dat");

	protected byte[][] parse(byte[] data)
	{

		if (data.length <= 393234)
		{ // 18 + ((256 * 256) * (2 * 3)) - it's minimal size of geodata (whole region with flat blocks)
			return null;
		}

		// Indexing geo files, so we will know where each block starts
		int index = 18; // Skip firs 18 bytes, they have nothing with data;

		byte[][] blocks = new byte[65536][]; // 256 * 256 блоков в регионе геодаты

		for (int block = 0, n = blocks.length; block < n; block++)
		{
			short type = makeShort(data[index + 1], data[index]);
			index += 2;

			byte[] geoBlock;
			if (type == 0)
			{

				// Создаем блок геодаты
				geoBlock = new byte[2 + 1];

				// Читаем нужные даные с геодаты
				geoBlock[0] = GeoEngine.BLOCKTYPE_FLAT;
				geoBlock[1] = data[index + 2];
				geoBlock[2] = data[index + 3];

				// Добавляем блок геодаты
				blocks[block] = geoBlock;
				index += 4;
			}
			else if (type == 0x0040)
			{

				// Создаем блок геодаты
				geoBlock = new byte[128 + 1];

				// Читаем даные с геодаты
				geoBlock[0] = GeoEngine.BLOCKTYPE_COMPLEX;
				System.arraycopy(data, index, geoBlock, 1, 128);

				// Увеличиваем индекс
				index += 128;

				// Добавляем блок геодаты
				blocks[block] = geoBlock;
			}
			else
			{

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				baos.write(GeoEngine.BLOCKTYPE_MULTILEVEL);

				// Т.к. у нас нет фиксированой длинны геодаты, то делаем конвертацию на лету
				for (int b = 0; b < 64; b++)
				{
					byte layers = (byte) makeShort(data[index + 1], data[index]);
					if (layers > GeoEngine.MAX_LAYERS)
					{
						GeoEngine.MAX_LAYERS = layers;
					}

					index += 2;

					baos.write(layers);
					for (int i = 0; i < layers << 1; i++)
					{
						baos.write(data[index++]);
					}
				}

				// Добавляем даные в масив
				blocks[block] = baos.toByteArray();
			}
		}

		return blocks;
	}

	protected short makeShort(byte b1, byte b0)
	{
		return (short) (b1 << 8 | b0 & 0xff);
	}

	public Pattern getPattern()
	{
		return PATTERN;
	}

	public byte[] convert(byte[] data)
	{
		return data;
	}
}
