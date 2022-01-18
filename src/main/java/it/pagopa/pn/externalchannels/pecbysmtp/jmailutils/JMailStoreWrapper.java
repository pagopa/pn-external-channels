package it.pagopa.pn.externalchannels.pecbysmtp.jmailutils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import java.io.IOException;
import java.util.*;


@Slf4j
public class JMailStoreWrapper {

    private final Store store;
    private final String singleFolder;

    public JMailStoreWrapper(Store store, String singleFolder) {
        this.store = store;
        this.singleFolder = singleFolder;
    }


    public List<PecEntry> listEntries() {
        List<PecEntry> pecEntries = new LinkedList<>();

        if(StringUtils.isNotBlank(singleFolder )) {
            log.info("List IMAP messages for folderName={}", singleFolder);
            Folder f = getFolderFromStore( this.singleFolder );
            pecEntries.addAll( listEntries( f ) );
        }
        else {
            log.info("List IMAP messages for all folders");
            for(Folder f: allStoreFolders() ) {
                pecEntries.addAll( listEntries( f ) );
            }
        }

        return new ArrayList<>( pecEntries );
    }

    private Folder getFolderFromStore( String folderName) {
        try {
            return store.getDefaultFolder().getFolder( folderName );
        } catch (MessagingException exc) {
            throw new PnInternalException( "Reading IMAP folder " + folderName, exc );
        }
    }

    private List<Folder> allStoreFolders() {
        List<Folder> allStoreFolders = new ArrayList<>();

        try {
            log.info("List 'PersonalNamespaces' folder and sub folders");
            Folder[] personalFolders = store.getPersonalNamespaces();
            for( Folder f: personalFolders) {
                allStoreFolders.addAll( walkFolderTree( f ));
            }
            allStoreFolders.addAll(Arrays.asList( personalFolders ) );
        }
        catch ( MessagingException exc) {
            log.error("Listing personal folders", exc);
            throw new PnInternalException( "Listing personal folders", exc );
        }


        try {
            log.info("List 'SharedNamespaces' folder and sub folders");
            Folder[] sharedFolders = store.getSharedNamespaces();
            for( Folder f: sharedFolders) {
                allStoreFolders.addAll( walkFolderTree( f ));
            }
            allStoreFolders.addAll(Arrays.asList( sharedFolders ) );
        }
        catch ( MessagingException exc) {
            log.error("Listing shared folders", exc);
            throw new PnInternalException( "Listing shared folders", exc );
        }

        return allStoreFolders;
    }

    private List<PecEntry> listEntries(Folder folder ) {
        List<PecEntry> pecEntries = new ArrayList<>();
        log.debug("Read messages from IMAP folder {}", folder.getFullName() );

        // - Open SMTP Folder
        try {
            folder.open(Folder.READ_ONLY);
        } catch (MessagingException exc) {
            throw new PnInternalException("Opening SMTP folder " + folder.getFullName(), exc);
        }

        // - List messages in the opened folder (not the children folders)
        //   and extract PecEntry information
        for (Message jMailMsg : getMessages(folder)) {
            try {
                PecEntry pecEntry = new PecEntry(jMailMsg);
                pecEntries.add(pecEntry);
                log.debug("Retrieved pecEntry from={} with type={} id={} referredId={}",
                        pecEntry.getFrom(),
                        pecEntry.getType(),
                        pecEntry.getEntryId(),
                        pecEntry.getReferredId()
                    );
            } catch (MessagingException | IOException exc) {
                throw new PnInternalException("Listing messages in SMTP folder " + folder.getFullName(), exc);
            }
        }

        // - Close the folder
        try {
            folder.close( false );
        } catch (MessagingException exc) {
            throw new PnInternalException("Closing SMTP folder " + folder.getFullName(), exc);
        }

        return pecEntries;
    }

    private Message[] getMessages(Folder folder) {
        try {
            return folder.getMessages();
        } catch (MessagingException exc) {
            throw new PnInternalException("Listing messages from folder " + folder.getFullName(), exc);
        }
    }


    private List<Folder> walkFolderTree( Folder root ) {
        List<Folder> allNodes = new LinkedList<>();

        Deque<Folder> stack = new ArrayDeque<>();
        stack.push( root );

        while( !stack.isEmpty() ) {
            Folder currentNode = stack.pop();
            log.debug("List recursively IMAP folderName={}", currentNode.getFullName() );
            try {
                currentNode.open(Folder.READ_ONLY);
                allNodes.add( currentNode );
                for(Folder child: currentNode.list()) {
                    stack.push( child );
                }
                currentNode.close( false );

            } catch (MessagingException exc) {
                log.error("walking in IMAP folderName=\"" + currentNode.getFullName() +"\"", exc);
            }
        }

        return allNodes;
    }

    public void close() throws MessagingException {
        this.store.close();
    }
}
