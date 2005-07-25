/*
 * Created on 24/07/2005
 */
package com.python.pydev.analysis.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.python.pydev.editor.codecompletion.revisited.IToken;

import com.python.pydev.analysis.IAnalysisPreferences;
import com.python.pydev.analysis.messages.CompositeMessage;
import com.python.pydev.analysis.messages.IMessage;
import com.python.pydev.analysis.messages.Message;

public class MessagesManager {

    /**
     * preferences for indicating the severities
     */
    private IAnalysisPreferences prefs;

    /**
     * this map should hold the generator source token and the messages that are generated for it
     */
    public Map<IToken, List<IMessage>> messages = new HashMap<IToken, List<IMessage>>();


    public List<IMessage> independentMessages = new ArrayList<IMessage>();
    
    public MessagesManager(IAnalysisPreferences prefs) {
        this.prefs = prefs;
    }
    
    /**
     * adds a message of some type given its formatting params
     */
    public void addMessage(int type, IToken generator, Object ...objects ) {
        independentMessages.add(new Message(type, objects, generator, prefs));
    }

    /**
     * adds a message of some type for a given token
     */
    public void addMessage(int type, IToken token) {
        List<IMessage> msgs = getMsgsList(token);
        msgs.add(new Message(type, token.getRepresentation(),token, prefs));
    }

    /**
     * adds a message of some type for some Found instance
     */
    public void addMessage(int type, Found f) {
        IToken generator = f.generator;
        List<IMessage> msgs = getMsgsList(generator);
        msgs.add(new Message(type, f.tok.getRepresentation(), f.tok, prefs));
    }

    /**
     * @return the messages associated with a token
     */
    public List<IMessage> getMsgsList(IToken generator) {
        List<IMessage> msgs = messages.get(generator);
        if (msgs == null){
            msgs = new ArrayList<IMessage>();
            messages.put(generator, msgs);
        }
        return msgs;
    }

    /**
     * @return the generated messages.
     */
    public IMessage[] getMessages() {
        
        List<IMessage> result = new ArrayList<IMessage>();
        
        //let's get the messages
        for (List<IMessage> l : messages.values()) {
            if(l.size() < 1){
                //we need at least one message
                continue;
            }
            
            IMessage message = l.get(0);
            if(message.getSeverity() == IAnalysisPreferences.SEVERITY_IGNORE){
                continue;
            }
            
            if(l.size() == 1){
                result.add(message);
                
            } else{
                //the generator token has many associated messages
                CompositeMessage compositeMessage = new CompositeMessage(message.getType(), message.getGenerator(), prefs);
                for(IMessage m : l){
                    compositeMessage.addMessage(m);
                }
                result.add(compositeMessage);
            }
        }
        
        for(IMessage message : independentMessages){
            if(message.getSeverity() == IAnalysisPreferences.SEVERITY_IGNORE){
                continue;
            }
            
            result.add(message);
        }
        return (IMessage[]) result.toArray(new IMessage[0]);
    }

}
