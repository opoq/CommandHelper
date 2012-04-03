/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author Layton
 */
public class PlayerManagement {

    public static String docs() {
        return "This class of functions allow players to be managed";
    }

    @api
    public static class player extends AbstractFunction {

        public String getName() {
            return "player";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            
            if (args.length == 1) {
                p = Static.GetPlayer(args[0]);
            }            
            
            if (p != null && p.instanceofPlayer()) {
                return new CString(((MCPlayer) p).getName(), t);
            } else if (p != null && p.instanceofMCConsoleCommandSender()) {
                return new CString("~console", t);
            } else {
                return new CNull(t);
            }
        }

        public String docs() {
            return "string {[playerName]} Returns the full name of the partial Player name specified or the Player running the command otherwise. If the command is being run from"
                    + " the console, then the string '~console' is returned. If the command is coming from elsewhere, null is returned, and the behavior is undefined."
                    + " Note that most functions won't support the user '~console' (they'll throw a PlayerOfflineException), but you can use this to determine"
                    + " where a command is being run from.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class all_players extends AbstractFunction {

        public String getName() {
            return "all_players";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCPlayer[] pa = Static.getServer().getOnlinePlayers();
            CString[] sa = new CString[pa.length];
            for (int i = 0; i < pa.length; i++) {
                sa[i] = new CString(pa[i].getName(), t);
            }
            return new CArray(t, sa);
        }

        public String docs() {
            return "array {} Returns an array of all the player names of all the online players on the server";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class ploc extends AbstractFunction {

        public String getName() {
            return "ploc";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 1) {
                m = Static.GetPlayer(args[0]);
            }
            Static.AssertPlayerNonNull(m, t);
            MCLocation l = m.getLocation();
            MCWorld w = m.getWorld();
            return new CArray(t,
                    new CDouble(l.getX(), t),
                    new CDouble(l.getY() - 1, t),
                    new CDouble(l.getZ(), t),
                    new CString(w.getName(), t));
        }

        public String docs() {
            return "array {[playerName]} Returns an array of x, y, z coords of the player specified, or the player running the command otherwise. Note that the y coordinate is"
                    + " in relation to the block the player is standing on. The array returned will also include the player's world in index 3 of the array.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class set_ploc extends AbstractFunction {

        public String getName() {
            return "set_ploc";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3, 4};
        }

        public String docs() {
            return "boolean {[player], locationArray | [player], x, y, z} Sets the location of the player to the specified coordinates. If the coordinates"
                    + " are not valid, or the player was otherwise prevented from moving, false is returned, otherwise true. If player is omitted, "
                    + " the current player is used. Note that 1 is automatically added to the y component, which means that sending a player to"
                    + " x, y, z coordinates shown with F3 will work as expected, instead of getting them stuck inside the floor. ";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            String MCPlayer = null;
            double x;
            double y;
            double z;
            MCPlayer m = null;
            MCLocation l = null;
            if (args.length == 1) {
                if (args[0] instanceof CArray) {
                    CArray ca = (CArray) args[0];
                    l = ObjectGenerator.GetGenerator().location(ca, (p instanceof MCPlayer ? ((MCPlayer) p).getWorld() : null), t);
                    x = Static.getNumber(ca.get(0, t));
                    y = Static.getNumber(ca.get(1, t));
                    z = Static.getNumber(ca.get(2, t));
                    if (p instanceof MCPlayer) {
                        m = ((MCPlayer) p);
                    }

                } else {
                    throw new ConfigRuntimeException("Expecting an array at parameter 1 of set_ploc",
                            ExceptionType.CastException, t);
                }
            } else if (args.length == 2) {
                if (args[1] instanceof CArray) {
                    CArray ca = (CArray) args[1];
                    MCPlayer = args[0].val();
                    l = ObjectGenerator.GetGenerator().location(ca, Static.GetPlayer(MCPlayer, t).getWorld(), t);
                    x = l.getX();
                    y = l.getY();
                    z = l.getZ();
                } else {
                    throw new ConfigRuntimeException("Expecting parameter 2 to be an array in set_ploc",
                            ExceptionType.CastException, t);
                }
            } else if (args.length == 3) {
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
                x = Static.getNumber(args[0]);
                y = Static.getNumber(args[1]);
                z = Static.getNumber(args[2]);
                l = m.getLocation();
            } else {
                MCPlayer = args[0].val();
                x = Static.getNumber(args[1]);
                y = Static.getNumber(args[2]);
                z = Static.getNumber(args[3]);
                l = StaticLayer.GetLocation(Static.GetPlayer(MCPlayer, t).getWorld(), x, y, z, 0, 0);
            }
            if (m == null && MCPlayer != null) {
                m = Static.GetPlayer(MCPlayer, t);
            }
            Static.AssertPlayerNonNull(m, t);
            return new CBoolean(m.teleport(StaticLayer.GetLocation(l.getWorld(), x, y + 1, z, m.getLocation().getYaw(), m.getLocation().getPitch())), t);
        }
    }

    @api
    public static class pcursor extends AbstractFunction {

        public String getName() {
            return "pcursor";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "array {[player]} Returns an array with the (x, y, z, world) coordinates of the block the player has highlighted"
                    + " in their crosshairs. If player is omitted, the current player is used. If the block is too far, a"
                    + " RangeException is thrown.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_2;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (args.length == 0) {
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
            } else {
                m = Static.GetPlayer(args[0]);
            }
            Static.AssertPlayerNonNull(m, t);
            MCBlock b = m.getTargetBlock(null, 10000);
            if (b == null) {
                throw new ConfigRuntimeException("No block in sight, or block too far",
                        ExceptionType.RangeException, t);
            }
            return new CArray(t, new CInt(b.getX(), t),
                    new CInt(b.getY(), t),
                    new CInt(b.getZ(), t),
                    new CString(b.getWorld().getName(), t));
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class kill extends AbstractFunction {

        public String getName() {
            return "kill";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (args.length == 1) {
                m = Static.GetPlayer(args[0]);
            } else {
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
            }
            Static.AssertPlayerNonNull(m, t);
            m.setHealth(0);
            return new CVoid(t);
        }

        public String docs() {
            return "void {[playerName]} Kills the specified player, or the current player if it is omitted";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class pgroup extends AbstractFunction {

        public String getName() {
            return "pgroup";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (args.length == 0) {
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
            } else {
                m = Static.GetPlayer(args[0]);
            }

            Static.AssertPlayerNonNull(m, t);
            
            String[] sa = Static.getPermissionsResolverManager().getGroups(m.getName());
            Construct[] ca = new Construct[sa.length];
            for (int i = 0; i < sa.length; i++) {
                ca[i] = new CString(sa[i], t);
            }
            CArray a = new CArray(t, ca);
            return a;
        }

        public String docs() {
            return "array {[playerName]} Returns an array of the groups a player is in. If playerName is omitted, the current player is used.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class pinfo extends AbstractFunction {

        public String getName() {
            return "pinfo";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "mixed {[pName], [value]} Returns various information about the player specified, or the current player if no argument was given."
                    + "If value is set, it should be an integer of one of the following indexes, and only that information for that index"
                    + " will be returned. Otherwise if value is not specified (or is -1), it returns an array of"
                    + " information with the following pieces of information in the specified index: "
                    + "<ul><li>0 - player's name; This will return the player's exact name, "
                    + " even if called with a partial match.</li><li>1 - player's location; an array of the player's xyz coordinates</li><li>2 - player's cursor; an array of the "
                    + "location of the player's cursor, or null if the block is out of sight.</li><li>3 - player's IP; Returns the IP address of this player.</li><li>4 - Display name; The name that is used when the"
                    + " player's name is displayed on screen typically. </li><li>5 - player's health; Gets the current health of the player, which will be an int"
                    + " from 0-20.</li><li>6 - Item in hand; The value returned by this will be similar to the value returned by get_block_at()</li><li>7 - "
                    + "World name; Gets the name of the world this player is in.</li><li>8 - Is Op; true or false if this player is an op.</li><li>9 - player groups;"
                    + " An array of the permissions groups the player is in.</li><li>10 - The player's hostname (or IP if a hostname can't be found)</li>"
                    + " <li>11 - Is sneaking?</li></ul>";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender m = env.GetCommandSender();
            String player = "";
            int index = -1;
            if (args.length == 0) {
                player = (m instanceof MCPlayer ? ((MCPlayer) m).getName() : null);
                index = -1;
            } else if (args.length == 1) {
                player = args[0].val();
                index = -1;
            } else {
                player = args[0].val();
                index = (int) Static.getInt(args[1]);
            }
            
            MCPlayer p = Static.GetPlayer(player, t);

            Static.AssertPlayerNonNull(p, t);
            if (index < -1 || index > 11) {
                throw new ConfigRuntimeException("pinfo expects the index to be between -1 and 11",
                        ExceptionType.RangeException, t);
            }
            assert index >= -1 && index <= 11;
            ArrayList<Construct> retVals = new ArrayList<Construct>();
            if (index == 0 || index == -1) {
                //MCPlayer name 
                retVals.add(new CString(p.getName(), t));
            }
            if (index == 1 || index == -1) {
                //MCPlayer location
                retVals.add(new CArray(t, new CDouble(p.getLocation().getX(), t),
                        new CDouble(p.getLocation().getY() - 1, t), new CDouble(p.getLocation().getZ(), t)));
            }
            if (index == 2 || index == -1) {
                //MCPlayer cursor
                MCBlock b = p.getTargetBlock(null, 200);
                if (b == null) {
                    retVals.add(new CNull(t));
                } else {
                    retVals.add(new CArray(t, new CInt(b.getX(), t), new CInt(b.getY(), t), new CInt(b.getZ(), t)));
                }
            }
            if (index == 3 || index == -1) {
                //MCPlayer IP                
                retVals.add(new CString(p.getAddress().getAddress().getHostAddress(), t));
            }
            if (index == 4 || index == -1) {
                //Display name
                retVals.add(new CString(p.getDisplayName(), t));
            }
            if (index == 5 || index == -1) {
                //MCPlayer health
                retVals.add(new CInt((long) p.getHealth(), t));
            }
            if (index == 6 || index == -1) {
                //Item in hand
                MCItemStack is = p.getItemInHand();
                int data;
                if(is.getTypeId() < 256){
                    if (is.getData() != null) {
                        data = is.getData().getData();
                    } else {
                        data = 0;
                    }
                } else {
                    data = is.getDurability();
                }
                retVals.add(new CString(is.getTypeId() + ":" + data, t));
            }
            if (index == 7 || index == -1) {
                //World name
                retVals.add(new CString(p.getWorld().getName(), t));
            }
            if (index == 8 || index == -1) {
                //Is op
                retVals.add(new CBoolean(p.isOp(), t));
            }
            if (index == 9 || index == -1) {
                //MCPlayer groups
                String[] sa = Static.getPermissionsResolverManager().getGroups(p.getName());
                Construct[] ca = new Construct[sa.length];
                for (int i = 0; i < sa.length; i++) {
                    ca[i] = new CString(sa[i], t);
                }
                CArray a = new CArray(t, ca);
                retVals.add(a);
            }
            if (index == 10 || index == -1) {
                String hostname = p.getAddress().getAddress().getHostAddress();
                if(CommandHelperPlugin.hostnameLookupCache.containsKey(p.getName())){
                    hostname = CommandHelperPlugin.hostnameLookupCache.get(p.getName());
                }
                retVals.add(new CString(hostname, t));
            }
            if(index == 11 || index == -1){
                retVals.add(new CBoolean(p.isSneaking(), t));
            }
            if (retVals.size() == 1) {
                return retVals.get(0);
            } else {
                CArray ca = new CArray(t);
                for (Construct c : retVals) {
                    ca.push(c);
                }
                return ca;
            }
        }
    }

    @api
    public static class pworld extends AbstractFunction {

        public String getName() {
            return "pworld";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "string {[playerName]} Gets the world of the player specified, or the current player, if playerName isn't specified.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_0;
        }

        public Boolean runAsync() {
            return true;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (args.length == 0) {
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
            } else {
                m = Static.GetPlayer(args[0]);
            }
            Static.AssertPlayerNonNull(m, t);
            return new CString(m.getWorld().getName(), t);
        }
    }

    @api
    public static class kick extends AbstractFunction {

        public String getName() {
            return "kick";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "void {[playerName], [message]} Kicks the specified player, with an optional message. If no message is specified, "
                    + "\"You have been kicked\" is used. If no player is specified, the current player is used, with the default message.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            String message = "You have been kicked";
            MCPlayer m = null;
            if (args.length == 0) {
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
            }
            if (args.length >= 1) {
                m = Static.GetPlayer(args[0]);
            }
            if (args.length >= 2) {
                message = args[1].val();
            }
            MCPlayer ptok = m;
            Static.AssertPlayerNonNull(ptok, t);
            ptok.kickPlayer(message);
            return new CVoid(t);
        }
    }

    @api
    public static class set_display_name extends AbstractFunction {

        public String getName() {
            return "set_display_name";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {playerName, newDisplayName | newDisplayName} Sets a player's display name. If the second usage is used,"
                    + " it sets the display name of the player running the command. See reset_display_name also. playerName, as well"
                    + " as all CommandHelper commands expect the player's real name, not their display name.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_2;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer MCPlayer = null;
            String name;
            if (args.length == 1) {
                if (p instanceof MCPlayer) {
                    MCPlayer = (MCPlayer) p;
                }
                name = args[0].val();
            } else {
                MCPlayer = Static.GetPlayer(args[0]);
                name = args[1].val();
            }
            Static.AssertPlayerNonNull(MCPlayer, t);
            MCPlayer.setDisplayName(name);
            return new CVoid(t);
        }
    }

    @api
    public static class reset_display_name extends AbstractFunction {

        public String getName() {
            return "reset_display_name";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "void {[playerName]} Resets a player's display name to their real name. If playerName isn't specified, defaults to the"
                    + " player running the command.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_2;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer MCPlayer = null;
            if (args.length == 0) {
                if (p instanceof MCPlayer) {
                    MCPlayer = (MCPlayer) p;
                }
            } else {
                MCPlayer = Static.GetPlayer(args[0]);
            }
            Static.AssertPlayerNonNull(MCPlayer, t);
            MCPlayer.setDisplayName(MCPlayer.getName());
            return new CVoid(t);
        }
    }

    @api
    public static class pfacing extends AbstractFunction {

        public String getName() {
            return "pfacing";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2, 3};
        }

        public String docs() {
            return "mixed {F | yaw, pitch | player, F | player, yaw, pitch | player | &lt;none&gt;} Sets the direction the player is facing. When using the first variation, expects an integer 0-3, which will"
                    + " set the direction the player faces using their existing pitch (up and down) but sets their yaw (left and right) to one of the"
                    + " cardinal directions, as follows: 0 - West, 1 - South, 2 - East, 3 - North, which corresponds to the directions given by F when"
                    + " viewed with F3. In the second variation, specific yaw and pitches can be provided. If the player is not specified, the current player"
                    + " is used. If just the player is specified, that player's yaw and pitch are returned as an array, or if no arguments are given, the"
                    + " player running the command's yaw and pitch are returned as an array. The function returns void when setting the values. (Note that while this"
                    + " function looks like it has ambiguous arguments, players cannot be named numbers.) A note on numbers: The values returned by the getter will always be"
                    + " as such: pitch will always be a number between 90 and -90, with -90 being the player looking up, and 90 being the player looking down. Yaw will"
                    + " always be a number between 0 and 359.9~. When using it as a setter, pitch must be a number between -90 and 90, and yaw may be any number."
                    + " If the number given is not between 0 and 359.9~, it will be normalized first. 0 is dead west, 90 is north, etc.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            //Getter
            if (args.length == 0 || args.length == 1) {
                MCLocation l = null;
                if (args.length == 0) {
                    if (p instanceof MCPlayer) {
                        l = ((MCPlayer) p).getLocation();
                    }
                } else if (args.length == 1) {
                    //if it's a number, we are setting F. Otherwise, it's a getter for the MCPlayer specified.
                    try {
                        Integer.parseInt(args[0].val());
                    } catch (NumberFormatException e) {
                        MCPlayer p2 = Static.GetPlayer(args[0]);
                        l = p2.getLocation();                        
                    }
                }
                if (l != null) {
                    float yaw = l.getYaw();
                    float pitch = l.getPitch();
                    //normalize yaw
                    if (yaw < 0) {
                        yaw = (((yaw) % 360) + 360);
                    }
                    return new CArray(t, new CDouble(yaw, t), new CDouble(pitch, t));
                }
            }
            //Setter
            MCPlayer toSet = null;
            float yaw = 0;
            float pitch = 0;
            if (args.length == 1) {
                //We are setting F for this MCPlayer
                if (p instanceof MCPlayer) {
                    toSet = (MCPlayer) p;
                    pitch = toSet.getLocation().getPitch();
                }
                int g = (int) Static.getInt(args[0]);
                if (g < 0 || g > 3) {
                    throw new ConfigRuntimeException("The F specifed must be from 0 to 3",
                            ExceptionType.RangeException, t);
                }
                yaw = g * 90;
            } else if (args.length == 2) {
                //Either we are setting this MCPlayer's pitch and yaw, or we are setting the specified MCPlayer's F.
                //Check to see if args[0] is a number
                try {
                    Float.parseFloat(args[0].val());
                    //It's the yaw, pitch variation
                    if (p instanceof MCPlayer) {
                        toSet = (MCPlayer) p;
                    }
                    yaw = (float) Static.getNumber(args[0]);
                    pitch = (float) Static.getNumber(args[1]);
                } catch (NumberFormatException e) {
                    //It's the MCPlayer, F variation
                    toSet = Static.GetPlayer(args[0]);
                    pitch = toSet.getLocation().getPitch();
                    int g = (int) Static.getInt(args[1]);
                    if (g < 0 || g > 3) {
                        throw new ConfigRuntimeException("The F specifed must be from 0 to 3",
                                ExceptionType.RangeException, t);
                    }
                    yaw = g * 90;
                }
            } else if (args.length == 3) {
                //It's the MCPlayer, yaw, pitch variation
                toSet = Static.GetPlayer(args[0]);
                yaw = (float) Static.getNumber(args[1]);
                pitch = (float) Static.getNumber(args[2]);
            }

            //Error check our data
            if (pitch > 90 || pitch < -90) {
                throw new ConfigRuntimeException("pitch must be between -90 and 90",
                        ExceptionType.RangeException, t);
            }
            MCLocation l = toSet.getLocation().clone();
            l.setPitch(pitch);
            l.setYaw(yaw);
            toSet.teleport(l);
            return new CVoid(t);
        }
    }    

    @api
    public static class pmode extends AbstractFunction {

        public String getName() {
            return "pmode";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "string {[player]} Returns the player's game mode. It will be one of \"CREATIVE\" or \"SURVIVAL\".";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 1) {
                m = Static.GetPlayer(args[0]);
            }
            
            Static.AssertPlayerNonNull(m, t);
            String mode = m.getGameMode().name();
            return new CString(mode, t);
        }
    }

    @api
    public static class set_pmode extends AbstractFunction {

        public String getName() {
            return "set_pmode";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player], mode} Sets the player's game mode. mode must be either \"CREATIVE\" or \"SURVIVAL\""
                    + " (case doesn't matter)";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            String mode = "";
            MCGameMode gm;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0]);
                mode = args[1].val();
            } else {
                mode = args[0].val();
            }

