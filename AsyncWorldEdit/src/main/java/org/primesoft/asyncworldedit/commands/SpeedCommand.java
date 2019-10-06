/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit.commands;

import org.primesoft.asyncworldedit.core.Help;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.permissions.Permission;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.IntUtils;

/**
 *
 * @author SBPrime
 */
public class SpeedCommand {

    public static void execte(IAsyncWorldEditCore sender, IPlayerEntry player, String[] args) {
        if (args.length < 1 || args.length > 3) {
            Help.ShowHelp(player, Commands.COMMAND_SPEED);
            return;
        }

        IPlayerManager manager = sender.getPlayerManager();

        if (args.length == 1) {
            if (!player.isInGame()) {
                player.say(MessageType.INGAME.format());
                return;
            }
            if (!player.isAllowed(Permission.SPEED_VIEW)) {
                player.say(MessageType.NO_PERMS.format());
                return;
            }

            tellSpeed(player, player);
            return;
        }

        String arg = args[1];
        IPlayerEntry wrapper;
        if (args.length == 2) {
            if (arg.startsWith("u:")) {
                String name = arg.substring(2);
                wrapper = sender.getPlayerManager().getPlayer(name);

                if (!player.isAllowed(Permission.SPEED_VIEW_OTHER)) {
                    player.say(MessageType.NO_PERMS.format());
                    return;
                }

                if (!wrapper.isPlayer()) {
                    player.say(MessageType.PLAYER_NOT_FOUND.format(name));
                    return;
                }

                tellSpeed(player, wrapper);
                return;
            }

            if (!player.isAllowed(Permission.SPEED_CHANGE)) {
                player.say(MessageType.NO_PERMS.format());
                return;
            }

            InOutParam<Integer> speedIO = InOutParam.Out();
            if (!IntUtils.TryParseInteger(arg, speedIO)) {
                player.say(MessageType.CMD_SPEED_INVALID.format(arg));
                return;
            }
            Integer speed = speedIO.getValue();
            if (speed <= 0) {
                speed = null;
            }

            setSpeed(player, player, speed);
            return;
        }

        if (!arg.startsWith("u:")) {
            Help.ShowHelp(player, Commands.COMMAND_SPEED);
            return;
        }

        if (!player.isAllowed(Permission.SPEED_CHANGE_OTHER)) {
            player.say(MessageType.NO_PERMS.format());
            return;
        }

        wrapper = sender.getPlayerManager().getPlayer(arg.substring(2));
        if (!wrapper.isPlayer()) {
            player.say(MessageType.PLAYER_NOT_FOUND.format());
            return;
        }

        InOutParam<Integer> speedIO = InOutParam.Out();
        if (!IntUtils.TryParseInteger(args[2], speedIO)) {
            player.say(MessageType.CMD_SPEED_INVALID.format(args[2]));
            return;
        }
        Integer speed = speedIO.getValue();
        if (speed <= 0) {
            speed = null;
        }

        setSpeed(player, wrapper, speed);

    }

    private static void tellSpeed(IPlayerEntry player, IPlayerEntry wrapper) {
        player.say(MessageType.CMD_SPEED.format(wrapper.getName(), new Integer(wrapper.getRenderBlocks()).toString()));
    }

    private static void setSpeed(IPlayerEntry player, IPlayerEntry wrapper, Integer speed) {
        wrapper.setRenderBlocks(speed);
        tellSpeed(player, wrapper);
    }
}
