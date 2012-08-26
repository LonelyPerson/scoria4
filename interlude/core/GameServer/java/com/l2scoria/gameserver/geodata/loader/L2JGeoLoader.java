package com.l2scoria.gameserver.geodata.loader;

import com.l2scoria.gameserver.geodata.GeoEngine;

import java.util.logging.Logger;
import java.util.regex.Pattern;

public class L2JGeoLoader extends AbstractGeoLoader
{

	protected static final Logger log = Logger.getLogger(L2JGeoLoader.class.getName());

	private static final Pattern PATTERN = Pattern.compile("[\\d]{2}_[\\d]{2}.l2j");

	protected byte[][] parse(byte[] data)
	{

		if (data.length <= 196608)
		{ // 256 * 256 * 3 - it's minimal size of geodata (whole region with flat blocks)
			return null;
		}

		byte[][] blocks = new byte[65536][]; // 256 * 256 блоков в регионе геодаты

		// Indexing geo files, so we will know where each block starts

		int index = 0;

		for (int block = 0, n = blocks.length; block < n; block++)
		{
			byte type = data[index];
			index++;

			byte[] geoBlock;
			switch (type)
			{
				case GeoEngine.BLOCKTYPE_FLAT:

					// Создаем блок геодаты
					geoBlock = new byte[2 + 1];

					// Читаем нужные даные с геодаты
					geoBlock[0] = type;
					geoBlock[1] = data[index];
					geoBlock[2] = data[index + 1];

					// Добавляем блок геодаты
					blocks[block] = geoBlock;
					index += 2;
					break;

				case GeoEngine.BLOCKTYPE_COMPLEX:

					// Создаем блок геодаты
					geoBlock = new byte[128 + 1];

					// Читаем даные с геодаты
					geoBlock[0] = type;
					System.arraycopy(data, index, geoBlock, 1, 128);

					// Увеличиваем индекс
					index += 128;

					// Добавляем блок геодаты
					blocks[block] = geoBlock;
					break;

				case GeoEngine.BLOCKTYPE_MULTILEVEL:
					// Оригинальный индекс
					int orgIndex = index;

					// Считаем длинну блока геодаты
					for (int b = 0; b < 64; b++)
					{
						byte layers = data[index];
						if (layers > GeoEngine.MAX_LAYERS)
						{
							GeoEngine.MAX_LAYERS = layers;
						}
						index += (layers << 1) + 1;
					}

					// Получаем длинну
					int diff = index - orgIndex;

					// Создаем массив геодаты
					geoBlock = new byte[diff + 1];

					// Читаем даные с геодаты
					geoBlock[0] = type;
					System.arraycopy(data, orgIndex, geoBlock, 1, diff);

					// Добавляем блок геодаты
					blocks[block] = geoBlock;
					break;
				default:
					log.severe("GeoEngine: invalid block type: " + type);
			}
		}

		return blocks;
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
