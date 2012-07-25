/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.hupa.server.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.mail.Flags;
import javax.mail.Flags.Flag;

import org.apache.hupa.shared.data.Message.IMAPFlag;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.UIDSet;

/**
 * Util class which helps to convert from hupa internal data representation to
 * javamaill classes
 * 
 */
public class JavamailUtil {

    /**
     * Convert the given Flags to a ArrayList of IMAPFlags
     * 
     * @param flags
     * @return imapFlags
     */
    public static ArrayList<IMAPFlag> convert(Flags flags) {
        ArrayList<IMAPFlag> fList = new ArrayList<IMAPFlag>();
        for (Flag flag : flags.getSystemFlags()) {
            fList.add(convert(flag));
        }
        return fList;

    }

    public static IMAPFlag convert(Flag flag) {
        if (flag.equals(Flag.SEEN)) {
            return IMAPFlag.SEEN;
        } else if (flag.equals(Flag.RECENT)) {
            return IMAPFlag.RECENT;
        } else if (flag.equals(Flag.ANSWERED)) {
            return IMAPFlag.ANSWERED;
        } else if (flag.equals(Flag.DELETED)) {
            return IMAPFlag.DELETED;
        } else if (flag.equals(Flag.DRAFT)) {
            return IMAPFlag.DRAFT;
        } else if (flag.equals(Flag.FLAGGED)) {
            return IMAPFlag.FLAGGED;
        } else if (flag.equals(Flag.USER)) {
            return IMAPFlag.USER;
        }

        throw new IllegalArgumentException("Flag not supported " + flag);
    }

    /**
     * Convert the given ArrayList of IMAPFlags to a Flags object
     * 
     * @param imapFlags
     * @return flags
     */
    public static Flags convert(ArrayList<IMAPFlag> imapFlags) {
        Flags iFlags = new Flags();
        for (IMAPFlag flag : imapFlags) {
            iFlags.add(convert(flag));
        }
        return iFlags;
    }

    public static Flag convert(IMAPFlag flag) {
        if (flag.equals(IMAPFlag.SEEN)) {
            return Flag.SEEN;
        } else if (flag.equals(IMAPFlag.RECENT)) {
            return Flag.RECENT;
        } else if (flag.equals(IMAPFlag.ANSWERED)) {
            return Flag.ANSWERED;
        } else if (flag.equals(IMAPFlag.DELETED)) {
            return Flag.DELETED;
        }
        throw new IllegalArgumentException("Flag not supported");

    }

    public static List<UIDSet> convertToUidSet(List<Long> uids) {
        SortedSet<Long> input = new TreeSet<Long>();
        input.addAll(uids);
        return convertToUidSet(input);
    }

    public static List<UIDSet> convertToUidSet(SortedSet<Long> input) {
        List<UIDSet> result = new ArrayList<UIDSet>();
        UIDSet current = null;
        long lastNumber = Long.MIN_VALUE;
        for (Long id : input) {
            long currentId = id;
            if ((current == null) || (lastNumber + 1 != currentId)) {
                current = new UIDSet();
                current.start = currentId;
                result.add(current);
            }
            current.end = currentId;
            lastNumber = currentId;
        }
        return result;
    }

    static class CopyByUID implements IMAPFolder.ProtocolCommand {

        List<UIDSet> uids;
        String       mbox;

        public CopyByUID(List<UIDSet> uids, String mbox) {
            super();
            this.uids = uids;
            this.mbox = mbox;
        }

        @Override
        public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
            String encodedMailbox = BASE64MailboxEncoder.encode(mbox);

            String msgSequence = UIDSet.toString(uids.toArray(new UIDSet[uids.size()]));
            Argument args = new Argument();
            args.writeAtom(msgSequence);
            args.writeString(encodedMailbox);

            protocol.simpleCommand("UID COPY", args);

            return null;
        }

    }

    static class SetFlagsByUID implements IMAPFolder.ProtocolCommand {

        public SetFlagsByUID(Flags flags, boolean set, String msgset) {
            super();
            this.flags = flags;
            this.set = set;
            this.msgset = msgset;
        }

        Flags flags;
        boolean set;
        String msgset;
        
        @Override
        public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
            Response[] r;
            if (set)
                r = protocol.command("UID STORE " + msgset + " +FLAGS " + createFlagList(flags), null);
            else
                r = protocol.command("UID STORE " + msgset + " -FLAGS " + createFlagList(flags), null);

            // Dispatch untagged responses
            protocol.notifyResponseHandlers(r);
            protocol.handleResult(r[r.length - 1]);
            return r;
        }

    }

    /**
     * Creates an IMAP flag_list from the given Flags object.
     */
    static String createFlagList(Flags flags) {
        StringBuffer sb = new StringBuffer();
        sb.append("("); // start of flag_list

        Flags.Flag[] sf = flags.getSystemFlags(); // get the system flags
        boolean first = true;
        for (int i = 0; i < sf.length; i++) {
            String s;
            Flags.Flag f = sf[i];
            if (f == Flags.Flag.ANSWERED)
                s = "\\Answered";
            else if (f == Flags.Flag.DELETED)
                s = "\\Deleted";
            else if (f == Flags.Flag.DRAFT)
                s = "\\Draft";
            else if (f == Flags.Flag.FLAGGED)
                s = "\\Flagged";
            else if (f == Flags.Flag.RECENT)
                s = "\\Recent";
            else if (f == Flags.Flag.SEEN)
                s = "\\Seen";
            else
                continue; // skip it
            if (first)
                first = false;
            else
                sb.append(' ');
            sb.append(s);
        }

        String[] uf = flags.getUserFlags(); // get the user flag strings
        for (int i = 0; i < uf.length; i++) {
            if (first)
                first = false;
            else
                sb.append(' ');
            sb.append(uf[i]);
        }

        sb.append(")"); // terminate flag_list
        return sb.toString();
    }

}
