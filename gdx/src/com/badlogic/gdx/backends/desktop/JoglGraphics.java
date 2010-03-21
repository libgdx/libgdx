package com.badlogic.gdx.backends.desktop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.swing.JFrame;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.ShaderProgram;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.WindowedMean;

/**
 * An implementation of the {@link Graphics} interface based on Jogl.
 * @author mzechner
 *
 */
final class JoglGraphics implements Graphics, RenderListener
{
	/** the jframe **/
	private final JFrame frame;
	
	/** the graphic panel **/	
	protected final JoglPanel graphicPanel;
	
	/** the render listener **/
	private RenderListener listener;	
	
	/** GL10 instance **/
	private GL10 gl10;
	
	/** GL11 instance **/
	private GL11 gl11;
	
	/** GL20 instance **/
	private GL20 gl20;
	
	/** wheter to use opengl 2 **/
	private final boolean useGL2;
		
	/** the last frame time **/
	private long lastFrameTime = System.nanoTime();
		
	/** the deltaTime **/
	private float deltaTime = 0;
	
	/** the deltaTime mean **/
	private WindowedMean mean = new WindowedMean( 5 );
	
	JoglGraphics( final JoglApplication application, String title, int width, int height, boolean useGL2IfAvailable )
	{		
		frame = new JFrame( title );
		graphicPanel = new JoglPanel( application );        
        graphicPanel.setPreferredSize( new Dimension( width, height ) );
        frame.setSize( width + frame.getInsets().left + frame.getInsets().right, frame.getInsets().top + frame.getInsets().bottom + height );
        frame.add(graphicPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible( true );                
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {                 	                	
            	graphicPanel.dispose(); 
            	if( application.listener != null )
            	{
            		application.listener.pause( application );
            		application.listener.destroy(application);
            	}
            }
        });                      
        useGL2 = useGL2IfAvailable;
        graphicPanel.addGraphicListener( this );
	}

	@Override
	public GL10 getGL10() 
	{	
		return gl10;
	}

	@Override
	public GL11 getGL11() 
	{	
		return gl11;
	}

	@Override
	public GL20 getGL20() 
	{	
		return gl20;
	}

	@Override
	public int getHeight() 
	{	
		return graphicPanel.getHeight();
	}

	@Override
	public int getWidth() 
	{	
		return graphicPanel.getWidth();
	}

	@Override
	public boolean isGL11Available() 
	{	
		return gl11 != null;
	}

	@Override
	public boolean isGL20Available() 
	{	
		return gl20 != null;
	}

	@Override
	public Font newFont(String fontName, int size, FontStyle style, boolean managed ) 
	{	
		return new JoglFont( this, fontName, size, style, managed );
	}

	@Override
	public Font newFont(FileHandle file, int size, FontStyle style, boolean managed) 
	{					
		JoglFileHandle jHandle = (JoglFileHandle)file;
		InputStream in;
		try {
			in = new FileInputStream( jHandle.getFile() );
			JoglFont font = new JoglFont(this, in, size, style, managed);			
			in.close();
			
			return font;
		} catch (Exception e) 
		{		
			return null;
		}		
	}

	@Override
	public Pixmap newPixmap(int width, int height, Format format) 
	{	
		return new JoglPixmap( width, height, format );
	}

	@Override
	public Pixmap newPixmap(InputStream in) 
	{	
		try
		{
			BufferedImage img = (BufferedImage)ImageIO.read( in );
			return new JoglPixmap( img );
		}
		catch( Exception ex )
		{
			return null;
		}		
	}
	
	@Override
	public Pixmap newPixmap(FileHandle file) 
	{	
		try
		{
			BufferedImage img = (BufferedImage)ImageIO.read( ((JoglFileHandle)file).getFile() );
			return new JoglPixmap( img );
		}
		catch( Exception ex )
		{
			return null;
		}		
	}

	@Override
	public Pixmap newPixmap(Object nativePixmap) 
	{	
		return new JoglPixmap( (BufferedImage)nativePixmap );
	}

	@Override
	public ShaderProgram newShaderProgram(String vertexShader, String fragmentShader) 
	{	
		throw new UnsupportedOperationException( "not implemented yet" ); // FIXME
	}

	@Override
	public Texture newTexture(int width, int height, Pixmap.Format format, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap, boolean managed) 
	{	
		if( format == Format.Alpha )
			return new JoglTexture( width, height, BufferedImage.TYPE_BYTE_GRAY, minFilter, magFilter, uWrap, vWrap, managed );
		else
			return new JoglTexture( width, height, BufferedImage.TYPE_4BYTE_ABGR, minFilter, magFilter, uWrap, vWrap, managed );
	}

	@Override
	public Texture newTexture(Pixmap pixmap, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap, boolean managed) 
	{
		return new JoglTexture( (BufferedImage)pixmap.getNativePixmap(), minFilter, magFilter, uWrap, vWrap, managed );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRenderListener( RenderListener listener) 
	{	
		if( this.listener != null )
			graphicPanel.removeGraphicListener( this.listener );
		graphicPanel.addGraphicListener(listener);
		this.listener = listener;
	}

	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{			
		// calculate delta time
		deltaTime = ( System.nanoTime() - lastFrameTime ) / 1000000000.0f;
		lastFrameTime = System.nanoTime();
		mean.addValue( deltaTime );			
	}

	@Override
	public void surfaceCreated(Application app) 
	{
		String version = graphicPanel.getGL().glGetString( GL.GL_VERSION );
		int major = Integer.parseInt("" + version.charAt(0));
		int minor = Integer.parseInt("" + version.charAt(2));
		
		if( useGL2 && major >= 2 )		
		{
			// FIXME add check wheter gl 2.0 is supported
			gl20 = new JoglGL20( graphicPanel.getGL() );
		}
		else
		{
			if( major == 1 && minor < 5 )
			{
				gl10 = new JoglGL10( graphicPanel.getGL() );
			}
			else
			{
				gl11 = new JoglGL11( graphicPanel.getGL() );
				gl10 = gl11;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getDeltaTime() 
	{
		return mean.getMean() == 0?deltaTime:mean.getMean();
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}
}
