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

import javax.mail.Message;

import org.apache.hupa.server.FileItemRegistry;
import org.apache.hupa.server.HupaGuiceTestCase;
import org.apache.hupa.server.mock.MockIMAPFolder;
import org.apache.hupa.server.mock.MockIMAPStore;
import org.apache.hupa.server.utils.SessionUtils;
import org.apache.hupa.server.utils.TestUtils;
import org.apache.hupa.shared.data.IMAPFolder;
import org.apache.hupa.shared.data.SMTPMessage;
import org.apache.hupa.shared.rpc.ReplyMessage;

import com.sun.mail.imap.IMAPStore;

public class ReplyMessageHandlerTest extends HupaGuiceTestCase {

    public void testForwardMessage() throws Exception {
        IMAPStore store = storeCache.get(testUser);

        FileItemRegistry registry = SessionUtils.getSessionRegistry(logger, httpSession);
        
        MockIMAPFolder sentbox = (MockIMAPFolder) store.getFolder(MockIMAPStore.MOCK_SENT_FOLDER);
        assertTrue(sentbox.getMessages().length == 0);

        MockIMAPFolder inbox = (MockIMAPFolder) store.getFolder(MockIMAPStore.MOCK_INBOX_FOLDER);
        assertTrue(inbox.getMessages().length >= 0);

        // Create a mime message with 2 attachments and 1 inline image, and put it in the inbox
        Message message = TestUtils.createMockMimeMessage(session, 2);
        TestUtils.addMockAttachment(message, "inlineImage.jpg", true);
        
        inbox.appendMessages(new Message[]{message});
        long msgUid = inbox.getUID(message);
        message = inbox.getMessageByUID(msgUid);
        assertNotNull(message);
        
        String expected = "multipart/mixed\n"
                        + " multipart/alternative\n"
                        + "  text/plain\n"
                        + "  text/html\n"
                        + " mock/attachment => file_1.bin\n"
                        + " mock/attachment => file_2.bin\n"
                        + " image/mock => inlineImage.jpg\n";
        assertEquals(expected, TestUtils.summaryzeContent(message).toString());
        
        // Create a reply user action with an uploaded message
        SMTPMessage smtpmsg = TestUtils.createMockSMTPMessage(registry, 1);
        ReplyMessage action = new ReplyMessage(smtpmsg, new IMAPFolder(inbox.getFullName()), msgUid);
        
        message = reMsgHndl.createMessage(session, action);
        message = reMsgHndl.fillBody(message, action);
        
        // The final message has to include the file uploaded by the user and the inline image
        expected = "multipart/mixed\n"
                 + " multipart/alternative\n"
                 + "  text/plain\n"
                 + "  text/html\n"
                 + " image/mock => inlineImage.jpg\n"
                 + " mock/attachment => uploadedFile_1.bin\n";
        
        assertEquals(expected, TestUtils.summaryzeContent(message).toString());
    }
}
