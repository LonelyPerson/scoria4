package com.l2scoria.telnet;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <b>пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅ.</b><br>
 * 
 * @author ProGramMoS
 */

public class Information
{
	/**
	 * пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ.
	 */
	public static void printCpuInfo(PrintWriter type)
	{
		type.println("Avaible CPU(s): " + Runtime.getRuntime().availableProcessors());
		type.println("Processor(s) Identifier: " + System.getenv("PROCESSOR_IDENTIFIER"));
		type.println("..................................................");
		type.println("..................................................");
	}

	/**
	 * пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅ пїЅпїЅ.
	 */
	public static void printOSInfo(PrintWriter type)
	{
		type.println("OS: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version"));
		type.println("OS Arch: " + System.getProperty("os.arch"));
		type.println("..................................................");
		type.println("..................................................");
	}

	/**
	 * пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅ JAVA Runtime Enviroment
	 */
	public static void printJreInfo(PrintWriter type)
	{
		type.println("Java Platform Information");
		type.println("Java Runtime  Name: " + System.getProperty("java.runtime.name"));
		type.println("Java Version: " + System.getProperty("java.version"));
		type.println("Java Class Version: " + System.getProperty("java.class.version"));
		type.println("..................................................");
		type.println("..................................................");
	}

	/**
	 * пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅ пїЅпїЅпїЅпїЅпїЅпїЅ.
	 */
	public static void printRuntimeInfo(PrintWriter type)
	{
		type.println("Runtime Information");
		type.println("Current Free Heap Size: " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + " mb");
		type.println("Current Heap Size: " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + " mb");
		type.println("Maximum Heap Size: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " mb");
		type.println("..................................................");
		type.println("..................................................");

	}

	/**
	 * пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅ.
	 */
	public static void printSystemTime(PrintWriter type)
	{
		Date dateInfo = new Date();
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa");
		String dayInfo = df.format(dateInfo);

		type.println("..................................................");
		type.println("System Time: " + dayInfo);
		type.println("..................................................");
	}

	/**
	 * пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅ-пїЅпїЅпїЅпїЅпїЅпїЅ.
	 */
	public static void printJvmInfo(PrintWriter type)
	{
		type.println("Virtual Machine Information (JVM)");
		type.println("JVM Name: " + System.getProperty("java.vm.name"));
		type.println("JVM installation directory: " + System.getProperty("java.home"));
		type.println("JVM version: " + System.getProperty("java.vm.version"));
		type.println("JVM Vendor: " + System.getProperty("java.vm.vendor"));
		type.println("JVM Info: " + System.getProperty("java.vm.info"));
		type.println("..................................................");
		type.println("..................................................");
	}
}
