package com.badlogic.gdx.backends.lwjgl3;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;

/**
 * A simple controller that updates and control all GLFW window/Gdx Application. <br>
 * 
 * @author Natan Guilherme
 */
public class Lwjgl3WindowController
{
	Array<Lwjgl3Application> windows;

	Runnable runnable;

	boolean shareContext;
	/**
	 * Sharecontext is for object sharing with multiple windows (1 Texture for all windows for example). <br> <br>
	 * 
	 * If the first window is destroyed than other windows will have a black texture. <br>
	 * You can change the parent context with {@link Lwjgl3WindowController#changeParentWindow(long)}
	 * 
	 */
	public Lwjgl3WindowController(boolean shareContext)
	{
		Lwjgl3NativesLoader.load();

		if (glfwInit() != GL11.GL_TRUE)
			throw new IllegalStateException("Unable to initialize GLFW");

		this.shareContext = shareContext;
		windows = new Array<Lwjgl3Application>();
	}

	/**
	 * Main Loop for GLFW. This call will block <br>
	 * 
	 */
	public void start()
	{
		while (windows.size > 0)
		{
			glfwPollEvents();
			
			for (int i = 0; i < windows.size; i++)
			{
				Lwjgl3Application app = windows.get(i);

				if (app.running)
				{

					if (app.init == false)
					{
						app.init = true;

						app.graphics.initWindow();
						glfwMakeContextCurrent(app.graphics.window);
						
						if(shareContext == false)
							app.context = GLContext.createFromCurrent();
						else
						{
							if(Lwjgl3Graphics.contextShare == 0)
							{
								app.context = GLContext.createFromCurrent();
								Lwjgl3Graphics.contextShare = app.graphics.window;
							}
						}
						
						app.graphics.initGL();
						app.graphics.show();
						
						app.initStaticVariables();
						
						app.input.addCallBacks();
						app.addCallBacks();
					}
					
					app.loop();
				}
				else
				{
					app.dispose();
					windows.removeIndex(i);
					i--;
				}
			}
		}
		glfwTerminate();
	}

	public void addWindow(String id, ApplicationListener listener, final Lwjgl3ApplicationConfiguration config)
	{
		addWindow(id, new Lwjgl3Application(listener, config, false));
	}

	public void addWindow(String id, Lwjgl3Application app)
	{
		if (app.autoloop) // cannot have a running loop
			return;

		if (getWindow(id) == null)
		{
			app.id = id;
			windows.add(app);
		}
	}

	public boolean removeWindow(String id)
	{
		for (int i = 0; i < windows.size; i++)
		{
			Lwjgl3Application app = windows.get(i);
			if (app.id.equals(id))
			{
				app.exit();
				return true;
			}
		}
		return false;
	}

	public Lwjgl3Application getWindow(String id)
	{
		for (int i = 0; i < windows.size; i++)
		{
			Lwjgl3Application app = windows.get(i);
			if (app.id.equals(id))
			{
				return app;
			}
		}
		return null;
	}
	/**
	 * Set a new window to share its context. Default is 0. Only for new windows.
	 */
	public void changeParentWindow(long firstWindow)
	{
		Lwjgl3Graphics.contextShare = firstWindow;
	}

}
