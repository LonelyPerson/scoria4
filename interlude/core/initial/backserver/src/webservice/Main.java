/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webservice;

/**
 *
 * @author zenn
 */
public class Main {


	public static void main(String []args) {
                        Config.load();
                        info();
			new Server();
        }

        public static void info()
        {
                        log("            #######                                                      ");
                        log("          /       ###                                    #               ");
                        log("         /         ##                                   ###              ");
                        log("         ##        #                                     #               ");
                        log("          ###                                                            ");
                        log("         ## ###           /###      /###   ###  /###   ###       /###    ");
                        log("          ### ###        / ###  /  / ###  / ###/ #### / ###     / ###  / ");
                        log("            ### ###     /   ###/  /   ###/   ##   ###/   ##    /   ###/  ");
                        log("              ### /##  ##        ##    ##    ##          ##   ##    ##   ");
                        log("                #/ /## ##        ##    ##    ##          ##   ##    ##   ");
                        log("                 #/ ## ##        ##    ##    ##          ##   ##    ##   ");
                        log("                  # /  ##        ##    ##    ##          ##   ##    ##   ");
                        log("        /##        /   ###     / ##    ##    ##          ##   ##    /#   ");
                        log("       /  ########/     ######/   ######     ###         ### / ####/ ##  ");
                        log("      /     #####        #####     ####       ###         ##/   ###   ## ");
                        log("      |                      Developed by scoria.ru                      ");
                        log("       \\)                     Copyright 2008-2011                       ");
                        log("INFO: BackEnd webServer loaded on "+Config.BACKSERVER_IP+":"+Config.PORT);
        }

        public static void log(String value) {
            System.out.println(value);
        }

}
