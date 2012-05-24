/* 
	coded by Balancer
	balancer@balancer.ru
	http://balancer.ru

	version 0.1, 2005-10-31
*/

package com.l2scoria.util;


import com.l2scoria.crypt.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.regex.Pattern;

public class Strings
{
	//private static Logger _log = Logger.getLogger(Strings.class.getName());

	private static final char hex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	public static final char[] rot13_from = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	public static final char[] rot13_to = "nopqrstuvwxyzabcdefghijklmNOPQRSTUVWXYZABCDEFGHIJKLM".toCharArray();

	public static String bytesToString (byte[] b)
	{
		String ret = "";
		for (int i = 0; i < b.length; i++)
		{
			ret += String.valueOf(hex[(b[i] & 0xF0) >> 4]);
			ret += String.valueOf(hex[b[i] & 0x0F]);
		}
		return ret;
	}

	public static String addSlashes (String s)
	{
		if (s == null)
		{
			return "";
		}

		s = s.replace("\\", "\\\\");
		s = s.replace("\"", "\\\"");
		s = s.replace("@", "\\@");
		s = s.replace("'", "\\'");
		return s;
	}

	public static String stripSlashes (String s)
	{
		s = s.replace("\\'", "'");
		s = s.replace("\\\\", "\\");
		return s;
	}

	public static Integer parseInt (Object x)
	{
		if (x == null)
		{
			return 0;
		}

		if (x instanceof Integer)
		{
			return (Integer) x;
		}

		if (x instanceof Double)
		{
			return ((Double) x).intValue();
		}

		if (x instanceof Boolean)
		{
			return (Boolean) x ? -1 : 0;
		}

		Integer res = 0;
		try
		{
			res = Integer.parseInt("" + x);
		}
		catch (Exception e)
		{
		}
		return res;
	}

	public static Double parseFloat (Object x)
	{
		if (x instanceof Double)
		{
			return (Double) x;
		}

		if (x instanceof Integer)
		{
			return 0.0 + (Integer) x;
		}

		if (x == null)
		{
			return 0.0;
		}

		Double res = 0.0;
		try
		{
			res = Double.parseDouble("" + x);
		}
		catch (Exception e)
		{
		}
		return res;
	}

	public static Boolean parseBoolean (Object x)
	{
		if (x instanceof Integer)
		{
			return (Integer) x != 0;
		}

		if (x == null)
		{
			return false;
		}

		if (x instanceof Boolean)
		{
			return (Boolean) x;
		}

		if (x instanceof Double)
		{
			return Math.abs((Double) x) < 0.00001;
		}

		return !(("" + x).equals(""));
	}

	public static String replace (String str, String regex, int flags, String replace)
	{
		return Pattern.compile(regex, flags).matcher(str).replaceAll(replace);
	}

	public static boolean matches (String str, String regex, int flags)
	{
		return Pattern.compile(regex, flags).matcher(str).matches();
	}

	public static String bbParse (String s)
	{
		if (s == null)
		{
			return null;
		}

		String result;
		result = replace(s, "\r", Pattern.DOTALL, "");
		// *text to highlight* //
		result = replace(result, "(\\s|\"|\'|\\(|^|\n)\\*(.*?)\\*(\\s|\"|\'|\\)|\\?|\\.|!|:|;|,|$|\n)", Pattern.DOTALL, "$1<font color=\"LEVEL\">$2</font>$3");
		// !text to highlight\n
		result = replace(result, "^!(.*?)$", Pattern.MULTILINE, "<font color=\"LEVEL\">$1</font>\n\n");
		// text to %%next line%%
		result = replace(result, "%%\\s*\n", Pattern.DOTALL, "<br1>\n");
		//
		result = replace(s, "@", Pattern.DOTALL, "\" msg=\"");
		if (!result.contains("<br>"))
		{
			result = replace(s, "\n\n+", Pattern.DOTALL, " <br>\n");
		}
		// [link|name]
		result = replace(s, "\\[([^\\]\\|]*?)\\|([^\\]]*?)\\]", Pattern.DOTALL, "<a action=\"bypass -h $1\">$2</a>");
		//if(Config.DEBUG)
		//	_log.info((new StringBuilder()).append("to \n==========================\n").append(s).append("\n==========================\n").toString());

		return result;
	}


	public static String utf2win (String u)
	{
		String w;
		try
		{
			w = new String(u.getBytes("Cp1251"));
		}
		catch (UnsupportedEncodingException e)
		{
			w = u;
		}
		return w;
	}

	@Deprecated
	public static String utf2dos (String u)
	{
		String d;
		try
		{
			d = new String(u.getBytes("Cp866"));
		}
		catch (UnsupportedEncodingException e)
		{
			d = u;
		}
		return d;
	}

	public static String getNonDiff (String s1, String s2)
	{
		int i = 0;
		String tmp = (s1.length() >= s2.length()) ? s2 : s1;

		while (s1.charAt(i) == s2.charAt(i) && i < tmp.length())
		{
			i++;
		}
		return tmp.substring(0, i);
	}

	/**
	 * Capitalizes the first letter of a string, and returns the result.<BR> (Based on ucfirst() function of PHP)
	 *
	 * @param String str
	 *
	 * @return String containing the modified string.
	 */
	public static String capitalizeFirst (String str)
	{
		str = str.trim();

		if (str.length() > 0 && Character.isLetter(str.charAt(0)))
		{
			return str.substring(0, 1).toUpperCase() + str.substring(1);
		}

		return str;
	}

