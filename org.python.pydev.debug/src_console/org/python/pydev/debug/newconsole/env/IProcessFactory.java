/*
 * Created on Mar 20, 2006
 */
package org.python.pydev.debug.newconsole.env;

import java.io.File;
import java.util.Collection;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.Launch;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.SocketUtil;
import org.python.pydev.runners.SimpleJythonRunner;
import org.python.pydev.runners.SimplePythonRunner;
import org.python.pydev.runners.SimpleRunner;

/**
 * This class is used to create the given IProcess and get the console that is attached to that process. 
 */
public class IProcessFactory {

    /**
     * @return a shell that we can use.
     */
    public Shell getShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

    public static final String INTERACTIVE_LAUNCH_PORT = "INTERACTIVE_LAUNCH_PORT";

    /**
     * Creates a launch (and its associated IProcess) for the xml-rpc server to be used in the interactive console.
     * 
     * It'll ask the user how to create it:
     * - editor
     * - python interpreter
     * - jython interpreter
     * 
     * @return the Launch created
     * 
     * @throws UserCanceledException
     * @throws Exception
     */
    public ILaunch createInteractiveLaunch()
            throws UserCanceledException, Exception {
    	
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = workbenchWindow.getActivePage();
        IEditorPart activeEditor = activePage.getActiveEditor();
        PyEdit edit = null;
        
        if (activeEditor instanceof PyEdit) {
            edit = (PyEdit) activeEditor;
        }
        
        ChooseProcessTypeDialog dialog = new ChooseProcessTypeDialog(getShell(), edit);
        if(dialog.open() == ChooseProcessTypeDialog.OK){
	        
        	Collection<String> pythonpath = dialog.getPythonpath();
        	IInterpreterManager interpreterManager = dialog.getInterpreterManager();
        	
			if(pythonpath != null && interpreterManager != null){
		        int port = SocketUtil.findUnusedLocalPort();
		        final Launch launch = new Launch(null, "interactive", null);
		        launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, "false");
		        launch.setAttribute(INTERACTIVE_LAUNCH_PORT, ""+port);
		        
		        String pythonpathEnv = SimpleRunner.makePythonPathEnvFromPaths(pythonpath);
		        String[] env = SimpleRunner.createEnvWithPythonpath(pythonpathEnv);
		
		        File scriptWithinPySrc = PydevPlugin.getScriptWithinPySrc("pydevconsole.py");
		        String commandLine;
		        if(interpreterManager.isPython()){
		        	commandLine = SimplePythonRunner.makeExecutableCommandStr(scriptWithinPySrc.getAbsolutePath(), 
		        			new String[]{""+port});
		        	
		        }else if(interpreterManager.isJython()){
		        	commandLine = SimpleJythonRunner.makeExecutableCommandStr(scriptWithinPySrc.getAbsolutePath(), 
		        			pythonpathEnv, new String[]{""+port});
		        	
		        }else{
		        	throw new RuntimeException("Expected interpreter manager to be python or jython related.");
		        }
		        Process process = Runtime.getRuntime().exec(commandLine, env, null);
		        PydevSpawnedInterpreterProcess spawnedInterpreterProcess = 
		        	new PydevSpawnedInterpreterProcess(process, launch);
		        
		        launch.addProcess(spawnedInterpreterProcess);
		        
		        return launch;
        	}
        }
        return null;
    }


}