            try {
                gm = MCGameMode.valueOf(mode.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ConfigRuntimeException("Mode must be either 'CREATIVE' or 'SURVIVAL'", ExceptionType.FormatException, t);
            }
            Static.AssertPlayerNonNull(m, t);
            m.setGameMode(gm);
            return new CVoid(t);
        }
    }

    @api
    public static class pexp extends AbstractFunction {

        public String getName() {
            return "pexp";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "int {[player]} Gets the experience of a player within this level, as a percentage, from 0 to 99. (100 would be next level,"
                    + " therefore, 0.)";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 1) {
                m = Static.GetPlayer(args[0].val(), t);
            }
            Static.AssertPlayerNonNull(m, t);
            return new CInt((int) (m.getExp() * 100), t);
        }
    }

    @api
    public static class set_pexp extends AbstractFunction {

        public String getName() {
            return "set_pexp";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player], xp} Sets the experience of a player within the current level, as a percentage, from 0 to 100.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            int xp = 0;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), t);
                xp = (int) Static.getInt(args[1]);
            } else {
                xp = (int) Static.getInt(args[0]);
            }
            Static.AssertPlayerNonNull(m, t);
            m.setExp(((float) xp) / 100.0F);
            return new CVoid(t);
        }
    }

    @api
    public static class give_pexp extends AbstractFunction {

        public String getName() {
            return "give_pexp";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player], exp} Gives the player the specified amount of xp.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return true;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = environment.GetCommandSender();
            MCPlayer m = null;
            int xp = 0;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), t);
                xp = (int) Static.getInt(args[1]);
            } else {
                xp = (int) Static.getInt(args[0]);
            }
            Static.AssertPlayerNonNull(m, t);
            m.giveExp(xp);

            return new CVoid(t);
        }
    }

    @api
    public static class plevel extends AbstractFunction {

        public String getName() {
            return "plevel";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "int {[player]} Gets the player's level.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 1) {
                m = Static.GetPlayer(args[0].val(), t);
            }
            Static.AssertPlayerNonNull(m, t);
            return new CInt(m.getLevel(), t);
        }
    }

    @api
    public static class set_plevel extends AbstractFunction {

        public String getName() {
            return "set_plevel";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player], level} Sets the level of a player.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            int level = 0;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), t);
                level = (int) Static.getInt(args[1]);
            } else {
                level = (int) Static.getInt(args[0]);
            }
            Static.AssertPlayerNonNull(m, t);
            m.setLevel(level);
            return new CVoid(t);
        }
    }

    @api
    public static class ptexp extends AbstractFunction {

        public String getName() {
            return "ptexp";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "int {[player]} Gets the total experience of a player.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 1) {
                m = Static.GetPlayer(args[0].val(), t);
            }
            Static.AssertPlayerNonNull(m, t);
            return new CInt(m.getTotalExperience(), t);
        }
    }

    @api
    public static class set_ptexp extends AbstractFunction {

        public String getName() {
            return "set_ptexp";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player], xp} Sets the total experience of a player.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            int xp = 0;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), t);
                xp = (int) Static.getInt(args[1]);
            } else {
                xp = (int) Static.getInt(args[0]);
            }
            Static.AssertPlayerNonNull(m, t);
            m.setTotalExperience(xp);