	/**
	 * Capitalizes the first letter of every "word" in a string.<BR> (Based on ucwords() function of PHP)
	 *
	 * @param String str
	 *
	 * @return String containing the modified string.
	 */
	public static String capitalizeWords (String str)
	{
		char[] charArray = str.toCharArray();
		String result = "";

		// Capitalize the first letter in the given string!
		charArray[0] = Character.toUpperCase(charArray[0]);

		for (int i = 0; i < charArray.length; i++)
		{
			if (Character.isWhitespace(charArray[i]))
			{
				charArray[i + 1] = Character.toUpperCase(charArray[i + 1]);
			}

			result += Character.toString(charArray[i]);
		}

		return result;
	}

	/**
	 * Returns the number of "words" in a given string.
	 *
	 * @param String str
	 *
	 * @return int numWords
	 */
	public static int countWords (String str)
	{
		return str.trim().split(" ").length;
	}

	/**
	 * Returns a delimited string for an given array of string elements.<BR> (Based on implode() in PHP)
	 *
	 * @param String[] strArray
	 * @param String   strDelim
	 *
	 * @return String implodedString
	 */
	public static String implode (String[] list, String strDelim)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.length - 1; i++)
		{
			sb.append(list[i]).append(strDelim);
		}
		return sb.append(list[list.length - 1]).toString();
	}

	/**
	 * Returns a delimited string for an given collection of string elements.<BR> (Based on implode() in PHP)
	 *
	 * @param Collection&lt;String&gt; strCollection
	 * @param String				   strDelim
	 *
	 * @return String implodedString
	 */
	public static String implode (Collection<String> strCollection, String strDelim)
	{
		return implode(strCollection.toArray(new String[strCollection.size()]), strDelim);
	}

	public static String getFormatted (String s, int len)
	{
		if (s == null)
		{
			return "";
		}
		int slen = s.length();
		for (int i = 0; i < (len - slen); i++)
		{
			s += " ";
		}
		return s;
	}// PHP ;-)

	public static String str_rot13 (String str)
	{
		int i;
		char[] xlat = new char[256];
		if (str == null || str.length() < 1)
		{
			return str;
		}
		for (i = 0; i < 52; xlat[rot13_from[i]] = rot13_to[i], i++)
		{
			;
		}
		char[] tmp = str.toCharArray();
		for (i = 0; i < str.length(); i++)
		{
			if (tmp[i] > 'a' && tmp[i] < 'Z')
			{
				tmp[i] = xlat[tmp[i]];
			}
		}
		return new String(tmp);
	}

	public static int mhash (byte data[])
	{
		int ret = 0;
		if (data != null)
		{
			for (int i = 0; i < data.length; i++)
			{
				byte element = data[i];
				ret = 7 * ret + element;
			}
		}
		return Math.abs(ret);
	}

	public static boolean isAlphaNumeric (String text)
	{
		if (text == null)
		{
			return false;
		}
		char[] chars = text.toCharArray();
		for (char aChar : chars)
		{
			if (!Character.isLetterOrDigit(aChar))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Return amount of adena formatted with "," delimiter
	 *
	 * @param amount
	 *
	 * @return String formatted adena amount
	 */
	public static String formatAdena (int amount)
	{
		String s = "";
		int rem = amount % 1000;
		s = Integer.toString(rem);
		amount = (amount - rem) / 1000;
		while (amount > 0)
		{
			if (rem < 99)
			{
				s = '0' + s;
			}
			if (rem < 9)
			{
				s = '0' + s;
			}
			rem = amount % 1000;
			s = Integer.toString(rem) + "," + s;
			amount = (amount - rem) / 1000;
		}
		return s;
	}

	/**
	 * @param string
	 *
	 * @return decoded string
	 *
	 * @throws java.io.UnsupportedEncodingException
	 *
	 */
	public static String getText (String string)
	{
		try
		{
			return new String(Base64.decode(string), "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}

	/**
	 * @param string
	 *
	 * @return decoded string
	 *
	 * @see Strigns#getText(java.lang.String)
	 *
	 */
	public static String getText0(String string)
	{
		return getText(getText(string));
	}

	/***
	 * Склеивалка для строк
	 * @param glueStr - строка разделитель, может быть пустой строкой или null
	 * @param strings - массив из строк которые надо склеить
	 * @param startIdx - начальный индекс, если указать отрицательный то он отнимется от количества строк
	 * @param maxCount - мескимум элементов, если 0 - вернутся пустая строка, если отрицательный то учитыватся не будет
	 */
	public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
	{
		String result = "";
		if(startIdx < 0)
		{
			startIdx += strings.length;
			if(startIdx < 0)
				return result;
		}
		while(startIdx < strings.length && maxCount != 0)
		{
			if(!result.isEmpty() && glueStr != null && !glueStr.isEmpty())
				result += glueStr;
			result += strings[startIdx++];
			maxCount--;
		}
		return result;
	}

	/***
	 * Склеивалка для строк
	 * @param glueStr - строка разделитель, может быть пустой строкой или null
	 * @param strings - массив из строк которые надо склеить
	 * @param startIdx - начальный индекс, если указать отрицательный то он отнимется от количества строк
	 */
	public static String joinStrings(String glueStr, String[] strings, int startIdx)
	{
		return joinStrings(glueStr, strings, startIdx, -1);
	}

	/***
	 * Склеивалка для строк
	 * @param glueStr - строка разделитель, может быть пустой строкой или null
	 * @param strings - массив из строк которые надо склеить
	 */
	public static String joinStrings(String glueStr, String[] strings)
	{
		return joinStrings(glueStr, strings, 0);
	}
}
