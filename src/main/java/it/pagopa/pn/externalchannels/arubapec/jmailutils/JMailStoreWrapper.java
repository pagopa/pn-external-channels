package it.pagopa.pn.externalchannels.arubapec.jmailutils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.extern.slf4j.Slf4j;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import java.io.IOException;
import java.util.*;


@Slf4j
public class JMailStoreWrapper {

    private final Store store;

    public JMailStoreWrapper(Store store) {
        this.store = store;
    }


    public List<PecEntry> listEntries() {
        List<PecEntry> pecEntries = new LinkedList<>();

        try {
            Folder f = store.getDefaultFolder().getFolder("INBOX");
            pecEntries.addAll( listEntries( f ) );
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        /*for(Folder f: allStoreFolders() ) {
            pecEntries.addAll( listEntries( f ) );
        }*/

        return new ArrayList<>( pecEntries );
    }

    private List<Folder> allStoreFolders() {
        List<Folder> allStoreFolders = new ArrayList<>();

        try {
            Folder[] personalFolders = store.getPersonalNamespaces();
            //for( Folder f: personalFolders) {
            //    allStoreFolders.addAll( walkFolderTree( f ));
            //}
            allStoreFolders.addAll(Arrays.asList( personalFolders ) );
        }
        catch ( MessagingException exc) {
            log.error("Listing personal folders", exc);
        }


        /*try {
            Folder[] sharedFolders = store.getSharedNamespaces();
            //for( Folder f: sharedFolders) {
            //    allStoreFolders.addAll( walkFolderTree( f ));
            //}
            allStoreFolders.addAll(Arrays.asList( sharedFolders ) );
        }
        catch ( MessagingException exc) {
            log.error("Listing shared folders", exc);
        }*/

        return allStoreFolders;
    }

    private List<PecEntry> listEntries(Folder folder ) {
        List<PecEntry> pecEntries = new ArrayList<>();
        try {
            folder.open(Folder.READ_ONLY);
            for (Message jMailMsg : getMessages(folder)) {
                try {
                    PecEntry pecEntry = new PecEntry(jMailMsg);
                    pecEntries.add(pecEntry);
                } catch (MessagingException | IOException exc) {
                    throw new PnInternalException("Elencando messaggi nel folder " + folder.getFullName(), exc);
                }

            }
            folder.close( false );
        } catch (MessagingException exc) {
            throw new PnInternalException("Elencando messaggi nel folder " + folder.getFullName(), exc);
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

        Stack<Folder> stack = new Stack();
        stack.push( root );

        while( !stack.empty() ) {
            Folder currentNode = stack.pop();
            log.info("IMAP folder name=\"" + currentNode.getFullName() +"\"");
            try {
                currentNode.open(Folder.READ_ONLY);
                allNodes.add( currentNode );
                for(Folder child: currentNode.list()) {
                    stack.push( child );
                }
                currentNode.close( false );

            } catch (MessagingException exc) {
                log.error("walking in IMAP folder name=\"" + currentNode.getFullName() +"\"", exc);
            }
        }

        return allNodes;
    }

    public void close() throws MessagingException {
        this.store.close();
    }
}
