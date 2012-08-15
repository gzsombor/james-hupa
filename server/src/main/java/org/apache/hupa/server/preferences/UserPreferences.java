/**
 * 
 */
package org.apache.hupa.server.preferences;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hupa.shared.rpc.ContactsResult.Contact;

/**
 * @author zsombor
 *
 */
public class UserPreferences implements Serializable {

    protected static final String REGEX_OMITTED_EMAILS = "^.*(reply)[A-z0-9._%\\+\\-]*@.*$";

    
    static class FolderScanTimes implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        Date first;
        Date last;
        
        public void add(Date date) {
            if (first == null || first.after(date)) {
                first = date;
            }
            if (last == null || last.before(date)) {
                last = date;
            }
        }
        
        public boolean isInRange(Date date) {
            return first.before(date) && last.after(date);
        }

        @Override
        public String toString() {
            return "FolderScanTimes [" + (first != null ? "first=" + first + ", " : "")
                            + (last != null ? "last=" + last : "") + "]";
        }
        
        
    }
    
    public static class ExtractedContacts {
        Date seen;
        String folderName;
        Set<Contact> contacts = new HashSet<Contact>();

        public ExtractedContacts(String folderName,Date seen) {
            this.seen = seen;
            this.folderName = folderName;
        }
        
        public ExtractedContacts() {
            this.seen = new Date();
            this.folderName = null;
        }

        public ExtractedContacts add(Contact... newContacts) {
            for(Contact c : newContacts) {
                if (c != null) {
                    this.contacts.add(c);
                }
            }
            return this;
        }
        
        public ExtractedContacts add(String name) {
            if (name != null && !name.matches(REGEX_OMITTED_EMAILS)) {
                contacts.add(new Contact(name));
            }
            return this;
        }
        
        public ExtractedContacts add(Collection<String> newNames) {
            if (newNames != null) {
                for (String name: newNames) {
                    add(name);
                }
            }
            return this;
        }

        @Override
        public String toString() {
            return "ExtractedContacts [" + (seen != null ? "seen=" + seen + ", " : "")
                            + (folderName != null ? "folderName=" + folderName + ", " : "")
                            + (contacts != null ? "contacts=" + contacts : "") + "]";
        }
        

    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    Map<String, Contact> contacts = new HashMap<String, Contact>();
    
    Map<String, FolderScanTimes> scanInfo = new HashMap<String, FolderScanTimes>();
    
    private transient boolean changed = false;
    
    /**
     * 
     */
    public UserPreferences() {
    }
    
    
    public boolean isScannedYet(String folderName, Date created) {
        FolderScanTimes scanTimes = scanInfo.get(folderName);
        return scanTimes != null && scanTimes.isInRange(created);
    }
    
    public void addContact(String name) {
        addContact(new ExtractedContacts().add(name));
    }
    
    private void addContact(String folderName, Date seen, Collection<Contact> newContacts) {
        if (folderName != null) {
            FolderScanTimes scanTimes = scanInfo.get(folderName);
            if (scanTimes == null) {
                scanTimes = new FolderScanTimes();
                scanInfo.put(folderName, scanTimes);
            }
            scanTimes.add(seen);
        }
        for (Contact c : newContacts) {
            Contact old = contacts.get(c.mail);
            if (old == null) {
                old = new Contact(c.realname, c.mail);
                changed = true;
                contacts.put(old.mail, old);
            }
            old.seen ++;
            old.seenAt(seen);
        }
    }
    
    public void addContact(ExtractedContacts mailInfo) {
        addContact(mailInfo.folderName, mailInfo.seen, mailInfo.contacts);
    }
    
    public Contact[] getAllContacts() {
        return contacts.values().toArray(new Contact[contacts.size()]);
    }


    public void addContact(Contact... c) {
        addContact(new ExtractedContacts().add(c));
    }
    
    public boolean isChanged() {
        return changed;
    }
    
    public void clearChanged() {
        changed = false;
    }
    

}