//            m.setLevel(0);
//            m.setExp(0);
//            m.setTotalExperience(0);
//            m.giveExp(xp);
            return new CVoid(t);
        }
    }

    @api
    public static class pfood extends AbstractFunction {

        public String getName() {
            return "pfood";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "int {[player]} Returns the player's current food level.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 1) {
                m = Static.GetPlayer(args[0].val(), t);
            }
            Static.AssertPlayerNonNull(m, t);
            return new CInt(m.getFoodLevel(), t);
        }
    }

    @api
    public static class set_pfood extends AbstractFunction {

        public String getName() {
            return "set_pfood";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player], level} Sets the player's food level. This is an integer from 0-?";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            int level = 0;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), t);
                level = (int) Static.getInt(args[1]);
            } else {
                level = (int) Static.getInt(args[0]);
            }
            Static.AssertPlayerNonNull(m, t);
            m.setFoodLevel(level);
            return new CVoid(t);
        }
    }

    @api
    public static class set_peffect extends AbstractFunction {

        public String getName() {
            return "set_peffect";
        }

        public Integer[] numArgs() {
            return new Integer[]{3, 4};
        }

        public String docs() {
            return "boolean {player, potionID, strength, [seconds]} Not all potions work of course, but effect is 1-19. Seconds defaults to 30."
                    + " If the potionID is out of range, a RangeException is thrown, because out of range potion effects"
                    + " cause the client to crash, fairly hardcore. See http://www.minecraftwiki.net/wiki/Potion_effects for a"
                    + " complete list of potions that can be added. To remove an effect, set the strength (or duration) to 0."
                    + " It returns true if the effect was added or removed as desired. It returns false if the effect was"
                    + " not added or removed as desired (however, this currently only will happen if an effect is attempted"
                    + " to be removed, yet isn't already on the player).";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException,
            ExceptionType.RangeException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCPlayer m = Static.GetPlayer(args[0].val(), t);

            int effect = (int) Static.getInt(args[1]);
            //To work around a bug in bukkit/vanilla, if the effect is invalid, throw an exception
            //otherwise the client crashes, and requires deletion of
            //player data to fix.
            if(effect < 1 || effect > 19){
                throw new ConfigRuntimeException("Invalid effect ID recieved, must be from 1-19", ExceptionType.RangeException, t);
            }
            int strength = (int) Static.getInt(args[2]);
            int seconds = 30;
            if (args.length == 4) {
                seconds = (int) Static.getInt(args[3]);
            }
            Static.AssertPlayerNonNull(m, t);
            if(seconds == 0 || strength == 0){
                return new CBoolean(m.removeEffect(effect), t);
            } else {
                m.addEffect(effect, strength, seconds);
                return new CBoolean(true, t);
            }
        }
    }

    @api
    public static class set_phealth extends AbstractFunction {

        public String getName() {
            return "set_phealth";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player], health} Sets the player's health. health should be an integer from 0-20.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException, ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            int health = 0;
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), t);
                health = (int) Static.getInt(args[1]);
            } else {
                health = (int) Static.getInt(args[0]);
            }
            if (health < 0 || health > 20) {
                throw new ConfigRuntimeException("Health must be between 0 and 20", ExceptionType.RangeException, t);
            }
            Static.AssertPlayerNonNull(m, t);
            m.setHealth(health);
            return new CVoid(t);
        }
    }

    @api
    public static class ponline extends AbstractFunction {

        public String getName() {
            return "ponline";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {player} Returns whether or not the specified player is online. Note"
                    + " that the name must match exactly, but it will not throw a PlayerOfflineException"
                    + " if the player is not online, or if the player doesn't even exist.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            //We have to use this method here, because we might be in the midst
            //of an event, in which the player is offline, but not really. It will
            //throw an exception if the player doesn't exist
            MCPlayer p = null;
            try{
                p = Static.GetPlayer(args[0]);
            } catch(ConfigRuntimeException e){
                //They aren't in the player list
            }
            //If the player we grabbed doesn't match exactly, we're referring to another player
            //However, we had to check with Static.GetPlayer first, in case this is an injected player.
            //Otherwise, we need to use the player returned from Static.GetPlayer, not the one returned
            //from the server directly
            if(p != null && !p.getName().equals(args[0].val())){
                MCOfflinePlayer player = Static.getServer().getOfflinePlayer(args[0].val());                
                return new CBoolean(player.isOnline(), t);
            } else if(p != null){
                return new CBoolean(p.isOnline(), t);
            } else {
                return new CBoolean(false, t);
            }
        }
    }

    @api
    public static class pwhitelisted extends AbstractFunction {

        public String getName() {
            return "pwhitelisted";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {player} Returns whether or not this player is whitelisted. Note that"
                    + " this will work with offline players, but the name must be exact.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCOfflinePlayer pl = Static.getServer().getOfflinePlayer(args[0].val());
            return new CBoolean(pl.isWhitelisted(), t);
        }
    }

    @api
    public static class set_pwhitelisted extends AbstractFunction {

        public String getName() {
            return "set_pwhitelisted";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {player, isWhitelisted} Sets the whitelist flag of the specified player. Note that"
                    + " this will work with offline players, but the name must be exact.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCOfflinePlayer pl = Static.getServer().getOfflinePlayer(args[0].val());
            boolean whitelist = Static.getBoolean(args[1]);
            pl.setWhitelisted(whitelist);
            return new CVoid(t);
        }
    }

    @api
    public static class pbanned extends AbstractFunction {

        public String getName() {
            return "pbanned";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {player} Returns whether or not this player is banned. Note that"
                    + " this will work with offline players, but the name must be exact. At this"
                    + " time, this function only works with the vanilla ban system. If you use"
                    + " a third party ban system, you should instead run the command for that"
                    + " plugin instead.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCOfflinePlayer pl = Static.getServer().getOfflinePlayer(args[0].val());
            return new CBoolean(pl.isBanned(), t);
        }
    }

    @api
    public static class set_pbanned extends AbstractFunction {

        public String getName() {
            return "set_pbanned";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {player, isBanned} Sets the ban flag of the specified player. Note that"
                    + " this will work with offline players, but the name must be exact. At this"
                    + " time, this function only works with the vanilla ban system. If you use"
                    + " a third party ban system, you should instead run the command for that"
                    + " plugin instead.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCOfflinePlayer pl = Static.getServer().getOfflinePlayer(args[0].val());
            boolean ban = Static.getBoolean(args[1]);
            pl.setBanned(ban);
            return new CVoid(t);
        }
    }

    @api
    public static class pisop extends AbstractFunction {

        public String getName() {
            return "pisop";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "boolean {[player]} Returns whether or not the specified player (or the current"
                    + " player if not specified) is op";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer m = environment.GetPlayer();
            if (args.length == 1) {
                m = Static.GetPlayer(args[0].val(), t);
            }
            Static.AssertPlayerNonNull(m, t);
            return new CBoolean(m.isOp(), t);
        }
    }
    
    @api public static class set_compass_target extends AbstractFunction{

        public String getName() {
            return "set_compass_target";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "array {[player], locationArray} Sets the player's compass target, and returns the old location.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer m = null;
            MCLocation l;
            if(args.length == 1){
                l = ObjectGenerator.GetGenerator().location(args[0], null, t);
            } else {
                m = Static.GetPlayer(args[0].val(), t);
                l = ObjectGenerator.GetGenerator().location(args[1], null, t);
            }
            if (m == null) {
                throw new ConfigRuntimeException("That player is not online", ExceptionType.PlayerOfflineException, t);
            }
            Static.AssertPlayerNonNull(m, t);
            MCLocation old = m.getCompassTarget();
            m.setCompassTarget(l);
            return ObjectGenerator.GetGenerator().location(old);
        }
        
    }
    
    @api public static class get_compass_target extends AbstractFunction{

        public String getName() {
            return "get_compass_target";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "array {[player]} Gets the compass target of the specified player";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer m = environment.GetPlayer();
            if(args.length == 1){
                m = Static.GetPlayer(args[0].val(), t);
            }
            Static.AssertPlayerNonNull(m, t);
            return ObjectGenerator.GetGenerator().location(m.getCompassTarget());
        }
        
    }
    
    @api public static class ponfire extends AbstractFunction{

        public String getName() {
            return "ponfire";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "int {[player]} Returns the number of ticks remaining that this player will"
                    + " be on fire for. If the player is not on fire, 0 is returned, which incidentally"
                    + " is false.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = environment.GetPlayer();
            if(args.length == 1){
                p = Static.GetPlayer(args[0]);
            }
            Static.AssertPlayerNonNull(p, t);
            int left = p.getRemainingFireTicks();
            return new CInt(left, t);
        }
        
    }
    
    @api public static class set_ponfire extends AbstractFunction{

        public String getName() {
            return "set_ponfire";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player], ticks} Sets the player on fire for the specified number of"
                    + " ticks. If a boolean is given for ticks, false is 0, and true is 20.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = environment.GetPlayer();
            Construct ticks;
            if(args.length == 2){
                p = Static.GetPlayer(args[0]);
                ticks = args[1];
            } else {
                ticks = args[0];
            }
            int tick = 0;
            if(ticks instanceof CBoolean){
                boolean value = ((CBoolean)ticks).getBoolean();
                if(value){
                    tick = 20;
                }
            } else {
                tick = (int) Static.getInt(ticks);
            }
            Static.AssertPlayerNonNull(p, t);
            p.setRemainingFireTicks(tick);
            return new CVoid(t);
        }
        
    }
    
    @api public static class phas_flight extends AbstractFunction{

        public String getName() {
            return "phas_flight";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "boolean {[player]} Returns whether or not the player has the ability to fly";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = environment.GetPlayer();
            if(args.length == 1){
                p = Static.GetPlayer(args[0]);
            }
            Static.AssertPlayerNonNull(p, t);
            return new CBoolean(p.getAllowFlight(), t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
    }
    
    @api public static class pset_flight extends AbstractFunction{

        public String getName() {
            return "pset_flight";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player], flight} Sets whether or not this player is allowed to fly";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = environment.GetPlayer();
            boolean flight;
            if(args.length == 1){
                flight = Static.getBoolean(args[0]);
            } else {
                p = Static.GetPlayer(args[0]);
                flight = Static.getBoolean(args[1]);
            }
            Static.AssertPlayerNonNull(p, t);
            p.setAllowFlight(flight);
            return new CVoid(t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
    }

    private static final SortedMap<String, Construct> TimeLookup = new TreeMap<String, Construct>();
    static{
        Properties p = new Properties();
        try {
            p.load(Minecraft.class.getResourceAsStream("/time_names.txt"));
            Enumeration e = p.propertyNames();
            while(e.hasMoreElements()){
                String name = e.nextElement().toString();
                TimeLookup.put(name, new CString(p.getProperty(name).toString(), Target.UNKNOWN));
            }
        } catch (IOException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @api public static class pset_time extends AbstractFunction{

        public String getName() {
            return "pset_time";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            StringBuilder doc = new StringBuilder();
            doc.append("void {[player], time} Sets the time of a given player. Should be a number from 0 to"
                    + " 24000, if not, it is modulo scaled. Alternatively, common time notation (9:30pm, 4:00 am)"
                    + " is acceptable, and convenient english mappings also exist:"
            );
            doc.append("<ul>");
            for(String key : TimeLookup.keySet()){
                doc.append("<li>").append(key).append(" = ").append(TimeLookup.get(key)).append("</li>\n");
            }
            doc.append("</ul>");
            return doc.toString();
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = null;
            if(environment.GetPlayer() != null){
                p = environment.GetPlayer();
            }
            if(args.length == 2){
                p = Static.GetPlayer(args[0]);
            }
            Static.AssertPlayerNonNull(p, t);
            long time = 0;
            String stime = (args.length == 1?args[0]:args[1]).val().toLowerCase();
            if(TimeLookup.containsKey(stime.replaceAll("[^a-z]", ""))){
                stime = TimeLookup.get(stime.replaceAll("[^a-z]", "")).val();
            }
            if(stime.matches("^([\\d]+)[:.]([\\d]+)[ ]*?(?:([pa])\\.*m\\.*){0,1}$")){
                Pattern pa = Pattern.compile("^([\\d]+)[:.]([\\d]+)[ ]*?(?:([pa])\\.*m\\.*){0,1}$");
                Matcher m = pa.matcher(stime);
                m.find();
                int hour = Integer.parseInt(m.group(1));
                int minute = Integer.parseInt(m.group(2));
                String offset = "a";
                if(m.group(3) != null){
                    offset = m.group(3);
                }
                if(offset.equals("p")){
                    hour += 12;
                }
                if(hour == 24) hour = 0;
                if(hour > 24){
                    throw new ConfigRuntimeException("Invalid time provided", ExceptionType.FormatException, t);
                }
                if(minute > 59){
                    throw new ConfigRuntimeException("Invalid time provided", ExceptionType.FormatException, t);
                }
                hour -= 6;
                hour = hour % 24;
                long ttime = hour * 1000;
                ttime += ((minute / 60.0) * 1000);
                stime = Long.toString(ttime);
            }
            try{
                Long.valueOf(stime);
            } catch(NumberFormatException e){
                throw new ConfigRuntimeException("Invalid time provided", ExceptionType.FormatException, t);
            }
            time = Long.parseLong(stime);
            p.setPlayerTime(time);
            return new CVoid(t);
        }

    }

    @api public static class pget_time extends AbstractFunction{

        public String getName() {
            return "pget_time";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "int {[player]} Returns the time of the specified player, as an integer from"
                    + " 0 to 24000-1";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = null;
            if(environment.GetPlayer() != null){
                p = environment.GetPlayer();
            }
            if(args.length == 1){
                p = Static.GetPlayer(args[0]);
            }
            Static.AssertPlayerNonNull(p, t);
            return new CInt(p.getPlayerTime(), t);
        }

    }

    @api public static class preset_time extends AbstractFunction{

        public String getName() {
            return "preset_time";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "void {[player]} Resets the time of the player to the time of the world.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = null;
            if(environment.GetPlayer() != null){
                p = environment.GetPlayer();
            }
            if(args.length == 1){
                p = Static.GetPlayer(args[0]);
            }
            Static.AssertPlayerNonNull(p, t);
            p.resetPlayerTime();
            return new CVoid(t);
        }

    }

    //Disabled until bukkit fixes their bug
//    @api public static class pvelocity extends AbstractFunction{
//
//        public String getName() {
//            return "pvelocity";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{0, 1};
//        }
//
//        public String docs() {
//            return "array {[player]} Returns an associative array that represents the player's velocity."
//                    + " The array contains the following items: magnitude, x, y, z. These represent a"
//                    + " 3 dimensional Vector. The important part is x, y, z, however, the magnitude is provided"
//                    + " for you as a convenience. (It should equal sqrt(add(exp(x, 2), exp(y, 2), exp(z, 2))))";
//        }
//
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
//        }
//
//        public boolean isRestricted() {
//            return true;
//        }
//
//        public boolean preResolveVariables() {
//            return true;
//        }
//
//        public Boolean runAsync() {
//            return false;
//        }
//
//        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
//            MCPlayer p = environment.GetPlayer();
//            if(args.length == 1){
//                p = Static.GetPlayer(args[0]);
//            }
//            CArray vector = new CArray(t);
//            vector.forceAssociativeMode();
//            MCPlayer.Velocity velocity = p.getVelocity();
//            vector.set("magnitude", new CDouble(velocity.magnitute, t));
//            vector.set("x", new CDouble(velocity.x, t));
//            vector.set("y", new CDouble(velocity.y, t));
//            vector.set("z", new CDouble(velocity.z, t));
//            return vector;
//        }
//
//        public CHVersion since() {
//            return CHVersion.V3_3_0;
//        }
//        
//    }
    
}
