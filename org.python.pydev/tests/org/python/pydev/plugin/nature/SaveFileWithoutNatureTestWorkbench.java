package org.python.pydev.plugin.nature;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.python.pydev.core.REF;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.editor.codecompletion.revisited.javaintegration.AbstractJavaIntegrationTestWorkbench;
import org.python.pydev.plugin.PydevPlugin;

public class SaveFileWithoutNatureTestWorkbench extends AbstractJavaIntegrationTestWorkbench{
	
    protected void setUp() throws Exception {
    	//no setup (because we won't have the nature in this test)
    }
    

    
    public void testEditWithNoNature() throws Exception {
    	NullProgressMonitor monitor = new NullProgressMonitor();
    	IProject project = createProject(monitor, "pydev_no_nature_project");
    	
        IFile myFile = project.getFile("my_file.py");
        
        String contents = "";
        String newContents = "class Foo(object):\n    pass";
        
        myFile.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
        project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
        try {
			editor = (PyEdit) PydevPlugin.doOpenEditor(myFile, true);
			editor.getDocument().set(newContents);
			editor.doSave(monitor);
		} finally {
			editor.close(true);
			editor = null;
		}
		assertEquals(newContents, REF.getDocFromResource(myFile).get());
        
	}

}